package net.nawaman.javacompiler.test;

/** Do all tests */
public class AllTests {
    
    /** Prints the test title */
    static public void PrintTestTitle() {
        String ClsName = "";
        try { System.out.println(5 / 0); }
        catch (Exception E) {
            ClsName = E.getStackTrace()[1].getClassName();
            try { ClsName = Class.forName(ClsName).getSimpleName(); }
            catch (ClassNotFoundException CNFE) {}
        }
        System.out.print(String.format("%s %"+(30 - ClsName.length())+"s", ClsName, " ... "));
    }
    
    /** Main function of the tests */
    public static void main(String[] args) {
        Test_00_UnitTests    .main(args);
        Test_01_SimpleCompile.main(args);
        Test_02_Classpath    .main(args);
        Test_03_ErrorHandling.main(args);
        Test_04_Persistent   .main(args);
        
        System.out.println("DONE!!!");
    }

}
