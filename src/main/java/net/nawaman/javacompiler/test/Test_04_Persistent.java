package net.nawaman.javacompiler.test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import net.nawaman.javacompiler.*;


public class Test_04_Persistent {
    
    public static void main(String[] args) {
        
        AllTests.PrintTestTitle();
        
        PrintStream PrintSOrg = System.out;
        String      Error     = null;
        ClassData   CData1    = null;
        ClassData   CData2    = null;
        
        JavaCompiler JC = JavaCompiler.Instance;
        
        ByteArrayOutputStream Output0 = new ByteArrayOutputStream();
        ByteArrayOutputStream Output1 = new ByteArrayOutputStream();
        PrintStream           PrintS0   = new PrintStream(Output0);
        PrintStream           PrintS1   = new PrintStream(Output1);
                
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
        
        System.setOut(PrintS0);
        Class<? extends Thread> ThreadClass = null;
        try {
            ThreadClass = JC.forName(CName).asSubclass(Thread.class); 
        } catch(ClassNotFoundException CNFE) {
            throw new AssertionError("Compilation have failed because the result class is not found.");
        } catch(ClassCastException CCE) {
            throw new AssertionError("Compilation have failed because the result class is not a Thread class.");
        }
        
        Thread T1 = null;
        try { T1 = ThreadClass.getConstructor().newInstance(); }
        catch (IllegalAccessException IAE)  {}
        catch (InstantiationException IE)   {}
        catch (IllegalArgumentException e)  {}
        catch (InvocationTargetException e) {}
        catch (NoSuchMethodException e)     {}
        catch (SecurityException e)         {}
        if(T1 == null) throw new AssertionError("Compilation have failed because the result class has no default constructor.");
        
        T1.start();
        while(T1.isAlive());
        
        // Make sure that the saved code is the same and the non-saved ClassData is null --------------------
        
        CData1 = JC.getCompiledClassData(CName, true);
        CData2 = JC.getCompiledClassData(CName, false);
        
        String CData1_Code = CData1.getCode();
        if(!CData1_Code.equals(Code.toString()))
            throw new AssertionError("The saved code is not the same.");
        
        if(CData2.getCode() != null)
            throw new AssertionError("The ClassData with no code should have no code.");
        
        JC = new JavaCompiler(null);
        try {
            JC.forName(CName);
            throw new AssertionError("Something is wrong the class should not be found.");
        }
        catch(ClassNotFoundException CNFE) {}
        catch(ClassCastException CCE) {}
        
        // Test 3: Make sure the added class exist now ------------------------------------------------------
        
        JC.addClassData(CData2);
        
        System.setOut(PrintS1);
        ThreadClass = null;
        try {
            ThreadClass = JC.forName(CName).asSubclass(Thread.class); 
        } catch(ClassNotFoundException CNFE) {
            throw new AssertionError("Compilation have failed because the result class is not found.");
        } catch(ClassCastException CCE) {
            throw new AssertionError("Compilation have failed because the result class is not a Thread class.");
        }
        
        Thread T2 = null;
        try { T2 = ThreadClass.getConstructor().newInstance(); }
        catch(IllegalAccessException IAE)   {}
        catch(InstantiationException IE)    {}
        catch (IllegalArgumentException e)  {}
        catch (InvocationTargetException e) {}
        catch (NoSuchMethodException e)     {}
        catch (SecurityException e)         {}
        
        if(T2 == null) throw new AssertionError("Compilation have failed because the result class has no default constructor.");
        
        T2.start();
        while(T2.isAlive());
        
        System.setOut(PrintSOrg);
        if(!Output0.toString().equals(Output1.toString())) {
            System.out.printf("Output0 (%d): %s\n", Output0.toString().length(), Output0.toString());
            System.out.printf("Output1 (%d): %s\n", Output1.toString().length(), Output1.toString());
            throw new AssertionError("The results are not equals.");
        }
        
        // Test 4: Both threads and theirs classes must not be the same
        
        if(T1 == T2)
            throw new AssertionError("T1 and T2 are the same objcet so the class was not actually reloaded.");
        
        if(T1.getClass() == T2.getClass())
            throw new AssertionError("Classes of T1 and T2 are the exact same class so the class was not actually reloaded.");
        
        // Done ---------------------------------------------------------------------------------------------
        
        System.out.println("DONE!!!");
    }
    
}
