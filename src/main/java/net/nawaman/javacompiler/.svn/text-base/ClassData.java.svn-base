package net.nawaman.javacompiler;

import java.io.*;
import java.util.*;

/** Data of Class - for persistence */
final public class ClassData implements Serializable {
    
    static private final long serialVersionUID = 5863156353154656515L;
    
    static public  final byte[]      EMPTY_ARRAY_BYTE      = new byte[0];
    static public  final ClassData[] EMPTY_ARRAY_CLASSDATA = new ClassData[0];
    
    static private int CACHE_INDEX__HAS_CODE   = 0;
    static private int CACHE_INDEX__HAS_NOCODE = 1;
    static private HashMap<JCompiler, HashMap<String, ClassData[]>> CACHE_CLASS_DATAS =
            new HashMap<JCompiler, HashMap<String, ClassData[]>>();
    
    static private int GetCacheIndex(final boolean pIsToSaveCode) {
        final int aCacheIndex = pIsToSaveCode
                    ? ClassData.CACHE_INDEX__HAS_CODE
                    : ClassData.CACHE_INDEX__HAS_NOCODE;
        return aCacheIndex;
    }
    
    static ClassData GetClassData(
            final String    pName,
            final JCompiler pJCompiler,
            final boolean   pIsToSaveCode) {
        
        final HashMap<String, ClassData[]> aCache = ClassData.CACHE_CLASS_DATAS.get(pJCompiler);
        if (aCache == null)
            return null;
        
        final int         aCacheIndex = GetCacheIndex(pIsToSaveCode);
        final ClassData[] aClassDatas = aCache.get(pName);
        if (aClassDatas == null)
            return null;
        
        final ClassData aClassData = aClassDatas[aCacheIndex];
        return aClassData;
    }
            
    static ClassData NewClassData(
            final String                    pName,
            final JCompiler                 pJCompiler,
            final Class<?>                  pClass,
            final JavaClassMemoryFileObject JCMFO,
            final boolean                   pIsToSaveCode) {
        
        HashMap<String, ClassData[]> aCache = ClassData.CACHE_CLASS_DATAS.get(pJCompiler);
        if (aCache == null) {
            aCache = new HashMap<String, ClassData[]>();
            ClassData.CACHE_CLASS_DATAS.put(pJCompiler, aCache);
        }
        
        ClassData[] aClassDatas = aCache.get(pName);
        if (aClassDatas == null) {
            aClassDatas = new ClassData[2];
            aCache.put(pName, aClassDatas);
        }
        
        final int aCacheIndex = GetCacheIndex(pIsToSaveCode);
        ClassData aClassData  = aClassDatas[aCacheIndex];
        if (aClassData == null) {
            aClassData               = new ClassData(pName, pJCompiler, pClass, JCMFO, pIsToSaveCode);
            aClassDatas[aCacheIndex] = aClassData;
            
            aClassData.prepareEnclosingClassDatas(pJCompiler, pClass);
            aClassData.prepareEnclosedClassDatas (pJCompiler, pClass);
        }
        
        return aClassData;
    }
    
    private ClassData(
            final String                    pName,
            final JCompiler                 pJCompiler,
            final Class<?>                  pClass,
            final JavaClassMemoryFileObject JCMFO,
            final boolean                   pIsToSaveCode) {
        
        this.name  = JCMFO.getName();
        this.path  = JCMFO.getPath();
        this.bytes = JCMFO.getBAOS().toByteArray().clone();
        this.code  = pIsToSaveCode ? pJCompiler.getCode(pName) : null;
    }
    
    // Required data
    final private String name;
    final private String path;
    final private byte[] bytes;
    
    // Optional data 
    private String      code            = null;
    private ClassData   enclosingClass  = null;
    private ClassData[] enclosedClasses = ClassData.EMPTY_ARRAY_CLASSDATA;
    
    /** Returns the name of the class */
    public String getName() {
        return this.name;
    }
    /** Returns the path of the class class file in the file manager */
    public String getPath() {
        return this.path;
    }
    
    /** Returns Java code of the class or null if the code is not saved */
    public String getCode() {
        return this.code;
    }
    
    /** Returns the length of the byte code */
    public int getByteCount() {
        if (this.bytes == null)
            return 0;
        
        final int aCount = this.bytes.length;
        return aCount;
    }
    /** Returns the byte at the position */
    public Byte getByte(final int I) {
        if ((I < 0) || (I > this.bytes.length)) 
            return null;
        
        final Byte aByte = this.bytes[I];
        return aByte;
    }
    
    /** Returns the bytes */
    byte[] getRawBytes() {
        return this.bytes;
    }
    /** Returns the bytes */
    public byte[] getBytes() {
        return this.bytes.clone();
    }
    
    /** Prepare the enclosing ClassDatas - used by JCompiler */
    private void prepareEnclosingClassDatas(
            final JCompiler pJCompiler,
            final Class<?>  pClass) {
        final Class<?> aEnclosingClass = pClass.getEnclosingClass();
        if(aEnclosingClass == null) {
            this.enclosingClass = null;
            return;
        }
        
        final String    aEnclosingClassName = aEnclosingClass.getName();
        final ClassData aEnclosingClassData = pJCompiler.getCompiledClassData(aEnclosingClassName);
        this.enclosingClass = aEnclosingClassData;
    }
    /** Returns the Enclosing ClassData this class is enclosed in */
    public ClassData getEnclosingClassClassData() {
        return this.enclosingClass;
    }
    
    /** Prepare the enclosed ClassDatas - used by JCompiler */
    private void prepareEnclosedClassDatas(
            final JCompiler pJCompiler,
            final Class<?>  pClass) {
        // Get the enclosed classes
        final Class<?>   aClass                 = pClass;
        final Class<?>[] aEnclosedClasses       = aClass.getDeclaredClasses();
        final int        aEnclosedClasses_Count = aEnclosedClasses.length;
        
        if (aEnclosedClasses_Count == 0) {
            this.enclosedClasses = ClassData.EMPTY_ARRAY_CLASSDATA;
            return;
        }
        
        this.enclosedClasses = new ClassData[aEnclosedClasses_Count];
        for (int i = 0; i < aEnclosedClasses_Count; i++) {
            final Class<?> EnclosedClass = aEnclosedClasses[i];
            if (EnclosedClass == null)
                continue;
                
            final String    aEnclosedClass_Name = EnclosedClass.getName();
            final ClassData aEnclosedClass_Data = pJCompiler.getCompiledClassData(aEnclosedClass_Name);
            this.enclosedClasses[i] = aEnclosedClass_Data;
        }        
    }
    
    /** Returns the number of Enclosed ClassData this class is enclosing */
    public int getEnclosedClassDataCount() {
        final int aCount = this.enclosedClasses.length;
        return aCount;
    }
    /** Returns the Enclosed ClassData this class is enclosing at the index */
    public ClassData getEnclosedClassData(final int I) {
        if ((I < 0) || (I >= this.enclosedClasses.length))
            return null;
        
        final ClassData aClassData = this.enclosedClasses[I];
        return aClassData;
    }
    
    /** Returns a shared empty byte array if the given array is null or empty */
    static private byte[] GetNonNullByteArray(final byte[] pBytes) {
        if ((pBytes == null) || (pBytes.length == 0))
            return ClassData.EMPTY_ARRAY_BYTE;
        
        return pBytes;
    }
    
    // Test ------------------------------------------------------------------------------------------------------------
    
    static public void Test_Me() {
        Test_GetNonNullByteArray();
    }
    
    static private void Test_GetNonNullByteArray() {
        assert(GetNonNullByteArray(null)        == ClassData.EMPTY_ARRAY_BYTE);
        assert(GetNonNullByteArray(new byte[0]) == ClassData.EMPTY_ARRAY_BYTE);
        
        byte[] aNewOne = new byte[5];
        assert(GetNonNullByteArray(aNewOne) == aNewOne);
    }
}