package swing;

import edu.wis.jtlv.env.Env;

public class Statistic {
    Runtime runtime;
    long modelBDD,modelVar;
    long beginTime,beginMemory,beginBDD,beginVar;

    public Statistic() {
        runtime = Runtime.getRuntime();
        modelBDD = Env.TRUE().getFactory().getNodeNum();
        modelVar = Env.TRUE().getFactory().varNum();
    }


    public void startTimeMemory() {
        beginTime = System.currentTimeMillis();
        runtime.gc();
        beginMemory =runtime.freeMemory();// 开始时的剩余内存
    }
    public void startBDDVar() {// BDD and Var of building model
        beginBDD=Env.TRUE().getFactory().getNodeNum();
        beginVar=Env.TRUE().getFactory().varNum();
    }


    public String endTime() {
        return "execution time:" + ((System.currentTimeMillis() - beginTime) / 1000.0) + " s\n";
    }

    public String endMemory() {// -剩余内存 现在
        return "Memory in use:" + (beginMemory-runtime.freeMemory()) / 1024.0 + " KB\n";
    }

    public String endBDD() {
        //System.out.println("endBDD------" + Env.TRUE().getFactory().getNodeNum());
        return "Number of BDD nodes:" + (Env.TRUE().getFactory().getNodeNum() - beginBDD+modelBDD) + "\n";
    }

    public String endVar() {
        //System.out.println("endVar------" + Env.TRUE().getFactory().varNum());
        return "Number of BDD variables:" + (Env.TRUE().getFactory().varNum()- beginVar+modelVar) + "\n";
    }
}
