package visitor;
import syntaxtree.*;
import java.util.*;

public class TypeChecker extends GJNoArguDepthFirst<String>{
    private SymbolTable symbolTable;
    private Map<String, String> Args = new HashMap<>();
    private Map<String, String> LocalVars = new HashMap<>();
    private String currClass;
    private Stack<List<String>> methodList = new Stack<>();

    public TypeChecker(SymbolTable st) {
        this.symbolTable = st;
        st.check();
    }

    public String getType(String varName){
        if (LocalVars.containsKey(varName)) return LocalVars.get(varName);
        else if (Args.containsKey(varName)) return Args.get(varName);
        else return symbolTable.getVarType(currClass, varName);
    }

    @Override 
    public String visit(NodeToken n) {
        return n.tokenImage;
    }

    @Override
    public String visit(MainClass n) {
        currClass = null;
        Args.put(n.f11.accept(this), "String[]");
        n.f14.accept(this);
        Args.clear();
        return null;
    }

    @Override
    public String visit(ClassDeclaration n) {
        currClass = n.f1.accept(this);
        n.f4.accept(this);
        currClass = null;
        return null;
    }

    @Override
    public String visit(ClassExtendsDeclaration n){
        currClass = n.f1.accept(this);
        n.f6.accept(this);
        currClass = null;
        return null;
    }

    @Override
    public String visit(VarDeclaration n) {
        String typeName = n.f0.accept(this);
        String varName = n.f1.accept(this);
        if (LocalVars.containsKey(varName) || Args.containsKey(varName)) {
            throw new TypeException("Redefinition of local variable with name " + varName);
        }
        LocalVars.put(varName, typeName);
        return null;
    }

    @Override
    public String visit(MethodDeclaration n) {
		String retType = n.f1.accept(this);
        n.f4.accept(this);
        n.f7.accept(this);
        n.f8.accept(this);
        String retExpr = n.f10.accept(this);
        if (!symbolTable.isSubClass(retExpr, retType)) {
            throw new TypeException("Method " + n.f2.accept(this) + " fails to return a valid type!");
        }
        Args.clear();
        LocalVars.clear();
        return null;
    }


    @Override
    public String visit(FormalParameter n) {
        String typeName = n.f0.accept(this);
        String varName = n.f1.accept(this);
        if (Args.containsKey(varName)) {
            throw new TypeException("Redefinition of formal parameter with name " + varName);
        }
        Args.put(varName, typeName);
        return null;
    }

    @Override
    public String visit(Type n) {
        String ret = n.f0.accept(this);
        if (!symbolTable.classExists(ret)) {
            throw new ResolveException("Unable to resolve unknown type " + ret + "!");
        }
        return ret;
    }

    @Override
    public String visit(ArrayType n) {
	    return "int[]";
    }

    @Override
    public String visit(BooleanType n) {
	    return n.f0.accept(this);
    }

    @Override
    public String visit(IntegerType n) {
		return n.f0.accept(this);
    }

    @Override
    public String visit(Statement n) {
        n.f0.accept(this);
        return null;
    }

    @Override
    public String visit(Block n) {
        n.f1.accept(this);
        return null;
    }

    @Override
    public String visit(AssignmentStatement n) {
        String varType = getType(n.f0.accept(this));
        String assnType = n.f2.accept(this);
        if (!symbolTable.isSubClass(assnType, varType)) {
            throw new TypeException("Invalid type when assigning variable " + n.f0.accept(this));
        }
        return null;
    }

    @Override
    public String visit(ArrayAssignmentStatement n) {
		String varType = getType(n.f0.accept(this));
        String indexType = n.f2.accept(this);
        String assnType = n.f5.accept(this);
        if ((!varType.equals("int[]")) || (!indexType.equals("int")) || (!assnType.equals("int"))) {
            throw new TypeException("Invalid type when assigning variable " + n.f0.accept(this));
        }
        return null;
    }

    @Override
    public String visit(IfthenStatement n) {
        String condType = n.f2.accept(this);
        if (!condType.equals("boolean")) {
            throw new TypeException("Condition at if statement is not boolean");
        }
        n.f4.accept(this);
        return null;
    }

    @Override
    public String visit(IfthenElseStatement n) {
        String condType = n.f2.accept(this);
        if (!condType.equals("boolean")) {
            throw new TypeException("Condition at if-else statement is not boolean");
        }
        n.f4.accept(this);
        n.f6.accept(this);
        return null;
    }

    @Override
    public String visit(WhileStatement n) {
	    String condType = n.f2.accept(this);
        if (!condType.equals("boolean")) {
            throw new TypeException("Condition at while statement is not boolean");
        }
        n.f4.accept(this);
        return null;
    }

    @Override
    public String visit(PrintStatement n) {
        String printType = n.f2.accept(this);
        if (!printType.equals("int")) {
            throw new TypeException("Argument in print statement is not integer");
        }
        return null;
    }

    @Override
    public String visit(Expression n) {
        return n.f0.accept(this);
    }

    @Override
    public String visit(AndExpression n) {
        String exprType1 = n.f0.accept(this);
        String exprType2 = n.f2.accept(this);
        if ((!exprType1.equals("boolean")) || (!exprType2.equals("boolean"))) {
            throw new TypeException("Operands at && operator are not boolean");
        }
        return "boolean";
    }

    @Override
    public String visit(OrExpression n) {
        String exprType1 = n.f0.accept(this);
        String exprType2 = n.f2.accept(this);
        if ((!exprType1.equals("boolean")) || (!exprType2.equals("boolean"))) {
            throw new TypeException("Operands at || operator are not boolean");
        }
        return "boolean";
    }


    @Override
    public String visit(CompareExpression n) {
        String exprType1 = n.f0.accept(this);
        String exprType2 = n.f2.accept(this);
        if ((!exprType1.equals("int")) || (!exprType2.equals("int"))) {
            throw new TypeException("Operands at <= operator are not of valid types");
        }
        return "boolean";
    }

    @Override
    public String visit(neqExpression n) {
        String exprType1 = n.f0.accept(this);
        String exprType2 = n.f2.accept(this);
        if ((!symbolTable.isSubClass(exprType1, exprType2)) && (!symbolTable.isSubClass(exprType2, exprType1))) {
            throw new TypeException("Operands at != operator are not of valid types");
        }
        return "boolean";
    }

    @Override
    public String visit(PlusExpression n) {
        String exprType1 = n.f0.accept(this);
        String exprType2 = n.f2.accept(this);
        if ((!exprType1.equals("int")) || (!exprType2.equals("int"))) {
            throw new TypeException("Operands at + operator are not integer");
        }
        return "int";
    }

    @Override
    public String visit(MinusExpression n) {
        String exprType1 = n.f0.accept(this);
        String exprType2 = n.f2.accept(this);
        if ((!exprType1.equals("int")) || (!exprType2.equals("int"))) {
            throw new TypeException("Operands at - operator are not integer");
        }
        return "int";
    }
 
    @Override
    public String visit(TimesExpression n) {
        String exprType1 = n.f0.accept(this);
        String exprType2 = n.f2.accept(this);
        if ((!exprType1.equals("int")) || (!exprType2.equals("int"))) {
            throw new TypeException("Operands at * operator are not integer");
        }
        return "int";
    }

    @Override
    public String visit(DivExpression n) {
        String exprType1 = n.f0.accept(this);
        String exprType2 = n.f2.accept(this);
        if ((!exprType1.equals("int")) || (!exprType2.equals("int"))) {
            throw new TypeException("Operands at / operator are not integer");
        }
        return "int";
    }

    @Override
    public String visit(ArrayLookup n) {
        String exprType1 = n.f0.accept(this);
        String exprType2 = n.f2.accept(this);
        if ((!exprType1.equals("int[]")) || (!exprType2.equals("int"))) {
            throw new TypeException("Operands at array lookup are not of valid types");
        }
        return "int";
    }

    @Override
    public String visit(ArrayLength n) {
        String varType = n.f0.accept(this);
        if (!varType.equals("int[]")) {
            throw new TypeException("Looking up length of invalid variable type");
        }
        return "int";
    }

    @Override
    public String visit(MessageSend n) {
	    String varType = n.f0.accept(this);
        if (varType.equals("int") || varType.equals("int[]") || varType.equals("boolean")) {
            throw new TypeException("Calling methods on integer/array/boolan");
        }
        String methodName = n.f2.accept(this);

        methodList.push(symbolTable.getMethodParam(varType, methodName));
        n.f4.accept(this);

        if (!methodList.peek().isEmpty()) {
            throw new TypeException("Too few arguments to method call");
        }

        methodList.pop();
        return symbolTable.getMethodType(varType, methodName);

    }

    @Override
    public String visit(ExpressionList n) {
        String exprType = n.f0.accept(this);
        if ((methodList.peek().isEmpty()) || (!symbolTable.isSubClass(exprType, methodList.peek().getFirst()))) {
            throw new TypeException("Argument type mismatch in method call");
        }
        methodList.peek().removeFirst();
        n.f1.accept(this);
        return null;
    }

    @Override
    public String visit(ExpressionRest n) {
        String exprType = n.f1.accept(this);
        if ((methodList.peek().isEmpty()) || (!symbolTable.isSubClass(exprType, methodList.peek().getFirst()))) {
            throw new TypeException("Argument type mismatch in method call");
        }
        methodList.peek().removeFirst();
        return null;
    }

    @Override
    public String visit(PrimaryExpression n) {
        if (n.f0.choice instanceof Identifier) {
            String idName = ((Identifier) n.f0.choice).f0.tokenImage;
            return getType(idName);
        }
        return n.f0.accept(this);
    }

    @Override
    public String visit(IntegerLiteral n) {
        return "int";
    }

    @Override
    public String visit(TrueLiteral n) {
        return "boolean";
    }

    @Override
    public String visit(FalseLiteral n) {
        return "boolean";
    }

    @Override
    public String visit(Identifier n) {
	    return n.f0.accept(this);
    }

    @Override
    public String visit(ThisExpression n) {
        if (currClass == null) {
            throw new TypeException("Using this inside main class");
        }
        return currClass;
    }

    @Override
    public String visit(ArrayAllocationExpression n) {
		String sizeType = n.f3.accept(this);
        if (!sizeType.equals("int")) {
            throw new TypeException("Allocating array with invalid argument type");
        }
        return "int[]";
    }

    @Override
    public String visit(AllocationExpression n) {
        String classType = n.f1.accept(this);
        if (!symbolTable.classExists(classType)) {
            throw new ResolveException("Allocating unknown class type " + classType);
        }
        return classType;
    }

    @Override
    public String visit(NotExpression n) {
        String exprType = n.f1.accept(this);
        if (!exprType.equals("boolean")) {
            throw new TypeException("Operand at ! operator is not boolean");
        }
        return "boolean";
    }

    @Override
    public String visit(BracketExpression n) {
        return n.f1.accept(this);
    }
}
