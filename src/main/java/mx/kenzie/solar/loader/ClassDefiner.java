package mx.kenzie.solar.loader;

public class ClassDefiner extends ClassLoader {
    
    public static ClassDefiner definer = new ClassDefiner(ClassDefiner.class.getClassLoader());
    
    protected ClassDefiner(ClassLoader parent) {
        super(parent);
    }
    
    public Class<?> defineClass(String name, byte[] code) {
        try {
            return Class.forName(name, true, this);
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
            return this.defineClass(name, code, 0, code.length);
        }
    }
    
}
