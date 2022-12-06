package net.nawaman.javacompiler;

import org.junit.jupiter.api.Test;

import net.nawaman.javacompiler.helpers.TestCompiler;

class ClasspathTest {
    
    private static final String className           = "TestClass";
    private static final String dependencyJarFile   = "mysql-connector-java.jar";
    private static final String dependencyClassName = "com.mysql.cj.jdbc.Driver";
    
    private JavaCompiler javaCompiler = new JavaCompiler();
    private TestCompiler testCompiler = new TestCompiler(javaCompiler);
    
    @Test
    public void ensureClassNotAccessibleStatically() {
        try {
            Class.forName(dependencyClassName);
            throw new AssertionError("We can access the class, so the test is not valid");
        }
        catch(ClassNotFoundException CNF) {}
        catch(ClassCastException CCE)     {}
    }
    
    @Test
    public void ensureClassNotAccessibleDynamically() throws Exception {
        var message   = "Confirm that the compiled code cannot access to the class dynamically.";
        var classCode = new StringBuilder();
        classCode
        .append("public class " + className + " extends Thread {\n")
        .append("    @Override public void run() {\n")
        .append("        try {\n")
        .append("            Class.forName(\"" + dependencyClassName + "\");\n")
        .append("            throw new AssertionError(\"We can access ther class, so the test is not valid\");\n")
        .append("        } catch (Exception exception) {\n")
        .append("           System.out.println(\"" + message + "\");\n")
        .append("        }\n")
        .append("    }\n")
        .append("}\n");
        
        testCompiler
            .compileCode(className, classCode)
            .validateOut(message);
    }
    
    @Test
    public void ensureClassAccessibleDynamicallyWithClassPath() throws Exception {
        var message   = "Confirm that with the dependency, the compiled code can now access to the class dynamically.";
        var classCode = new StringBuilder();
        classCode
        .append("public class " + className + " extends Thread {\n")
        .append("    @Override public void run() {\n")
        .append("        try {\n")
        .append("            Class.forName(\"" + dependencyClassName + "\");\n")
        .append("            System.out.println(\"" + message + "\");\n")
        .append("            return;\n")
        .append("        } catch(Exception exception) {\n")
        .append("           throw new AssertionError(\"Unable to acccess the class so the classpath is not added.\");\n")
        .append("        }\n")
        .append("    }\n")
        .append("}\n");
        
        testCompiler
            .addDependency(dependencyJarFile)
            .compileCode  (className, classCode)
            .validateOut  (message);
    }
    
    @Test
    public void ensureClassAccessibleStaticallyWithClassPath() throws Exception {
        var message   = "Confirm that with the dependency, the compiled code can now access to the class statically: ";
        var classCode = new StringBuilder();
        classCode
        .append("public class " + className + " extends Thread {\n")
        .append("    @Override public void run() {\n")
        .append("        System.out.println(\"" + message + "\" + " + dependencyClassName + ".class.getCanonicalName());\n")
        .append("    }\n")
        .append("}\n");
        
        testCompiler
            .addDependency(dependencyJarFile)
            .compileCode  (className, classCode)
            .validateOut  (message + dependencyClassName);
    }
    
}
