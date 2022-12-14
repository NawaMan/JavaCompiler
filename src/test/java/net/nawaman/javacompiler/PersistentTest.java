package net.nawaman.javacompiler;

import static net.nawaman.javacompiler.helpers.TestHelper.assertAsString;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import net.nawaman.javacompiler.helpers.TestCompiler;
import net.nawaman.javacompiler.helpers.TestThread;

class PersistentTest {
    
    public static final boolean saveCode = true;
    public static final boolean noCode   = false;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void persistentTest() throws Exception {
        var testCompiler = new TestCompiler(new JavaCompiler());
        
        var className = "TestClass03A";
        var classCode = new StringBuilder();
        classCode.append("public class " + className + " extends Thread {\n");
        classCode.append("    @Override\n");
        classCode.append("    public void run() {\n");
        classCode.append("        for(int Counter = 0; Counter < 10; Counter++) {\n");
        classCode.append("            System.out.println(\"Counter: \" + Counter);\n");
        classCode.append("        }\n");
        classCode.append("    }\n");
        classCode.append("}\n");
        
        testCompiler
        .compileCode(className, classCode)
        .validateOut(
                  "Counter: 0\n"
                + "Counter: 1\n"
                + "Counter: 2\n"
                + "Counter: 3\n"
                + "Counter: 4\n"
                + "Counter: 5\n"
                + "Counter: 6\n"
                + "Counter: 7\n"
                + "Counter: 8\n"
                + "Counter: 9");
        
        var classDataWithCode    = testCompiler.getCompiledClassData(className, saveCode);
        var classDataWithoutCode = testCompiler.getCompiledClassData(className, noCode);
        
        assertAsString(
                "The saved code is not the same.",
                classCode.toString(),
                classDataWithCode.getCode());
        
        assertNull(
                classDataWithoutCode.getCode(),
                "The ClassData with no code should have no code.");
        
        
        var newTestCompiler = new TestCompiler(new JavaCompiler());
        
        newTestCompiler
        .getClassByName(className)
        .ifPresent(__ -> {
            throw new AssertionError("Something is wrong the class should not be found.");
        });
        
        newTestCompiler.addClassData(classDataWithoutCode);
        newTestCompiler
                .getClassByName(className)
                .map(c -> ((Class)c).asSubclass(Thread.class))
                .map(TestThread::new)
                .get()
                .validateOut(
                          "Counter: 0\n"
                        + "Counter: 1\n"
                        + "Counter: 2\n"
                        + "Counter: 3\n"
                        + "Counter: 4\n"
                        + "Counter: 5\n"
                        + "Counter: 6\n"
                        + "Counter: 7\n"
                        + "Counter: 8\n"
                        + "Counter: 9");
    }
    
}
