package mx.kenzie.solar.integration;

public enum HandlerMode {
    
    LOCAL, // a handle controlled by the local JVM
    FLUID, // a handle that can be transferred between JVMs
    REMOTE // a handle controlled by a remote JVM
    
}
