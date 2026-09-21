package visitor;
import syntaxtree.*;
import java.util.*;

public class SymbolTable {
    public record MethodEntry(String returnType, List<String> paramTypes) {}

    private Map<String, Map<String, MethodEntry>> methodTable = new HashMap<>();
    private Map<String, String> inheritance = new HashMap<>();
    private Map<String, Map<String, String>> memberTable = new HashMap<>();
    private String mainClass;

    public void setMain(String c) {
        mainClass = c;
    }

    private boolean validOverride (MethodEntry a, MethodEntry b) {
        if (!isSubClass(a.returnType, b.returnType)) {
            return false;
        }
        if (!a.paramTypes.equals(b.paramTypes)) {
            return false;
        }
        return true;
    }

    private MethodEntry getInherited(String pName, String mName) {
        while(pName != null) {
            if (methodTable.get(pName).containsKey(mName)) {
                return methodTable.get(pName).get(mName);
            }
            pName = inheritance.get(pName);
        }

        return null;
    }

    public void check() {
        // check if all classes and their parents exist
        for (Map.Entry<String, String> entry : inheritance.entrySet()) {
            if (entry.getValue() != null && !classExists(entry.getValue())) {
                throw new ResolveException("Could not resolve parent class " + entry.getValue() + " of child class " + entry.getKey());
            }
        }

        // checking for cyclic inheritance
        for (String className : inheritance.keySet()) {
            Set<String> visited = new HashSet<>();
            String current = className;

            while (current != null) {
                if (visited.contains(current)) {
                    throw new TypeException("Cyclic inheritance involving class " + className);
                }

                visited.add(current);
                current = inheritance.get(current);
            }
        }

        // redeclaration of method declared already in parent class
        for (Map.Entry<String, Map<String, MethodEntry>> entry : methodTable.entrySet()) {
            String parentClass = inheritance.get(entry.getKey());
            if (parentClass != null) {
                for (String methodName : entry.getValue().keySet()) {
                    MethodEntry parentMethod = getInherited(parentClass, methodName);
                    if (parentMethod != null && !validOverride(entry.getValue().get(methodName), parentMethod)) {
                        throw new TypeException("Invalid overriding of function " + methodName);
                    }
                }
            }
        }

        // checking that types of class variables exist
        for (Map<String, String> member : memberTable.values()) {
            for (String type : member.values()) {
                if (!classExists(type)) {
                    throw new ResolveException("Could not resolve type " + type);
                }
            }
        }

        // checking return types of all methods exist
        for (Map.Entry<String, Map<String, MethodEntry>> methodEntry : methodTable.entrySet()) {
            if (methodEntry.getKey().equals(mainClass)) {
                continue;
            }
            Map<String, MethodEntry> entry = methodEntry.getValue();
            for (Map.Entry<String, MethodEntry> me : entry.entrySet()) {
                if (!classExists(me.getValue().returnType)) {
                    throw new ResolveException("Could not resolve return type " + me.getValue().returnType);
                }
            }
        }
    }

    public SymbolTable() {
        inheritance.put("int", null);
        methodTable.put("int", new HashMap<>());
        memberTable.put("int", new HashMap<>());
        inheritance.put("boolean", null);
        methodTable.put("boolean", new HashMap<>());
        memberTable.put("boolean", new HashMap<>());
        inheritance.put("int[]", null);
        methodTable.put("int[]", new HashMap<>());
        memberTable.put("int[]", new HashMap<>());
    }

    public boolean classExists(String className) {
        return inheritance.containsKey(className);
    }

    public boolean isSubClass(String className, String parentName) {
        while(className != null) {
            if (className.equals(parentName)) {
                return true;
            }
            className = inheritance.get(className);
        }
        return false;
    }

    public String getVarType(String className, String varName) {
        while(className != null) {
            if (memberTable.get(className).containsKey(varName)) return memberTable.get(className).get(varName);
            className = inheritance.get(className);
        }
        throw new ResolveException("Variable " + varName + " could not be resolved!");
    }

    public String getMethodType(String className, String methodName) {
        while(className != null) {
            if (methodTable.get(className).containsKey(methodName)) return methodTable.get(className).get(methodName).returnType;
            className = inheritance.get(className);
        }
        throw new ResolveException("Method " + methodName + " could not be resolved!");
    }

    public List<String> getMethodParam(String className, String methodName) {
        while(className != null) {
            if (methodTable.get(className).containsKey(methodName)) return new ArrayList<>(methodTable.get(className).get(methodName).paramTypes);
            className = inheritance.get(className);
        }
        throw new ResolveException("Method " + methodName + " could not be resolved!");
    }

    public void insertClass(String className, String parentName) {
        if (classExists(className)) {
            throw new TypeException("Class " + className + " already exists!");
        }
        inheritance.put(className, parentName);
        methodTable.put(className, new HashMap<>());
        memberTable.put(className, new HashMap<>());
    }

    public void insertMethod(String className, String methodName, String returnType) {
        if (methodTable.get(className).containsKey(methodName)) {
            throw new TypeException("Method " + methodName + " already exists within class " + className + "!");
        }
        methodTable.get(className).put(methodName, new MethodEntry(returnType, new ArrayList<>()));
    }

    public void addParam(String className, String methodName, String paramType) {
        methodTable.get(className).get(methodName).paramTypes.add(paramType);
    }

    private boolean memberExists(String className, String memberName) {
        return memberTable.get(className).containsKey(memberName);
    }

    public void addMember(String className, String varName, String varType) {
        if (memberExists(className, varName)) {
            throw new TypeException("Member " + varName + " already exists within class " + className + "!");
        }
        memberTable.get(className).put(varName, varType);
    }
}
