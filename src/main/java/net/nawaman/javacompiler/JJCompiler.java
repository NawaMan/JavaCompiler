package net.nawaman.javacompiler;

import java.io.*;
import java.util.*;

import javax.tools.*;
import javax.tools.JavaCompiler.*;

final class JJCompiler {
    
    JJCompiler(final JCompiler pJCompiler) {
        this.jcompiler = pJCompiler;
        this.compiler  = this.getSystemJavaCompiler();
        
        StandardJavaFileManager aSFManager = this.compiler.getStandardFileManager(null, null, null);
        this.memoryFileManager = new MemoryFileManager(aSFManager);
    }
    
    private JCompiler                jcompiler;
    private javax.tools.JavaCompiler compiler;
    private MemoryFileManager        memoryFileManager;
    private boolean                  hasCompiled = false;
    
    final private HashMap<String, String> compiledCodes   = new HashMap<String, String>();
    
    
    MemoryFileManager getMemoryFileManager() {
        return this.memoryFileManager;
    }
    
    /** Checks if JavaCompiler has ever compiled a class. **/
    boolean hasCompiled() {
        return this.hasCompiled;
    }
    
    String getCode(final String pFileName) {
        final String aCode = this.compiledCodes.get(pFileName);
        return aCode;
    }
    
    String compile(
            final String[]                            pJavaCOptions,
            final DiagnosticCollector<JavaFileObject> pDiagnosticListener) {
        
        this.saveCodes_asCompiledCodes();
        
        final String aErrorString = this.doCompile(pJavaCOptions, pDiagnosticListener);
        this.clearCompiledCode();
        
        return aErrorString;
    }
    private void saveCodes_asCompiledCodes() {
        final MemoryFileManager aMFManager = this.memoryFileManager;
        final Set<String>       aCodeNames = aMFManager.getCodeNames();
        
        for (final String CName : aCodeNames) {
            final String aCode = aMFManager.getCode(CName);
            this.compiledCodes.put(CName, aCode);
        }
    }
    private String doCompile(
            final String[]                            pJavaCOptions,
            final DiagnosticCollector<JavaFileObject> pDiagnosticListener) {
        
        final ByteArrayOutputStream aError;
        if(pDiagnosticListener == null)
             aError = new ByteArrayOutputStream();
        else aError = null;
        
        final CompilationTask aCompilationTask = this.prepareCompilationTask(
                pJavaCOptions,
                pDiagnosticListener,
                aError);
        
        aCompilationTask.call();
        this.hasCompiled = true;
        
        final String aErrorString = (aError == null) ? null : aError.toString();
        return aErrorString;
    }
    private void clearCompiledCode() {
        this.memoryFileManager.clearCode();
    }
    
    private CompilationTask prepareCompilationTask(
            final String[]                            pOptions,
            final DiagnosticCollector<JavaFileObject> pDiagnosticListener,
            final ByteArrayOutputStream               pBAOS) {
        
        final Iterable<JavaFileObject> aFiles_tobeCompiled = this.getAllFiles_tobeCompiled();
        final Vector<String>           aCompilationOptions = this.getCompilationOptions(pOptions);
        final OutputStreamWriter       aOSWriter_forErrors;
        
        if (pBAOS != null)
             aOSWriter_forErrors = new OutputStreamWriter(pBAOS);
        else aOSWriter_forErrors = null;
        
        /** Actually compile the code */
        CompilationTask aCompilationTask = this.compiler.getTask(
                /* Output writer */              aOSWriter_forErrors,
                /* FileManager */                this.memoryFileManager,
                /* DiagnosticListener */         pDiagnosticListener,
                /* Option:String[] */            aCompilationOptions,
                /* File names (annotation) */    null,
                /* FileObjects to be compiled */ aFiles_tobeCompiled);
        return aCompilationTask;
    }
    @SuppressWarnings("unchecked")
    private Iterable<JavaFileObject> getAllFiles_tobeCompiled() {
        final MemoryFileManager                  aMFManager = this.memoryFileManager;
        final Set<String>                        aFileNames = aMFManager.getCodeNames();
        final Iterable<? extends JavaFileObject> aFOs       = aMFManager.getJavaFileObjectsFromStrings(aFileNames);
        final Iterable<JavaFileObject>           aFiles     = (Iterable<JavaFileObject>)aFOs;
        return aFiles;
    }
    private Vector<String> getCompilationOptions(final String[] pJavaCOptions) {
        final String         aClasspath = this.getClassPathForCompilation();
        final Vector<String> aOptions   = new Vector<String>();
        
        // Add other options - except for classpath (because we are going to use the one managed by this.clasapaths)
        if (pJavaCOptions != null) {
            final int OptionCount = pJavaCOptions.length;
            for(int i = 0; i < OptionCount; i++) {
                final String aOption = pJavaCOptions[i];
                if(aOption == null)
                    continue;
                
                final String aTrimmedOption = aOption.trim();
                
                // Skip classpath as it should be added with JCompiler classpath
                if(aTrimmedOption.equals("-cp") || aTrimmedOption.equals("-classpath")) {
                    i++;
                    continue;
                }
                
                aOptions.add(aTrimmedOption);
            }
        }
        
        // Add class path 
        aOptions.add("-cp");
        aOptions.add(aClasspath);
        
        // Add unchecked as warning will complicated things
        aOptions.add("-Xlint:unchecked");
        return aOptions;
    }
    private String getClassPathForCompilation() { 
        String aClasspath = this.jcompiler.getClasspaths();
        
        final ClassLoader aParentClassLoader = this.jcompiler.getParent();
        if (aParentClassLoader instanceof JCompiler) {
            try (final JCompiler pParent = (JCompiler)aParentClassLoader) {
                aClasspath += ":" + pParent.getClasspaths();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return aClasspath;
    }
    
    private javax.tools.JavaCompiler getSystemJavaCompiler() {
        final javax.tools.JavaCompiler aCompiler = ToolProvider.getSystemJavaCompiler();
        
        if (aCompiler == null) {
            // Unable to initialize the java compiler
            throw new RuntimeException(
                "Unable to initialize the java compiler. Ensure that the class 'com.sun.tools.javac.api.JavacTool' " +
                "(found in <jdk-dir>/lib/tools.jar) is in the class path.");
        }
        
        return aCompiler;
    }
    
}
