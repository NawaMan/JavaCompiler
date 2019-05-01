package net.nawaman.javacompiler;

import java.io.*;

/**
 * Write an object to a stream and takes care of all the class data.
 *
 * Use this class with JCObjectInputStream as they maintain the same protocol.
 * Using JCObjectInputStream/JCObjectOutputStream rather than JavaCompiler as you do not need to
 * worry about the writing of object.
 * 
 * IMPORTANT NOTE: The close(...) method of this class MUST BE CALLED to endure all the data is
 * saved. This is a weak point of the technique as it wait until all objects are written to be sure
 * that all class-data are saved and that is when close(...) is called.
 * 
 * If do not have any specific use need, Use JCObjectOutputStream.SerializeObjects(...) and
 * JCObjectOutputStream.DeSerializeObjects(...) methods for even more continent.
 * 
 * SOME TRICK: It is not possible to compile a class with JavaCompiler and have it loaded and saved
 * with other ObjectOutputStream as the loading process will always needs the class information
 * before any customization can be done. HOWEVER, it is possible to create a class (compile by
 * a regular java compiler) (called now as the host object) that have a class that is compiled by
 * JavaCompiler (called now as the target object) to be serializable like regular object. To do so,
 * the host object will have to override the methods readObject(...) and writeObject(...) to
 * separate the serialization of the target object class data and the target object itself. This
 * technique is called the "double serialization" technique.
 *     
 * USE THE DOUBLE SERIALIZATION TECHNIQUE WITH CONSIDERATION!!!! The technique will result in
 * a separated pool of objects as they are serialized separately. For example, if the host object
 * and the target object have a reference to another object. This 'another object' will
 * be serialized twice - once as referred by the host object and another as referred by
 * the target object.
 **/
public class JavaCompilerObjectOutputStream extends ObjectOutputStream {
    
    static public final String MAGIC_WORD            = "JCO-NEEDED";
    static public final String PROTOCOL_NAME         = "JCO";
    static public final int    PROTOCOL_VERSION_1_00 = 100;    // Version 1.00
    
    private ByteArrayOutputStream objectBytesBuffer;
    private ByteArrayOutputStream classBytesBuffer;
    private int                   classCount = 0;
    
    final private ObjectOutputStream classBuffer;
    final private OutputStream       OS;
    
    /** Creates a new JavaOutputStream */
    static public JavaCompilerObjectOutputStream NewJavaCompilerObjectOutputStream(
            final  OutputStream pOS)
            throws IOException {
        final ByteArrayOutputStream          aOBuffer = new ByteArrayOutputStream();
        final JavaCompilerObjectOutputStream aOStream = new JavaCompilerObjectOutputStream(aOBuffer, pOS);
        return aOStream;
    }
    
    /** Constructs an ObjectWriter */
    protected JavaCompilerObjectOutputStream(
            final ByteArrayOutputStream pBAOS,
            final OutputStream          pOS)
            throws IOException {
        super(pBAOS);
        this.objectBytesBuffer = pBAOS;
        
        if (pOS == null)
            throw new NullPointerException();
        
        this.OS               = pOS;
        this.classBytesBuffer = new ByteArrayOutputStream();
        this.classBuffer      = new ObjectOutputStream(this.classBytesBuffer);
    }
    
    /** Just in case, close the stream */ @Override
    protected void finalize() throws Throwable {
        // I know, I know ... We should not rely on this. But this is just in case.
        try { this.close(); }
        catch (Throwable T) {}
    }
    
    // Capture the class that needs to save the data.
    
    /** Write the class' data of the given class into the class data container */
    private void writeClassDataNoCheck(
            final Class<?> pClass)
            throws IOException {
        
        final JCompiler aJCompiler = ((JCompiler)pClass.getClassLoader());
        final String    aClassName = pClass.getName();
        
        ClassData aClassData = aJCompiler.getCompiledClassData(aClassName);
        if(aClassData == null)
            return;
            
        while(aClassData.getEnclosingClassClassData() != null)
            aClassData = aClassData.getEnclosingClassClassData();
        
        this.classBuffer.writeObject(aClassData);                             
        this.classCount++;
    }
    
    /** Write the class' data of the given class into the class data container */
    protected void writeClassData(
            final Class<?> pClass)
            throws IOException {
        final ClassLoader aClassLoader = pClass.getClassLoader();
        if(!(aClassLoader instanceof JCompiler))
            return;
        
        this.writeClassDataNoCheck(pClass);
    }
    
    /**{@inheritDoc}*/ @Override
    protected void writeClassDescriptor(
            final ObjectStreamClass pClassDescriptor)
            throws IOException {
        super.writeClassDescriptor(pClassDescriptor);
        final Class<?>    aClass       = pClassDescriptor.forClass();
        final ClassLoader aClassLoader = aClass.getClassLoader();
        if(aClassLoader instanceof JCompiler)
            this.writeClassDataNoCheck(aClass);
    }
    
    /**{@inheritDoc}*/ @Override
    public void close() throws IOException {
        if (this.classBytesBuffer == null)
            return;
        
        this.ensureAllBufferClosed();
        
        final ObjectOutputStream OOS = this.getObjectOutputStream();
        this.writeHeader          (OOS);
        this.writeClassInformation(OOS);
        this.finishWriting        (OOS);
    }
    private void ensureAllBufferClosed() throws IOException {
        this.objectBytesBuffer.flush();
        this.objectBytesBuffer.close();
        this.classBuffer.flush();
        this.classBuffer.close();
    }
    private ObjectOutputStream getObjectOutputStream() throws IOException {
        if (this.OS instanceof ObjectOutputStream)
             return (ObjectOutputStream)   this.OS;
        else return new ObjectOutputStream(this.OS);
    }
    private void writeHeader(
            final ObjectOutputStream OOS)
            throws IOException {
        OOS.writeUTF(MAGIC_WORD);
        OOS.writeUTF(PROTOCOL_NAME);
        OOS.writeInt(PROTOCOL_VERSION_1_00);
    }
    private void writeClassInformation(
            final ObjectOutputStream OOS)
            throws IOException {
        OOS.writeInt(this.classCount);
        OOS.writeObject(this.classBytesBuffer .toByteArray());
        OOS.writeObject(this.objectBytesBuffer.toByteArray());
    }
    private void finishWriting(
            final ObjectOutputStream OOS)
            throws IOException {
        OOS.flush();
        OOS.close();
        this.classBytesBuffer  = null;
        this.objectBytesBuffer = null;
        
    }
    
    // Utilities -------------------------------------------------------------------------------------------------------
    
    /** Save objects to a byte array */
    static public void SerializeObjects(
            final File             pFile,
            final Serializable ... pObjs)
            throws IOException {
        final FileOutputStream   FOS = new FileOutputStream(pFile);
        final ObjectOutputStream OOS = new ObjectOutputStream(FOS);
        OOS.writeObject(SerializeObjects(pObjs));
        OOS.flush();
        OOS.close();
    }
    /** Save objects to a byte array */
    static public byte[] SerializeObjects(final Serializable ... pObjs) {
        ByteArrayOutputStream           BAOS = null;
        JavaCompilerObjectOutputStream  JCOS = null;
        try {
            BAOS = new ByteArrayOutputStream();
            JCOS = JavaCompilerObjectOutputStream.NewJavaCompilerObjectOutputStream(BAOS);
            
            // Write all object
            JCOS.writeObject(pObjs);
            
            JCOS.close();
            BAOS.close();
            
            final byte[] Bytes = BAOS.toByteArray();
            
            BAOS = null;
            JCOS = null;
            
            return Bytes;
            
        } catch (Exception e) {
            // There should not be a problem here
            return null;
            
        } finally {
            try { if(BAOS != null) BAOS.close(); } catch (Exception E) {}
            try { if(JCOS != null) JCOS.close(); } catch (Exception E) {}
            
        }
    }
    
    /** Save objects to a byte array */
    static public Serializable[] DeSerializeObjects(
            final File pFile)
            throws IOException, ClassNotFoundException {
        final Serializable[] aSerializables = DeSerializeObjects(pFile, (JavaCompiler)null);
        return aSerializables;
    }
    /** Save objects to a byte array */
    static public Serializable[] DeSerializeObjects(
            final File        pFile,
            final ClassLoader pParentClassLoader)
            throws IOException, ClassNotFoundException {
        final JavaCompiler   aJavaCompiler  = JavaCompiler.NewJavaCompiler(pParentClassLoader);
        final Serializable[] aSerializables = DeSerializeObjects(pFile, aJavaCompiler);
        return aSerializables;
    }
    /** Save objects to a byte array */
    static public Serializable[] DeSerializeObjects(
            final File         pFile,
            final JavaCompiler pJavaCompiler)
            throws IOException, ClassNotFoundException {
        final FileInputStream   aFIS           = new FileInputStream(pFile);
        final ObjectInputStream aOIS           = new ObjectInputStream(aFIS);
        final byte[]            aBytes         = (byte[])aOIS.readObject();
        final Serializable[]    aSerializables = DeSerializeObjects(aBytes, pJavaCompiler);
        aOIS.close();
        return aSerializables;
    }
    
    /** Extract two ByteArrayInputStream out of one. **/
    static public Serializable[] DeSerializeObjects(final byte[] pBytes) {
        final Serializable[] aSerializables = DeSerializeObjects(pBytes, (JavaCompiler)null);
        return aSerializables;
    }
    
    /** Extract two ByteArrayInputStream out of one. **/
    static public Serializable[] DeSerializeObjects(
            byte[]      pBytes,
            ClassLoader pParentClassLoader) {
        final JavaCompiler   aJavaCompiler  = JavaCompiler.NewJavaCompiler(pParentClassLoader);
        final Serializable[] aSerializables = DeSerializeObjects(pBytes, aJavaCompiler);
        return aSerializables;
    }
    
    /** Extract two ByteArrayInputStream out of one. **/
    static public Serializable[] DeSerializeObjects(
            final byte[]       pBytes,
            final JavaCompiler pJCompiler) {
        ByteArrayInputStream           BAIS = null;
        JavaCompilerObjectInputStream  JCOL = null;
        
        try {
            BAIS = new ByteArrayInputStream(pBytes);
            JCOL = JavaCompilerObjectInputStream.NewJavaCompilerObjectInputStream(BAIS, pJCompiler);
            return (Serializable[])JCOL.readObject();
        } catch (Exception e) {
            // There should not be a problem here
            return null;
            
        } finally {
            try { if(BAIS != null) BAIS.close(); } catch (Exception E) {}
            try { if(JCOL != null) JCOL.close(); } catch (Exception E) {}
            
        }
    }
}
