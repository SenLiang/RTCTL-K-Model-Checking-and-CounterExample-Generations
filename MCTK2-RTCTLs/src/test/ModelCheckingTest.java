package test;

import java.io.IOException;

import edu.wis.jtlv.env.core.spec.InternalSpecLanguage;
import edu.wis.jtlv.lib.AlgExceptionI;
import edu.wis.jtlv.lib.AlgRunnerThread;
import edu.wis.jtlv.lib.mc.ATLsK.ATLsK_ModelCheckAlg;
import edu.wis.jtlv.lib.mc.CTL.CTLModelCheckAlg;
import edu.wis.jtlv.lib.mc.LTL.LTLModelCheckAlg;
import edu.wis.jtlv.lib.mc.RTCTLK.RTCTLKModelCheckAlg;


import edu.wis.jtlv.old_lib.mc.LTLModelChecker;
import edu.wis.jtlv.old_lib.mc.CTLModelChecker;
import edu.wis.jtlv.old_lib.mc.ModelCheckException;
import edu.wis.jtlv.old_lib.mc.SimpleModelChecker;
import net.sf.javabdd.BDD;
import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.module.SMVModule;
import edu.wis.jtlv.env.spec.Spec;

/**
 * @version {@value edu.wis.jtlv.env.Env#version}
 * @author yaniv sa'ar.
 *
 */
public class ModelCheckingTest {
	public static void main(String[] args) throws ModelCheckException, IOException {
/*
		simpleCheckDeadlock();
		Env.resetEnv();
		simpleCheckInvariance();
		Env.resetEnv();
		simpleCheckTempEntail();
		Env.resetEnv();
		simpleCheckReact();
		Env.resetEnv();
*/
		//Check_test();
		Env.resetEnv();
		rtctlCheck();
		//ltlCheck();
		//rtctlkCheck();
		//CTLCounterExample();
		System.out.println("DONE");
	}

	public static void simpleCheckDeadlock() {
		// System.setProperty("bdd", "buddy");
		try {
			SMVModule.initModulesWithoutRunningVar();
			Env.loadModule("testcases/mux-sem.smv");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		SMVModule main = (SMVModule) Env.getModule("main");
		main.setFullPrintingMode(true);
		for (SMVModule m : main.getAllInstances()) {
			m.setFullPrintingMode(true);
		}
		System.out.println("========= DONE Loading Modules ==========");

		SimpleModelChecker checker;
		try {
			checker = new SimpleModelChecker(main, true);
		} catch (ModelCheckException e) {
			e.printStackTrace();
			return;
		}

		// ///////////////////////////////////////
		// deadlock test 1 ///////////////////////
		System.out.println("-- Module muxsem with initials.");
		checker.checkDeadlockStrandardOutput();
		// ///////////////////////////////////////

		// ///////////////////////////////////////
		// deadlock test 2 - removing initials ///
		BDD old_initials = null;
		BDD[] old_sub_initials = new BDD[main.getAllInstances().length];
		try {
			old_initials = main.initial().id();
			main.setInitial(Env.TRUE());
			SMVModule[] sub = main.getAllInstances();
			for (int i = 0; i < sub.length; i++) {
				old_sub_initials[i] = sub[i].initial().id();
				sub[i].setInitial(Env.TRUE());
			}
		} catch (ModuleException e) {
		}
		System.out.println("-- Module muxsem without initials.");
		checker.checkDeadlockStrandardOutput();
		// returning the initials.
		try {
			main.setInitial(old_initials);
			SMVModule[] sub = main.getAllInstances();
			for (int i = 0; i < sub.length; i++)
				sub[i].setInitial(old_sub_initials[i]);
		} catch (ModuleException e) {
		}

	}

	public static void simpleCheckInvariance() {
		// System.setProperty("bdd", "buddy");
		try {
			SMVModule.initModulesWithoutRunningVar();
			Env.loadModule("testcases/mux-sem.smv");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		SMVModule main = (SMVModule) Env.getModule("main");
		main.setFullPrintingMode(true);
		for (SMVModule m : main.getAllInstances()) {
			m.setFullPrintingMode(true);
		}
		System.out.println("========= DONE Loading Modules ==========");
		// ///////////////////////////////////////

		SimpleModelChecker checker;
		try {
			checker = new SimpleModelChecker(main, true);
		} catch (ModelCheckException e) {
			e.printStackTrace();
			return;
		}

		// ///////////////////////////////////////
		// invariance tests //////////////////////
		System.out.println("========= Loading Specs =================");
		String to_parse = "";
		to_parse += "SPEC\n" + " !((proc[1].loc = 3) & (proc[2].loc = 3)) \n"; // 0
		to_parse += "SPEC\n" + " !((proc[1].loc = 3) & (proc[2].loc = 2)) \n"; // 1
		Spec[] all_specs = Env.loadSpecString(to_parse);
		System.out.println("========= DONE Loading Specs ============");

		checker.checkInvarianceStrandardOutput(all_specs[0]);
		checker.checkInvarianceStrandardOutput(all_specs[1]);
		// ///////////////////////////////////////

	}
	public static void simpleCheckTempEntail() {
		// System.setProperty("bdd", "buddy");
		try {
			SMVModule.initModulesWithoutRunningVar();
			Env.loadModule("testcases/simple_mc.smv");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		SMVModule main = (SMVModule) Env.getModule("main");
		main.setFullPrintingMode(true);
		for (SMVModule m : main.getAllInstances()) {
			m.setFullPrintingMode(true);
		}
		System.out.println("========= DONE Loading Modules ==========");

		SimpleModelChecker checker;
		LTLModelChecker ltl_checker;
		try {
			checker = new SimpleModelChecker(main, true);
			ltl_checker = new LTLModelChecker(main, true);
		} catch (ModelCheckException e) {
			e.printStackTrace();
			return;
		}
		// ///////////////////////////////////////
		// ///////////////////////////////////////
		// tempEntail tests //////////////////////

		String to_parse = "";
		to_parse += "LTLSPEC\n" + "[](aa -> <> aa)\n";
		to_parse += "SPEC\n" + "aa\n";
		to_parse += "LTLSPEC\n" + "[](aa -> <> bb)\n";
		to_parse += "SPEC\n" + "bb\n";
		to_parse += "LTLSPEC\n" + "[](aa -> <> !bb)\n";
		to_parse += "SPEC\n" + "!bb\n";
		to_parse += "LTLSPEC\n" + "[](aa -> <> ((!bb) & cc))\n";
		to_parse += "SPEC\n" + "((!bb) & cc)\n";

		Spec[] all_specs = Env.loadSpecString(to_parse);
		System.out.println("========= DONE Loading Specs ============");

		ltl_checker.modelCheckStandardOutput(all_specs[0]);
		checker.checkTempEntailStrandardOutput(all_specs[1], all_specs[1]);
		ltl_checker.modelCheckStandardOutput(all_specs[2]);
		checker.checkTempEntailStrandardOutput(all_specs[1], all_specs[3]);
		ltl_checker.modelCheckStandardOutput(all_specs[4]);
		checker.checkTempEntailStrandardOutput(all_specs[1], all_specs[5]);
		ltl_checker.modelCheckStandardOutput(all_specs[6]);
		checker.checkTempEntailStrandardOutput(all_specs[1], all_specs[7]);
		// ///////////////////////////////////////
	}

	public static void simpleCheckReact() {
		// System.setProperty("bdd", "buddy");
		try {
			SMVModule.initModulesWithoutRunningVar();
			Env.loadModule("testcases/simple_mc.smv");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		SMVModule main = (SMVModule) Env.getModule("main");
		main.setFullPrintingMode(true);
		for (SMVModule m : main.getAllInstances()) {
			m.setFullPrintingMode(true);
		}
		System.out.println("========= DONE Loading Modules ==========");

		// SimpleModelChecker checker;
		// try {
		// checker = new SimpleModelChecker(main, true);
		// } catch (ModelCheckException e) {
		// e.printStackTrace();
		// return;
		// }
	}

	public static void ctlCheck() {
		// System.setProperty("bdd", "buddy");
		try {
			//Env.loadModule("testcases/simple_mc.smv");
			Env.loadModule("testcases/testMAS.smv");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		SMVModule main = (SMVModule) Env.getModule("main");
		main.setFullPrintingMode(true);
		System.out.println("========= DONE Loading Modules ==========");

		String to_parse = "";
		to_parse += "SPEC\n" + "AF aa -> EF aa\n"; // 0
		to_parse += "SPEC\n" + "(AF (EG aa)) -> EG aa\n"; // 1
		to_parse += "SPEC\n" + "EG aa -> (EX aa & EF aa)\n"; // 2
		to_parse += "SPEC\n" + "EF aa\n"; // 3
		to_parse += "SPEC\n" + "(EG aa & AX aa) -> (EF !aa)\n"; // 4
		to_parse += "SPEC\n" + "AF bb -> EF bb\n"; // 5
		to_parse += "SPEC\n" + "EG bb -> (EX bb & EF bb)\n"; // 6
		to_parse += "SPEC\n" + "EF bb -> AF bb\n"; // 7
		to_parse += "SPEC\n" + "EF bb\n"; // 8
		to_parse += "SPEC\n" + "(EG bb & AX bb) -> (EF !bb)\n"; // 9
		to_parse += "SPEC\n" + "AF cc -> EF cc\n"; // 10
		to_parse += "SPEC\n" + "(AF (EG cc)) -> EG cc\n"; // 11
		to_parse += "SPEC\n" + "EG cc -> (EX cc & EF cc)\n"; // 12
		to_parse += "SPEC\n" + "EF cc -> AF cc\n"; // 13
		to_parse += "SPEC\n" + "EF cc\n"; // 14
		to_parse += "SPEC\n" + "(EG cc & AX cc) -> (EF !cc)\n"; // 15
		to_parse += "SPEC\n" + "EF EG bb\n"; // 16
		to_parse += "SPEC\n" + "EF EG !bb\n"; // 17 fails
		to_parse += "SPEC\n" + "EF aa -> AF aa\n"; // 18 fails
		to_parse += "SPEC\n" + "(AF (EG bb)) -> EG bb\n"; // 19 fails
		to_parse += "SPEC\n" + "EF AG !bb\n"; // 20 fails
		to_parse += "SPEC\n" + "EF AG bb\n"; // 21 fails

		Spec[] all_specs = Env.loadSpecString("SPEC (agent1 KNOW TRUE) ");//"SPEC (agent1 KNOW v1)\n" ); //to_parse);
		System.out.println("========= DONE Loading Specs ============");

		CTLModelChecker checker;
		try {
			checker = new CTLModelChecker(main, true);
		} catch (ModelCheckException e) {
			e.printStackTrace();
			return;
		}
		// model checking a module
		checker.modelCheckStandardOutput(all_specs[0]);
		checker.modelCheckStandardOutput(all_specs[1]);
		checker.modelCheckStandardOutput(all_specs[2]);
		checker.modelCheckStandardOutput(all_specs[3]);
		checker.modelCheckStandardOutput(all_specs[4]);
		checker.modelCheckStandardOutput(all_specs[5]);
		checker.modelCheckStandardOutput(all_specs[6]);
		checker.modelCheckStandardOutput(all_specs[7]);
		checker.modelCheckStandardOutput(all_specs[8]);
		checker.modelCheckStandardOutput(all_specs[9]);
		checker.modelCheckStandardOutput(all_specs[10]);
		checker.modelCheckStandardOutput(all_specs[11]);
		checker.modelCheckStandardOutput(all_specs[12]);
		checker.modelCheckStandardOutput(all_specs[13]);
		checker.modelCheckStandardOutput(all_specs[14]);
		checker.modelCheckStandardOutput(all_specs[15]);
		checker.modelCheckStandardOutput(all_specs[16]);
		checker.modelCheckStandardOutput(all_specs[17]); // fails
		checker.modelCheckStandardOutput(all_specs[18]); // fails
		checker.modelCheckStandardOutput(all_specs[19]); // fails
		checker.modelCheckStandardOutput(all_specs[20]); // fails
		checker.modelCheckStandardOutput(all_specs[21]); // fails
	}

	public static void ltlCheck() {
		// System.setProperty("bdd", "buddy");
		try {
			Env.loadModule("testcases/testMAS.smv");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		SMVModule main = (SMVModule) Env.getModule("main");
		main.setFullPrintingMode(true);
		System.out.println("========= DONE Loading Modules ==========");

//		String to_parse = "LTLSPEC agent1 KNOW v1";
		String to_parse = "LTLSPEC v1 & !(TRUE KNOW v1 KNOW v2)";
/*		to_parse += "LTLSPEC\n" + "GLOBALLY aa -> FINALLY aa\n";
		to_parse += "LTLSPEC\n" + "([](aa -> ()aa)) -> (<>aa -> <>[]aa)\n";
		to_parse += "LTLSPEC\n" + "[] aa -> () aa\n";
		to_parse += "LTLSPEC\n" + "[] aa -> <> aa\n";
		to_parse += "LTLSPEC\n" + "[] aa\n"; // failure 1
		to_parse += "LTLSPEC\n" + "<>[] aa\n"; // failure 2
		to_parse += "LTLSPEC\n" + "<> aa -> [] aa\n"; // failure 3
		to_parse += "LTLSPEC\n([](aa -> ()aa))->(<>aa -> []aa)\n"; // failure
*/
		Spec[] all_specs = Env.loadSpecString(to_parse);
		System.out.println("========= DONE Loading LTL Specs ============");
		Spec[] ls = Env.loadSpecString(to_parse);
		for (Spec s : ls) {
			System.out.println("=========");
			System.out.println(s);
			if (s == null)
				continue;
			System.out.print("isPropSpec --------------- ");
			System.out.println(s.isPropSpec());
			System.out.print("isCTLSpec ---------------- ");
			System.out.println(s.isCTLSpec());
			System.out.print("isRTCTLSpec -------- ");
			System.out.println(s.isRTCTLSpec());
			System.out.print("isLTLSpec ---------------- ");
			System.out.println(s.isLTLSpec());
			System.out.print("isFutureLTLSpec ---------- ");
			System.out.println(s.isFutureLTLSpec());
			System.out.print("isPastLTLSpec ------------ ");
			System.out.println(s.isPastLTLSpec());
			System.out.print("isCTLStarSpec ------------ ");
			System.out.println(s.isCTLStarSpec());
			System.out.print("isRTLTLKSpec ------------ ");
			System.out.println(s.isRTLTLKSpec());
			System.out.print("hasTemporalOperators ----- ");
			System.out.println(s.hasTemporalOperators());
		}

		LTLModelChecker checker;
		// checking valid without a module
		LTLModelChecker.standAloneValidStarndardOutput(all_specs[0]);
/*		LTLModelChecker.standAloneValidStarndardOutput(all_specs[1]);
		LTLModelChecker.standAloneValidStarndardOutput(all_specs[2]);
		LTLModelChecker.standAloneValidStarndardOutput(all_specs[3]);
		LTLModelChecker.standAloneValidStarndardOutput(all_specs[4]); // failure 1
		LTLModelChecker.standAloneValidStarndardOutput(all_specs[5]); // failure 2
		LTLModelChecker.standAloneValidStarndardOutput(all_specs[6]); // failure 3
		LTLModelChecker.standAloneValidStarndardOutput(all_specs[7]); // failure 4
*/		try {
			checker = new LTLModelChecker(main, true);
		} catch (ModelCheckException e) {
			e.printStackTrace();
			return;
		}
		// checking valid with a module
		checker.validStarndardOutput(all_specs[0]);
		checker.validStarndardOutput(all_specs[1]);
		checker.validStarndardOutput(all_specs[2]);
		checker.validStarndardOutput(all_specs[3]);
		checker.validStarndardOutput(all_specs[4]); // failure 1
		checker.validStarndardOutput(all_specs[5]); // failure 2
		checker.validStarndardOutput(all_specs[6]); // failure 3
		checker.validStarndardOutput(all_specs[7]); // failure 4
		// model checking a module
		checker.modelCheckStandardOutput(all_specs[0]);
		checker.modelCheckStandardOutput(all_specs[1]);
		checker.modelCheckStandardOutput(all_specs[2]);
		checker.modelCheckStandardOutput(all_specs[3]);
		checker.modelCheckStandardOutput(all_specs[4]); // failure 1
		checker.modelCheckStandardOutput(all_specs[5]); // failure 2
		checker.modelCheckStandardOutput(all_specs[6]); // failure 3
		checker.modelCheckStandardOutput(all_specs[7]); // failure 4
	}

	public static void rtctlCheck() throws IOException {
		try {
			Env.loadModule("testcases/BUTest.smv");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		SMVModule main = (SMVModule) Env.getModule("main");
		main.setFullPrintingMode(true);
		System.out.println("========= DONE Loading Modules ==========");
		String to_parse = "";
//		to_parse += "CTLSPEC E [a=1 BU 2..10 a=200 ];\n"; // 0 F
//		to_parse += "CTLSPEC ABF 3..8 a=3;\n"; // 0
//		to_parse += "CTLSPEC EBF 6..10 a=4;\n"; // 0
//		to_parse += "CTLSPEC ABG 6..10 a=1;\n"; // 0
//     	to_parse += "CTLSPEC AF a=30;\n"; //
//		to_parse += "CTLSPEC AF a=3;\n"; //
	    to_parse += "CTLSPEC E [a=1 U a=2];\n"; //
//		to_parse += "CTLSPEC A [a=10 BU 3..12 a=3 ];\n"; //
		//to_parse += "SPEC AX a=2;\n"; //
		//to_parse += "ATL*SPEC AF a=2;\n"; //--T
		//to_parse += "CTLSPEC A [a=1 BU 6..13 a=3 ];\n"; //  T
		//to_parse += "CTLSPEC ABG 3..10 a=3;  "; // 2 T
		//to_parse += "CTLSPEC a=2 -> EBF 6..10 a=20;"; // F
		//to_parse += "CTLSPEC a=1 -> ABF 6..10 a=2;\n"; // 2 T

		Spec[] all_specs = Env.loadSpecString(to_parse);
		AlgRunnerThread runner;
		// checking valid with a module
		if(all_specs!=null) {
//			System.out.println("========= DONE Loading Specs ============");
			for (int i = 0; i < all_specs.length; i++) {
					runner = new AlgRunnerThread(new RTCTLKModelCheckAlg(main, all_specs[i]));
				     //runner = new AlgRunnerThread(new ATLsK_ModelCheckAlg(main, all_specs[i]));
				    runner.runSequential();
				if (runner.getDoResult() != null)
					System.out.println(runner.getDoResult().resultString());
				if (runner.getDoException() != null)
					System.err.println(runner.getDoException().getMessage());
			}
		}
	}

	public static void rtctlkCheck() throws ModelCheckException {
		// System.setProperty("bdd", "buddy");
		try {
			//Env.loadModule("testcases/testMAS.smv");
			Env.loadModule("testcases/dc3.smv");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		SMVModule main = (SMVModule) Env.getModule("main");
		main.setFullPrintingMode(true);
		System.out.println("========= DONE Loading Modules ==========");
		String to_parse="";
		//estcases/testMAS.smv
//		to_parse += "SPEC AG (agent2 KNOW v1);\n"; // 0
//		to_parse += "CTLSPEC AG (agent1 KNOW v1);\n";
//		to_parse += "CTLSPEC ABG 1..10 (agent1 KNOW agent1.y.x1);\n";
//		to_parse += "CTLSPEC ABG 1..10 (agent1 KNOW agent2.y.x2);\n";
//		to_parse += "CTLSPEC EBF 8..10 (agent1 KNOW agent2.z);\n";
//		to_parse += "SPEC A [(agent1 KNOW agent2.y.x2) BU 6..10 (agent1 KNOW agent2.z)];\n"; // 1

		//to_parse += "SPEC AX (dc1 KNOW dc2.said);\n"; // 0
		//to_parse += "SPEC EX coin1;\n"; // 0
//		to_parse += "SPEC A [dc1.said BU 6..10 (dc1 KNOW dc3.paid)];\n"; // 1
		to_parse += "SPEC E [dc1.said BU 6..10 (dc1 KNOW dc3.paid)];\n"; // 1
		//	to_parse += "CTLSPEC ABF 1..100 (dc1 KNOW dc3.paid);\n"; // 2
//		to_parse += "CTLSPEC EBF 6..10 (dc2 KNOW dc1.said);\n"; // 3
		//to_parse += "CTLSPEC EBG 6..10 (dc3 KNOW dc2.paid);\n"; // 4
		//to_parse += "CTLSPEC ABG 6..10 (dc1 KNOW  dc3.said);\n"; // 5
//		to_parse += "SPEC dc1.paid -> EF(dc1 KNOW dc1.paid) ;\n";//T
//		to_parse += "CTLSPEC dc1.paid -> EBF 6..10 (dc2 KNOW dc1.said);\n"; // F
//		to_parse += "CTLSPEC dc1.paid -> EBF 1..10 (dc1 KNOW dc1.paid);\n"; // F
		Spec[] all_specs = Env.loadSpecString(to_parse);
		System.out.println("========= DONE Loading Specs ============");

		AlgRunnerThread runner;
		// ///////////////////////////////////////
		// model checking a module
		for (int i = 0; i < all_specs.length; i++) {
			// 17, 18, 19, 20, and 21 fails.
			runner = new AlgRunnerThread(new RTCTLKModelCheckAlg(main,
					all_specs[i]));
			runner.runSequential();
			if (runner.getDoResult() != null)
				System.out.println(runner.getDoResult().resultString());
			if (runner.getDoException() != null)
				System.err.println(runner.getDoException().getMessage());

		}
	}
	public static void CTLCounterExample()  {
		try {
			//Env.loadModule("testcases/simple_mc.smv");
			Env.loadModule("BUTest.smv");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		SMVModule main = (SMVModule) Env.getModule("main");
		main.setFullPrintingMode(true);
		System.out.println("========= DONE Loading Modules ==========");
		String to_parse = "";
//		to_parse += "CTLSPEC AX a=2 \n"; // 0 F
//		to_parse += "CTLSPEC AG a=1;\n"; // 0 F
//		to_parse += "CTLSPEC A[ a=1 U a=20];\n"; // 0 F
//		to_parse += "CTLSPEC EG a!=2;\n"; //0 F SMV返回TRUE
		//to_parse += "CTLSPEC AF a=2;\n"; //0 F SMV返回TRUE
		//to_parse += "CTLSPEC AF a=20;\n";

		//RTCTL EU、EG
		//to_parse += "CTLSPEC ABG 0..10 a=20;\n";
		to_parse += "CTLSPEC ABF 0..6 a=10;\n";
		//to_parse += "CTLSPEC ABG 7..11 a=2;\n";
		//to_parse += "CTLSPEC ABG 12..16 a=3;\n";
		//to_parse += "CTLSPEC ABF 0..15 a=1;\n";
		// need to be motified!!!
		//to_parse += "SPEC ABG 1..7 a=10;\n"; // 1
		//to_parse += "SPEC A [a=10 BU 0..10 a=30];\n";
		//to_parse += "SPEC E [a!=3 BU 6..10 a!=1 & a!=3];\n"; // 1


		Spec[] all_specs = Env.loadSpecString(to_parse);
		for (int i = 0; i < all_specs.length; i++) {
			CTLModelCheckAlg checker = new CTLModelCheckAlg(main,all_specs[i]);
			// model checking a module
			try {
				checker.preAlgorithm();
			} catch (AlgExceptionI algExceptionI) {
				algExceptionI.printStackTrace();
			}
			try {
				String s=checker.doAlgorithm().resultString();
				if(!s.contains("*** Property is VALID ***"))
					System.err.println(i+"---"+s);
				else
					System.out.println(i+"---"+s);
			} catch (AlgExceptionI algExceptionI) {
				algExceptionI.printStackTrace();
			}
		}
	}
	public static void Check_test() {
		// System.setProperty("bdd", "buddy");
		try {
			Env.loadModule("testcases/simple_mc.smv");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		SMVModule main = (SMVModule) Env.getModule("main");
		main.setFullPrintingMode(true);
		System.out.println("========= DONE Loading Modules ==========");

		String to_parse = "";
		to_parse += "LTLSPEC\n" + "G aa=100\n";
		//to_parse += "LTLSPEC\n" + "([](aa -> ()aa)) -> (<>aa -> <>[]aa)\n";
//		to_parse += "LTLSPEC\n" + "[] aa -> () aa\n";
		to_parse += "LTLSPEC\n" + "[] aa -> <> aa\n";
		to_parse += "LTLSPEC\n" + "[] aa\n"; // failure 1
//		to_parse += "LTLSPEC\n" + "<>[] aa\n"; // failure 2
//		to_parse += "LTLSPEC\n" + "<> aa -> [] aa\n"; // failure 3
//		to_parse += "LTLSPEC\n([](aa -> ()aa))->(<>aa -> []aa)\n"; // failure

		Spec[] all_specs = Env.loadSpecString(to_parse);
		System.out.println("========= DONE Loading LTL Specs ============");

		for (int i = 0; i < all_specs.length; i++) {
			LTLModelCheckAlg checker = new LTLModelCheckAlg(main, all_specs[i]);
			// model checking a module
			try {
				checker.preAlgorithm();
			} catch (AlgExceptionI algExceptionI) {
				algExceptionI.printStackTrace();
			}
			try {
				System.out.println(i + checker.doAlgorithm().resultString());
			} catch (AlgExceptionI algExceptionI) {
				algExceptionI.printStackTrace();
			}
		}
	}
}
