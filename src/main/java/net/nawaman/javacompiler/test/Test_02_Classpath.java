package net.nawaman.javacompiler.test;

import java.io.*;

import net.nawaman.javacompiler.*;

public class Test_02_Classpath {
    
    public static void main(String[] args) {
        
        AllTests.PrintTestTitle();
        
        final PrintStream  PrintSOrg = System.out;
        final JavaCompiler JC        = JavaCompiler.Instance;
        final String       ClassName = "com.mysql.jdbc.Driver";
        
        final ByteArrayOutputStream Output0 = new ByteArrayOutputStream();
        final ByteArrayOutputStream Output1 = new ByteArrayOutputStream();
        final ByteArrayOutputStream Output2 = new ByteArrayOutputStream();
        final ByteArrayOutputStream Output3 = new ByteArrayOutputStream();
        final PrintStream           PrintS0   = new PrintStream(Output0);
        final PrintStream           PrintS1   = new PrintStream(Output1);
        final PrintStream           PrintS2   = new PrintStream(Output2);
        final PrintStream           PrintS3   = new PrintStream(Output3);
        
        String Error = null;
        
        // Test 1: The class should not be accessible statically
        
        try {
            Class.forName(ClassName);
            throw new AssertionError("We can access the class, so the test is not valid");
        }
        catch(ClassNotFoundException CNF) {}
        catch(ClassCastException CCE)     {}
        
        PrintS0.println(ClassName);
        
        // Test 2: The class should not be accessible dynamically when the classpath is not added
        
        String        CName = "TestClass02A";
        StringBuilder Code  = new StringBuilder();
        Code.append("public class "+CName+" extends Thread {\n");
        Code.append("    @Override public void run() {\n");
        Code.append("        try {\n");
        Code.append("            Class.forName(\""+ClassName+"\");\n");
        Code.append("            throw new AssertionError(\"We can access ther class, so the test is not valid\");\n");
        Code.append("        }\n");
        Code.append("        catch(ClassNotFoundException CNF) {}\n");
        Code.append("        catch(ClassCastException CCE)     {}\n");
        Code.append("        \n");
        Code.append("        System.out.println(\""+ClassName+"\");\n");
        Code.append("    }\n");
        Code.append("}\n");
        JC.addCode(CName + ".java", "", Code.toString());
        if((Error = JC.compile()) != null)
            throw new AssertionError("Problem compiling the test class '"+CName+"': \n" + Error);
        
        System.setOut(PrintS1);
        Class<? extends Thread> ThreadClass = null;
        try {
            ThreadClass = JC.forName(CName).asSubclass(Thread.class); 
        } catch(ClassNotFoundException CNFE) {
            throw new AssertionError(
                    "Compilation have failed because the result class is not found.");
        } catch(ClassCastException CCE) {
            throw new AssertionError(
                    "Compilation have failed because the result class is not a Thread class.");
        }
        
        Thread T1 = null;
        try { T1 = ThreadClass.newInstance(); }
        catch(IllegalAccessException IAE) {}
        catch(InstantiationException IE)  {}
        if(T1 == null)
            throw new AssertionError(
                    "Compilation have failed because the result class has no default constructor.");
        
        T1.start();
        while(T1.isAlive());
        
        System.setOut(PrintSOrg);
        if(!Output0.toString().equals(Output1.toString())) {
            System.out.printf("Output0 (%d): %s\n", Output0.toString().length(), Output0.toString());
            System.out.printf("Output1 (%d): %s\n", Output1.toString().length(), Output1.toString());
            throw new AssertionError("The results are not equals.");
        }
        
        // Test 3: The class should be accessible dynamically when the classpath is added
        
        CName = "TestClass02B";
        Code  = new StringBuilder();
        Code.append("public class "+CName+" extends Thread {\n");
        Code.append("    @Override public void run() {\n");
        Code.append("        try {\n");
        Code.append("            Class C = this.getClass().forName(\""+ClassName+"\");\n");
        Code.append("            System.out.println(C.getCanonicalName());\n");
        Code.append("            return;\n");
        Code.append("        }\n");
        Code.append("        catch(ClassNotFoundException CNF) {}\n");
        Code.append("        catch(ClassCastException CCE)     {}\n");
        Code.append("        throw new AssertionError(\"Unable to acccess the class so the classpath is not added.\");\n");
        Code.append("    }\n");
        Code.append("}\n");
        String Path = (new File(".")).getAbsolutePath()
                + "/source/net/nawaman/javacompiler/test/mysql-connector-java.jar";
        
        Path = Path.replace("/./", "/");
        JC.addJarFile(Path);
        JC.addCode(CName + ".java", "", Code.toString());
        if((Error = JC.compile()) != null)
            throw new AssertionError("Problem compiling the test class '"+CName+"': \n" + Error);
        
        System.setOut(PrintS2);
        ThreadClass = null;
        try {
            ThreadClass = JC.forName(CName).asSubclass(Thread.class); 
        } catch(ClassNotFoundException CNFE) {
            throw new AssertionError(
                    "Compilation have failed because the result class is not found.");
        } catch(ClassCastException CCE) {
            throw new AssertionError(
                    "Compilation have failed because the result class is not a Thread class.");
        }
        
        T1 = null;
        try { T1 = ThreadClass.newInstance(); }
        catch(IllegalAccessException IAE) {}
        catch(InstantiationException IE)  {}
        if(T1 == null)
            throw new AssertionError(
                    "Compilation have failed because the result class has no default constructor.");
        
        T1.start();
        while(T1.isAlive());
        
        System.setOut(PrintSOrg);
        if(!Output0.toString().equals(Output2.toString())) {
            System.out.printf("Output0 (%d): %s\n", Output0.toString().length(), Output0.toString());
            System.out.printf("Output2 (%d): %s\n", Output2.toString().length(), Output2.toString());
            throw new AssertionError("The results are not equals.");
        }
        
        // Test 4: The class should be accessible statically when the classpath is added
        
        CName = "TestClass03B";
        Code  = new StringBuilder();
        Code.append("import "+ClassName+";\n");
        Code.append("\n");
        Code.append("public class "+CName+" extends Thread {\n");
        Code.append("    @Override public void run() {\n");
        Code.append("        System.out.println("+ClassName+".class.getCanonicalName());\n");
        Code.append("    }\n");
        Code.append("}\n");
        JC.addCode(CName + ".java", "", Code.toString());
        if((Error = JC.compile()) != null)
            throw new AssertionError("Problem compiling the test class '"+CName+"': \n" + Error);
        
        System.setOut(PrintS3);
        ThreadClass = null;
        try {
            ThreadClass = JC.forName(CName).asSubclass(Thread.class); 
        } catch(ClassNotFoundException CNFE) {
            throw new AssertionError(
                    "Compilation have failed because the result class is not found.");
        } catch(ClassCastException CCE) {
            throw new AssertionError(
                    "Compilation have failed because the result class is not a Thread class.");
        }
        
        T1 = null;
        try { T1 = ThreadClass.newInstance(); }
        catch(IllegalAccessException IAE) {}
        catch(InstantiationException IE)  {}
        if(T1 == null)
            throw new AssertionError(
                    "Compilation have failed because the result class has no default constructor.");
        
        T1.start();
        while(T1.isAlive());
        
        System.setOut(PrintSOrg);
        if(!Output0.toString().equals(Output3.toString())) {
            System.out.printf("Output0 (%d): %s\n", Output0.toString().length(), Output0.toString());
            System.out.printf("Output3 (%d): %s\n", Output3.toString().length(), Output3.toString());
            throw new AssertionError("The results are not equals.");
        }
        
        // Done ------------------------------------------------------------------------------------
        
        System.out.println("DONE!!!");
    }
    
}
