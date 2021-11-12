package net.nawaman.javacompiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 * This class is an actual implementation of Java Compiler.
 * 
 * Ghost-pattern is used to make adding/removing classpath transparent. This because adding and
 *   removing the classpath require a new class loader to be re-created.
 * 
 * See JavaCompiler
 **/
class JCompiler extends URLClassLoader {
    
    /** Constructs a JavaCompiler using the ClassLoader of this class as base */
    JCompiler() {
        this(null);
    }
    
    /** Constructs a JavaCompiler using the given parent ClassLoader as base */
    JCompiler(final ClassLoader pParentClassLoader) {
        super(new URL[0], JCompiler.GetParentClassLoader(pParentClassLoader));
        this.jjcompiler = new JJCompiler(this);
        this.classpaths = new ClassPaths(this);
    }
    
    // Fields ----------------------------------------------------------------------------------------------------------
    
    private ClassPaths classpaths;
    private JJCompiler jjcompiler;
    
    // Has Compiled ----------------------------------------------------------------------------------------------------
    
    /**
     * Checks if JavaCompiler has ever compiled a class.
     * This is needed to workaround classpath adding/removing problem.
     **/
    boolean hasCompiled() {
        return this.jjcompiler.hasCompiled();
    }
    
    // Class Paths -----------------------------------------------------------------------------------------------------
    
    ClassPaths getClassPaths() {
        return this.classpaths;
    }
    String getClasspaths() {
        return this.classpaths.getClasspaths();
    }
    
    /**{@inheritDoc}*/ @Override
    protected void addURL(final URL pURL) {
        super.addURL(pURL);
    }
    
    // Class persistent ------------------------------------------------------------------------------------------------
    
    /** Checks if the class data exist with the same data */
    private Boolean isExactSameClassExist(final ClassData pClassData) {
        if (pClassData == null)
            return null;
        
        return this.isExactSameClassExist(
                pClassData.getName(),
                pClassData.getPath(),
                pClassData.getBytes()
        );
    }
    /** Checks if the class data exist with the same data */
    private Boolean isExactSameClassExist(
            final String pName,
            final String pPath,
            final byte[] pByteCode) {
        
        final int    SimpleNameLength = pName.length() - MemoryFileManager.CLASS_FILE_SUFFIX_LENGTH;
        final String SimpleName       = pName.substring(0, SimpleNameLength);
        
        final MemoryFileManager         aMFMemory = this.jjcompiler.getMemoryFileManager();
        final JavaClassMemoryFileObject CJCMFO    = aMFMemory.getClassFileObjectByName(SimpleName);
        if (CJCMFO == null)
            return null;
        
        final String aPath = CJCMFO.getPath();
        if (!CheckEquals(aPath, pPath))
            return false;
        
        final byte[] aBytes = CJCMFO.getBAOS().toByteArray();
        return Arrays.equals(aBytes, pByteCode);
    }
    
    /** Add a class data to this JCompiler */
    boolean addClassData(final ClassData pClassData) {
        if(pClassData == null)
            return true;
        
        final Boolean IsExist = this.isExactSameClassExist(pClassData);
        if(IsExist != null)
            return IsExist.booleanValue();
        
        boolean aIsSuccess;
        
        aIsSuccess = this.addClassByteCode(pClassData);
        if (!aIsSuccess)
            return false;
        
        aIsSuccess = this.addEnclosingClassData(pClassData);
        if (!aIsSuccess)
            return false;
        
        this.addEnclosedClassData(pClassData);
        return true;
    }
    private boolean addEnclosingClassData(final ClassData pClassData) {
        final ClassData aEnclosingClassData = pClassData.getEnclosingClassClassData();
        final boolean   aIsSuccess          = this.addClassData(aEnclosingClassData);
        return aIsSuccess;
    }
    private void addEnclosedClassData(final ClassData pClassData) {
        final int aEnclosedClassDataCount = pClassData.getEnclosedClassDataCount();
        for(int i = 0; i < aEnclosedClassDataCount; i++) {
            final ClassData aEnclosedClassData = pClassData.getEnclosedClassData(i);
            this.addClassData(aEnclosedClassData);
        }
    }
    
    /** Add a class byte code to this JCompiler */
    boolean addClassDataAsBytes(final byte[] pSerializedClassDataAsByte) {
        ByteArrayInputStream BAIS;
        ObjectInputStream    OIS;
        ClassData            aClassData;
        try {
            BAIS       = new ByteArrayInputStream(pSerializedClassDataAsByte);
            OIS        = new ObjectInputStream(BAIS);
            aClassData = (ClassData)OIS.readObject();
            OIS.close();
            return this.addClassData(aClassData);
        } catch(IOException E) {
            return false;
        } catch(ClassNotFoundException E) {
            return false;
        }
    }
    
    /** Add a class byte code to this JCompiler */
    boolean addClassByteCode(final ClassData pClassData) {
        return this.addClassByteCode(
                pClassData.getName(),
                pClassData.getPath(),
                pClassData.getRawBytes()
        );
    }
    
    /** Add a class byte code to this JCompiler */
    boolean addClassByteCode(
            final String pName,
            final String pPath,
            final byte[] pByteCode) {
        if(pName     == null) return false;
        if(pByteCode == null) return false;
        
        final Boolean IsExistProperly = this.isExactSameClassExist(pName, pPath, pByteCode);
        if(IsExistProperly != null)
            return IsExistProperly.booleanValue();
        
        JavaClassMemoryFileObject CJCMFO = null;
        try { CJCMFO = new JavaClassMemoryFileObject(pName, pPath, pByteCode); }
        catch(IOException E) { }
        
        if (CJCMFO == null)
            return false;
        
        final int    SimpleNameLength = pName.length() - MemoryFileManager.CLASS_FILE_SUFFIX_LENGTH;
        final String SimpleName       = pName.substring(0, SimpleNameLength);
        this.jjcompiler.getMemoryFileManager().putClassFileObjectByName(SimpleName, CJCMFO);
        return true;
    }
    
    /** Returns the ClassData of the compiled class */
    ClassData getCompiledClassData(final String pName) {
        final ClassData aCData = this.getCompiledClassData(pName, false);
        return aCData;
    }
    
    /** Returns the ClassData of the compiled class - include its code into the ClassData */
    ClassData getCompiledClassData(
            final String  pName,
            final boolean pIsToSaveCode) {
        if (pName == null)
            return null;
        
        final ClassData aClassData = ClassData.GetClassData(pName, this, pIsToSaveCode);
        if (aClassData != null)
            return aClassData;
        
        try {
            final MemoryFileManager         aFMemory = this.jjcompiler.getMemoryFileManager();
            final JavaClassMemoryFileObject JCMFO    = aFMemory.getClassFileObjectByName(pName);
            if(JCMFO == null)
                return null;
            
            final Class<?>  aClass        = this.forName(pName);
            final ClassData aNewClassData = ClassData.NewClassData(
                                                pName,
                                                this,
                                                aClass,
                                                JCMFO,
                                                pIsToSaveCode);        
            return aNewClassData;
            
        } catch (ClassNotFoundException E) {
            throw new RuntimeException(
                    "Internal Error: Class not found for the class that is sure to exist.", E);
        }
    }
    /** Returns the byte code of the class */
    byte[] getCompiledClassDataAsBytes(final String pName) {
        try {
            final ClassData             aClassData = this.getCompiledClassData(pName);
            final ByteArrayOutputStream BAOS       = new ByteArrayOutputStream();
            final ObjectOutputStream    OOS        = new ObjectOutputStream(BAOS);
            OOS.writeObject(aClassData);
            OOS.close();
            return BAOS.toByteArray();
        } catch(Exception E) {
            return null;
        }
    }
    
    // Code ------------------------------------------------------------------------------------------------------------
    
    /** Add Java Code into the Compiler */
    boolean addCode(
            final String pName,
            final String pPath,
            final String pCode) {
        final MemoryFileManager aFMemory   = this.jjcompiler.getMemoryFileManager();
        final boolean           aIsSuccess = aFMemory.addCode(pName, pPath, pCode);
        return aIsSuccess;
    }
    
    /**
     * Delete a code output this file manager
     * 
     * If the file has been successfully compiled, this method will return false.
     **/
    boolean deleteCode(final String pName) {
        final MemoryFileManager aFMemory   = this.jjcompiler.getMemoryFileManager();
        final boolean           aIsSuccess = aFMemory.deleteCode(pName);
        return aIsSuccess;
    }
    
    /**
     * Checks if the given class name is a local (compiler by this JavaCompile or its parent) and not the one from the
     *    classpath. This can be used to allow the removal of classpath to take effect without loosing the access to the
     *    compiled class.
     **/
    boolean isLocalClass(final String pCName) {
        final MemoryFileManager         aFMemory = this.jjcompiler.getMemoryFileManager();
        final JavaClassMemoryFileObject JCMFO    = aFMemory.getClassFileObjectByName(pCName);
        if(JCMFO != null)
            return true;
        
        final ClassLoader aParentClassLoader = this.getParent();
        if (!(aParentClassLoader instanceof JCompiler))
            return false;
        
        final boolean IsLocalClass = ((JCompiler)aParentClassLoader).isLocalClass(pCName);
        return IsLocalClass;
    }
    
    /** Returns the code of the compile class (only available for locally compiled class) */
    String getCode(final String pName) {
        final String aFileName = this.ensureJavaSourceFileName(pName);
        final String aFileCode = this.jjcompiler.getCode(aFileName);
        if (aFileCode != null)
            return aFileCode;
        
        final ClassLoader aParentClassLoader = this.getParent();
        if (!(aParentClassLoader instanceof JCompiler))
            return null;
        
        final String aCode = ((JCompiler)aParentClassLoader).getCode(aFileName);
        return aCode;
    }
    private String ensureJavaSourceFileName(final String pName) {
        String aName = pName;
        if(!pName.endsWith(MemoryFileManager.SOURCE_FILE_SUFFIX))
            aName += MemoryFileManager.SOURCE_FILE_SUFFIX;
        return aName;
    }
    
    // Compilation -----------------------------------------------------------------------------------------------------
    
    /** Compile all code files and return the error report if any */
    String compile() {
        final String aError = this.compile(null);
        return aError;
    }
    
    /**
     * Compile all code files
     * 
     * Return the error report if any (If pDiagnostics was given, the error will be in there).
     **/
    String compile(final DiagnosticCollector<JavaFileObject> pDiagnostics) {
        final String aError = this.compile(null, pDiagnostics);
        return aError;
    }
    
    /**
     * Compile all code files
     * 
     * Return the error report if any (If pDiagnostics was given, the error will be in there).
     **/
    String compile(
            final String[]                            pJavaCOptions,
            final DiagnosticCollector<JavaFileObject> pDiagnosticListener) {
        final String aError  = this.jjcompiler.compile(pJavaCOptions, pDiagnosticListener);
        final String aReturn = this.prepareReturn(pDiagnosticListener, aError);
        return aReturn;
    }
    private String prepareReturn(
            final DiagnosticCollector<JavaFileObject> pDiagnosticListener,
            final String                              pError) {
        if (pDiagnosticListener == null)
            return (pError.length() == 0) ? null : pError;
        
        if (pDiagnosticListener.getDiagnostics().size() == 0)
            return null;
        
        return "Compile failed (See in Diagnosic collection for more info.)";
    }
    
    // Result class ----------------------------------------------------------------------------------------------------
    
    /** Get a class by name - throw an exception when not found*/
    Class<?> forName(
            final String pName)
            throws ClassNotFoundException {
        try {
            final Class<?> aClass = Class.forName(pName, true, this);
            return aClass;
        }
        catch(ClassNotFoundException CNFE) { }
        
        try {
            final Class<?> aClass = this.findClass(pName);
            return aClass;
        } catch(ClassNotFoundException CNFE) { }
        
        throw new ClassNotFoundException(pName);
    }
    
    /** Get a class by name - return null when not found */
    Class<?> getClassByName(final String pName) {
        try {
            final Class<?> aClass = Class.forName(pName, true, this);
            return aClass;
        } catch(ClassNotFoundException CNFE) { }
        
        try {
            final Class<?> aClass = this.findClass(pName);
            return aClass;
        } catch(ClassNotFoundException CNFE) { }
        
        final ClassLoader aParentClassLoader = this.getParent();
        if (!(aParentClassLoader instanceof JCompiler))
            return null;
        
        final Class<?> aClass = ((JCompiler)aParentClassLoader).getClassByName(pName);
        return aClass;
    }
    
    // Satisfy class loader --------------------------------------------------------------------------------------------
    
    /** {@inheritDoc} */ @Override
    protected Class<?> findClass(
            final String pClassName)
            throws ClassNotFoundException {
        try {
            final Class<?> aClass = super.findClass(pClassName);
            if(aClass != null)
                return aClass;
        } catch(ClassNotFoundException E) {}
        
        final Class<?> aClassInParent = this.findClassInParent(pClassName);
        if (aClassInParent != null)
            return aClassInParent;
        
        final MemoryFileManager         aFMemory = this.jjcompiler.getMemoryFileManager();
        final JavaClassMemoryFileObject JCMFO    = aFMemory.getClassFileObjectByName(pClassName); 
        if(JCMFO == null)
            throw new ClassNotFoundException(pClassName);
        
        final Class<?> aClass = this.defineClass(pClassName, JCMFO);
        return aClass;
    }
    
    private Class<?> findClassInParent(final String pClassName) {
        final ClassLoader aParentClassLoader = this.getParent();
        if (!(aParentClassLoader instanceof JCompiler))
            return null;
        
        try (final JCompiler aParent = (JCompiler)aParentClassLoader) {
            if(aParent.isLocalClass(pClassName)) {
                final Class<?> aClass = aParent.findClass(pClassName);
                return aClass;
            }
        }
        catch(ClassNotFoundException CNFE) {}
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    private Class<?> defineClass(
            final String                    pClassName,
            final JavaClassMemoryFileObject pJCMFO)
            throws ClassNotFoundException {
        if(pJCMFO == null)
            throw new ClassNotFoundException(pClassName);
        
        if(pJCMFO.getBAOS() == null)
            throw new ClassNotFoundException(pClassName);
        
        final ByteArrayOutputStream aBAOS  = pJCMFO.getBAOS();
        final byte[]                aBytes = aBAOS.toByteArray();
        final Class<?>              aClass = this.defineClass(pClassName, aBytes, 0, aBytes.length);
        return aClass;
    }
    
    // Privates --------------------------------------------------------------------------------------------------------
    
    static private ClassLoader GetParentClassLoader(final ClassLoader pParentClassLoader) {
        if (pParentClassLoader == null)
             return JCompiler.class.getClassLoader();
        else return pParentClassLoader;
    }
    
    static private boolean CheckEquals(
            final String pString1,
            final String pString2) {
        if (pString1 == pString2)
            return true;
        
        if (pString1 == null)
            return false;
        
        final boolean aIsEquals = pString1.equals(pString2);
        return aIsEquals;
    }
}