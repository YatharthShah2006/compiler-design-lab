package visitor;
import syntaxtree.*;

public class BinOpVisitor extends GJNoArguDepthFirst<String> {
    String dest;
    String src1;
    String op;

    public BinOpVisitor(String o, String d, String s) {
        op = o;
        src1 = s;
        dest = d;
    }

    public String visit (SimpleExp n) {
        return n.f0.accept(this);
    }

    public String visit(Reg n) {
        String r = "$" + n.f0.accept(this);
        System.out.println(op + " " + dest + ", " + src1 + ", " + r);
        return null;
    }

    public String visit(Label n) {
        String lbl = n.f0.accept(this);
        String tmp = "$v0";
        if (src1.equals(tmp)) tmp = "$v1";
        System.out.println("la " + tmp + ", " + lbl);
        System.out.println(op + " " + dest + ", " + src1 + ", " + tmp);
        return null;
    }

    public String visit(IntegerLiteral n) {
        String num = n.f0.accept(this);
        String tmp = "$v0";
        if (src1.equals(tmp)) tmp = "$v1";
        System.out.println("li " + tmp + ", " + num);
        System.out.println(op + " " + dest + ", " + src1 + ", " + tmp);
        return null;
    }
}
