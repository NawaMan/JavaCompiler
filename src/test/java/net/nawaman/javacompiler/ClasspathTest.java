package net.nawaman.javacompiler;

import static net.nawaman.javacompiler.helpers.CompileHelper.compile;
import static net.nawaman.javacompiler.helpers.TestHelper.assertAsString;
import static net.nawaman.javacompiler.helpers.TestHelper.captureOut;

import java.io.File;

import org.junit.jupiter.api.Test;

class ClasspathTest {
    
    private static final String className           = "TestClass";
    private static final String dependencyJarFile   = "mysql-connector-java.jar";
    private static final String dependencyClassName = "com.mysql.cj.jdbc.Driver";
    
    @Test
    public void ensureClassAccessibleStaticly() {
        try {
            Class.forName(dependencyClassName);
            throw new AssertionError("We can access the class, so the test is not valid");
        }
        catch(ClassNotFoundException CNF) {}
        catch(ClassCastException CCE)     {}
    }
    
    @Test
    public void ensureClassNotAccessibleDynamically() throws Exception {
        // The class should not be accessible dynamically when the classpath is not added
        // This test is a control to ensure that without doing anything the feature was not already there.
        var classCode = new StringBuilder();
        classCode
        .append("public class " + className + " extends Thread {\n")
        .append("    @Override public void run() {\n")
        .append("        try {\n")
        .append("            Class.forName(\"" + dependencyClassName + "\");\n")
        .append("            throw new AssertionError(\"We can access ther class, so the test is not valid\");\n")
        .append("        } catch (Exception exception) {\n")
        .append("           System.out.println(\"" + dependencyClassName + "\");\n")
        .append("        }\n")
        .append("    }\n")
        .append("}\n");
        
        var compiler = new JavaCompiler();
        ensureCompileAndRun(compiler, classCode);
    }
    
    @Test
    public void ensureClassAccessibleDynamicallyWithClassPath() throws Exception {
        // The class should be accessible dynamically by the class being compiled when the classpath is added
        var classCode = new StringBuilder();
        classCode
        .append("public class " + className + " extends Thread {\n")
        .append("    @Override public void run() {\n")
        .append("        try {\n")
        .append("            Class.forName(\"" + dependencyClassName + "\");\n")
        .append("            System.out.println(\"" + dependencyClassName + "\");\n")
        .append("            return;\n")
        .append("        } catch(Exception exception) {\n")
        .append("           throw new AssertionError(\"Unable to acccess the class so the classpath is not added.\");\n")
        .append("        }\n")
        .append("    }\n")
        .append("}\n");
        
        var compiler = new JavaCompiler();
        addDependencyJar(compiler);
        ensureCompileAndRun(compiler, classCode);
    }
    
    @Test
    public void ensureClassAccessibleStaticallyWithClassPath() throws Exception {
        var classCode = new StringBuilder();
        classCode
        .append("public class " + className + " extends Thread {\n")
        .append("    @Override public void run() {\n")
        .append("        System.out.println(" + dependencyClassName + ".class.getCanonicalName());\n")
        .append("    }\n")
        .append("}\n");
        
        var compiler = new JavaCompiler();
        addDependencyJar(compiler);
        ensureCompileAndRun(compiler, classCode);
    }
    
    private void addDependencyJar(JavaCompiler compiler) {
        var jarPath = ((new File(".")).getAbsolutePath() + "/" + dependencyJarFile).replace("/./", "/");
        compiler.addJarFile(jarPath);
    }
    
    private void ensureCompileAndRun(JavaCompiler compiler, CharSequence classCode) throws Exception {
        var thread
                = compile(compiler, className, classCode, Thread.class)
                .getConstructor()
                .newInstance();
        
        var capturedOut = captureOut(() -> {
            thread.start();
            while(thread.isAlive());
        });
        
        assertAsString(dependencyClassName, capturedOut.trim());
    }
    
}
