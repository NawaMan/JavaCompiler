package net.nawaman.javacompiler.helpers;

import java.io.File;

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
        return compileCode(className, "", classCode);
    }
    
    public TestThread compileCode(String className, String codeDirectory, CharSequence classCode) {
        var codeContent = classCode.toString();
        compiler.addCode(className + ".java", codeDirectory, codeContent);
        
        var compilationError = compiler.compile();
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
