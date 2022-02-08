package net.nawaman.javacompiler;

import java.net.*;

import javax.tools.*;

/** FileObject that holds data in the memory */
abstract class MemoryFileObject implements JavaFileObject {
    
    MemoryFileObject(
            final String pName,
            final String pPath) {
        this.name = pName;
        this.path = pPath;
        this.updateLastModified();
    }
    
    final private String name;
    final private String path;
    
    private long lastModified;
    
    /** Update the LastModified value */
    final void updateLastModified() {
        this.lastModified = System.currentTimeMillis();
    }
    
    // FileObject ----------------------------------------------------------------------------------
    
    /** Returns a URI identifying this file object. */
    public URI toUri() {
        try {
            final String aName = this.getName();
            final URI    aURI  = new URI(aName);
            return aURI;
        } catch (Exception E) {
            if (JavaCompiler.DEBUG_MODE) {
                System.out.println(E.toString());
                E.printStackTrace();
            }
            return null;
        }
    }
    
    /** Gets a user-friendly name for this file object. */
    public String getName() {
        final String aName = this.getPath() + this.name;
        return aName;
    }
    /** Gets the path for this file object. */
    public String getPath() {
        if (this.path == null)
             return "";
        else return this.path;
    }
    
    /** Gets the time this file object was last modified. */
    public long getLastModified() {
        return this.lastModified;
    }
    
    /** Deletes this file object. In case of errors, returns false. */
    public boolean delete() {
        return false;
    }
    
}
