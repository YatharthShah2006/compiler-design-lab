package visitor;

import java.util.*;
import syntaxtree.*;

class StatementBlock {
  public Stmt statement;
  public Set<Integer> in;
  public Set<Integer> out;
  public Set<String> out_labels;
  public Set<String> used;
  public Set<String> defs;
  public Set<String> live_in;
  public Set<String> live_out;
  public SimpleExp return_exp;

  public StatementBlock() {
    statement = null;
    return_exp = null;
    in = new HashSet<>();
    out = new HashSet<>();
    out_labels = new HashSet<>();
    used = new HashSet<>();
    defs = new HashSet<>();
    live_in = new HashSet<>();
    live_out = new HashSet<>();
  }
}

class ProcInfo {
    public Map<Integer, StatementBlock> cfg;
    public int counter;
    public Map<String, String> labelMap;
    public Map<String, String> regs;
    public int maxArgs;
    public int stackSpace;
    public boolean spilled;
    public int numArgs;

    public ProcInfo() {
        cfg = new HashMap<>();
        labelMap = new HashMap<>();
        regs = new HashMap<>();
    }
}

public class Analysis extends GJDepthFirst<String, String> {
    public Map<String, ProcInfo> procInfo;
    private int labelCounter;
    public record IntPair(int first, int second) {};
    private Map<String, IntPair> tempIntervals;
    private Set<String> procNames;
    private String procName;
    private Map<String, Integer> labels;

    public Analysis() {
        procInfo = new HashMap<>();
        procNames = new HashSet<>();
        labels = new HashMap<>();
        tempIntervals = new HashMap<>();
    }

    public String visit(NodeListOptional n, String argu) {
        if ( n.present() ) {
            String _ret=null;
            int _count=0;
            for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
                if (argu.equals("call")) {
                    procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(e.nextElement().accept(this,argu));
                    if (_count + 1 > procInfo.get(procName).maxArgs) procInfo.get(procName).maxArgs = _count + 1;
                }
                else e.nextElement().accept(this,argu);
                _count++;
            }
            return _ret;
        }
        else
            return null;
   }

    
   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
    public String visit(Procedure n, String argu) {
        if (argu.equals("addNames")) {
            procNames.add(n.f0.f0.tokenImage);
            return null;
        }
        labels.clear();
        tempIntervals.clear();
        procName = n.f0.accept(this, "");
        procInfo.put(procName, new ProcInfo());
        procInfo.get(procName).numArgs = Integer.valueOf(n.f2.accept(this, argu));
        if (procInfo.get(procName).numArgs > 4) procInfo.get(procName).stackSpace += procInfo.get(procName).numArgs - 4;
        n.f4.accept(this, argu);
        completeGraph();
        computeLiveliness();
        allocateRegisters();
        procInfo.get(procName).stackSpace += 18;
        return null;
    }

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
    public String visit(Goal n, String argu) {
        procName = n.f0.accept(this, "");
        procInfo.put(procName, new ProcInfo());
        labelCounter = 0;
        procInfo.get(procName).counter = 0;
        procInfo.get(procName).maxArgs = 0;
        procInfo.get(procName).numArgs = 0;
        procInfo.get(procName).spilled = false;
        procInfo.get(procName).stackSpace = 0;
        n.f3.accept(this, "addNames");
        n.f1.accept(this, argu);
        procInfo.get(procName).cfg.put(procInfo.get(procName).counter, new StatementBlock());
        procInfo.get(procName).counter++;
        completeGraph();
        computeLiveliness();
        allocateRegisters();
        procInfo.get(procName).stackSpace += 18;
        n.f3.accept(this, argu);
        return null;
    }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public String visit(StmtList n, String argu) {
      n.f0.accept(this, "Stmt");
      return null;
   }

   /**
    * f0 -> NoOpStmt()
    *       | ErrorStmt()
    *       | CJumpStmt()
    *       | JumpStmt()
    *       | HStoreStmt()
    *       | HLoadStmt()
    *       | MoveStmt()
    *       | PrintStmt()
    */
    public String visit(Stmt n, String argu) {
        procInfo.get(procName).cfg.put(procInfo.get(procName).counter, new StatementBlock());
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).statement = n;
        n.f0.accept(this, argu);
        procInfo.get(procName).counter++;
        return null;
    }

   /**
    * f0 -> "NOOP"
    */
    public String visit(NoOpStmt n, String argu) {
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).out.add(procInfo.get(procName).counter + 1);
        return null;
    }

   /**
    * f0 -> "ERROR"
    */
    public String visit(ErrorStmt n, String argu) {
        return null;
    }

   /**
    * f0 -> "CJUMP"
    * f1 -> Temp()
    * f2 -> Label()
    */
    public String visit(CJumpStmt n, String argu) {
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(n.f1.accept(this, argu));
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).out.add(procInfo.get(procName).counter + 1);
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).out_labels.add(n.f2.accept(this, ""));
        return null;
    }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
    public String visit(JumpStmt n, String argu) {
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).out_labels.add(n.f1.accept(this, ""));
        return null;
    }

   /**
    * f0 -> "HSTORE"
    * f1 -> Temp()
    * f2 -> IntegerLiteral()
    * f3 -> Temp()
    */
    public String visit(HStoreStmt n, String argu) {
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(n.f1.accept(this, argu));
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(n.f3.accept(this, argu));
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).out.add(procInfo.get(procName).counter + 1);
        return null;
    }

   /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Temp()
    * f3 -> IntegerLiteral()
    */
    public String visit(HLoadStmt n, String argu) {
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).defs.add(n.f1.accept(this, argu));
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(n.f2.accept(this, argu));
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).out.add(procInfo.get(procName).counter + 1);
        return null;
    }

   /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
    public String visit(MoveStmt n, String argu) {
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).defs.add(n.f1.accept(this, argu));
        String e = n.f2.accept(this, argu);
        if (e != null && e.startsWith("TEMP ")) {
            procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(e);
        }
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).out.add(procInfo.get(procName).counter + 1);
        return null;
    }

   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
    public String visit(PrintStmt n, String argu) {
        String operand2 = n.f1.accept(this, argu);
        if (operand2.startsWith("TEMP ")) {
            procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(operand2);
        }
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).out.add(procInfo.get(procName).counter + 1);
        return null;
    }

   /**
    * f0 -> Call()
    *       | HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    */
    public String visit(Exp n, String argu) {
        return n.f0.accept(this, argu);
    }

   /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> SimpleExp()
    * f4 -> "END"
    */
    public String visit(StmtExp n, String argu) {
        n.f1.accept(this, argu);
        procInfo.get(procName).cfg.put(procInfo.get(procName).counter, new StatementBlock());
        String operand2 = n.f3.accept(this, argu);
        if (operand2.startsWith("TEMP ")) {
            procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(operand2);
        }
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).return_exp = n.f3;
        procInfo.get(procName).counter++;
        return null;
    }

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    * f2 -> "("
    * f3 -> ( Temp() )*
    * f4 -> ")"
    */
    public String visit(Call n, String argu) {
        String operand2 = n.f1.accept(this, argu);
        if (operand2.startsWith("TEMP ")) {
            procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(operand2);
        }
        n.f3.accept(this, "call");
        return null;
    }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
    public String visit(HAllocate n, String argu) {
        String operand2 = n.f1.accept(this, argu);
        if (operand2.startsWith("TEMP ")) {
            procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(operand2);
        }
        return null;
    }

   /**
    * f0 -> Operator()
    * f1 -> Temp()
    * f2 -> SimpleExp()
    */
    public String visit(BinOp n, String argu) {
        procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(n.f1.accept(this, argu));
        String operand2 = n.f2.accept(this, argu);
        if (operand2.startsWith("TEMP ")) {
            procInfo.get(procName).cfg.get(procInfo.get(procName).counter).used.add(operand2);
        }
        return null;
    }

   /**
    * f0 -> "LE"
    *       | "NE"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    *       | "DIV"
    */
    public String visit(Operator n, String argu) {
        return n.f0.accept(this, argu);
    }

   /**
    * f0 -> Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
    public String visit(SimpleExp n, String argu) {
        return n.f0.accept(this, "");
    }

   /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
    public String visit(Temp n, String argu) {
        return "TEMP " + n.f1.accept(this, argu);
    }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n, String argu) {
        return n.f0.accept(this, argu);
    }

   /**
    * f0 -> <IDENTIFIER>
    */
    public String visit(Label n, String argu) {
        String lname = n.f0.tokenImage;
        if (!procNames.contains(lname)) {
            if (!procInfo.get(procName).labelMap.containsKey(lname)) {
                procInfo.get(procName).labelMap.put(lname, "YatharthLabel" + labelCounter++);
            }
            lname = procInfo.get(procName).labelMap.get(lname);
        }
        if (argu.equals("Stmt")) {  
            labels.put(lname, procInfo.get(procName).counter);
        }
        return lname;
    }

    private void completeGraph() {
        for (int i = 0; i < procInfo.get(procName).counter; i++) {
            for (String x : procInfo.get(procName).cfg.get(i).out_labels) {
                procInfo.get(procName).cfg.get(i).out.add(labels.get(x));
            }
            for (Integer x : procInfo.get(procName).cfg.get(i).out) {
                procInfo.get(procName).cfg.get(x).in.add(i);
            }
        }
    }

    private void computeLiveliness() {
        boolean flag = true;
        while(flag) {
            flag = false;
            for (int i = 0; i < procInfo.get(procName).counter; i++) {
                Set<String> new_out = new HashSet<>();
                for (int x : procInfo.get(procName).cfg.get(i).out) {
                    new_out.addAll(procInfo.get(procName).cfg.get(x).live_in);
                }
                Set<String> new_in = new HashSet<>(new_out);
                new_in.removeAll(procInfo.get(procName).cfg.get(i).defs);
                new_in.addAll(procInfo.get(procName).cfg.get(i).used);
                if (!new_in.equals(procInfo.get(procName).cfg.get(i).live_in) || !new_out.equals(procInfo.get(procName).cfg.get(i).live_out)) {
                    procInfo.get(procName).cfg.get(i).live_in = new_in;
                    procInfo.get(procName).cfg.get(i).live_out = new_out;
                    flag = true;
                }
            }
        }

        for (int i = 0; i < procInfo.get(procName).counter; i++) {
            Set<String> used_here = new HashSet<>(procInfo.get(procName).cfg.get(i).live_in);
            used_here.addAll(procInfo.get(procName).cfg.get(i).live_out);
            used_here.addAll(procInfo.get(procName).cfg.get(i).defs);
            for (String t : used_here) {
                if (tempIntervals.containsKey(t)) {
                    tempIntervals.put(t, new IntPair(tempIntervals.get(t).first, i));
                }
                else {
                   tempIntervals.put(t, new IntPair(i, i));
                }
            }
        }
        for (int i = 0; i < procInfo.get(procName).numArgs; i++) {
            String t = "TEMP " + i;
            if (tempIntervals.containsKey(t)) tempIntervals.put(t, new IntPair(0, tempIntervals.get(t).second));
        }
    }

    private void allocateRegisters() {
        List<String> temps = new ArrayList<>(tempIntervals.keySet());
        for (int i = 0; i < temps.size(); i++) {
            for (int j = i + 1; j < temps.size(); j++) {
                if (isGreater(temps.get(i), temps.get(j))) {
                    String t = temps.get(i);
                    temps.set(i, temps.get(j));
                    temps.set(j, t);
                }
            }
        }

        Set<String> freeRegs = new HashSet<>();
        freeRegs.add("t0");
        freeRegs.add("t1");
        freeRegs.add("t2");
        freeRegs.add("t3");
        freeRegs.add("t4");
        freeRegs.add("t5");
        freeRegs.add("t6");
        freeRegs.add("t7");
        freeRegs.add("t8");
        freeRegs.add("t9");
        freeRegs.add("s0");
        freeRegs.add("s1");
        freeRegs.add("s2");
        freeRegs.add("s3");
        freeRegs.add("s4");
        freeRegs.add("s5");
        freeRegs.add("s6");
        freeRegs.add("s7");

        Map<String, Integer> active = new HashMap<>();
        int spillcount = procInfo.get(procName).stackSpace;

        for (int i = 0; i < temps.size(); i++) {
            int currentStart = tempIntervals.get(temps.get(i)).first();

            Iterator<Map.Entry<String, Integer>> it = active.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = it.next();

                if (entry.getValue() < currentStart) {
                    freeRegs.add(entry.getKey());
                    it.remove();
                }
            }

            if (freeRegs.isEmpty()) {
                procInfo.get(procName).regs.put(temps.get(i), "SPILLEDARG " + spillcount);
                procInfo.get(procName).spilled = true;
                spillcount++;
            }
            else {
                for (String x : freeRegs) {
                    procInfo.get(procName).regs.put(temps.get(i), x);
                    freeRegs.remove(x);
                    active.put(x, tempIntervals.get(temps.get(i)).second());
                    break;
                }
            }
        }

        procInfo.get(procName).stackSpace = spillcount;
    }

    private boolean isGreater(String t1, String t2) {
        if (tempIntervals.get(t1).first() == tempIntervals.get(t2).first()) {
            return tempIntervals.get(t1).second() > tempIntervals.get(t2).second();
        }
        else return tempIntervals.get(t1).first() > tempIntervals.get(t2).first();
    }
}
