import syntaxtree.*;
import visitor.*;

public class P4 {
   public static void main(String [] args) {
      try {
         Node root = new microIRParser(System.in).Goal();
         //System.out.println("Program parsed successfully");
         Analysis analyzer = new Analysis();
         root.accept(analyzer, "");
         root.accept(new CodeGenerator(analyzer.procInfo), "");
         // Object value = root.accept(new GJDepthFirst(), null); // Your assignment part is invoked here.
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
} 



