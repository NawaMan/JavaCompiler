package net.nawaman.javacompiler;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

/**
 * Manage classpaths for the Compiler.
 * 
 * NOTE: This class needs to be refactored as the code is quite a mess.
 **/
final class ClassPaths {
    
    static private final String CLASSPATH_NOTFOUND_OR_INVALID =
                "The give class path URL is not found or mal-formed: %s.";
    
    static private final String CLASSPATH_JAR_FILE_NOTFOUND = "The give jar file is not found.";
    
    ClassPaths(final JCompiler pJCompiler) {
        this.jcompiler  = pJCompiler;
        this.classPaths = System.getProperty("java.class.path") + File.pathSeparator + ".";
    }
    
    private JCompiler jcompiler;
    private String    classPaths;
    
    public String getClasspaths() {
        return this.classPaths;
    }
    
    /** Add a jar file as class path into the ClassLoader */
    JCompiler addClasspathURL(final String pUrlPath) {
        return this.changeClasspathURL(pUrlPath, false);
    }
    /** Add a jar file as class path into the ClassLoader */
    JCompiler removeClasspathURL(final String pUrlPath) {
        return this.changeClasspathURL(pUrlPath, true);
    }
    /** Add a jar file as class path into the ClassLoader */
    JCompiler changeClasspathURL(
            final String  pUrlPath,
            final boolean pIsRemove) {
        try {        
            String aUrlPath = pUrlPath.trim();
            
            if(aUrlPath.startsWith("file:/") && !aUrlPath.startsWith("file://"))
                aUrlPath = "file:///" + aUrlPath.substring("file:/".length());
            
            if(!aUrlPath.contains("://"))
                aUrlPath = "file://" + (new File(aUrlPath)).getAbsolutePath().toString();
            
            if(aUrlPath.contains("~") || aUrlPath.contains("..")) {
                aUrlPath = RemovePrefix("jar:",    aUrlPath);
                aUrlPath = RemovePrefix("file://", aUrlPath);
                
                aUrlPath = (new File(aUrlPath)).getAbsolutePath();
                String[] Paths = aUrlPath.split(((File.separatorChar == '\\') ? "\\\\" : File.separator));
                if((Paths != null) && (Paths.length != 0)) {
                    Vector<String> VPaths = new Vector<String>();
                    
                    for(int i = 0; i < Paths.length; i++) {
                        final String aPath = Paths[i];
                        if (aPath.length() == 1) {
                            final char C = aPath.charAt(0);
                            if (C == '.')
                                continue;
                            if (C == '~') {
                                VPaths.clear();
                                VPaths.add(System.getProperty("user.home"));
                                continue;
                            }
                        }
                        if (aPath.equals("..")) {
                            if(VPaths.size() > 0)
                                VPaths.remove(VPaths.size() - 1);
                            continue;
                        }
                        VPaths.add(aPath);
                    }
                    StringBuffer SB = new StringBuffer();
                    for(int i = 0; i < VPaths.size(); i++) {
                        SB.append(VPaths.get(i));
                        SB.append(File.separator);
                    }
                    aUrlPath = SB.toString();
                    while(aUrlPath.startsWith(File.separator + File.separator)) aUrlPath = aUrlPath.substring(1);
                    if(!aUrlPath.startsWith(File.separator)) aUrlPath = File.separator + aUrlPath; 
                    aUrlPath = "file://" + aUrlPath; 
                }
            }
            
            if(aUrlPath.endsWith(".jar"))  aUrlPath += "!/";
            aUrlPath = EnsureSuffix("/", aUrlPath);
            if(aUrlPath.endsWith(".jar!/") && !aUrlPath.startsWith("jar:")) {
                if(!aUrlPath.contains("://")) aUrlPath = "file://" + (new File(aUrlPath)).getAbsolutePath().toString();
                aUrlPath = "jar:" + aUrlPath;
            }
            
            final URL url = new URL(aUrlPath);
            
            JCompiler TheJC     = this.jcompiler;
            boolean   IsRemoved = false;
            // Create a new one if needed (if the compiler has been used to compile something,
            //   the new should be used instead)
            if(this.jcompiler.hasCompiled() || pIsRemove) {    // Create a new one if needed
                TheJC = AccessController.doPrivileged(new PrivilegedAction<JCompiler>() {
                    public JCompiler run() {
                        return new JCompiler(jcompiler);
                    }
                });
                // Copy
                final URL[] Us = this.jcompiler.getURLs();
                if (Us != null) {
                    for (int i = 0; i < Us.length; i++) {
                        URL aURL = Us[i];
                        if (aURL == null)
                            continue;
                        
                        if (this.compareURLs(aURL, url)) {
                            if(pIsRemove) {
                                IsRemoved = true;
                                // Just skip this URL
                                continue;
                            } else {
                             // Already added so, no need to do anything
                                return this.jcompiler;
                            }
                        }
                        this.addClasspathURL(Us[i].toString());
                    }
                }
            }
            if(pIsRemove) return IsRemoved ? TheJC : this.jcompiler;
            TheJC.addURL(url);
            
            String Path = url.getPath();
            if(Path.endsWith(  ".jar!/"))
                Path = Path.substring(0, Path.length() - "!/".length());
            
            Path = RemovePrefix("jar:",  Path);
            Path = RemovePrefix("file:", Path);
            
            this.classPaths += java.io.File.pathSeparator;
            this.classPaths += Path;
            
            return TheJC;
        } catch(MalformedURLException MUE) {
            throw new RuntimeException(
                    new FileNotFoundException(
                            String.format(CLASSPATH_NOTFOUND_OR_INVALID, pUrlPath)));
        }
    }
    private boolean compareURLs(
            final URL aURL1,
            final URL aURL2) {
        if (aURL1 == aURL2)
            return true;
        
        if ((aURL1 == null) || (aURL2 == null))
            return false;
        
        if (!aURL1.getProtocol().equals(aURL2.getProtocol())) return false;
        if (!aURL1.getHost()    .equals(aURL2.getHost()    )) return false;
        if (!aURL1.getFile()    .equals(aURL2.getFile()    )) return false;
        if ( aURL1.getPort()    !=      aURL2.getPort()     ) return false;
        
        return true;
    }
    
    /** Add a jar file as class path into the ClassLoader */
    public JCompiler addJarFile(final String pPath) {
        return this.changeJarFile(pPath, false);
    }
    /** Add a jar file as class path into the ClassLoader */
    public JCompiler removeJarFile(final String pPath) {
        return this.changeJarFile(pPath, true);
    }
    
    /** Add a jar file as class path into the ClassLoader */
    JCompiler changeJarFile(
            final String  pPath,
            final boolean pIsRemove) {
        try {
            String aPath = pPath;
            
            aPath = EnsurePrefix("file:", aPath, "file://");
            aPath = EnsurePrefix("jar:" , aPath);
            aPath = EnsureSuffix("!/"   , aPath);
            
            final URL aURLPath  = new URL(aPath);
            JCompiler TheJC     = this.jcompiler;
            boolean   IsRemoved = false;
            // Create a new one if needed (it the compiler has been used to compile something,
            //   the one should be used instead)
            if(this.jcompiler.hasCompiled()) {
                TheJC = AccessController.doPrivileged(new PrivilegedAction<JCompiler>() {
                    public JCompiler run() {
                        final JCompiler aJCompiler = new JCompiler(jcompiler);
                        return aJCompiler;
                    }
                });
                
                final URL[] Us = this.jcompiler.getURLs();
                if (Us != null) {
                    for (int i = 0; i < Us.length; i++) {
                        final URL aURL = Us[i];
                        if (aURL == null)
                            continue;
                        
                        final boolean IsURLEquals = this.compareURLs(aURL, aURLPath);
                        if (IsURLEquals) {
                            if (pIsRemove) {
                                IsRemoved = true;
                                // Just skip this URL
                                continue;
                            } else {
                                // Already added so, no need to do anything
                                return this.jcompiler;
                            }
                        }
                        final String aURLString = aURL.toString();
                        this.addClasspathURL(aURLString);
                    }
                }
                
                if(pIsRemove) {
                    return IsRemoved
                            ? TheJC
                            : this.jcompiler;
                }
            }
            
            TheJC.addURL(aURLPath);
            
            String Path = aURLPath.getPath();
            if(Path.endsWith(".jar!/")) Path = Path.substring(0, Path.length() - "!/".length());
            this.classPaths += java.io.File.pathSeparator;
            this.classPaths += Path;
            
            return TheJC;
            
        } catch(MalformedURLException MUE) {
            throw new RuntimeException(new FileNotFoundException(CLASSPATH_JAR_FILE_NOTFOUND));
        }
    }
    
    // Private -------------------------------------------------------------------------------------
    
    static private String EnsurePrefix(
            final String pPrefix,
            final String pString) {
        return EnsurePrefix(pPrefix, pString, null);
    }
    static private String EnsurePrefix(
            final String pPrefix,
            final String pString,
            final String pReplacePrefix) {
        final String aReplacement = (pReplacePrefix == null)
                    ? pPrefix
                    : pReplacePrefix;
        if(!pString.startsWith(pPrefix))
             return aReplacement + pString;
        else return                pString;
    }
    static private String EnsureSuffix(
            final String pSuffix,
            final String pString) {
        if(!pString.endsWith(pSuffix))
            return pString + pSuffix;
       else return pString;
    }
    
    static private String RemovePrefix(
            final String pPrefix,
            final String pString) {
        if(pString.startsWith(pPrefix))
             return pString.substring(pPrefix.length());
        else return pString;
    }
}
