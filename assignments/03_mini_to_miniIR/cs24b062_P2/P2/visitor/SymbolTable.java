package visitor;

import java.util.*;

class ClassEntry {
    String newName;
    String parentName;
    Map<String, String> methods;
    List<String> members;
    Map<String, Integer> vtable;
    Map<String, String> fieldTypes;
    Map<String, String> methodTypes;

    public ClassEntry(String pName, int num) {
        parentName = pName;
        newName = "class" + num;
        methods = new HashMap<>();
        members = new ArrayList<>();
        vtable = new HashMap<>();
        fieldTypes = new HashMap<>();
        methodTypes = new HashMap<>();
    }
}

public class SymbolTable {
    private Queue<String> ancestorClasses;
    public Map<String, ClassEntry> classData;
    private Map<String, List<String>> children;

    public SymbolTable() {
        classData = new HashMap<>();
        ancestorClasses = new LinkedList<>();
        children = new HashMap<>();
    }

    public void complete() {
        while(!ancestorClasses.isEmpty()) {
            String currClass = ancestorClasses.poll();
            String parent = classData.get(currClass).parentName;

            if (parent == null) {
                for (String x : classData.get(currClass).methods.keySet()) {
                    classData.get(currClass).vtable.put(x, classData.get(currClass).vtable.size());
                }
            }
            else {
                for (String x : classData.get(parent).members.reversed()) {
                    classData.get(currClass).members.add(0, x);
                    if (!classData.get(currClass).fieldTypes.containsKey(x)) {
                        classData.get(currClass).fieldTypes.put(x, classData.get(parent).fieldTypes.get(x));
                    }
                }

                for (String x : classData.get(parent).vtable.keySet()) {
                    if (classData.get(currClass).methods.containsKey(x)) {
                        classData.get(currClass).vtable.put(x, classData.get(parent).vtable.get(x));           // in parent, overridden by child
                    }
                    else {
                        classData.get(currClass).vtable.put(x, classData.get(parent).vtable.get(x));           // in parent, dne in child
                        classData.get(currClass).methods.put(x, classData.get(parent).methods.get(x));
                        classData.get(currClass).methodTypes.put(x, classData.get(parent).methodTypes.get(x));
                    }
                }
                for (String x : classData.get(currClass).methods.keySet()) {
                    if (!classData.get(parent).methods.containsKey(x)) {
                        classData.get(currClass).vtable.put(x, classData.get(currClass).vtable.size());           // in child, not in parent
                    }
                }
            }

            for (String x : children.getOrDefault(currClass, new ArrayList<>())) {
                ancestorClasses.add(x);
            }
        }
    }

    public void insertClass(String className, String parentName) {
        if (parentName == null) {
            ancestorClasses.add(className);
        }
        else {
            if (children.containsKey(parentName)) {
                children.get(parentName).add(className);
            }
            else {
                children.put(parentName, new ArrayList<>());
                children.get(parentName).add(className);
            }
        }

        int temp = classData.size();
        classData.put(className, new ClassEntry(parentName, temp));
    }

    public void insertMethod(String className, String methodName, String retType) {
        int temp = classData.get(className).methods.size();
        classData.get(className).methods.put(methodName, classData.get(className).newName + "_method" + temp);
        classData.get(className).methodTypes.put(methodName, retType);
    }

    public void insertMember(String className, String varName, String type) {
        classData.get(className).members.add(varName);
        classData.get(className).fieldTypes.put(varName, type);
    }

    public String getName(String cName, String mName) {
        return classData.get(cName).methods.get(mName);
    }

    public int getOffset(String cName, String vName) {
        int i = classData.get(cName).members.size() - 1;
        while(i >= 0) {
            if (classData.get(cName).members.get(i).equals(vName)) {
                return i + 1;
            }
            i--;
        }
        return -1;
    }
}