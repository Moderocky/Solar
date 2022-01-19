package mx.kenzie.solar.host;

import mx.kenzie.jupiter.socket.SocketHub;
import mx.kenzie.mimic.MethodErasure;
import mx.kenzie.solar.connection.ConnectionOptions;
import mx.kenzie.solar.connection.Protocol;
import mx.kenzie.solar.error.ConnectionError;
import mx.kenzie.solar.error.IOError;
import mx.kenzie.solar.error.MethodCallError;
import mx.kenzie.solar.integration.*;
import mx.kenzie.solar.security.SecurityKey;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RemoteVMServer extends JVMServer implements VMServer {
    
    protected final InetSocketAddress address;
    protected final Socket socket;
    
    protected RemoteVMServer(InetSocketAddress address, Socket socket) {
        super(false);
        this.address = address;
        this.socket = socket;
    }
    
    static RemoteVMServer connect(InetAddress address, int port) throws ConnectionError {
        return connect(address, port, ConnectionOptions.DEFAULT);
    }
    
    static RemoteVMServer connect(InetAddress address, int port, ConnectionOptions options) throws ConnectionError {
        try {
            final RemoteVMServer server;
            if (options.ssl())
                server = new RemoteVMServer(new InetSocketAddress(address, port), SocketHub.connectSecure(address, port));
            else server = new RemoteVMServer(new InetSocketAddress(address, port), SocketHub.connect(address, port));
            if (server.init(options.key()) != Protocol.ESTABLISH_CONNECTION) {
                throw new ConnectionError("Security key match failed.");
            }
            return server;
        } catch (IOException ex) {
            throw new ConnectionError("Unable to establish remote connection.", ex);
        }
    }
    
    protected int init(SecurityKey key) throws IOException {
        while (!socket.isConnected()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        key.write(socket.getOutputStream());
        return socket.getInputStream().read();
    }
    
    @Override
    public InetSocketAddress address() {
        return address;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <Type> Type acquire(Code code, InetSocketAddress address) {
        synchronized (socket) {
            try {
                final OutputStream stream = this.socket.getOutputStream();
                stream.write(Protocol.RECEIVE_OBJECT);
                stream.write(code.bytes());
                this.marshaller.transfer(address, stream);
            } catch (IOException ex) {
                throw new ConnectionError("Unable to send transfer request.", ex);
            }
            try {
                final InputStream stream = this.socket.getInputStream();
                final Type type = (Type) this.marshaller.receive(stream);
                return type;
            } catch (IOException ex) {
                throw new ConnectionError("Unable to send transfer request.", ex);
            }
        }
    }
    
    @Override
    public <Type> Handle<Type> export(Type object, Code code) {
        return this.export(object, code, HandlerMode.LOCAL);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <Type> Handle<Type> export(Type object, Code code, HandlerMode mode) {
        if (mode == HandlerMode.LOCAL) {
            throw new IllegalStateException("Cannot create local handle in a remote server.");
        }
        synchronized (socket) {
            try {
                final OutputStream stream = this.socket.getOutputStream();
                stream.write(Protocol.SEND_OBJECT);
                stream.write(code.bytes());
                this.marshaller.transfer(object, stream);
                stream.write(mode.ordinal());
            } catch (IOException ex) {
                throw new MethodCallError("Unable to transfer ownership.", ex);
            }
        }
        return switch (mode) {
            case FLUID -> FluidHandle.createRemote(this, code, (Class<Type>) object.getClass());
            default -> new RemoteHandle<>(this, code, (Class<Type>) object.getClass());
        };
    }
    
    @Override
    public <Type> void export(Handle<Type> handle) {
        final Code code = handle.code();
        synchronized (socket) {
            try {
                final OutputStream stream = this.socket.getOutputStream();
                stream.write(Protocol.DISPATCH_HANDLE);
                stream.write(code.bytes());
                stream.write(handle instanceof FluidHandle<Type> ? 1 : 0);
                this.marshaller.transfer(handle.owner().address(), stream);
                this.marshaller.transfer(handle.type(), stream);
            } catch (IOException ex) {
                throw new MethodCallError("Unable to dispatch handle.", ex);
            }
        }
    }
    
    @Override
    public void close() {
        try {
            synchronized (handles) {
                this.handles.clear();
            }
            this.socket.close();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
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
                final HandlerMode mode = HandlerMode.values()[stream.read()];
                final Object object = this.marshaller.receive(stream);
                if (object == null) return null;
                if (!(object instanceof Class<?> type)) return null;
                return (Handle<Type>) switch (mode) {
                    case FLUID -> FluidHandle.createRemote(this, code, type);
                    default -> new RemoteHandle<>(this, code, type);
                };
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
                final Object object = this.marshaller.receive(stream);
                return object;
            } catch (IOException ex) {
                throw new MethodCallError("Unable to retrieve method call result.", ex);
            }
        }
    }
    
}
