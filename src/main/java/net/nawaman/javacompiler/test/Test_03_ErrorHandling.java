package net.nawaman.javacompiler.test;

import java.util.*;

import javax.tools.*;
import javax.tools.Diagnostic.*;

import net.nawaman.javacompiler.JavaCompiler;


public class Test_03_ErrorHandling {
    
    public static void main(String[] args) {
        
        AllTests.PrintTestTitle();
        
        String Error = null;
        
        final JavaCompiler JC = JavaCompiler.Instance;
        
        // Test 1: Capture error as string --------------------------------------------------------
        String        CName = "TestClass03A";
        StringBuilder Code  = new StringBuilder();
        Code.append("public class "+CName+" extends Thread {\n");
        Code.append("    @Override public void run() {\n");
        Code.append("        return 5;\n");
        Code.append("    }\n");
        Code.append("}\n");
        JC.addCode(CName + ".java", "", Code.toString());
        
        StringBuilder ExpectedError = new StringBuilder();
        ExpectedError.append("TestClass03A.java:3: cannot return a value from method whose result type is void\n");
        ExpectedError.append("        return 5;\n");
        ExpectedError.append("               ^\n");
        ExpectedError.append("1 error\n");
        
        if((Error = JC.compile()) != null) {
            if(!ExpectedError.toString().equals(Error.toString())) {
                System.out.printf("ExpectedError (%d): %s\n", ExpectedError.toString().length(), ExpectedError.toString());
                System.out.printf("Error         (%d): %s\n", Error        .toString().length(), Error        .toString());
                throw new AssertionError("The results are not equals.");
            }
        }
        
        // Test 2: Capture error as Diagnostics ---------------------------------------------------
        CName = "TestClass03B";
        Code  = new StringBuilder();
        Code.append("public class "+CName+" extends Thread {\n");
        Code.append("    @Override public void run() {\n");
        Code.append("        return 5;\n");
        Code.append("    }\n");
        Code.append("}\n");
        JC.addCode(CName + ".java", "", Code.toString());
        
        DiagnosticCollector<JavaFileObject> Diagnostics = new DiagnosticCollector<JavaFileObject>();
        
        ExpectedError = new StringBuilder("Compile failed (See in Diagnosic collection for more info.)");
        if((Error = JC.compile(Diagnostics)) != null) {
            if(!ExpectedError.toString().equals(Error.toString())) {
                System.out.printf("ExpectedError (%d): %s\n", ExpectedError.toString().length(), ExpectedError.toString());
                System.out.printf("Error         (%d): %s\n", Error        .toString().length(), Error        .toString());
                throw new AssertionError("The results are not equals.");
            }
        }
        
        List<Diagnostic<? extends JavaFileObject>> Problems = Diagnostics.getDiagnostics();
        if(Problems.size() != 1) throw new AssertionError("There should be only problem here.");
        Diagnostic<? extends JavaFileObject> Problem = Problems.get(0);
        
        if(!Problem.getCode().equals("compiler.err.cant.ret.val.from.meth.decl.void"))
            throw new AssertionError("The problem's 'code' is not the same.");
        if(Problem.getColumnNumber() != 16)
            throw new AssertionError("The problem's 'column number' is not the same.");
        if(Problem.getEndPosition() != 93)
            throw new AssertionError("The problem's 'end position' is not the same.");
        if(Problem.getKind() != Kind.ERROR)
            throw new AssertionError("The problem's 'kind' is not the same.");
        if(Problem.getLineNumber() != 3)
            throw new AssertionError("The problem's 'line number' is not the same.");
        if(!Problem.getMessage(Locale.getDefault()).equals("TestClass03B.java:3: cannot return a value from method whose result type is void"))
            throw new AssertionError("The problem's 'message' is not the same.");
        if(Problem.getPosition() != 92)
            throw new AssertionError("The problem's 'position' is not the same.");
        if(!Problem.getSource().toString().startsWith("net.nawaman.javacompiler.JavaCodeMemoryFileObject@"))
            throw new AssertionError("The problem's 'source' is not the same.");
        if(Problem.getStartPosition() != 92)
            throw new AssertionError("The problem's 'position' is not the same.");        
        
        // Done ---------------------------------------------------------------------------------------------
        
        System.out.println("DONE!!!");
        
    }
    
}
