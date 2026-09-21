package visitor;
import syntaxtree.*;
import java.util.*;

public class SymbolTableBuilder extends GJDepthFirst<String, SymbolTableBuilder.State> {
    public SymbolTable symbolTable = new SymbolTable();

    public record State (String className, String methodName) {}

    @Override 
    public String visit(MainClass n, State argu) {
        String mainName = n.f1.accept(this, argu);
        symbolTable.setMain(mainName);
        symbolTable.insertClass(mainName, null);
        symbolTable.insertMethod(mainName, "main", "void");
        symbolTable.addParam(mainName, "main", "String[]");
        return null;
    }
    
    @Override
    public String visit(ClassDeclaration n, State argu) {
        String className = n.f1.accept(this, argu);
        symbolTable.insertClass(className, null);
        n.f3.accept(this, new State(className, null));
        n.f4.accept(this, new State(className, null));
        return null;
    }

    @Override
    public String visit(MethodDeclaration n, State argu) {
        String methodName = n.f2.accept(this, argu);
        String returnType = n.f1.accept(this, argu);
        symbolTable.insertMethod(argu.className, methodName, returnType);
        n.f4.accept(this, new State(argu.className, methodName));
        return null;
    }

    @Override
    public String visit(FormalParameter n, State argu) {
        String type = n.f0.accept(this, argu);
        symbolTable.addParam(argu.className, argu.methodName, type);
        return null;
    }

    @Override 
    public String visit(ClassExtendsDeclaration n, State argu) {
        String className = n.f1.accept(this, argu);
        String parentName = n.f3.accept(this, argu);
        symbolTable.insertClass(className, parentName);
        n.f5.accept(this, new State(className, null));
        n.f6.accept(this, new State(className, null));
        return null;
    }

    @Override
    public String visit(VarDeclaration n, State argu) {
        if (argu.methodName == null) {
            String type = n.f0.accept(this, argu);
            String name = n.f1.accept(this, argu);
            symbolTable.addMember(argu.className, name, type);
        }
        return null;
    }

    @Override
    public String visit(Identifier n, State argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(IntegerType n, State argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(BooleanType n, State argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(ArrayType n, State argu) {
        return "int[]";
    }

    @Override
    public String visit(Type n, State argu) {
        return n.f0.accept(this, argu);
    }
}


// no arguments, store symbol table as member of SymbolTableBuilder and directly update
// in class declaration, create new entry in cymbol table
// in method declaration, get types of parameters and create an entry and store
// parameter should return its type
// class -> [method -> [type]]