package visitor;
import syntaxtree.*;

public class SymbolTableBuilder extends GJDepthFirst<String, String> {
    public SymbolTable symbolTable = new SymbolTable();

    @Override 
    public String visit(MainClass n, String argu) {
        String mainName = n.f1.accept(this, argu);
        symbolTable.insertClass(mainName, null);
        symbolTable.insertMethod(mainName, "main", "void");
        return null;
    }
    
    @Override
    public String visit(ClassDeclaration n, String argu) {
        String className = n.f1.accept(this, argu);
        symbolTable.insertClass(className, null);
        n.f3.accept(this, className);
        n.f4.accept(this, className);
        return null;
    }

    @Override
    public String visit(MethodDeclaration n, String argu) {
        String methodName = n.f2.accept(this, argu);
        String retType = n.f1.accept(this, argu);
        symbolTable.insertMethod(argu, methodName, retType);
        return null;
    }

    @Override 
    public String visit(ClassExtendsDeclaration n, String argu) {
        String className = n.f1.accept(this, argu);
        String parentName = n.f3.accept(this, argu);
        symbolTable.insertClass(className, parentName);
        n.f5.accept(this, className);
        n.f6.accept(this, className);
        return null;
    }

        @Override
    public String visit(Type n, String argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(ArrayType n, String argu) {
	    return "int[]";
    }

    @Override
    public String visit(BooleanType n, String argu) {
	    return "boolean";
    }

    @Override
    public String visit(IntegerType n, String argu) {
		return "int";
    }

    @Override
    public String visit(VarDeclaration n, String argu) {
        String name = n.f1.accept(this, argu);
        String type = n.f0.accept(this, argu);
        symbolTable.insertMember(argu, name, type);
        return null;
    }

    @Override
    public String visit(Identifier n, String argu) {
        return n.f0.accept(this, argu);
    }
}