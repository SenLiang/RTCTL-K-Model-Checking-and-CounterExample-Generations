package test;

import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.core.spec.InternalSpecLanguage;
import edu.wis.jtlv.env.module.SMVModule;
import edu.wis.jtlv.env.spec.Spec;
import edu.wis.jtlv.lib.AlgRunnerThread;
import edu.wis.jtlv.lib.mc.LTL.LTLModelCheckAlg;
import edu.wis.jtlv.lib.mc.RTCTLK.RTCTLKModelCheckAlg;
import edu.wis.jtlv.lib.mc.RTCTL_STAR.RTCTL_STAR_ModelCheckAlg;
import swing.Statistic;

import java.io.IOException;

/**
 * @author yaniv sa'ar.
 * @version {@value Env#version}
 */
public class Thesis_Experiment {
    private Statistic getStat; //get the time consuming, memory, etc.

    public static void main(String[] args) throws IOException {
        Env.resetEnv();
        //RTCTLsTestMwOven();  //算法测试
        //RTCTLsCheckTraffic();  //论文实验1
        //RTCTLsCheckDC();    //论文实验2
        new Thesis_Experiment().RTCTLsCheckBT();    //论文实验3
    }

    public static void RTCTLsTestMwOven() {
        try {
            Env.loadModule("testcases/mwOven.smv");
        } catch (IOException e) {
            e.printStackTrace();
        }
        SMVModule main = (SMVModule) Env.getModule("main");
        main.setFullPrintingMode(true);
        System.out.println("========= DONE Loading Modules ==========");
        String to_parse;
        to_parse = "RTCTL*SPEC E( BG 3..18 (start -> F heat));";
        to_parse = "RTCTL*SPEC A( BG 3..18 (start -> F heat));";
        to_parse = "RTCTL*SPEC !E( !start BU 3..8 !heat ) ;";
        to_parse = "RTCTL*SPEC !E( TRUE BU 3..8 E(heat) ) ;";
        //to_parse = "RTCTL*SPEC A(start R A(X heat) ) ;";
        //to_parse = "RTCTL*SPEC A (start & A(X heat));";
        //to_parse = "RTCTL*SPEC (start & AX heat);";
        //to_parse = "RTCTL*SPEC (X start) BR 5..5  heat ;";
        //to_parse = "RTCTL*SPEC !E(E(X start));";
        //to_parse = "RTCTL*SPEC  !start U X heat;";
        //to_parse = "RTCTL*SPEC A( BF 3..-1  X close);\n";//√
        //to_parse = "RTCTL*SPEC E (BF 3..5 X heat) ;";//√
        //to_parse = "RTCTL*SPEC BG 3..5 (heat & close & start & error) ;";
        //to_parse = "RTCTL*SPEC !E(BF 3..5 (heat & close & start & error)) ;";//√
        //to_parse = "RTCTL*SPEC !E(BF 0..5 (heat & close & start)) ;";//√
        //to_parse = "RTCTL*SPEC !E(BF 3..5 F(heat & close & start)) ;";//√

        Spec[] all_specs = Env.loadSpecString(to_parse);
        System.out.println("========= DONE Loading Specs ============");

        AlgRunnerThread runner;
        // ///////////////////////////////////////
        // model checking a module
        for (int i = 0; i < all_specs.length; i++) {
            System.out.println(all_specs[i].hasLTLOperators());
            runner = new AlgRunnerThread(new RTCTL_STAR_ModelCheckAlg(main,
                    all_specs[i]));
            runner.runSequential();
            if (runner.getDoResult() != null)
                System.out.println(runner.getDoResult().resultString());
            if (runner.getDoException() != null)
                System.err.println(runner.getDoException().getMessage());
        }
        // ///////////////////////////////////////
    }

    public static void RTCTLsCheckTraffic() throws IOException {
        Env.loadModule("testcases/traffic.smv");
        System.out.println("========= Loading Modules ==========");
        SMVModule main = (SMVModule) Env.getModule("main");
        System.out.println("========= Done Loading Modules ==========");
        main.setFullPrintingMode(true);
        String to_parse = "";
        to_parse = "RTCTL*SPEC A(G nsLight=GREEN & E(F ewLight=GREEN));";//√
        //to_parse = "SPEC AG nsLight=GREEN;";//√
        Spec[] all_specs = Env.loadSpecString(to_parse);
        AlgRunnerThread runner;
        // checking valid with a module
        if (all_specs != null) {
//			System.out.println("========= DONE Loading Specs ============");
            for (int i = 0; i < all_specs.length; i++) {
                if (all_specs[i].getLanguage() == InternalSpecLanguage.RTCTLs)
                    runner = new AlgRunnerThread(new RTCTL_STAR_ModelCheckAlg(main, all_specs[i]));
                else if (all_specs[i].getLanguage() == InternalSpecLanguage.CTL)
                    runner = new AlgRunnerThread(new RTCTLKModelCheckAlg(main, all_specs[i]));
                else if (all_specs[i].getLanguage() == InternalSpecLanguage.LTL)
                    runner = new AlgRunnerThread(new LTLModelCheckAlg(main, all_specs[i]));
                else
                    throw new IOException("Currently cannot model check " + all_specs[i].getLanguage() + " specification " + all_specs[i]);

                runner.runSequential();
                if (runner.getDoResult() != null)
                    System.out.println(runner.getDoResult().resultString());
                if (runner.getDoException() != null)
                    System.err.println(runner.getDoException().getMessage());
            }
        }
    }

    public static void RTCTLsCheckDC() throws IOException {
        Env.loadModule("testcases/dc3-n=23.smv");

        System.out.println("========= Loading Modules ==========");
        SMVModule main = (SMVModule) Env.getModule("main");
        System.out.println("========= Done Loading Modules ==========");
        main.setFullPrintingMode(true);
        String to_parse = "";
        to_parse = "RTCTL*SPEC !A(G (!dc1.paid -> E(F((!dc1.paid & !dc2.paid & !dc3.paid)| (dc2.paid | dc3.paid)))));";
        to_parse = "RTCTL*SPEC A(X coin1) | A(X coin2) | A(X coin3); ";
        Spec[] all_specs = Env.loadSpecString(to_parse);
        AlgRunnerThread runner;
        if (all_specs != null) {
            for (int i = 0; i < all_specs.length; i++) {
                if (all_specs[i].getLanguage() == InternalSpecLanguage.RTCTLs)
                    runner = new AlgRunnerThread(new RTCTL_STAR_ModelCheckAlg(main, all_specs[i]));
                else if (all_specs[i].getLanguage() == InternalSpecLanguage.CTL)
                    runner = new AlgRunnerThread(new RTCTLKModelCheckAlg(main, all_specs[i]));
                else if (all_specs[i].getLanguage() == InternalSpecLanguage.LTL)
                    runner = new AlgRunnerThread(new LTLModelCheckAlg(main, all_specs[i]));
                else
                    throw new IOException("Currently cannot model check " + all_specs[i].getLanguage() + " specification " + all_specs[i]);

                runner.runSequential();
                if (runner.getDoResult() != null)
                    System.out.println(runner.getDoResult().resultString());
                if (runner.getDoException() != null)
                    System.err.println(runner.getDoException().getMessage());
            }
        }
    }

    public void RTCTLsCheckBT() throws IOException {
        Runtime runtime=Runtime.getRuntime();
        runtime.gc();
        long beginMemory =runtime.freeMemory();// 开始时的剩余内存

        Env.loadModule("testcases/bit_transmission.smv");
        System.out.println("========= Loading Modules ==========");
        SMVModule main = (SMVModule) Env.getModule("main");
        System.out.println("========= Done Loading Modules ==========");
        getStat = new Statistic();
        main.setFullPrintingMode(true);
        String to_parse = "";
        to_parse = "RTCTL*SPEC !E(BF 8..16 (receiver.state=r0 | receiver.state=r1));";
        to_parse = "RTCTL*SPEC G sender.bit=0 -> A(F(receiver.state=r0 & BF 7..16 sender.ack)) ";
        //to_parse = "RTCTL*SPEC G(sender.bit=0) -> F(receiver.state=r0 &  (BF 3..8 sender.ack));";
        //to_parse ="RTCTL*SPEC BG 7..12 ( (sender.bit=1 U receiver.state=r1) | (X sender.ack=1));";
        to_parse = "RTCTL*SPEC A( BG 7..12 ( (sender.bit=1 U receiver.state=r1) | A(X sender.ack=1)) )";
        //to_parse = "RTCTL*SPEC (receiver.state = r0|receiver.state = r1) BU 7..14 sender.ack";

        //to_parse = "RTCTL*SPEC A(X sender.ack)";
        //to_parse = "SPEC AF sender.ack;";
        Spec[] all_specs = Env.loadSpecString(to_parse);
        AlgRunnerThread runner;
        if (all_specs != null) {
            for (int i = 0; i < all_specs.length; i++) {
                getStat.startBDDVar();
                getStat.startTimeMemory();
                if (all_specs[i].getLanguage() == InternalSpecLanguage.RTCTLs)
                    runner = new AlgRunnerThread(new RTCTL_STAR_ModelCheckAlg(main, all_specs[i]));
//				else if (all_specs[i].getLanguage() == InternalSpecLanguage.CTL)
//					runner = new AlgRunnerThread(new RTCTLKModelCheckAlg(main, all_specs[i]));
                else
                    throw new IOException("Currently cannot model check " + all_specs[i].getLanguage() + " specification " + all_specs[i]);
                runner.runSequential();
                if (runner.getDoResult() != null)
                    System.out.println(runner.getDoResult().resultString()
                            + "\n" + getStat.endTime() + getStat.endBDD() + getStat.endVar() + getStat.endMemory());
                if (runner.getDoException() != null)
                    System.err.println(runner.getDoException().getMessage());
            }
        }
    }
}
