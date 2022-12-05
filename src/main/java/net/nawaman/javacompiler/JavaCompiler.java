package net.nawaman.javacompiler;

import java.net.*;
import java.security.*;

import javax.tools.*;

/**
 * JavaCompiler compiles Java codes into Java-class byte code on-the-fly without the need for read
 *   or write files on disc.
 * 
 * NOTE: JavaCompiler is actually a wrapper of JCompiler. This is done in order to overcome the
 *   limitation of JCompiler about adding/removing classpath. All public methods of JavaCompiler
 *   delegates to those of JCompiler.  
 * 
 * <h2>Compilation:</h2>
 *   JCompiler is a ClassLoader that looks for Java-class byte code from multiple sources including
 *     the one compiled by javax.tools.JavaCompiler. The class use MemoryFileManager,
 *     MemoryFileObject, JavaClassMemoryFileObject and JavaCodeMemoryFileObject to simulate actual
 *     files from disc in the memory; hence, all the compilation can be done within the memory.
 * 
 * <h2>Package Handling</h2>
 *   At the moment, all class compiled by JavaCompiler should be in &ltdefault$gt package. This is
 *     due to the additional complexity in handling directory structure in MemoryFileManager to
 *     simulate the package structure. Adding a proper package support will need to ensure
 *     the proper handling of compiled package (no class can be later added in to a compiled
 *     package, for example). If you would want to used JavaCompiler to compile packaged class, do
 *     it at your-own risk.
 * 
 * <h2>Compilation-Error Handling:</h2>
 *   JavaCompiler fully supports compilation-error handling as javax.tools.JavaComiler does. This is
 *     done by simply delegate the error handling to javax.tools.JavaComiler during the compilation.
 *     The compilation error is returned as String when DiagnosticCollector is not given. 
 * 
 * <h2>Classpath:</h2>
 *   As a URLClassLoader, JCompiler can easily access to classes from those URL. This ability is
 *     used to simulate run-time classpath adding. Due to the constrains of class access in JVM,
 *     adding or removing classpath after at least class has been compiled, however, will be
 *     ignored. Therefore, a new JavaCompiler is created when that happens and it should be used for
 *     further compilation. Because of all this, JavaCompiler is designed to be a wrapper of
 *     JCompiler in which JavaCompiler makes sure that classpath adding/removing is done behind
 *     the scene.  
 * 
 * <h2>Raw byte-code adding:</h2>
 *   A raw byte-code can be added into JavaCompiler as on-the-fly classpath.
 * 
 * <h2>ClassData adding: </h2>
 *   ClassData is a container of java byte codes and other information need to restore compiled
 *     class into a new JavaCompiler. Together with JavaCompilerObjectInputStream, this is very
 *     useful for persistent of compiled class so there is no need to re-compiled it.
 * 
 * <h2>Debugging: </h2>
 *   Since the documentation about javax.tools.JavaCompiler is not complete enough to have a clear
 *   picture (at the time JavaCompiler is written), most of the knowledge comes from the experiment
 *   and trial error. Debugging is an important mechanism to see what method is called and what
 *   parameter is given. The debugging mechanism is will partly available for either educational and
 *   further development purposes.
 *   
 * <h2>CLASSPATH NOTE:</h2>
 * In order to use this class, ensure that the class 'com.sun.tools.javac.api.JavacTool'
 *     (found in <jdk-dir>/lib/tools.jar) is in the class path.
 * 
 **/
final public class JavaCompiler {
    
    static public JavaCompiler Instance = new JavaCompiler();
    
    /** Constructs a JavaCompiler using the given parent ClassLoader as base */
    static public JavaCompiler NewJavaCompiler(final ClassLoader pParentClassLoader) {
        if(pParentClassLoader == null)
            return Instance;
        
        ClassLoader CL = Instance.getClass().getClassLoader();
        while(CL != null) {
            if(pParentClassLoader == CL) return Instance;
            CL = CL.getParent();
        }
        
        final JavaCompiler aJCompiler = new JavaCompiler(pParentClassLoader);
        return aJCompiler;
    }
    
    /** Constructs a JavaCompiler using the ClassLoader of this class as base */
    public JavaCompiler() {
        this.jcompiler = new JCompiler();
    }
    
    /** Constructs a JavaCompiler using the given parent ClassLoader as base */
    public JavaCompiler(final ClassLoader pParentClassLoader) {
        this.jcompiler = AccessController.doPrivileged(new PrivilegedAction<JCompiler>() {
            public JCompiler run() {
                final JCompiler aJCompiler = new JCompiler(pParentClassLoader);
                return aJCompiler;
            }
        });
    }
    
    private JCompiler jcompiler;
    
    /** Debug flag */
    static public boolean DEBUG_MODE = false;
    
    /**
     * Returns the current ClassLoader.
     * 
     * ClassLoader of JavaCompiler is constantly changed depending on its classpath and other factors. This method
     *   returns the current ClassLoader (the current classpath). 
     **/
    public ClassLoader getCurrentClassLoader() {
        return this.jcompiler;
    }
    
    // Classpath ------------------------------------------------------------------------------------------------------- 
    
    /** Add a jar file as class path into the ClassLoader */
    public void addClasspathURL(final String pUrlPath) {
        this.jcompiler = this.jcompiler.getClassPaths().addClasspathURL(pUrlPath);
    }
    /** Add a jar file as class path into the ClassLoader */
    public void removeClasspathURL(final String pUrlPath) {
        this.jcompiler = this.jcompiler.getClassPaths().removeClasspathURL(pUrlPath);
    }
    
    /** Add a jar file as class path into the ClassLoader */
    public void addJarFile(final String pPath) {
        this.jcompiler = this.jcompiler.getClassPaths().addJarFile(pPath);
    }
    /** Add a jar file as class path into the ClassLoader */
    public void removeJarFile(final String pPath) {
        this.jcompiler = this.jcompiler.getClassPaths().removeJarFile(pPath);
    }
    
    /** Returns an array of classpath known to this JavaCompiler */
    public URL[] getClasspaths() {
        final URL[] aURLS = this.jcompiler.getURLs();;
        return aURLS;
    }
    
    // Class persistence -----------------------------------------------------------------------------------------------
    
    /**
     * Loads a ClassData object to this JavaCompiler and return true if the byte code is successfully loaded and
     * initialized
     **/
    public boolean addClassData(final ClassData pClassData) {
        final boolean isSuccess = this.jcompiler.addClassData(pClassData);
        return isSuccess;
    }
    
    /**
     * Loads a Serialized ClassData object in a form of an array of bytes to this JavaCompiler and return true if the
     * byte code is successfully loaded and initialized.
     **/
    public boolean addClassDataAsBytes(final byte[] pSerializedClassDataAsByte) {
        final boolean isSuccess = this.jcompiler.addClassDataAsBytes(pSerializedClassDataAsByte);
        return isSuccess;
    }
    
    /**
     * Loads a Java class byte-code to this JavaCompiler and return true if the byte code is successfully loaded and
     * initialized
     **/
    public boolean addClassByteCode(
            final String pName,
            final String pPath,
            final byte[] pByteCode) {
        final boolean isSuccess = this.jcompiler.addClassByteCode(pName, pPath, pByteCode);
        return isSuccess;
    }
    
    
    /** Returns the byte code of the class */
    public ClassData getCompiledClassData(final String pName) {
        final ClassData aClassData = this.jcompiler.getCompiledClassData(pName);
        return aClassData;
    }
    
    /** Returns the byte code of the class */
    public ClassData getCompiledClassData(
            final String  pName,
            final boolean pIsToSaveCode) {
        ClassData aClassData = this.jcompiler.getCompiledClassData(pName, pIsToSaveCode);
        return aClassData;
    }
    /** Returns the byte code of the class */
    public byte[] getCompiledClassDataAsBytes(final String pName) {
        final byte[] aClassByteCode = this.jcompiler.getCompiledClassDataAsBytes(pName);
        return aClassByteCode;
    }
    
    // Static utilities ------------------------------------------------------------------
    
    /** Returns the ClassData of the given class or null of the class is not compiled with a JavaCompiler */
    static public ClassData GetClassDataOf(final Class<?> pCls) {
        final ClassData aClassData = GetClassDataOf(pCls, false);
        return aClassData;
    }
    
    /** Returns the ClassData of the given class or null of the class is not compiled with a JavaCompiler */
    static public ClassData GetClassDataOf(
            final Class<?> pClass,
            final boolean  pIsToSaveCode) {
        if(pClass == null)
            return null;
        
        final ClassLoader CL = pClass.getClassLoader();
        if(!(CL instanceof JCompiler))
            return null;
        
        final String    aClassName = pClass.getCanonicalName();
        final ClassData aClassData = ((JCompiler)CL).getCompiledClassData(aClassName, pIsToSaveCode);
        return aClassData;
    }
    
    
    // Code and compilation --------------------------------------------------------------------------------------------
    
    /** Add Java Code into the Compiler */
    public boolean addCode(
            final String pName,
            final String pPath,
            final String pCode) {
        final boolean isSuccess = this.jcompiler.addCode(pName, pPath, pCode);
        return isSuccess;
    }
    
    /**
     * Delete a code output this file manager
     * 
     * If the file has been successfully compiled, this method will return false.
     **/
    public boolean deleteCode(final String pName) {
        final boolean isSuccess = this.jcompiler.deleteCode(pName);
        return isSuccess;
    }
    
    // Compilation -----------------------------------------------------------------------------------------------------
    
    /** Compile all code files and return the error report if any */
    public String compile() {
        final String aError = this.jcompiler.compile();
        return aError;
    }
    
    /**
     * Compile all code files
     * 
     * Return the error report if any (If pDiagnostics was given, the error will be in there).
     **/
    public String compile(final DiagnosticCollector<JavaFileObject> pDiagnostics) {
        final String aError = this.jcompiler.compile(pDiagnostics);
        return aError;
    }
    
    /**
     * Compile all code files
     * 
     * Return the error report if any (If pDiagnostics was given, the error will be in there).
     **/
    public String compile(
            final String[]                            pJavaCOptions,
            final DiagnosticCollector<JavaFileObject> pDiagnostics) {
        final String aError = this.jcompiler.compile(pJavaCOptions, pDiagnostics);
        return aError;
    }
    
    // Resulted classes ------------------------------------------------------------------------------------------------
    
    /** Get a class by name - throw an exception when not found*/
    public Class<?> forName(final String pName) throws ClassNotFoundException {
        final Class<?> aClass = this.jcompiler.forName(pName);
        return aClass;
    }
    
    /** Get a class by name - return null when not found */
    public Class<?> getClassByName(final String pName) {
        final Class<?> aClass = this.jcompiler.getClassByName(pName);
        return aClass;
    }
}

