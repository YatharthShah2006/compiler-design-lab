import syntaxtree.*;
import visitor.*;
import java.util.*;

public class P1 {
   public static void main(String [] args) {
      try {
         Node root = new MiniJavaParser(System.in).Goal();
         SymbolTableBuilder stb = new SymbolTableBuilder();
         root.accept(stb, null);
         TypeChecker tc = new TypeChecker(stb.symbolTable);
         root.accept(tc);
         System.out.println("Program type checked successfully");
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
      catch (ResolveException e) {
         System.out.println("Symbol not found");
         // System.out.println(e.toString());
      }
      catch (TypeException e) {
         System.out.println("Type error");
         // System.out.println(e.toString());
      }
   }
}
