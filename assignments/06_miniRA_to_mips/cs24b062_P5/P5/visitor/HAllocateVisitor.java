package visitor;
import syntaxtree.*;

public class HAllocateVisitor extends GJNoArguDepthFirst<String> {
    String dest;

    public HAllocateVisitor(String d) {
        dest = d;
    }

    public String visit (SimpleExp n) {
        // System.out.println("hi from simpleexp in hv");
        n.f0.accept(this);
        System.out.println("move " + dest + ", $v0");
        return null;
    }

    public String visit(Reg n) {
        String r = "$" + n.f0.accept(this);
        System.out.println("move $a0, " + r);
        System.out.println("li $v0, 9");
        System.out.println("syscall");
        return null;
    }

    public String visit(Label n) {
        String lbl = n.f0.accept(this);
        System.out.println("la $a0, " + lbl);
        System.out.println("li $v0, 9");
        System.out.println("syscall");
        return null;
    }

    public String visit(IntegerLiteral n) {
        String num = n.f0.accept(this);
        System.out.println("li $a0, " + num);
        System.out.println("li $v0, 9");
        System.out.println("syscall");
        return null;
    }
}
