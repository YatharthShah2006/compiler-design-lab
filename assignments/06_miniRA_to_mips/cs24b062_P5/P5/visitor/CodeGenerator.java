package visitor;
import java.util.*;
import syntaxtree.*;

public class CodeGenerator extends GJNoArguDepthFirst<String> {
    //
    // User-generated visitor methods below
    //
    private boolean printLabel;
    private boolean isMove;
    private String moveDst;
    private int stackSpace;

    /**
        * f0 -> "MAIN"
        * f1 -> "["
        * f2 -> IntegerLiteral()
        * f3 -> "]"
        * f4 -> "["
        * f5 -> IntegerLiteral()
        * f6 -> "]"
        * f7 -> "["
        * f8 -> IntegerLiteral()
        * f9 -> "]"
        * f10 -> StmtList()
        * f11 -> "END"
        * f12 -> ( SpillInfo() )?
        * f13 -> ( Procedure() )*
        * f14 -> <EOF>
        */
    public String visit(Goal n) {
        isMove = false;
        stackSpace = (Integer.parseInt(n.f5.accept(this)) + 1) << 2;
        System.out.println(".text");
        System.out.println(".globl main");
        System.out.println("main:");
        System.out.println("subu $sp, $sp, " + stackSpace);
        System.out.println("sw $ra, 0($sp)");
        
        n.f10.accept(this);

        System.out.println("lw $ra, 0($sp)");
        System.out.println("addu $sp, $sp, " + stackSpace);
        System.out.println("j $ra");
        System.out.println("");
        
        n.f13.accept(this);

        System.out.println(".data");
        System.out.println(".align 0");
        System.out.println("newl:    .asciiz \"\\n\" ");
        System.out.println(".data");
        System.out.println(".align 0");
        System.out.println("str_er:  .asciiz \" ERROR: abnormal termination\\n\"");
        return null;
    }

    /**
        * f0 -> ( ( Label() )? Stmt() )*
        */
    public String visit(StmtList n) {
        printLabel = true;
        n.f0.accept(this);
        printLabel = false;
        return null;
    }

    /**
        * f0 -> Label()
        * f1 -> "["
        * f2 -> IntegerLiteral()
        * f3 -> "]"
        * f4 -> "["
        * f5 -> IntegerLiteral()
        * f6 -> "]"
        * f7 -> "["
        * f8 -> IntegerLiteral()
        * f9 -> "]"
        * f10 -> StmtList()
        * f11 -> "END"
        * f12 -> ( SpillInfo() )?
        */
    public String visit(Procedure n) {
        String procName = n.f0.accept(this);

        stackSpace = (Integer.parseInt(n.f5.accept(this)) + 1) << 2;

        System.out.println(".text");
        System.out.println(".globl " + procName);
        System.out.println(procName + ":");
        System.out.println("subu $sp, $sp, " + stackSpace);
        System.out.println("sw $ra, 0($sp)");
        
        n.f10.accept(this);        
        
        System.out.println("lw $ra, 0($sp)");
        System.out.println("addu $sp, $sp, " + stackSpace);
        System.out.println("j $ra");
        System.out.println("");

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
        *       | ALoadStmt()
        *       | AStoreStmt()
        *       | PassArgStmt()
        *       | CallStmt()
        */
    public String visit(Stmt n) {
        printLabel = false;
        n.f0.accept(this);
        printLabel = true;
        return null;
    }

    /**
        * f0 -> "NOOP"
        */
    public String visit(NoOpStmt n) {
        System.out.println("nop");
        return null;
    }

    /**
        * f0 -> "ERROR"
        */
    public String visit(ErrorStmt n) {
        System.out.println("li $v0, 4");
        System.out.println("la $a0, str_er");
        System.out.println("syscall");
        System.out.println("li $v0, 10");
        System.out.println("syscall");
        return null;
    }

    /**
        * f0 -> "CJUMP"
        * f1 -> Reg()
        * f2 -> Label()
        */
    public String visit(CJumpStmt n) {
        System.out.println("beqz " + n.f1.accept(this) + " " + n.f2.accept(this));
        return null;
    }

    /**
        * f0 -> "JUMP"
        * f1 -> Label()
        */
    public String visit(JumpStmt n) {
        System.out.println("b " + n.f1.accept(this));
        return null;
    }

    /**
        * f0 -> "HSTORE"
        * f1 -> Reg()
        * f2 -> IntegerLiteral()
        * f3 -> Reg()
        */
    public String visit(HStoreStmt n) {
        System.out.println("sw " + n.f3.accept(this) + ", " + n.f2.accept(this) + "(" + n.f1.accept(this) + ")");
        
        return null;
    }

    /**
        * f0 -> "HLOAD"
        * f1 -> Reg()
        * f2 -> Reg()
        * f3 -> IntegerLiteral()
        */
    public String visit(HLoadStmt n) {
        System.out.println("lw " + n.f1.accept(this) + ", " + n.f3.accept(this) + "(" + n.f2.accept(this) + ")");
        return null;
    }

    /**
        * f0 -> "MOVE"
        * f1 -> Reg()
        * f2 -> Exp()
        */
    public String visit(MoveStmt n) {
        moveDst = n.f1.accept(this); 
        // System.out.println("hi from movestmt in cg");
        n.f2.accept(new MoveVisitor(moveDst));
        return null;
    }

    /**
        * f0 -> "PRINT"
        * f1 -> SimpleExp()
        */
    public String visit(PrintStmt n) {
        n.f1.accept(new MoveVisitor("$a0"));
        System.out.println("li $v0, 1");
        System.out.println("syscall");
        System.out.println("la $a0, newl");
        System.out.println("li $v0, 4");
        System.out.println("syscall");
        return null;
    }

    /**
        * f0 -> "ALOAD"
        * f1 -> Reg()
        * f2 -> SpilledArg()
        */
    public String visit(ALoadStmt n) {
        String dest = n.f1.accept(this);
        int offset = stackSpace - (Integer.parseInt(n.f2.accept(this)) << 2) - 4;
        System.out.println("lw " + dest + ", " + offset + "($sp)");
        return null;
    }

    /**
        * f0 -> "ASTORE"
        * f1 -> SpilledArg()
        * f2 -> Reg()
        */
    public String visit(AStoreStmt n) {
        int offset = stackSpace - (Integer.parseInt(n.f1.accept(this)) << 2) - 4;
        String src = n.f2.accept(this);
        System.out.println("sw " + src + ", " + offset + "($sp)");
        return null;
    }

    /**
        * f0 -> "PASSARG"
        * f1 -> IntegerLiteral()
        * f2 -> Reg()
        */
    public String visit(PassArgStmt n) {
        int offset = -1 * (Integer.parseInt(n.f1.accept(this)) << 2);
        String src = n.f2.accept(this);
        System.out.println("sw " + src + ", " + offset + "($sp)");
        return null;
    }

    /**
        * f0 -> "CALL"
        * f1 -> SimpleExp()
        */
    public String visit(CallStmt n) {
        n.f1.accept(new Caller());   
        return null;
    }


    /**
        * f0 -> "SPILLEDARG"
        * f1 -> IntegerLiteral()
        */
    public String visit(SpilledArg n) {
        return n.f1.accept(this);
        
    }

    /**
        * f0 -> "a0"
        *       | "a1"
        *       | "a2"
        *       | "a3"
        *       | "t0"
        *       | "t1"
        *       | "t2"
        *       | "t3"
        *       | "t4"
        *       | "t5"
        *       | "t6"
        *       | "t7"
        *       | "s0"
        *       | "s1"
        *       | "s2"
        *       | "s3"
        *       | "s4"
        *       | "s5"
        *       | "s6"
        *       | "s7"
        *       | "t8"
        *       | "t9"
        *       | "v0"
        *       | "v1"
        */
    public String visit(Reg n) {
        return "$" + n.f0.accept(this);
    }

    /**
        * f0 -> <INTEGER_LITERAL>
        */
    public String visit(IntegerLiteral n) {
        return n.f0.accept(this);
    }

    /**
        * f0 -> <IDENTIFIER>
        */
    public String visit(Label n) {
        String lbl = n.f0.accept(this);
        if (printLabel) System.out.print(lbl + ":");
        return lbl;
    }
}
