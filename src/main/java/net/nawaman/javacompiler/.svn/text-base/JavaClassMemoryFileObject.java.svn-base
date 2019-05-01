package net.nawaman.javacompiler;

import java.io.*;

import javax.lang.model.element.*;

/**
 * This class represents a Java Class file.
 * 
 * When a java code is compiled by JavaCompiler, the result byte-code will be stored within
 *   instances of this class.
 * 
 * Implementation note:
 *   It is intentional that the class is package private. This is to prevent modification to
 *     the byte-code after it is compiled. If that occurs, the byte-code will loose its integrity
 *     and may result in a corrupted or illegal Java class.
 *   From the experiment the following method never seems to be invoked whatsoever. Therefore,
 *     there seems to be no need to implement them. 
 *     - NestingKind  getNestingKind()
 *     - Modifier     getAccessLevel() 
 **/
final class JavaClassMemoryFileObject extends MemoryFileObject {
    
    JavaClassMemoryFileObject(
            final String pName,
            final String pPath) {
        super(pName, pPath);
        this.BAOS = new ByteArrayOutputStream();
    }
    JavaClassMemoryFileObject(
            final String pName,
            final String pPath,
            final byte[] pByteCode)
            throws IOException {
        this(pName, pPath);
        
        this.BAOS.write(pByteCode);
        this.BAOS.close();
    }
    
    private ByteArrayOutputStream BAOS;

    /** Gets the kind of this file object. */
    public Kind getKind() {
        return Kind.CLASS;
    }

    /** Provides a hint about the nesting level of the class represented by this file object. */
    //public NestingKind getNestingKind() { return this.NKind; }
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
        
        final boolean IsTypeMatch = (pJFOKind == Kind.CLASS);
        if (!IsTypeMatch)
            return false;
        
        final String  aName       = this.getName();
        final boolean aIsNameMatch = aName.equals(pSimpleName);
        return aIsNameMatch;
    }
    
    ByteArrayOutputStream getBAOS() {
        return this.BAOS;
    }

    // FileObject ----------------------------------------------------------------------------------

    /** Gets an InputStream for this file object. */
    public InputStream openInputStream() {
        final byte[]      BAOS_Bytes   = this.BAOS.toByteArray();
        final InputStream aInputStream = new ByteArrayInputStream(BAOS_Bytes);
        return aInputStream;
    }

    /** Gets an OutputStream for this file object. */
    public OutputStream openOutputStream() {
        this.updateLastModified();
        this.BAOS = new ByteArrayOutputStream();
        return this.BAOS;
    }

    /** Gets a reader for this object. */
    public Reader openReader(final boolean ignoreEncodingErrors) {
        final InputStream aInputStream = this.openInputStream();
        final Reader      aReader      = new InputStreamReader(aInputStream);
        return aReader;
    }
    

    /** Gets the character content of this file object, if available. */
    public CharSequence getCharContent(
            final boolean ignoreEncodingErrors)
            throws IOException {
        final Reader aReader  = this.openReader(ignoreEncodingErrors);
        final String aContent = ReadCharContent(aReader);
        return aContent;
    }
    static private String ReadCharContent(
            final Reader pReader)
            throws IOException {
        final BufferedReader aBReader = new BufferedReader(pReader);
        final StringBuffer   aSBuffer = new StringBuffer();
        while (aBReader.ready()) {
            final String aLine = aBReader.readLine();
            aSBuffer.append(aLine);
        }
        return aSBuffer.toString();
    }

    /** Gets a Writer for this file object. */
    public Writer openWriter() {
        this.updateLastModified(); 
        final OutputStream aOutputStream = this.openOutputStream();
        final Writer       aWriter       = new OutputStreamWriter(aOutputStream);
        return aWriter;
    }

}