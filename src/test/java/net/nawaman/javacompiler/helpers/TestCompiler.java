package net.nawaman.javacompiler.helpers;

import java.io.File;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import net.nawaman.javacompiler.JavaCompiler;

public class TestCompiler {
    
    private final JavaCompiler compiler;
    
    public TestCompiler(JavaCompiler compiler) {
        this.compiler = compiler;
    }
    
    public TestCompiler addDependency(String dependencyJarFile) {
        var jarPath = ((new File(".")).getAbsolutePath() + "/" + dependencyJarFile).replace("/./", "/");
        compiler.addJarFile(jarPath);
        return this;
    }
    
    public TestThread compileCode(String className, CharSequence classCode) {
        return compileCode(className, "", classCode, null);
    }
    
    public TestThread compileCode(String className, String codeDirectory, CharSequence classCode) {
        return compileCode(className, "", classCode, null);
    }
    
    public TestThread compileCode(String className, CharSequence classCode, DiagnosticCollector<JavaFileObject> diagnostics) {
        return compileCode(className, "", classCode, diagnostics);
    }
    
    public TestThread compileCode(String className, String codeDirectory, CharSequence classCode, DiagnosticCollector<JavaFileObject> diagnostics) {
        var codeContent = classCode.toString();
        compiler.addCode(className + ".java", codeDirectory, codeContent);
        
        var compilationError = (diagnostics == null)
                             ? compiler.compile()
                             : compiler.compile(diagnostics);
        if(compilationError != null) {
            throw new AssertionError("Problem compiling the test class '" + className + "': \n" + compilationError);
        }
        
        try {
            @SuppressWarnings("unchecked")
            var threadClass = (Class<Thread>)compiler.forName(className).asSubclass(Thread.class);
            return new TestThread(threadClass);
            
        } catch(ClassNotFoundException exception) {
            throw new AssertionError("Compilation have failed because the result class is not found.", exception);
        } catch(ClassCastException exception) {
            throw new AssertionError("Compilation have failed because the result class is not a Thread class.", exception);
        }
    }
}
