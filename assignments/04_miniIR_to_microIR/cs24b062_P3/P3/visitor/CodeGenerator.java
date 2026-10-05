package visitor;
import syntaxtree.*;
import java.util.*;

public class CodeGenerator extends GJDepthFirst<String, String> {

   private Map<Integer, Integer> tempMap = new HashMap<>();
   private int tempCount;
   private String newTemp() {
      tempCount++;
      return "TEMP " + (tempCount - 1);
   }

   public String visit(NodeList n, String argu) {
      String _ret=null;
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this,argu);
         _count++;
      }
      return _ret;
   }

   public String visit(NodeListOptional n, String argu) {
      if (argu != null && argu.equals("args")) {
         if ( n.present() ) {
            String _ret="";
            for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
               String exp = e.nextElement().accept(this,argu);
               if (!exp.startsWith("TEMP ")) {
                  String newt = newTemp();
                  System.out.println("MOVE " + newt + " " + exp);
                  exp = newt;
               }
               _ret += exp;
               _ret += " ";
            }
            return _ret.substring(0, _ret.length() - 1);
         }
         else
            return "";
      }
      else if ( n.present() ) {
         String _ret=null;
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this,argu);
         }
         return _ret;
      }
      else
         return null;
   }

   public String visit(NodeOptional n, String argu) {
      if ( n.present() )
         return n.node.accept(this,argu);
      else
         return null;
   }

   public String visit(NodeSequence n, String argu) {
      String _ret=null;
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this,argu);
         _count++;
      }
      return _ret;
   }

   public String visit(NodeToken n, String argu) { return (String) n.tokenImage; }

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
   public String visit(Goal n, String argu) {
      String _ret=null;
      tempCount = 21;
      tempMap.clear();
      System.out.println("MAIN");
      n.f1.accept(this, argu);
      System.out.println("END");
      n.f3.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public String visit(StmtList n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      n.f0.accept(this, "Stmt");
      return _ret;
   }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
   public String visit(Procedure n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      tempCount = 21;
      tempMap.clear();
      String procname = n.f0.accept(this, "");
      String num = n.f2.accept(this, argu);
      System.out.println(procname + " [" + num + "]");
      n.f4.accept(this, "proc");
      return _ret;
   }

   /**
    * f0 -> NoOpStmt()
    *       | ErrorStmt()
    *       | CJumpStmt()
    *       | JumpStmt()
    *       | HStoreStmt()
    *       | HLoadStmt()
    *       | MoveStmt()
    *       | PrintStmt()
    */
   public String visit(Stmt n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "NOOP"
    */
   public String visit(NoOpStmt n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      System.out.println("NOOP");
      return _ret;
   }

   /**
    * f0 -> "ERROR"
    */
   public String visit(ErrorStmt n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      System.out.println("ERROR");
      return _ret;
   }

   /**
    * f0 -> "CJUMP"
    * f1 -> Exp()
    * f2 -> Label()
    */
   public String visit(CJumpStmt n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      String exp = n.f1.accept(this, argu);
      if (!exp.startsWith("TEMP ")) {
         String newt = newTemp();
         System.out.println("MOVE " + newt + " " + exp);
         exp = newt;
      }
      String dst = n.f2.accept(this, "");
      System.out.println("CJUMP " + exp + " " + dst);
      return _ret;
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public String visit(JumpStmt n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      String dst = n.f1.accept(this, "");
      System.out.println("JUMP " + dst);
      return _ret;
   }

   /**
    * f0 -> "HSTORE"
    * f1 -> Exp()
    * f2 -> IntegerLiteral()
    * f3 -> Exp()
    */
   public String visit(HStoreStmt n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      String e1 = n.f1.accept(this, argu);
      if (!e1.startsWith("TEMP ")) {
         String newt = newTemp();
         System.out.println("MOVE " + newt + " " + e1);
         e1 = newt;
      }
      String num = n.f2.accept(this, argu);
      String e2 = n.f3.accept(this, argu);
      if (!e2.startsWith("TEMP ")) {
         String newt = newTemp();
         System.out.println("MOVE " + newt + " " + e2);
         e2 = newt;
      }
      System.out.println("HSTORE " + e1 + " " + num + " " + e2);
      return _ret;
   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Exp()
    * f3 -> IntegerLiteral()
    */
   public String visit(HLoadStmt n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      String t = n.f1.accept(this, argu);
      String exp = n.f2.accept(this, argu);
      if (!exp.startsWith("TEMP ")) {
         String newt = newTemp();
         System.out.println("MOVE " + newt + " " + exp);
         exp = newt;
      }
      String num = n.f3.accept(this, argu);
      System.out.println("HLOAD " + t + " " + exp + " " + num);
      return _ret;
   }

   /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
   public String visit(MoveStmt n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      String t = n.f1.accept(this, argu);
      String e = n.f2.accept(this, argu);
      System.out.println("MOVE " + t + " " + e);
      return _ret;
   }

   /**
    * f0 -> "PRINT"
    * f1 -> Exp()
    */
   public String visit(PrintStmt n, String argu) {
		/* YOUR CODE HERE */

      String _ret=null;
      String e = n.f1.accept(this, argu);
      System.out.println("PRINT " + e);
      return _ret;
   }

   /**
    * f0 -> StmtExp()
    *       | Call()
    *       | HAllocate()
    *       | BinOp()
    *       | Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
   public String visit(Exp n, String argu) {
		/* YOUR CODE HERE */

      return n.f0.accept(this, "");
   }

   /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> Exp()
    * f4 -> "END"
    */
   public String visit(StmtExp n, String argu) {
		/* YOUR CODE HERE */
      if (argu != null && argu.equals("proc")) {
         String _ret=null;
         System.out.println("BEGIN");
         n.f1.accept(this, null);
         String ret = n.f3.accept(this, null);
         System.out.println("RETURN " + ret);
         System.out.println("END");
         return _ret;
      }
      else {
         n.f1.accept(this, null);
         String ret = n.f3.accept(this, null);
         return ret;
      }
   }

   /**
    * f0 -> "CALL"
    * f1 -> Exp()
    * f2 -> "("
    * f3 -> ( Exp() )*
    * f4 -> ")"
    */
   public String visit(Call n, String argu) {
		/* YOUR CODE HERE */

      String _ret = newTemp();
      String fname = n.f1.accept(this, argu);
      String args = n.f3.accept(this, "args");
      System.out.println("MOVE " + _ret + " CALL " + fname + " (" + args + ")");
      return _ret;
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> Exp()
    */
   public String visit(HAllocate n, String argu) {
		/* YOUR CODE HERE */

      String _ret = newTemp();
      String e = n.f1.accept(this, argu);
      System.out.println("MOVE " + _ret + " HALLOCATE " + e);
      return _ret;
   }

   /**
    * f0 -> Operator()
    * f1 -> Exp()
    * f2 -> Exp()
    */
   public String visit(BinOp n, String argu) {
		/* YOUR CODE HERE */

      String _ret = newTemp();
      String op = n.f0.accept(this, argu);
      String exp = n.f1.accept(this, argu);
      if (!exp.startsWith("TEMP ")) {
         String newt = newTemp();
         System.out.println("MOVE " + newt + " " + exp);
         exp = newt;
      }
      String e2 = n.f2.accept(this, argu);
      System.out.println("MOVE " + _ret + " " + op + " " + exp + " " + e2);
      return _ret;
   }

   /**
    * f0 -> "LE"
    *       | "NE"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    *       | "DIV"
    */
   public String visit(Operator n, String argu) {
		/* YOUR CODE HERE */

      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
   public String visit(Temp n, String argu) {
		/* YOUR CODE HERE */

      String c = n.f1.accept(this, argu);
      int num = Integer.parseInt(c);
      if (tempMap.containsKey(num)) {
         return "TEMP " + tempMap.get(num);
      }
      else if (num <= 20) {
         tempMap.put(num, num);
         return "TEMP " + num;
      }
      else {
         tempMap.put(num, tempCount);
         tempCount++;
         return "TEMP " + (tempCount - 1);
      }
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public String visit(IntegerLiteral n, String argu) {
		/* YOUR CODE HERE */

      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Label n, String argu) {
		/* YOUR CODE HERE */

      String lbl = n.f0.accept(this, argu);
      if (argu != null && argu.equals("Stmt")) System.out.println(lbl + " NOOP");
      return lbl;
   }

}
