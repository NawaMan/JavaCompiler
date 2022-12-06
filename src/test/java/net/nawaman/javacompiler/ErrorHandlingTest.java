package net.nawaman.javacompiler;

import static net.nawaman.javacompiler.helpers.TestHelper.assertAsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.junit.jupiter.api.Test;

import net.nawaman.javacompiler.helpers.TestCompiler;

class ErrorHandlingTest {
    
    @Test
    void testErrorHandling() {
        var testCompiler = new TestCompiler(new JavaCompiler());
        
        var className = "TestClass03A";
        var classCode = new StringBuilder();
        classCode.append("public class "+className+" extends Thread {\n");
        classCode.append("    @Override public void run() {\n");
        classCode.append("        return 5;\n");
        classCode.append("    }\n");
        classCode.append("}\n");
        
        try {
            testCompiler.compileCode(className, classCode);
            
        } catch (AssertionError error) {
            assertAsString(
                    "java.lang.AssertionError: Problem compiling the test class 'TestClass03A': \n"
                    + "TestClass03A.java:3: error: incompatible types: unexpected return value\n"
                    + "        return 5;\n"
                    + "               ^\n"
                    + "1 error\n"
                    + "",
                    error.getClass().getCanonicalName() + ": " + error.getMessage());
        }
    }
    
    @Test
    void testErrorHandling_withDiagnosics() {
        var testCompiler = new TestCompiler(new JavaCompiler());
        
        var className = "TestClass03B";
        var classCode = new StringBuilder();
        classCode.append("public class "+className+" extends Thread {\n");
        classCode.append("    @Override public void run() {\n");
        classCode.append("        return 5;\n");
        classCode.append("    }\n");
        classCode.append("}\n");
        
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try {
            testCompiler.compileCode(className, classCode, diagnostics);
            
        } catch (AssertionError error) {
            assertAsString(
                    "java.lang.AssertionError: Problem compiling the test class 'TestClass03B': \n"
                    + "Compile failed (See in Diagnosic collection for more info.)",
                    error.getClass().getCanonicalName() + ": " + error.getMessage());
        }
        
        var problems = diagnostics.getDiagnostics();
        if (problems.size() != 1) {
            throw new AssertionError("There should be only problem here.");
        }
        
        var problem = problems.get(0);
        
        assertAsString("compiler.err.prob.found.req", problem.getCode());
        
        assertEquals(16, problem.getColumnNumber(),  "The problem's 'column number' is not the same.");
        assertEquals(93, problem.getEndPosition(),   "The problem's 'end position' is not the same.");
        assertEquals(3,  problem.getLineNumber(),    "The problem's 'line number' is not the same.");
        assertEquals(92, problem.getPosition(),      "The problem's 'position' is not the same.");
        assertEquals(92, problem.getStartPosition(), "The problem's 'position' is not the same.");
        
        assertEquals(Kind.ERROR, problem.getKind(), "The problem's 'kind' is not the same.");
        
        assertAsString("The problem's 'message' is not the same.",
                       "incompatible types: unexpected return value",
                       problem.getMessage(Locale.getDefault()));
        
        assertTrue(problem.getSource().toString().startsWith("net.nawaman.javacompiler.JavaCodeMemoryFileObject@"),
                   "The problem's 'source' is not the same.");
    }
    
}
