package net.nawaman.javacompiler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

class SimpleCompileTest {
    
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
    
    static final PrintStream  PrintSOrg = System.out;
    static final JavaCompiler JC        = JavaCompiler.Instance;
    
    @Test
    void compileClassThatExtendsAClass() {
        String Error = null;
        
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
        try { T2 = ThreadClass.getConstructor().newInstance(); }
        catch (IllegalAccessException IAE)  {}
        catch (InstantiationException IE)   {}
        catch (IllegalArgumentException e)  {}
        catch (InvocationTargetException e) {}
        catch (NoSuchMethodException e)     {}
        catch (SecurityException e)         {}
        
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
    }
    
    @Test
    void compileClassThatImplementAnInterface() {
        String Error = null;
        
        // Test 2: Compile a class that implements an interface ------------------------------------
        
        var CName = "TestClass01B";
        var Code  = new StringBuilder();
        Code.append("public class "+CName+" implements Runnable {\n");
        Code.append("    @Override public void run() {\n");
        Code.append("        for(int Counter = 0; Counter < 10; Counter++)\n");
        Code.append("            System.out.println(\"Counter: \" + Counter);\n");
        Code.append("    }\n");
        Code.append("}\n");
        JC.addCode(CName + ".java", "", Code.toString());
        if((Error = JC.compile()) != null)
            throw new AssertionError("Problem compiling the test class '"+CName+"': \n" + Error);
        
        var Output1 = new ByteArrayOutputStream();
        var Output2 = new ByteArrayOutputStream();
        var PrintS1   = new PrintStream(Output1);
        var PrintS2   = new PrintStream(Output2);
        
        // Statically compiled Java class ---------------------------------------------------------
        System.setOut(PrintS1);
        var T1 = new Thread(new TestClass02());
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
        
        Thread T2 = null;
        try { T2 = new Thread(RunnableClass.getConstructor().newInstance()); }
        catch (IllegalAccessException IAE)  {}
        catch (InstantiationException IE)   {}
        catch (IllegalArgumentException e)  {}
        catch (SecurityException e)         {}
        catch (InvocationTargetException e) {}
        catch (NoSuchMethodException e) {}
        
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
