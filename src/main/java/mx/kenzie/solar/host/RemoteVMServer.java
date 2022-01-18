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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RemoteVMServer extends JVMServer implements VMServer {
    
    protected final InetSocketAddress address;
    protected final Socket socket;
    protected Marshaller marshaller;
    
    protected RemoteVMServer(InetSocketAddress address, Socket socket) {
        super(false);
        this.address = address;
        this.socket = socket;
        this.marshaller = new StandardMarshaller();
    }
    
    static RemoteVMServer connect(InetAddress address, int port) throws ConnectionError {
        try {
            return new RemoteVMServer(new InetSocketAddress(address, port), SocketHub.connect(address, port));
        } catch (IOException ex) {
            throw new ConnectionError("Unable to establish remote connection.", ex);
        }
    }
    
    @Override
    public <Type> Handle<Type> export(Type object, Code code) {
        synchronized (socket) {
            try {
                final OutputStream stream = this.socket.getOutputStream();
                stream.write(Protocol.DISPATCH_HANDLE);
                stream.write(code.bytes());
                this.marshaller.transfer(object.getClass(), stream);
            } catch (IOException ex) {
                throw new MethodCallError("Unable to dispatch handle.", ex);
            }
        }
        return new LocalHandle<>(this, code, object);
    }
    
    @Override
    public <Type> Handle<Type> request(Code code) {
        synchronized (socket) {
            try {
                final OutputStream stream = this.socket.getOutputStream();
                stream.write(Protocol.REQUEST_HANDLE);
                stream.write(code.bytes());
            } catch (IOException ex) {
                throw new MethodCallError("Unable to dispatch request.", ex);
            }
            try {
                final InputStream stream = this.socket.getInputStream();
                final Object object = this.marshaller.receive(stream);
                if (object == null) return null;
                if (!(object instanceof Class<?> type)) return null;
                return (Handle<Type>) new RemoteHandle<>(this, code, type);
            } catch (IOException ex) {
                throw new MethodCallError("Unable to retrieve object handle.", ex);
            }
        }
    }
    
    public Object call(Code code, MethodErasure erasure, Object... arguments) {
        synchronized (socket) {
            try {
                final OutputStream stream = this.socket.getOutputStream();
                stream.write(Protocol.METHOD_CALL);
                stream.write(code.bytes());
                this.marshaller.transfer(erasure, stream);
                stream.write(arguments.length);
                this.marshaller.transfer(arguments, stream);
            } catch (IOException ex) {
                throw new MethodCallError("Unable to dispatch method call.", ex);
            }
            try {
                final InputStream stream = this.socket.getInputStream();
                return this.marshaller.receive(stream);
            } catch (IOException ex) {
                throw new MethodCallError("Unable to retrieve method call result.", ex);
            }
        }
    }
    
}
