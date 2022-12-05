package net.nawaman.javacompiler;

import static net.nawaman.javacompiler.ClassData.getNonNullByteArray;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClassDataTest {
    
    @Test
    void testGetNonNullByteArray() {
        assertTrue(getNonNullByteArray(null)        == ClassData.EMPTY_ARRAY_BYTE);
        assertTrue(getNonNullByteArray(new byte[0]) == ClassData.EMPTY_ARRAY_BYTE);
        
        var aNewOne = new byte[5];
        assertTrue(getNonNullByteArray(aNewOne) == aNewOne);
    }
    
}
