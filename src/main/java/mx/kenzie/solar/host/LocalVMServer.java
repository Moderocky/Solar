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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LocalVMServer extends JVMServer {
    
    static {
        System.setProperty("solar.default_port", "5150");
    }
    
    protected final SocketHub hub;
    protected final Map<Code, Handle<?>> handles = new HashMap<>();
    protected final Map<InetSocketAddress, RemoteVMServer> servers = new HashMap<>();
    protected final SecurityKey key;
    
    LocalVMServer(int port, ConnectionOptions options) throws IOException {
        super(true);
        if (options.ssl()) this.hub = SocketHub.createSecure(port, this::open);
        else this.hub = new SocketHub(port, this::open);
        this.key = options.key();
    }
    
    private void open(Socket socket) throws IOException {
        if (!this.key.match(socket.getInputStream())) {
            socket.getOutputStream().write(Protocol.FAIL_CONNECTION);
            return;
        }
        socket.getOutputStream().write(Protocol.ESTABLISH_CONNECTION);
        final InetSocketAddress address = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
        final RemoteVMServer server = new RemoteVMServer(address, socket);
        this.servers.put(address, server);
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
                case Protocol.HAS_HANDLE -> {
                    final Code code = Code.read(stream);
                    final OutputStream target = socket.getOutputStream();
                    target.write(this.has(code) ? 1 : 0);
                }
                case Protocol.DESTROY_HANDLE -> {
                    final Code code = Code.read(stream);
                    this.remove(code);
                }
                case Protocol.REQUEST_HANDLE -> {
                    final Code code = Code.read(stream);
                    final Handle<?> handle;
                    synchronized (handles) {
                        handle = handles.get(code);
                    }
                    final OutputStream target = socket.getOutputStream();
                    target.write(handle instanceof FluidHandle<?> ? HandlerMode.FLUID.ordinal() : HandlerMode.REMOTE.ordinal());
                    this.marshaller.transfer(handle.type(), target);
                }
                case Protocol.DISPATCH_HANDLE -> {
                    final Code code = Code.read(stream);
                    final boolean fluid = stream.read() == 1;
                    final InetSocketAddress target = (InetSocketAddress) this.marshaller.receive(stream);
                    final VMServer owner = servers.get(target);
                    final Class<?> type = (Class<?>) this.marshaller.receive(stream);
                    final Handle<?> handle = fluid
                        ? FluidHandle.createRemote(owner, code, type)
                        : new RemoteHandle<>(owner, code, type);
                    synchronized (handles) {
                        this.handles.put(code, handle);
                        assert handles.containsKey(code);
                    }
                }
                case Protocol.RECEIVE_OBJECT -> {
                    final Code code = Code.read(stream);
                    final InetSocketAddress target = (InetSocketAddress) marshaller.receive(stream);
                    final Handle<?> handle = handles.get(code);
                    final Object object;
                    final VMServer owner = establishLink(target);
                    if (!handle.hasReins()) object = handle.owner().acquire(code, target);
                    else object = handle.reference();
                    handle.dispatchReins(owner);
                    this.marshaller.transfer(object, socket.getOutputStream());
                }
                case Protocol.SEND_OBJECT -> {
                    final Code code = Code.read(stream);
                    final Object object = marshaller.receive(stream);
                    final HandlerMode mode = HandlerMode.values()[stream.read()];
                    final Handle<?> handle = switch (mode) {
                        case FLUID -> FluidHandle.createLocal(this, code, object);
                        default -> new LocalHandle<>(this, code, object);
                    };
                    synchronized (handles) {
                        this.handles.put(code, handle);
                    }
                }
            }
        }
        this.servers.remove(address);
        server.close();
    }
    
    private Object call(Code code, MethodErasure erasure, Object[] arguments) {
        try {
            final Handle<?> handle = this.handles.get(code);
            return handle.callMethod(erasure, arguments);
        } catch (NoSuchMethodException ex) {
            throw new MethodCallError(ex);
        }
    }
    
    protected VMServer establishLink(InetSocketAddress address) {
        if (servers.containsKey(address)) return servers.get(address);
        final RemoteVMServer remote = (RemoteVMServer) Server.connect(address);
        this.servers.put(address, remote);
        return remote;
    }
    
    public static int defaultPort() {
        return Integer.parseInt(System.getProperty("solar.default_port", "5150"));
    }
    
    static LocalVMServer create(int port) throws ConnectionError {
        return create(port, ConnectionOptions.DEFAULT);
    }
    
    static LocalVMServer create(int port, ConnectionOptions options) throws ConnectionError {
        try {
            return new LocalVMServer(port, options);
        } catch (IOException ex) {
            throw new ConnectionError("Unable to create local server.", ex);
        }
    }
    
    public void install(Handle<?> handle) {
        synchronized (handles) {
            this.handles.put(handle.code(), handle);
        }
        this.establishLink(handle.owner());
    }
    
    protected void establishLink(VMServer server) {
        if (this == server) return;
        if (servers.containsKey(server.address())) return;
        if (this.address().equals(server.address())) return;
        final RemoteVMServer remote;
        if (server instanceof RemoteVMServer) remote = (RemoteVMServer) server;
        else remote = (RemoteVMServer) Server.connect(server.address());
        this.servers.put(server.address(), remote);
    }
    
    @Override
    public InetSocketAddress address() {
        return new InetSocketAddress(hub.getAddress(), hub.getPort());
    }
    
    @Override
    public <Type> Type acquire(Code code, InetSocketAddress address) {
        final Handle<Type> handle = (Handle<Type>) handles.get(code);
        if (handle.hasReins()) return handle.reference();
        return handle.owner().acquire(code, address);
    }
    
    @Override
    public <Type> Handle<Type> export(Type object, Code code) {
        return this.export(object, code, HandlerMode.LOCAL);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <Type> Handle<Type> export(Type object, Code code, HandlerMode mode) {
        final Handle<Type> handle;
        switch (mode) {
            case FLUID -> handle = FluidHandle.createLocal(this, code, (Class<Type>) object.getClass(), object);
            default -> handle = new LocalHandle<>(this, code, object);
        }
        synchronized (handles) {
            this.handles.put(code, handle);
        }
        return handle;
    }
    
    @Override
    public <Type> void export(Handle<Type> handle) {
        synchronized (handles) {
            if (handles.containsValue(handle)) return;
            this.handles.put(handle.code(), handle);
        }
    }
    
    @Override
    public boolean has(Code code) {
        return handles.containsKey(code);
    }
    
    @Override
    public void remove(Code code) {
        synchronized (handles) {
            final Handle<?> handle = this.handles.get(code);
            if (handle instanceof DestructibleHandle destroy) destroy.destroy();
            this.handles.remove(code);
        }
    }
    
    @Override
    public void close() {
        try {
            synchronized (handles) {
                this.handles.clear();
            }
            this.hub.close();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <Type> Handle<Type> request(Code code) {
        synchronized (handles) {
            return (Handle<Type>) handles.get(code);
        }
    }
    
    protected RemoteVMServer getServer(Socket socket) {
        final InetSocketAddress address = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
        return servers.get(address);
    }
}
