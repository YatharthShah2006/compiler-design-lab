package visitor;
import syntaxtree.*;
import java.util.*;

public class CodeGenerator extends GJNoArguDepthFirst<String>{
    
    private SymbolTable symbolTable;
    private Map<String, Integer> Args = new HashMap<>();
    private Map<String, String> ArgTypes = new HashMap<>();
    private String currClass;
    int temp;
    int label;
    TypeVisitor tv;

    public CodeGenerator(SymbolTable st) {
        label = 0;
        tv = new TypeVisitor();
        this.symbolTable = st;
        symbolTable.complete();
    }

    @Override 
    public String visit(NodeToken n) {
        return n.tokenImage;
    }

    @Override
    public String visit(MainClass n) {
        currClass = n.f1.accept(this);
        System.out.println("MAIN");
        Args.put("this", 0);
        Args.put(n.f11.accept(this), 1);
        temp = 2;
        n.f14.accept(this);
        Args.clear();
        ArgTypes.clear();
        System.out.println("END");
        return null;
    }

    @Override
    public String visit(ClassDeclaration n) {
        currClass = n.f1.accept(this);
        n.f4.accept(this);
        return null;
    }

    @Override
    public String visit(ClassExtendsDeclaration n){
        currClass = n.f1.accept(this);
        n.f6.accept(this);
        return null;
    }

    @Override
    public String visit(VarDeclaration n) {
        String varName = n.f1.accept(this);
        Args.put(varName, Args.size());
        ArgTypes.put(varName, n.f0.accept(this));
        return null;
    }

    @Override
    public String visit(MethodDeclaration n) {
        String methodName = n.f2.accept(this);
        Args.put("this", 0);
        ArgTypes.put("this", currClass);
        n.f4.accept(this);
        String newName = symbolTable.getName(currClass, methodName);
        System.out.println(newName + " [" + Args.size() + "] BEGIN");
        n.f7.accept(this);
        temp = Args.size();
        n.f8.accept(this);
        System.out.println("RETURN ");
        System.out.println(n.f10.accept(this));
        System.out.println("END");
        Args.clear();
        ArgTypes.clear();
        return null;
    }

    @Override
    public String visit(FormalParameter n) {
        String varName = n.f1.accept(this);
        Args.put(varName, Args.size());
        ArgTypes.put(varName, n.f0.accept(this));
        return null;
    }

    @Override
    public String visit(Type n) {
        return n.f0.accept(this);
    }

    @Override
    public String visit(ArrayType n) {
	    return "int[]";
    }

    @Override
    public String visit(BooleanType n) {
	    return "boolean";
    }

    @Override
    public String visit(IntegerType n) {
		return "int";
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
        String dest = n.f0.accept(this);
        String expr = n.f2.accept(this);
        if (Args.containsKey(dest)) {
            System.out.println("MOVE TEMP " + Args.get(dest) + " " + expr);
        }
        else {
            System.out.println("HSTORE TEMP 0 " + 4*symbolTable.getOffset(currClass, dest) + " " + expr);;
        }
        return null;
    }

    @Override
    public String visit(ArrayAssignmentStatement n) {
		String dest = n.f0.accept(this);
        String offset = n.f2.accept(this);
        String expr = n.f5.accept(this);
        String addrTmp = "TEMP " + temp++;
        String offsetTmp = "TEMP " + temp++;
        String sizeTmp = "TEMP " + temp++;
        String thenLabel = "Label" + label++;
        if (Args.containsKey(dest)) {
            System.out.println("MOVE " + addrTmp + " TEMP " + Args.get(dest));
        }
        else {
            System.out.println("HLOAD " + addrTmp + " TEMP 0 " + 4 * symbolTable.getOffset(currClass, dest));
        }
        System.out.println("MOVE " + offsetTmp + " " + offset);
        System.out.println("HLOAD " + sizeTmp + " " + addrTmp + " 0");
        System.out.println("CJUMP MINUS 1 TIMES LE " + offsetTmp + " MINUS " + sizeTmp + " 1 LE 0 " + offsetTmp + " " + thenLabel);
        System.out.println("ERROR");
        System.out.println(thenLabel + " NOOP");
        System.out.println("HSTORE PLUS " + addrTmp + " PLUS 4 TIMES 4 " + offsetTmp + " 0 " + expr);
        return null;
    }

    @Override
    public String visit(IfthenStatement n) {
        String cond = n.f2.accept(this);
        String endlabel = "Label" + String.valueOf(label++);
        System.out.println("CJUMP " + cond + " " + endlabel);
        n.f4.accept(this);
        System.out.println(endlabel + " NOOP");
        return null;
    }

    @Override
    public String visit(IfthenElseStatement n) {
        String cond = n.f2.accept(this);
        String thenlabel = "Label" + String.valueOf(label++);
        String endlabel = "Label" + String.valueOf(label++);
        System.out.println("CJUMP " + cond + " " + thenlabel);
        n.f4.accept(this);
        System.out.println("JUMP " + endlabel);
        System.out.println(thenlabel + " NOOP");
        n.f6.accept(this);
        System.out.println(endlabel + " NOOP");
        return null;
    }

    @Override
    public String visit(WhileStatement n) {
	    String cond = n.f2.accept(this);
        String endlabel = "Label" + String.valueOf(label++);
        String startlabel = "Label" + String.valueOf(label++);
        System.out.println(startlabel + " CJUMP " + cond + " " + endlabel);
        n.f4.accept(this);
        System.out.println("JUMP " + startlabel);
        System.out.println(endlabel + " NOOP");
        return null;
    }

    @Override
    public String visit(PrintStatement n) {
        String expr = n.f2.accept(this);
        System.out.println("PRINT " + expr);
        return null;
    }

    @Override
    public String visit(Expression n) {
        return n.f0.accept(this);
    }

    @Override
    public String visit(AndExpression n) {
        String expr1 = n.f0.accept(this);
        String expr2 = n.f2.accept(this);
        String ans = "TEMP " + String.valueOf(temp++);
        String yesLbl = "Label" + String.valueOf(label++);
        String endLbl = "Label" + String.valueOf(label++);
        String result = "BEGIN\n";
        result += "CJUMP " + expr1 + " " + yesLbl + "\n";
        result += "MOVE " + ans + " " + expr2 + "\n";
        result += "JUMP " + endLbl + "\n";
        result += yesLbl + " MOVE " + ans + " 0\n";
        result += endLbl + " NOOP\n";
        result += "RETURN " + ans + "\n";
        result += "END\n";
        return result;
    }

    @Override
    public String visit(OrExpression n) {
        String expr1 = n.f0.accept(this);
        String expr2 = n.f2.accept(this);
        String ans = "TEMP " + String.valueOf(temp++);
        String yesLbl = "Label" + String.valueOf(label++);
        String endLbl = "Label" + String.valueOf(label++);
        String result = "BEGIN\n";
        result += "CJUMP " + expr1 + " " + yesLbl + "\n";
        result += "MOVE " + ans + " 1\n";
        result += "JUMP " + endLbl + "\n";
        result += yesLbl + " MOVE " + ans + " " + expr2 + "\n";
        result += endLbl + " NOOP\n";
        result += "RETURN " + ans + "\n";
        result += "END\n";
        return result;
    }

    @Override
    public String visit(CompareExpression n) {
        String expr1 = n.f0.accept(this);
        String expr2 = n.f2.accept(this);
        return "LE " + expr1 + " " + expr2;
    }

    @Override
    public String visit(neqExpression n) {
        String expr1 = n.f0.accept(this);
        String expr2 = n.f2.accept(this);
        return "NE " + expr1 + " " + expr2;
    }

    @Override
    public String visit(PlusExpression n) {
        String expr1 = n.f0.accept(this);
        String expr2 = n.f2.accept(this);
        return "PLUS " + expr1 + " " + expr2;
    }

    @Override
    public String visit(MinusExpression n) {
        String expr1 = n.f0.accept(this);
        String expr2 = n.f2.accept(this);
        return "MINUS " + expr1 + " " + expr2;
    }
 
    @Override
    public String visit(TimesExpression n) {
        String expr1 = n.f0.accept(this);
        String expr2 = n.f2.accept(this);
        return "TIMES " + expr1 + " " + expr2;
    }

    @Override
    public String visit(DivExpression n) {
        String expr1 = n.f0.accept(this);
        String expr2 = n.f2.accept(this);
        return "DIV " + expr1 + " " + expr2;
    }
    
    @Override
    public String visit(ArrayLookup n) {
        String expr1 = n.f0.accept(this);
        String offset = n.f2.accept(this);
        String dest = "TEMP " + temp++;
        String offsetTmp = "TEMP " + temp++;
        String sizeTmp = "TEMP " + temp++;
        String addrTmp = "TEMP " + temp++;
        String thenLabel = "Label" + label++;
        String result = "BEGIN\n";
        result += "MOVE " + addrTmp + " " + expr1 + "\n";
        result += "MOVE " + offsetTmp + " " + offset + "\n";
        result += "HLOAD " + sizeTmp + " " + addrTmp + " 0\n";
        result += "CJUMP MINUS 1 TIMES LE " + offsetTmp + " MINUS " + sizeTmp + " 1 LE 0 " + offsetTmp + " " + thenLabel + "\n";
        result += "ERROR\n";
        result += thenLabel + " NOOP\n";
        result += "HLOAD " + dest + " PLUS PLUS 4 TIMES 4 " + offsetTmp + " " + addrTmp + " 0\nRETURN " + dest + "\nEND\n";
        return result;
    }

    @Override
    public String visit(ArrayLength n) {
        String expr1 = n.f0.accept(this);
        String dest = "TEMP " + temp++;
        String result = "BEGIN\n" + "HLOAD " + dest + " " + expr1 + " 0\nRETURN " + dest + "\nEND\n";
        return result;
    }

    @Override
    public String visit(MessageSend n) {
        
	    String var = n.f0.accept(this);
        String methodName = n.f2.accept(this);

        String receiverType = getType(n.f0);
        int offset = symbolTable.classData.get(receiverType).vtable.get(methodName);

        String result = "CALL\n";
        result += "BEGIN\n";
        String obj = "TEMP " + temp++;
        result += "MOVE " + obj + " " + var + "\n";
        String vtbl = "TEMP " + temp++;
        String fn = "TEMP " + temp++;
        result += "HLOAD " + vtbl + " " + obj + " 0\n";
        result += "HLOAD " + fn + " " + vtbl + " " + offset*4 + "\n";
        result += "RETURN " + fn + "\n";
        result += "END\n";

        result += "(" + obj + " ";
        String r = n.f4.accept(this);
        if (r != null) result += r;
        result += ")\n";
        return result;
    }

    @Override
    public String visit(ExpressionList n) {
       String result = n.f0.accept(this);
       for (Enumeration<Node> e = n.f1.elements(); e.hasMoreElements();)
           result += " " + e.nextElement().accept(this);
       return result;
    }

    @Override 
    public String visit(ExpressionRest n) {
        return n.f1.accept(this);
    }

    @Override
    public String visit(PrimaryExpression n) {
        if (n.f0.choice instanceof Identifier) {
            String varName = n.f0.accept(this);
            if (Args.containsKey(varName)) {
                return "TEMP " + Args.get(varName);
            }
            else {
                String result = "BEGIN\n";
                int tmp = temp++;
                result += "HLOAD TEMP " + tmp + " TEMP 0 " + 4*symbolTable.getOffset(currClass, varName) + "\n";
                result += "RETURN TEMP " + tmp + "\n";
                result += "END\n";
                return result;
            }
        }
        else return n.f0.accept(this);
    }

    @Override
    public String visit(IntegerLiteral n) {
        return n.f0.accept(this);
    }

    @Override
    public String visit(TrueLiteral n) {
        return "1";
    }

    @Override
    public String visit(FalseLiteral n) {
        return "0";
    }

    @Override
    public String visit(Identifier n) {
	    return n.f0.accept(this);
    }

    @Override
    public String visit(ThisExpression n) {
        return "TEMP 0";
    }

    @Override
    public String visit(ArrayAllocationExpression n) {
		String size = n.f3.accept(this);
        String len = "TEMP " + String.valueOf(temp++);
        String dest = "TEMP " + String.valueOf(temp++);
        String tmp = "TEMP " + String.valueOf(temp++);
        String startLabel = "Label" + String.valueOf(label++);
        String endLabel = "Label" + String.valueOf(label++);
        String result = "BEGIN\n";
        result += "MOVE " + len + " " + size + "\n";
        result += "MOVE " + dest + " HALLOCATE PLUS 4 TIMES 4 " + len + "\n";
        result += "HSTORE " + dest + " 0 " + len + "\n";
        result += "MOVE " + tmp + " 1\n";
        result += startLabel + " CJUMP LE " + tmp + " " + len + " " + endLabel + "\n";
        result += "HSTORE PLUS " + dest + " TIMES 4 " + tmp + " 0 0\n";
        result += "MOVE " + tmp + " PLUS " + tmp + " 1\n";
        result += "JUMP " + startLabel + "\n";
        result += endLabel + " NOOP\n";
        result += "RETURN " + dest + "\n";
        result += "END\n";
        return result;
    }

    @Override
    public String visit(AllocationExpression n) {
        String classType = n.f1.accept(this);
        int size = symbolTable.classData.get(classType).members.size() * 4 + 4;
        int numMethods = symbolTable.classData.get(classType).methods.size();
        String dest = "TEMP " + String.valueOf(temp++);
        String vtbl = "TEMP " + String.valueOf(temp++);
        String result = "BEGIN\n";
        result += "MOVE " + dest + " HALLOCATE " + size + "\n";
        if (numMethods == 0) {
            result += "MOVE " + vtbl + " HALLOCATE 4\n";
        }
        else {
            result += "MOVE " + vtbl + " HALLOCATE " + 4*numMethods + "\n";
        }
        for (String mName : symbolTable.classData.get(classType).methods.keySet()) {
            result += "HSTORE " + vtbl + " " + 4*symbolTable.classData.get(classType).vtable.get(mName) + " " + symbolTable.classData.get(classType).methods.get(mName) + "\n";
        }
        for (int i = 4; i < size; i += 4) {
            result += "HSTORE " + dest + " " + i + " 0\n";
        }
        result += "HSTORE " + dest + " 0 " + vtbl + "\n";
        result += "RETURN " + dest + "\n";
        result += "END\n";
        return result;
    }

    @Override
    public String visit(NotExpression n) {
        String expr = n.f1.accept(this);
        return "MINUS 1 " + expr;
    }
    
    @Override
    public String visit(PostfixExpression n) {
        return n.f0.accept(this);
    }

    @Override
    public String visit(UnaryExpression n) {
        return n.f0.accept(this);
    }

    @Override
    public String visit(BracketExpression n) {
        return n.f1.accept(this);
    }

    private String getType(PrimaryExpression n) {
        return n.accept(tv);
    }

    private class TypeVisitor extends GJNoArguDepthFirst<String> {
        @Override 
        public String visit(NodeToken n) {
            return n.tokenImage;
        }

        @Override
        public String visit(Expression n) {
            return n.f0.accept(this);
        }

        @Override
        public String visit(PostfixExpression n) {
            return n.f0.accept(this);
        }

        @Override
        public String visit(AndExpression n) {
            return "boolean";
        }

        @Override
        public String visit(OrExpression n) {
            return "boolean";
        }


        @Override
        public String visit(CompareExpression n) {
            return "boolean";
        }

        @Override
        public String visit(neqExpression n) {
            return "boolean";
        }

        @Override
        public String visit(PlusExpression n) {
            return "int";
        }

        @Override
        public String visit(MinusExpression n) {
            return "int";
        }
    
        @Override
        public String visit(TimesExpression n) {
            return "int";
        }

        @Override
        public String visit(DivExpression n) {
            return "int";
        }
        
        @Override
        public String visit(ArrayLookup n) {
            return "int";
        }

        @Override
        public String visit(ArrayLength n) {
            return "int";
        }

        @Override
        public String visit(MessageSend n) {
            String var = getType(n.f0);
            String methodName = n.f2.accept(this);
            return symbolTable.classData.get(var).methodTypes.get(methodName);
        }

        @Override
        public String visit(PrimaryExpression n) {
            if (n.f0.choice instanceof Identifier) {
                String vName = n.f0.accept(this);
                if (Args.containsKey(vName)) return ArgTypes.get(vName);
                else return symbolTable.classData.get(currClass).fieldTypes.get(vName);
            }
            else return n.f0.accept(this);
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
            return currClass;
        }

        @Override
        public String visit(ArrayAllocationExpression n) {
            return "int[]";
        }

        @Override
        public String visit(AllocationExpression n) {
            return n.f1.accept(this);
        }

        @Override
        public String visit(NotExpression n) {
            return "boolean";
        }

        @Override
        public String visit(BracketExpression n) {
            return n.f1.accept(this);
        }

        @Override
        public String visit(UnaryExpression n) {
            return n.f0.accept(this);
        }
    };
}
