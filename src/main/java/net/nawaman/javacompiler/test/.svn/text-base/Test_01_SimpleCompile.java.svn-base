package net.nawaman.javacompiler.test;

import java.io.*;

import net.nawaman.javacompiler.JavaCompiler;

/**
 * This simple compilations
 * 1. Test compiling a class that extends a class
 * 2. Test compiling a class that implements an interface
 */
public class Test_01_SimpleCompile {
    
    static public class TestClass01 extends Thread {
        @Override public void run() {
            for(int Counter = 0; Counter < 10; Counter++)
                System.out.println("Counter: " + Counter);
        }
    }
    
    static public class TestClass02 implements Runnable {
        @Override public void run() {
            for(int Counter = 0; Counter < 10; Counter++)
                System.out.println("Counter: " + Counter);
        }
    }
    
    public static void main(String[] args) {
        
        AllTests.PrintTestTitle();
        
        final PrintStream  PrintSOrg = System.out;
        final JavaCompiler JC        = JavaCompiler.Instance;
        
        String Error = null;
        
        
        // Test 1: Compile a class that extends a class --------------------------------------------
        
        String        CName = "TestClass01A";
        StringBuilder Code  = new StringBuilder();
        Code.append("public class "+CName+" extends Thread {\n");
        Code.append("    @Override public void run() {\n");
        Code.append("        for(int Counter = 0; Counter < 10; Counter++)\n");
        Code.append("            System.out.println(\"Counter: \" + Counter);\n");
        Code.append("    }\n");
        Code.append("}\n");
        JC.addCode(CName + ".java", "", Code.toString());
        if((Error = JC.compile()) != null)
            throw new AssertionError("Problem compiling the test class '"+CName+"': \n" + Error);
        
        ByteArrayOutputStream Output1 = new ByteArrayOutputStream();
        ByteArrayOutputStream Output2 = new ByteArrayOutputStream();
        PrintStream           PrintS1   = new PrintStream(Output1);
        PrintStream           PrintS2   = new PrintStream(Output2);
        
        // Statically compiled Java class ---------------------------------------------------------
        System.setOut(PrintS1);
        Thread T1 = new TestClass01();
        T1.start();
        while(T1.isAlive());
        
        // Dynamically compiled Java class --------------------------------------------------------
        System.setOut(PrintS2);
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
        
        Thread T2 = null;
        try { T2 = ThreadClass.newInstance(); }
        catch(IllegalAccessException IAE) {}
        catch(InstantiationException IE)  {}
        if(T2 == null)
            throw new AssertionError(
                    "Compilation have failed because the result class has no default constructor.");
        
        T2.start();
        while(T2.isAlive());
        
        System.setOut(PrintSOrg);
        if(!Output1.toString().equals(Output2.toString())) {
            System.out.printf("Output1 (%d): %s\n", Output1.toString().length(), Output1.toString());
            System.out.printf("Output2 (%d): %s\n", Output2.toString().length(), Output2.toString());
            throw new AssertionError("The results are not equals.");
        }
        
        // Test 2: Compile a class that implements an interface ------------------------------------
        
        CName = "TestClass01B";
        Code  = new StringBuilder();
        Code.append("public class "+CName+" implements Runnable {\n");
        Code.append("    @Override public void run() {\n");
        Code.append("        for(int Counter = 0; Counter < 10; Counter++)\n");
        Code.append("            System.out.println(\"Counter: \" + Counter);\n");
        Code.append("    }\n");
        Code.append("}\n");
        JC.addCode(CName + ".java", "", Code.toString());
        if((Error = JC.compile()) != null)
            throw new AssertionError("Problem compiling the test class '"+CName+"': \n" + Error);
        
        Output1 = new ByteArrayOutputStream();
        Output2 = new ByteArrayOutputStream();
        PrintS1   = new PrintStream(Output1);
        PrintS2   = new PrintStream(Output2);
        
        // Statically compiled Java class ---------------------------------------------------------
        System.setOut(PrintS1);
        T1 = new Thread(new TestClass02());
        T1.start();
        while(T1.isAlive());
        
        // Dynamically compiled Java class --------------------------------------------------------
        System.setOut(PrintS2);
        Class<? extends Runnable> RunnableClass = null;
        try {
            RunnableClass = JC.forName(CName).asSubclass(Runnable.class); 
        } catch(ClassNotFoundException CNFE) {
            throw new AssertionError(
                    "Compilation have failed because the result class is not found.");
        } catch(ClassCastException CCE) {
            throw new AssertionError(
                    "Compilation have failed because the result class is not a Runnable class.");
        }
        
        T2 = null;
        try { T2 = new Thread(RunnableClass.newInstance()); }
        catch(IllegalAccessException IAE) {}
        catch(InstantiationException IE)  {}
        if(T2 == null)
            throw new AssertionError(
                    "Compilation have failed because the result class has no default constructor.");
        
        T2.start();
        while(T2.isAlive());
        
        System.setOut(PrintSOrg);
        if(!Output1.toString().equals(Output2.toString())) {
            System.out.printf("Output1 (%d): %s\n", Output1.toString().length(), Output1.toString());
            System.out.printf("Output2 (%d): %s\n", Output2.toString().length(), Output2.toString());
            throw new AssertionError("The results are not equals.");
        }
        
        // Done ------------------------------------------------------------------------------------
        
        System.out.println("DONE!!!");
    }
    
}
