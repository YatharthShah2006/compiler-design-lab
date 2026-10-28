package visitor;
import syntaxtree.*;
import java.util.*;

public class CodeGenerator extends GJDepthFirst<String, String> {
    public Map<String, ProcInfo> procMap;
    private ProcInfo current;

    public CodeGenerator(Map<String, ProcInfo> p) {
        procMap = p;
    }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
    public String visit(Procedure n, String argu) {
        String name = n.f0.accept(this, argu);
        current = procMap.get(name);
        System.out.println(name + " [" + current.numArgs + "] [" + current.stackSpace + "] [" + current.maxArgs + "]");
        for (int i = 0; i < 8; i++)
            System.out.println("ASTORE SPILLEDARG " + (current.stackSpace - 1 - i) + " s" + i);
        
        for (int i = 0; i < current.numArgs; i++) {
            if (!current.regs.containsKey("TEMP " + i)) continue;
            String dest = current.regs.get("TEMP " + i);
            if (i < 4)
                if (dest.startsWith("SPILLEDARG"))
                    System.out.println("ASTORE " + dest + " a" + i);
                else
                    System.out.println("MOVE " + dest + " a" + i);
            else
                if (dest.startsWith("SPILLEDARG")) {
                    System.out.println("ALOAD v0 SPILLEDARG " + (i-4));
                    System.out.println("ASTORE " + dest + " v0");
                }
                else
                    System.out.println("ALOAD " + dest + " SPILLEDARG " + (i-4));
        }

        n.f4.accept(this, argu);
        
        for (int i = 0; i < 8; i++)
            System.out.println("ALOAD s" + i + " SPILLEDARG " + (current.stackSpace - 1 - i));
        System.out.println("END");
        if (current.spilled)
            System.out.println("// SPILLED");
        else
            System.out.println("// NOTSPILLED");
        return null;
    }


   /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
    public String visit(Goal n, String argu) {
        current = procMap.get("MAIN");
        System.out.println("MAIN [0] [" + current.stackSpace + "] [" + current.maxArgs + "]");
        n.f1.accept(this, argu);
        System.out.println("END");
        if (current.spilled)
            System.out.println("// SPILLED");
        else
            System.out.println("// NOTSPILLED");
        n.f3.accept(this, argu);
        return null;
    }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
    public String visit(StmtList n, String argu) {
        n.f0.accept(this, "Stmt");
        return null;
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
        n.f0.accept(this, "");
        return null;
    }

   /**
    * f0 -> "NOOP"
    */
    public String visit(NoOpStmt n, String argu) {
        System.out.println("NOOP");
        return null;
    }

   /**
    * f0 -> "ERROR"
    */
    public String visit(ErrorStmt n, String argu) {
        System.out.println("ERROR");
        return null;
    }

   /**
    * f0 -> "CJUMP"
    * f1 -> Temp()
    * f2 -> Label()
    */
    public String visit(CJumpStmt n, String argu) {
        String tmp = n.f1.accept(this, "v0");
        System.out.println("CJUMP " + tmp + " " + n.f2.accept(this, argu));
        return null;
    }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
    public String visit(JumpStmt n, String argu) {
        System.out.println("JUMP " + n.f1.accept(this, argu));
        return null;
    }

   /**
    * f0 -> "HSTORE"
    * f1 -> Temp()
    * f2 -> IntegerLiteral()
    * f3 -> Temp()
    */
    public String visit(HStoreStmt n, String argu) {
        System.out.println("HSTORE " + n.f1.accept(this, "v0") + " " + n.f2.accept(this, argu) + " " + n.f3.accept(this, "v1"));
        return null;
    }

   /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Temp()
    * f3 -> IntegerLiteral()
    */
    public String visit(HLoadStmt n, String argu) {
        String tmp = n.f1.accept(this, "def");
        if (tmp.startsWith("SPILLEDARG")) {
            System.out.println("HLOAD v1 " + n.f2.accept(this, "v0") + " " + n.f3.accept(this, argu));
            System.out.println("ASTORE " + tmp + " v1");
        }
        else {
            System.out.println("HLOAD " + tmp + " " + n.f2.accept(this, "v0") + " " + n.f3.accept(this, argu));
        }
        return null;
    }

   /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
    public String visit(MoveStmt n, String argu) {
        String tmp = n.f1.accept(this, "def");
        String exp = n.f2.accept(this, argu);
        if (exp.equals("call"))
            if (tmp.startsWith("SPILLEDARG"))
                System.out.println("ASTORE " + tmp + " v0");
            else
                System.out.println("MOVE " + tmp + " v0");
        else
            if (tmp.startsWith("SPILLEDARG")) {
                System.out.println("MOVE v0 " + n.f2.accept(this, argu));
                System.out.println("ASTORE " + tmp + " v0");
            }
            else
                System.out.println("MOVE " + tmp + " " + n.f2.accept(this, argu));
        return null;
    }

   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
    public String visit(PrintStmt n, String argu) {
        System.out.println("PRINT " + n.f1.accept(this, "v0"));
        return null;
    }

   /**
    * f0 -> Call()
    *       | HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    */
    public String visit(Exp n, String argu) {
        return n.f0.accept(this, "v0");
    }

   /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> SimpleExp()
    * f4 -> "END"
    */
    public String visit(StmtExp n, String argu) {
        n.f1.accept(this, argu);
        System.out.println("MOVE v0 " + n.f3.accept(this, "v0"));
        return null;
    }

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    * f2 -> "("
    * f3 -> ( Temp() )*
    * f4 -> ")"
    */
    public String visit(Call n, String argu) {
        NodeListOptional args = n.f3;
        for (int i = 0; i < 10; i++)
            System.out.println("ASTORE SPILLEDARG " + (current.stackSpace - 9 - i) + " t" + i);
        if (args.present()) {
            int count = 0;
            for (Enumeration<Node> e = args.elements(); e.hasMoreElements(); count++) {
                String arg = e.nextElement().accept(this, "v0");
                if (count < 4)
                    System.out.println("MOVE a" + count + " " + arg);
                else
                    System.out.println("PASSARG " + (count-3) + " " + arg);
            }
        }
        System.out.println("CALL " + n.f1.accept(this, "v1"));
        for (int i = 0; i < 10; i++)
            System.out.println("ALOAD t" + i + " SPILLEDARG " + (current.stackSpace - 9 - i));
        return "call";
    }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
    public String visit(HAllocate n, String argu) {
        return "HALLOCATE " + n.f1.accept(this, "v0");
    }

   /**
    * f0 -> Operator()
    * f1 -> Temp()
    * f2 -> SimpleExp()
    */
    public String visit(BinOp n, String argu) {
        return n.f0.accept(this, argu) + " " + n.f1.accept(this, "v0") + " " + n.f2.accept(this, "v1");
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
        return n.f0.accept(this, argu);
    }

   /**
    * f0 -> Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
    public String visit(SimpleExp n, String argu) {
        return n.f0.accept(this, argu);
    }

   /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
    public String visit(Temp n, String argu) {
        String t = current.regs.get("TEMP " + n.f1.accept(this, argu));
        if (argu.equals("def")) return t;
        else if (t.startsWith("SPILLEDARG")) {
            System.out.println("ALOAD " + argu + " " + t);
            return argu;
        }
        else return t;
    }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n, String argu) {
        return n.f0.accept(this, argu);
    }

   /**
    * f0 -> <IDENTIFIER>
    */
    public String visit(Label n, String argu) {
        String lbl = n.f0.accept(this, argu);
        if (current.labelMap.containsKey(lbl))
            lbl = current.labelMap.get(lbl);
        if (argu.equals("Stmt"))
            System.out.println(lbl);
        return lbl;
    }

}
