package net.nawaman.javacompiler;

import static net.nawaman.javacompiler.ClassData.getNonNullByteArray;

import org.junit.jupiter.api.Test;

class ClassDataTest {
    
    @Test
    void testGetNonNullByteArray() {
        assert(getNonNullByteArray(null)        == ClassData.EMPTY_ARRAY_BYTE);
        assert(getNonNullByteArray(new byte[0]) == ClassData.EMPTY_ARRAY_BYTE);
        
        byte[] aNewOne = new byte[5];
        assert(getNonNullByteArray(aNewOne) == aNewOne);
    }
    
}
