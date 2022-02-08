package net.nawaman.javacompiler;

import java.io.*;
import java.util.*;

/**
 * Loader objects from a stream and takes care of all the class data.
 * 
 * Using this class if you save the objects using JCObjectOutputStream. They maintain the same
 *   protocol.
 **/
public class JavaCompilerObjectInputStream extends ObjectInputStream {
    
    static private final String INVALID_DATA_FORMAT      = "The data in the stream is in an invalid fomat.";
    static private final String MAGIC_WORD_UNMATCH       = "Magic word is not match: %s";
    static private final String UNKNOWN_PROTOCOL         = "Unknown protocal: %s";
    static private final String UNKNOWN_PROTOCOL_VERSION = "Unknown protocal version: %s";
    
    static final protected class ConstructorData {
        ConstructorData() {}
        ByteArrayInputStream dataByteBuffer  = null;
        ByteArrayInputStream classByteBuffer = null;
        int                  classCount      =   -1;
    }
    
    // Extract two ByteArrayInputStream out of one.
    static public JavaCompilerObjectInputStream NewJavaCompilerObjectInputStream(
            final InputStream pIS)
            throws IOException, ClassNotFoundException {
        return NewJavaCompilerObjectInputStream(pIS, (JavaCompiler)null);
    }
    // Extract two ByteArrayInputStream out of one.
    static public JavaCompilerObjectInputStream NewJavaCompilerObjectInputStream(
            final InputStream pIS,
            final ClassLoader pParentClassLoader)
            throws IOException, ClassNotFoundException {
        final JavaCompiler aJavaCompiler = JavaCompiler.NewJavaCompiler(pParentClassLoader);
        return NewJavaCompilerObjectInputStream(pIS, aJavaCompiler);
    }
    // Extract two ByteArrayInputStream out of one.
    static public JavaCompilerObjectInputStream NewJavaCompilerObjectInputStream(
            final InputStream  pIS,
            final JavaCompiler pJavaCompiler)
            throws IOException, ClassNotFoundException {
        final JavaCompilerObjectInputStream aIStream =
                new JavaCompilerObjectInputStream(getConstructorData(pIS), pJavaCompiler);
        return aIStream;
    }
    
    // Extract two ByteArrayInputStream out of one.
    static protected ConstructorData getConstructorData(final InputStream pIS) throws IOException {
        try {
            final ObjectInputStream OIS = new ObjectInputStream(pIS);
            EnsureMagicWord(OIS);
            EnsureProtocol (OIS);
            EnsureVersion  (OIS);
            
            final ConstructorData JCOISCD = JavaCompilerObjectInputStream.ReadConstructorData(OIS);
            return JCOISCD;
            
        } catch(IOException IOE) {
            throw IOE;
            
        } catch (Exception E) {
            throw new IOException(INVALID_DATA_FORMAT, E);
        }
    }
    
    static private void EnsureMagicWord(final ObjectInputStream OIS) throws IOException {
        final String  aText             = OIS.readUTF();
        final boolean aIsMagicWordEqual = JavaCompilerObjectOutputStream.MAGIC_WORD.equals(aText);
        if(!aIsMagicWordEqual)
            throw new IllegalArgumentException(String.format(MAGIC_WORD_UNMATCH, aText));
    }
    static private void EnsureProtocol(ObjectInputStream OIS) throws IOException {
        final String  aText            = OIS.readUTF();
        final boolean aIsProtocolKnown = JavaCompilerObjectOutputStream.PROTOCOL_NAME.equals(aText);
        if(!aIsProtocolKnown)
            throw new IllegalArgumentException(String.format(UNKNOWN_PROTOCOL, aText));
    }
    static private void EnsureVersion(ObjectInputStream OIS) throws IOException {
        final int aVersion = OIS.readInt();
        if(aVersion != 100) {
            final String VersionStr = (aVersion / 100) + "." + (aVersion % 100);
            throw new IllegalArgumentException(String.format(UNKNOWN_PROTOCOL_VERSION, VersionStr));
        }
    }
    static private ConstructorData ReadConstructorData(
            final ObjectInputStream OIS)
            throws
                IOException,
                ClassNotFoundException {
        final ConstructorData aConstructorData = new ConstructorData();
        aConstructorData.classCount      = OIS.readInt();
        aConstructorData.classByteBuffer = new ByteArrayInputStream((byte[])OIS.readObject());
        aConstructorData.dataByteBuffer  = new ByteArrayInputStream((byte[])OIS.readObject());
        return aConstructorData;
    }
    
    /** Constructs an ObjectWriter */
    protected JavaCompilerObjectInputStream(
            final ConstructorData pConstructorData,
            final JavaCompiler    pJCompiler)
            throws
                IOException,
                ClassNotFoundException {
        super(pConstructorData.dataByteBuffer);
        
        this.jcompiler = (pJCompiler == null) ? JavaCompiler.Instance : pJCompiler;
        this.prepareClasses(pConstructorData);
    }
    
    JavaCompiler jcompiler;
    
    static private Map<String, String> replaceClasses;
    
    static {
        var map = new HashMap<String, String>();
        map.put("net.nawaman.regparser.ParseResult", "net.nawaman.regparser.result.ParseResult");
        replaceClasses = map;
    }
    
    private String replaceClassName(String orgName) {
        return replaceClasses
                .entrySet()
                .stream()
                .map(entry -> {
                    var fromClass = entry.getKey();
                    var toClass   = entry.getValue();
                    var from      = "^" + fromClass;
                    var fromArray = "^\\[L" + fromClass;
                    var toArray   = "[L" + toClass;
                    
                    return orgName
                            .replaceFirst(from, toClass)
                            .replaceFirst(fromArray, toArray);
                })
                .filter(name -> !orgName.equals(name))
                .findFirst()
                .orElse(orgName);
    }
    
    
    /** Returns the class loader of this option */
    public JavaCompiler getJavaCompiler() {
        return this.jcompiler;
    }
    
    private void prepareClasses(
            final ConstructorData pConstructorData)
            throws
                IOException,
                ClassNotFoundException {
        final ByteArrayInputStream aClassByteBuffer = pConstructorData.classByteBuffer;
        final ObjectInputStream    aClassDatas      = new ObjectInputStream(aClassByteBuffer);
        final HashSet<ClassData>   aAddedClassDatas = new HashSet<ClassData>();
        
        int       aClassCount = pConstructorData.classCount;
        ClassData aClassData  = null;
        
        while((aClassCount-- > 0) && (aClassData = (ClassData)aClassDatas.readObject()) != null) {
            if(aAddedClassDatas.contains(aClassData))
                continue;
            this.jcompiler.addClassData(aClassData);
            aAddedClassDatas.add(aClassData);
        }
    }
    
    /** Resolve a class */ @Override
    protected Class<?> resolveClass(
            final ObjectStreamClass pClassDesc)
            throws
                IOException,
                ClassNotFoundException {
        var originalName = pClassDesc.getName();
        var replacedName = replaceClassName(originalName);
        if (!originalName.equals(replacedName)) {
            return Class.forName(replacedName);
        }
        
        try {
            final Class<?> C = super.resolveClass(pClassDesc);
            if(C == null)
                return C;
        } catch(ClassNotFoundException CNFE) {
        }
        
        final String   aName  = originalName;
        final Class<?> aClass = this.jcompiler.forName(aName);
        return aClass;
    }
    
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass pClassDesc = super.readClassDescriptor();
        var originalName = pClassDesc.getName();
        var replacedName = replaceClassName(originalName);
        if(!replacedName.equals(pClassDesc.getName())) {
            pClassDesc = ObjectStreamClass.lookup(Class.forName(replacedName));
        }
        return pClassDesc;
    }
}
