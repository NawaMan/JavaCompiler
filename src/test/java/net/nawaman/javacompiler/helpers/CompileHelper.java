package net.nawaman.javacompiler.helpers;

import net.nawaman.javacompiler.JavaCompiler;

public class CompileHelper {
    
    public static <CLASS> Class<? extends CLASS> compile(JavaCompiler compiler, String className, CharSequence classCode, Class<CLASS> resultClass) {
        return compile(compiler, className, "", classCode, resultClass);
    }
    
    public static <CLASS> Class<? extends CLASS> compile(JavaCompiler compiler, String className, String codeDirectory, CharSequence classCode, Class<CLASS> resultClass) {
        var codeContent = classCode.toString();
        compiler.addCode(className + ".java", codeDirectory, codeContent);
        
        var compilationError = compiler.compile();
        if(compilationError != null) {
            throw new AssertionError("Problem compiling the test class '" + className + "': \n" + compilationError);
        }
        
        try {
            return compiler.forName(className).asSubclass(resultClass);
            
        } catch(ClassNotFoundException exception) {
            throw new AssertionError("Compilation have failed because the result class is not found.", exception);
        } catch(ClassCastException exception) {
            throw new AssertionError("Compilation have failed because the result class is not a Thread class.", exception);
        }
    }
    
}
