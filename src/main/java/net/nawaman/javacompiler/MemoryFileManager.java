package net.nawaman.javacompiler;

import java.io.*;
import java.util.*;

import javax.tools.*;
import javax.tools.JavaFileObject.*;

/**
 * File manager that get/set file in the memory.
 * 
 * This class simulate file system in memory allowing the compilation to be done all in memory.
 **/
final class MemoryFileManager implements StandardJavaFileManager {
    
    static public final String SOURCE_FILE_SUFFIX        = ".java";
    static public final String CLASS_FILE_SUFFIX         = ".class";
    static public final int    SOURCE_FILE_SUFFIX_LENGTH = MemoryFileManager.SOURCE_FILE_SUFFIX.length();
    static public final int    CLASS_FILE_SUFFIX_LENGTH  = MemoryFileManager.CLASS_FILE_SUFFIX .length();
    
    MemoryFileManager(final StandardJavaFileManager pSJFileManager) {
        this.fileManager = pSJFileManager;
        this.codes   = new HashMap<String, JavaCodeMemoryFileObject>();
        this.classes = new HashMap<String, JavaClassMemoryFileObject>();
    }
    
    private StandardJavaFileManager                    fileManager;
    private HashMap<String, JavaCodeMemoryFileObject>  codes;
    private HashMap<String, JavaClassMemoryFileObject> classes;
    
    @Override
    public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        return fileManager.listLocationsForModules(location);
    }
    
    @Override
    public String inferModuleName(Location location) throws IOException {
        return fileManager.inferModuleName(location);
    }
    
    /** Clear the object to avoid memory leak */
    final public void dispose() {
        this.fileManager  = null;
        this.codes                = null;
        // The classes must be left alone since it may be used by the Memory Class loader that cannot be disposed
    }
    
    /** Returns a local file associated with the given file object */
    MemoryFileObject getLocalFile(final File pFile) {
        if (pFile == null)
            return null;
        
        final String           aFileName         = pFile.getName();
        final MemoryFileObject aMemoryFileObject = this.getLocalFile(aFileName);
        return aMemoryFileObject;
    }
    /** Returns a local file associated with the given file name */
    MemoryFileObject getLocalFile(final String pFileName) {
        if(pFileName == null)
            return null;
        
        if(pFileName.endsWith(MemoryFileManager.SOURCE_FILE_SUFFIX)) {
            MemoryFileObject aCodeFileObject = this.codes.get(pFileName);
            return aCodeFileObject;
        }
        if(pFileName.endsWith(MemoryFileManager.CLASS_FILE_SUFFIX)) {
            int              aCFName_NewLength = pFileName.length() - MemoryFileManager.CLASS_FILE_SUFFIX_LENGTH;
            String           aClassFileName    = pFileName.substring(0, aCFName_NewLength);
            MemoryFileObject aClassFileObject  = this.classes.get(aClassFileName);
            return aClassFileObject;
        }
        return null;
    }
    
    /** Add a java code into this file manager */
    public boolean addCode(
            final String pName,
            final String pPath,
            final String pCode) {
        if(this.codes.containsKey(pName))
            return false;
        
        final JavaCodeMemoryFileObject aJavaCodeFile;
        aJavaCodeFile = new JavaCodeMemoryFileObject(pName, pPath, pCode);
        
        this.codes.put(pName, aJavaCodeFile);
        return true;
    }
    public Set<String> getCodeNames() {
        if (this.codes == null)
            return null;
        
        return this.codes.keySet();
    }
    public String getCode(final String pName) {
        final JavaCodeMemoryFileObject aCodeFileObject = this.getCodeObject(pName);
        if (aCodeFileObject == null)
            return null;
        
        final String aCode = aCodeFileObject.getCode();
        return aCode;
    }
    JavaCodeMemoryFileObject getCodeObject(final String pName) {
        if (this.codes == null)
            return null;
        
        final JavaCodeMemoryFileObject aCodeFile = this.codes.get(pName);
        return aCodeFile;
    }
    
    void putClassFileObjectByName(
            final String                    pName,
            final JavaClassMemoryFileObject pFileObject) {
        this.classes.put(pName, pFileObject);
    }
    JavaClassMemoryFileObject getClassFileObjectByName(final String pName) {
        final JavaClassMemoryFileObject aClassFileObject = this.classes.get(pName);
        return aClassFileObject;
    }
    
    void clearCode() {
        if (this.codes == null)
            return;
        
        this.codes.clear();
    }
    
    /** Delete a code output this file manager */
    public boolean deleteCode(final String pFileName) {
        final boolean hasCode = this.codes.containsKey(pFileName);
        if (!hasCode)
            return false;
        
        final boolean isJavaCodeName = pFileName.endsWith(MemoryFileManager.SOURCE_FILE_SUFFIX);
        if (!isJavaCodeName) {
            this.codes.remove(pFileName);
            return true;
        }
        
        final int    aSFName_NewLength = pFileName.length() - MemoryFileManager.SOURCE_FILE_SUFFIX_LENGTH;
        final String aSimpleFileName   = pFileName.substring(0, aSFName_NewLength);
        if(!this.codes.containsKey(aSimpleFileName)) {
            this.codes.remove(pFileName);
            return true;
        }
        return false;
    }
    
    // StandardJavaFileManager ----------------------------------------------------------------------
    
    /** Checks if the given files are the same */
    public boolean isSameFile(
            final FileObject pFileOne,
            final FileObject pFileTwo) {
        final boolean R = this.fileManager.isSameFile(pFileOne, pFileTwo);
        if (JavaCompiler.DEBUG_MODE) {
            System.out.println("isSameFile" + this.getToString(pFileOne, pFileTwo) + " = " + R);
            System.out.println();
        }
        return R;
    }
    
    private void addToFileObjects(
            final Vector<JavaFileObject> pJFOs,
            final String                 pFileName) {
        final MemoryFileObject MFO = this.getLocalFile(pFileName);
        if (MFO == null) {
            final Iterable<? extends JavaFileObject> aFileObjects;
            aFileObjects = this.fileManager.getJavaFileObjects(pFileName);
            for (final JavaFileObject aFO : aFileObjects)
                pJFOs.add(aFO);
        } else {
            pJFOs.add(MFO);
        }
        
    }
    private void addToFileObjects(
            final Vector<JavaFileObject> pJFOs,
            final File                   pFile) {
        final MemoryFileObject MFO = this.getLocalFile(pFile);
        if (MFO == null) {
            final Iterable<? extends JavaFileObject> aFileObjects;
            aFileObjects = this.fileManager.getJavaFileObjects(pFile);
            
            for (final JavaFileObject aFO : aFileObjects)
                pJFOs.add(aFO);
            
        } else {
            pJFOs.add(MFO);
        }
    }
    
    /** Gets file objects representing the given files. */
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
            final Iterable<? extends File> pFiles) {
        final Vector<JavaFileObject> JFOs = new Vector<JavaFileObject>();
        for (final File aFile : pFiles)
            this.addToFileObjects(JFOs, aFile);
        
        if (JavaCompiler.DEBUG_MODE) {
            System.out.println("getJavaFileObjectsFromFiles" + this.getToString(pFiles) + " = " + JFOs);
            System.out.println();
        }
        return JFOs;
    }
    /** Gets file objects representing the given files. */
    public Iterable<? extends JavaFileObject> getJavaFileObjects(final File... pFiles) {
        final Vector<JavaFileObject> JFOs = new Vector<JavaFileObject>();
        for (final File aFile : pFiles)
            this.addToFileObjects(JFOs, aFile);
        
        if (JavaCompiler.DEBUG_MODE) {
            System.out.println("getJavaFileObjects" + this.getToString((Object[])pFiles) + " = " + JFOs);
            System.out.println();
        }
        return JFOs;
    }
    /** Gets file objects representing the given file names. */
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
            final Iterable<String> pNames) {
        final Vector<JavaFileObject> JFOs = new Vector<JavaFileObject>();
        for (final String aName : pNames)
            this.addToFileObjects(JFOs, aName);
        
        if (JavaCompiler.DEBUG_MODE) {
            System.out.println("getJavaFileObjectsFromStrings" + this.getToString((Object)pNames) + " = " + JFOs);
            System.out.println();
        }
        return JFOs;
    }
    /** Gets file objects representing the given file names. */
    public Iterable<? extends JavaFileObject> getJavaFileObjects(final String... pNames) {
        final Vector<JavaFileObject> JFOs = new Vector<JavaFileObject>();
        for (final String aName : pNames)
            this.addToFileObjects(JFOs, aName);
        
        if (JavaCompiler.DEBUG_MODE) {
            System.out.println("getJavaFileObjects" + this.getToString((Object[])pNames) + " = " + JFOs);
            System.out.println();
        }
        return JFOs;
    }
    
    /** Associates the given path with the given location. */
    public void setLocation(
            final Location                 pLocation,
            final Iterable<? extends File> pPath)
            throws IOException {
        this.fileManager.setLocation(pLocation, pPath);
    }
    
    /** Gets the path associated with the given location. */
    public Iterable<? extends File> getLocation(final Location pLocation) {
        final Iterable<? extends File> aFileObjects = this.fileManager.getLocation(pLocation);
        return aFileObjects;
    }
    
    // JavaFileManager -----------------------------------------------------------------------------
    
    /** Gets a class loader for loading plug-ins from the given location. */
    public ClassLoader getClassLoader(final Location pLocation) {
        final ClassLoader aClassLoader = this.fileManager.getClassLoader(pLocation);
        return aClassLoader;
    }
    
    /** Lists all file objects matching the given criteria in the given location. */
    public Iterable<JavaFileObject> list(
            final Location  pLocation,
            final String    pPackageName,
            final Set<Kind> pKinds,
            final boolean   pRecurse)
            throws IOException {
        final Iterable<JavaFileObject> aFileObjects;
        aFileObjects = this.fileManager.list(pLocation, pPackageName, pKinds, pRecurse);
        return aFileObjects;
    }
    
    /** Infers a binary name of a file object based on a location. */
    public String inferBinaryName(
            final Location       pLocation,
            final JavaFileObject pFile) {
        final String inferBinaryName = this.fileManager.inferBinaryName(pLocation, pFile);
        return inferBinaryName;
    }
    
    /** Handles one option. */
    public boolean handleOption(
            final String           pCurrent,
            final Iterator<String> pRemaining) {
        final boolean handleOption = this.fileManager.handleOption(pCurrent, pRemaining);
        return handleOption;
    }
    
    /**
     * Determines if a location is known to this file manager.
     *
     * @param pLocation a location
     * @return true if the location is known
     */
    public boolean hasLocation(final Location pLocation) {
        final boolean hasLocation = this.fileManager.hasLocation(pLocation);
        return hasLocation;
    }
    
    /**
     * Gets a {@linkplain JavaFileObject file object} for input representing the specified class of the specified kind
     * in the given location.
     */
    public JavaFileObject getJavaFileForInput(
            final Location pLocation,
            final String   pClassName,
            final Kind     pKind)
            throws IOException {
        
        final JavaFileObject aFileObject;
        if(this.codes.containsKey(pClassName))
             aFileObject = this.codes.get(pClassName);
        else aFileObject = this.fileManager.getJavaFileForInput(pLocation, pClassName, pKind);
        
        // Log
        if(JavaCompiler.DEBUG_MODE) {
            String aParameterStr = this.getToString(pLocation, pClassName, pKind);
            String aResultStr    = " = " + aFileObject;
            System.out.println("getJavaFileForInput" + aParameterStr + aResultStr);
            System.out.println();
        }
        return aFileObject;
    }
    
    /**
     * Gets a {@linkplain JavaFileObject file object} for output representing the specified class of the specified kind
     * in the given location.
     */
    public JavaFileObject getJavaFileForOutput(
            final Location   pLocation,
            final String     pClassName,
            final Kind       pKind,
            final FileObject pSibling)
            throws IOException {
        
        final JavaFileObject aFileObject;
        if (this.classes.containsKey(pClassName)) {
            aFileObject = this.classes.get(pClassName);
            
        } else if (this.codes.containsKey(pSibling.getName())) {
            final String aClassName   = pClassName + MemoryFileManager.CLASS_FILE_SUFFIX;
            final String aSiblingPath = ((JavaCodeMemoryFileObject)pSibling).getPath();
            aFileObject = new JavaClassMemoryFileObject(aClassName, aSiblingPath);
            this.classes.put(pClassName, (JavaClassMemoryFileObject)aFileObject);
            
        } else {
            aFileObject
                = this.fileManager.getJavaFileForOutput(pLocation, pClassName, pKind, pSibling);
        }
        
        // Log
        if(JavaCompiler.DEBUG_MODE) {
            String aParameterStr = this.getToString(pLocation, pClassName, pKind, pSibling);
            String aResultStr    = " = " + aFileObject;
            System.out.println("getJavaFileForInput" + aParameterStr + aResultStr);
            System.out.println();
        }
        
        // Delete the code, so that it will not be called again, since the class is going to be saved
        final String aSourceName = pClassName + MemoryFileManager.SOURCE_FILE_SUFFIX;
        this.deleteCode(aSourceName);
        
        return aFileObject;
    }
    
    /** {@inheritDoc} */ @Override
    public FileObject getFileForInput(
            final Location pLocation,
            final String   pPackageName,
            final String   pRelativeName)
            throws IOException {
        
        final FileObject aFileObject;
        aFileObject = this.fileManager.getFileForInput(pLocation, pPackageName, pRelativeName);
        
        return aFileObject;
    }
    
    /** {@inheritDoc} */ @Override
    public FileObject getFileForOutput(
            final Location   pLocation,
            final String     pPackageName,
            final String     pRelativeName,
            final FileObject pSibling)
            throws IOException  {
        
        final FileObject aFileObject;
        aFileObject = this.fileManager.getFileForOutput(pLocation, pPackageName, pRelativeName, pSibling);
        
        return aFileObject;
    }
    
    /** {@inheritDoc} */ @Override
    public void flush() throws IOException  {
        this.fileManager.flush();
    }
    
    /** {@inheritDoc} */ @Override
    public void close() throws IOException  {
        this.fileManager.close();
    }
    
    // OptionChecker -------------------------------------------------------------------------------
    
    /** {@inheritDoc} */ @Override
    public int isSupportedOption(final String pOption) {
        final int aNumberOfSupportedOption = this.fileManager.isSupportedOption(pOption);
        return aNumberOfSupportedOption;
    }
    
    // Utilities -----------------------------------------------------------------------------------
    
    String getToString(final Object ... pParams) {
        final String aToString = Arrays.toString(pParams);
        return aToString;
    }
}