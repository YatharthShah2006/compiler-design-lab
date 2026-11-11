package visitor;
import syntaxtree.*;

public class MoveVisitor extends GJNoArguDepthFirst<String> {
    String dest;

    public MoveVisitor(String d) {
        dest = d;
    }

    public String visit(Exp n) {
        return n.f0.accept(this);
    }

    public String visit(HAllocate n) {
        return n.f1.accept(new HAllocateVisitor(dest));
    }

    public String visit(BinOp n) {
        String op = n.f0.accept(this);
        String src1 = "$" + n.f1.f0.accept(this);
        return n.f2.accept(new BinOpVisitor(op, dest, src1));
    }

    public String visit(Operator n) {
        String o = n.f0.accept(this);
        switch(o) {
            case "LE":
                return "sle";
            case "NE":
                return "sne";
            case "PLUS":
                return "addu";
            case "MINUS":
                return "subu";
            case "TIMES":
                return "mul";
            case "DIV":
                return "div";
            default:
                throw new RuntimeException();
        }
    }

    public String visit (SimpleExp n) {
        n.f0.accept(this);
        return null;
    }

    public String visit(Reg n) {
        String r = "$" + n.f0.accept(this);
        System.out.println("move " + dest + ", " + r);
        return null;
    }

    public String visit(Label n) {
        String lbl = n.f0.accept(this);
        System.out.println("la " + dest + ", " + lbl);
        return null;
    }

    public String visit(IntegerLiteral n) {
        String num = n.f0.accept(this);
        System.out.println("li " + dest + ", " + num);
        return null;
    }
}
