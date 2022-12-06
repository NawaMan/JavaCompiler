package net.nawaman.javacompiler.helpers;

import static net.nawaman.javacompiler.helpers.TestHelper.assertAsString;
import static net.nawaman.javacompiler.helpers.TestHelper.captureOut;

public class TestThread {
    
    private final Class<Thread> threadClass;
    
    public TestThread(Class<Thread> threadClass) {
        this.threadClass = threadClass;
    }
    
    public void validateOut(CharSequence expectedOut) throws Exception {
        var thread = threadClass
                .getConstructor()
                .newInstance();
        
        var capturedOut = captureOut(() -> {
            thread.start();
            while(thread.isAlive());
        });
        
        var expectedStr = expectedOut.toString();
        var capturedStr = capturedOut.trim();
        assertAsString(expectedStr, capturedStr);
    }
}
