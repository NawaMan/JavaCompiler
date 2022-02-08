package net.nawaman.javacompiler;

import java.io.*;

import javax.lang.model.element.*;

/**
 * This class represents a Java Code file.
 * 
 * This class hold a java code (as a string) so that the JavaCompiler can access to for
 *   the compilation.
 * 
 * Implementation note:
 *   From the experiment the following method never seems to be invoked whatsoever. Therefore,
 *     there seems to be no need to implement them. 
 *     - NestingKind  getNestingKind()
 *     - Modifier     getAccessLevel()
 *     - OutputStream openOutputStream()
 *     - Writer       openWriter()
 **/
final class JavaCodeMemoryFileObject extends MemoryFileObject {
    
    static public final String SOURCE_FILE_SUFFIX = ".java";
    
    JavaCodeMemoryFileObject(
            final String pName,
            final String pPath,
            final String pTheCode) {
        super(pName, pPath);
        this.code = pTheCode;
    }
    
    private String code;
    
    /** Gets the kind of this file object. */
    final public Kind getKind() {
        return Kind.SOURCE;
    }
    
    /** Provides a hint about the nesting level of the class represented by this file object. */
    public NestingKind getNestingKind() {
        return null;
    }
    
    /** Provides a hint about the access level of the class represented by this file object. */
    public Modifier getAccessLevel() {
        return null;
    }
    
    /** Checks if this file object is compatible with the specified simple name and kind. */ @Override
    public boolean isNameCompatible(
            final String pSimpleName,
            final Kind   pJFOKind) {
        
        final boolean IsTypeMatch = (pJFOKind == Kind.SOURCE);
        if (!IsTypeMatch)
            return false;
        
        final String  JavaSourceFileName = pSimpleName + JavaCodeMemoryFileObject.SOURCE_FILE_SUFFIX;
        final String  myName             = this.getName();              
        final boolean IsNameMatch        = myName.equals(JavaSourceFileName);
        return IsNameMatch;
    }
    
    public String getCode() {
        return this.code;
    }
    
    // FileObject ----------------------------------------------------------------------------------
    
    /** Gets an InputStream for this file object. */
    public InputStream openInputStream() {
        final byte[]      aCodeBytes   = this.code.getBytes();
        final InputStream aInputStream = new ByteArrayInputStream(aCodeBytes);
        return aInputStream;
    }
    
    /** Gets an OutputStream for this file object. */
    public OutputStream openOutputStream() {
        return null;
    }
    
    /** Gets a reader for this object. */
    public Reader openReader(final boolean ignoreEncodingErrors) {
        InputStream aInputStream = this.openInputStream();
        Reader      aReader      = new InputStreamReader(aInputStream);
        return aReader;
    }
    
    
    /** Gets the character content of this file object, if available. */
    public CharSequence getCharContent(
            final boolean ignoreEncodingErrors)
            throws  IOException {
        return this.code;
    }
    
    /** Gets a Writer for this file object. */
    public Writer openWriter() {
        return null;
    }
    
}