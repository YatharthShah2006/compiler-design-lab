package visitor;
import syntaxtree.*;

public class Caller extends GJNoArguDepthFirst<String> {
    public String visit (SimpleExp n) {
        n.f0.accept(this);
        return null;
    }

    public String visit(Reg n) {
        String r = "$" + n.f0.accept(this);
        System.out.println("jalr " + r);
        return null;
    }

    public String visit(Label n) {
        String lbl = n.f0.accept(this);
        System.out.println("jal " + lbl);
        return null;
    }

    public String visit(IntegerLiteral n) {
        String num = n.f0.accept(this);
        System.out.println("li $t0, " + num);
        System.out.println("jalr $t0");
        return null;
    }
}
