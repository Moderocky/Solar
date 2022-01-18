package mx.kenzie.solar.host;

import mx.kenzie.jupiter.socket.SocketHub;
import mx.kenzie.mimic.MethodErasure;
import mx.kenzie.solar.connection.Protocol;
import mx.kenzie.solar.error.ConnectionError;
import mx.kenzie.solar.error.MethodCallError;
import mx.kenzie.solar.integration.Code;
import mx.kenzie.solar.integration.Handle;
import mx.kenzie.solar.integration.LocalHandle;
import mx.kenzie.solar.integration.RemoteHandle;
import mx.kenzie.solar.marshal.Marshaller;
import mx.kenzie.solar.marshal.StandardMarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LocalVMServer extends JVMServer {
    
    protected final SocketHub hub;
    protected final Map<Code, Handle<?>> handles = new HashMap<>();
    protected Marshaller marshaller;
    
    LocalVMServer(int port) throws IOException {
        super(true);
        this.hub = new SocketHub(port, this::open);
        this.marshaller = new StandardMarshaller();
    }
    
    private void open(Socket socket) throws IOException {
        
        final RemoteVMServer server = new RemoteVMServer(new InetSocketAddress(socket.getInetAddress(), socket.getPort()), socket);
        while (socket.isConnected()) {
            final InputStream stream = socket.getInputStream();
            final int protocol = socket.getInputStream().read();
            switch (protocol) {
                case Protocol.METHOD_CALL -> {
                    final Code code = Code.read(stream);
                    final MethodErasure erasure = (MethodErasure) this.marshaller.receive(stream);
                    final int parameters = stream.read();
                    final Object[] arguments = (Object[]) this.marshaller.receive(stream);
                    final Object result = this.call(code, erasure, arguments);
                    this.marshaller.transfer(result, socket.getOutputStream());
                }
                case Protocol.REQUEST_HANDLE -> {
                    final Code code = Code.read(stream);
                    final Handle<?> handle;
                    synchronized (handles) {
                        handle = handles.get(code);
                    }
                    this.marshaller.transfer(handle.type(), socket.getOutputStream());
                }
                case Protocol.DISPATCH_HANDLE -> {
                    final Code code = Code.read(stream);
                    final Handle<?> handle = handles.get(code);
                    final Class<?> type = (Class<?>) this.marshaller.receive(stream);
                    synchronized (handles) {
                        this.handles.put(code, new RemoteHandle<>(server, code, type));
                    }
                    this.marshaller.transfer(handle.type(), socket.getOutputStream());
                }
            }
        }
    }
    
    private Object call(Code code, MethodErasure erasure, Object[] arguments) {
        try {
            final Handle<?> handle = this.handles.get(code);
            return handle.callMethod(erasure, arguments);
        } catch (NoSuchMethodException ex) {
            throw new MethodCallError(ex);
        }
    }
    
    static LocalVMServer create(int port) throws ConnectionError {
        try {
            return new LocalVMServer(port);
        } catch (IOException ex) {
            throw new ConnectionError("Unable to create local server.", ex);
        }
    }
    
    @Override
    public <Type> Handle<Type> export(Type object, Code code) {
        final LocalHandle<Type> handle = new LocalHandle<>(this, code, object);
        synchronized (handles) {
            this.handles.put(code, handle);
        }
        return handle;
    }
    
    @Override
    public <Type> Handle<Type> request(Code code) {
        synchronized (handles) {
            return (Handle<Type>) handles.get(code);
        }
    }
}
