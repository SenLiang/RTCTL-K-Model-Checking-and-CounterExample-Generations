package edu.wis.jtlv.lib.mc.RTCTLK;
import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.core.smv.schema.SMVAgentInfo;
import edu.wis.jtlv.env.module.Module;
import edu.wis.jtlv.env.module.ModuleWithStrongFairness;
import edu.wis.jtlv.env.module.ModuleWithWeakFairness;
import edu.wis.jtlv.env.spec.*;
import edu.wis.jtlv.lib.*;
import edu.wis.jtlv.lib.mc.CTL.CTLModelCheckAlg;
import edu.wis.jtlv.lib.mc.ModelCheckAlgException;
import edu.wis.jtlv.old_lib.mc.ModelCheckException;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDVarSet;

import javax.swing.*;
import java.util.*;

import static edu.wis.jtlv.env.spec.Operator.*;
import static edu.wis.jtlv.env.spec.Operator.EU;

public class TextRTCTLModelCheckAlg extends CTLModelCheckAlg {
    public TextRTCTLModelCheckAlg(ModuleWithStrongFairness design, Spec property) {
        super(design, property);
    }
    //add
    Module design = getDesign();
    BDD feas = design.feasible();//所有的可达状态
    JTextPane ctext;

    public BDD know(String agentName, BDD p) throws ModelCheckAlgException {
        if(agentName.equals("")) throw new ModelCheckAlgException("The agent name of the knowledge formula is null.");

        int idx_dot = agentName.indexOf('.');
        if(idx_dot==-1)
            agentName = "main." + agentName;
        else if (!agentName.substring(0, idx_dot).equals("main."))
            throw new ModelCheckAlgException("The agent's name " + agentName + " is illegal.");

        SMVAgentInfo agentInfo = Env.getAll_agent_modules().get(agentName);
        if(agentInfo==null) throw new ModelCheckAlgException("Cannot find the information of agent " + agentName + ".");

        BDDVarSet visVars = agentInfo.getVisVars_BDDVarSet();
        // X - agentName's visible variables
        BDDVarSet allInvisVars = Env.globalUnprimeVarsMinus(visVars);

        BDD FairReachStates = getFairStates().and(getReachableStates());

        BDD res = FairReachStates.imp(p).forAll(allInvisVars);

        return res;
    }

    public BDD satRTCTLK(Spec property) throws ModelCheckAlgException {
        if (property instanceof SpecBDD)
            return ((SpecBDD) property).getVal();
        // else it is SpecExp since this cannot be a Real Time CTL.
        // and it also cannot be a triplet operator.
        SpecExp propExp = (SpecExp) property;
        Operator op = propExp.getOperator();
        Spec[] child = propExp.getChildren();

        //Uppdate by LS on : 2017/10/21
        int noo = op.numOfOperands();
        SpecRange range = null;
        BDD left=null; BDD right=null;

        if (noo==1) //EX, EF, EG, AX, AF,AG left
            left=satRTCTLK(child[0]);
        if (noo==2) {//ABF, ABG, EBF, EBG  left or right//区间不作节点
            if (child[0] instanceof SpecRange)
            {   range = (SpecRange) child[0];
                left= satRTCTLK(child[1]); }
            else if(op == Operator.KNOW) {
                left = null;
                right = satRTCTLK(child[1]);
            }
            else
            {   left=satRTCTLK(child[0]);//AU GU
                right=satRTCTLK(child[1]);
            }
        }
        if (noo==3)// ABU, EBU
        {
            if (child[1] instanceof SpecRange) {
                range = (SpecRange) child[1];
                left = satRTCTLK(child[0]);
                right = satRTCTLK(child[2]);
            }
        }
        // propositional
        if (op == Operator.NOT)
            return left.not();
        if (op == Operator.AND)
            return left.and(right);
        if (op == Operator.OR)
            return left.or(right);
        if (op == Operator.XOR)
            return left.xor(right);
        if (op == Operator.XNOR)
            return left.xor(right).not();
        if (op == Operator.IFF)
            return left.biimp(right);
        if (op == Operator.IMPLIES)
            return left.imp(right);

        // unbounded CTL temporal
        if (op == Operator.EX)
            return EfX(left);
        if (op == Operator.AX)
            return AfX(left);
        if (op == Operator.EF)
            return EfF(left);
        if (op == Operator.AF)
            return AfF(left);
        if (op == Operator.EG)
            return EfG(left);
        if (op == Operator.AG)
            return AfG(left);
        if (op == Operator.AU)
            return AfU(left, right);
        if (op == Operator.EU)
            return EfU(left, right);

        // bounded CTL temporal
        if (op == Operator.EBU)
            return EfBU(range.getFrom(), range.getTo(), left, right);//EfBU(int from, int to, BDD p, BDD q)
        if (op == Operator.ABU)//AfBU(int from, int to, BDD p, BDD q)
            return AfBU(range.getFrom(), range.getTo(), left, right);
        if (op == Operator.EBF)//EfBF(int from, int to, BDD p)
            return EfBF(range.getFrom(), range.getTo(), left);
        if (op == Operator.ABF)//(int from, int to, BDD p)
            return AfBF(range.getFrom(), range.getTo(), left);
        if (op == Operator.EBG)//(int from, int to, BDD p)
            return EfBG(range.getFrom(), range.getTo(), left);
        if (op == Operator.ABG)//AfBG(int from, int to, BDD p)
            return AfBG(range.getFrom(), range.getTo(), left);

        // epistemic
        if (op == Operator.KNOW) {
            String agentName = child[0].toString();
            return know(agentName, right);
        }
        // something is wrong.
        throw new ModelCheckAlgException(
                "Cannot identify root operator for sub specification: " + property);
    }
    @Override
    public AlgResultI preAlgorithm() throws AlgExceptionI {
        if (!getProperty().isRTCTLKSpec())
            throw new ModelCheckAlgException("Cannot model check non RTCTLK specification: " + getProperty());
        return null;
    }
    @Override
    public AlgResultI doAlgorithm() throws AlgExceptionI {
        //System.out.println("model checking RTCTLK: " + getProperty());
        ctext.setText(ctext.getText()+"\nmodel checking RTCTLK: " + getProperty());
        if (getProperty() == null)
            return new AlgResultString(false, "Cannot model check a null specification.");
        if (!getProperty().isRTCTLKSpec())
            return new AlgResultString("Cannot model check non RTCTLK specification: " + getProperty());
        // could throw an exception...
        BDD calculateStates = satRTCTLK(getProperty());
        BDD FairInitStates = getDesign().initial().and(getFairStates());
        if(FairInitStates.imp(calculateStates).isOne()){
            return new AlgResultString(true, "*** Property is VALID ***");
        }else{
            BDD[] example = new BDD[100];
            try {
                example = extractWithness(getProperty());
            } catch (ModelCheckException e) {
                e.printStackTrace();
            }
            if (example==null)
                return new AlgResultString(false, "*** Property is NOT VALID ***");
            else
                return new AlgResultPath(false, example);
        }
    }

    public void SetText(JTextPane ctext) {
        this.ctext=ctext;
    }
    /*
    begin RTCTLK counterexample
     */
    private BDD[] extractWithness(Spec property) throws ModelCheckException, ModelCheckAlgException {
        //System.out.println("Spec  "+property+"initial  "+property);
        SpecExp propExp = (SpecExp) property;
        Operator op = propExp.getOperator();
        if(op==EX|op==EF|op==EG|op==EU|op==EBF|op==EBG|op==EBU) return null;
        Spec[] child = propExp.getChildren();
        int noo = op.numOfOperands();
        SpecRange range = null;
        BDD left=null;
        BDD right=null;
        if (noo==1) //EX, EF, EG, AX, AF,AG left
            left=satRTCTLK(child[0]);
        if (noo==2) {//ABF, ABG, EBF, EBG  left or right
            if (child[0] instanceof SpecRange)
            { range = (SpecRange) child[0];
                left=satRTCTLK(child[1]);}//xxxxxxxx
            else
            {   left=satRTCTLK(child[0]);//AU GU
                right=satRTCTLK(child[1]);
            }
        }
        if (noo==3)// ABU, EBU
        {
            if (child[1] instanceof SpecRange)
            { range = (SpecRange) child[1];
                left=satRTCTLK(child[0]);
                right=satRTCTLK(child[2]);
            }
        }
        //设置initial()为起点
        BDD s=design.initial().and(design.feasible().satOne(design.moduleUnprimeVars(),false));
        switch (op) {
            /** Except for NOT、FINALLY、GLOBALLY、HISTORICALLY、NEXT、NOT_PREV_NOT、ONCE、PREV、B_FINALLY、B_GLOBALLY
             AND、OR、XOR、XNOR、IFF、IMPLIES、RELEASES、SINCE、TRIGGERED、UNTIL、B_UNTIL、B_UNTIL0 **/
            case AX:
                return EX_example(s, left.not());
            case AG:
                return EU_example(s,Env.TRUE(),left.not());
            case AF:
                return EG_example(s,left.not());
            case AU:
                BDD F=design.initial().and(getDesign().feasible()).satOne(getDesign().moduleUnprimeVars(),false);
                BDD T=design.initial().and(getDesign().feasible()).satOne(getDesign().moduleUnprimeVars(),true);
                if (!F.and(left).equals(T.and(left)))
                {
                    BDD[] return_path=new BDD[1];
                    if (F.equals(Env.FALSE()))
                        return_path[0]=F;
                    else
                        return_path[0]=T;
                    return return_path;
                }////Uppdate by LS on : 2017/11/16
                else{
                    BDD[] EU_path= EU_example(s,right.not(),left.not().and(right.not()));
                    if (EU==null){
                        BDD[] EG_path= EG_example(s,right.not());
                        return EG_path;}
                    return EU_path;
                }
            case ABF:
                return EBG_example(s,range.getFrom(), range.getTo(),left.not());
            case ABG:
                return EBU_example(s,range.getFrom(), range.getTo(),Env.TRUE(),left.not());
            case ABU:
                BDD[] EBU= EBU_example(s,range.getFrom(), range.getTo(),right.not(),left.not().and(right.not()));
                if (EBU==null){
                    BDD[] EBG= EBG_example(s,range.getFrom(), range.getTo(),right.not());
                    return EBG;}
                return EBU;
//				System.out.println("EBG-----------------------------------------------------------");
//				for(int i=0;i<EBG.length  ;i++)
//				{  if(EBG[i]==null)break;
//					System.out.println(i+"---"+EBG[i]);
//				}
//				System.out.println("EBU-----------------------------------------------------------");
//				for(int i=0;i<EBU.length  ;i++)
//				{  if(EBU[i]==null)break;
//					System.out.println(i+"---"+EBU[i]);
//				}
            case KNOW:
                System.out.println("KNOW-------------");
                String agentName = child[0].toString();
                return KNOW_example(agentName,s,range.getFrom(), range.getTo(),left.not());
        }
        return null;
    }

    // E[f BU from..to g] under fairness
    public BDD EfBU(int from, int to, BDD f, BDD g) {//****************
        BDD Z, oldZ=null;
        if(isUsingReachableStates()) Z = getFairReachableStates().and(g);
        else Z = getFairStates().and(g);

        for (int i=to-1; i>=from; i--){
            oldZ=Z;
            Z=Z.id().or(f.and(EX(Z.id())));
            if (Z.equals(oldZ)) break;
        }

        for (int i=from-1;i>=0;i--){
            oldZ=Z;
            Z=f.and(EX(Z.id()));
            if (Z.equals(oldZ)) break;
        }
        return Z;
    }

    // A[f BU from..to g] under fairness
    public BDD AfBU(int from, int to, BDD f, BDD g) {//****************
        BDD Z, oldZ=null;
        if(isUsingReachableStates()) Z = getFairReachableStates().and(g);
        else Z = getFairStates().and(g);

        for (int i=to-1; i>=from; i--){
            oldZ=Z;
            Z=Z.id().or(f.and(AX(Z.id())));
            if (Z.equals(oldZ)) break;
        }

        for (int i=from-1;i>=0;i--){
            oldZ=Z;
            Z=f.and(AX(Z.id()));
            if (Z.equals(oldZ)) break;
        }
        return Z;
    }
    // E[BG from..to f] under fairness
    public BDD EfBG(int from, int to, BDD f) {//****************
        BDD Z, oldZ=null;
        if(isUsingReachableStates()) Z = getFairReachableStates().and(f);
        else Z = getFairStates().and(f);

        for (int i=to-1;i>=from;i--){
            oldZ=Z;
            Z=f.and(EX(Z.id()));
            if (Z.equals(oldZ)) break;
        }

        for (int i=from-1;i>=0;i--){
            oldZ=Z;
            Z=EX(Z.id());
            if (Z.equals(oldZ)) break;
        }
        return Z;
    }

    // EBF from..to p under fairness
    public BDD EfBF(int from, int to, BDD f) {//rrrrrrrrr
        return EfBU(from, to, Env.TRUE(), f);
    }

    // ABF from..to p under fairness
    public BDD AfBF(int from, int to, BDD f) {//rrrrrrrrr
        return EfBG(from, to, f.not()).not();
    }

    // ABG from..to p under fairness
    public BDD AfBG(int from, int to, BDD f) {//rrrrrrrrr
        return EfBF(from,to,f.not()).not();
    }
    public BDD[] KNOW_example(String agentName,BDD s,int from, int to, BDD f){
        BDD[] returned_path = new BDD[100];
        if (!s.and(f).equals(Env.FALSE())){
            returned_path[0]=s;
            return returned_path;}
        BDD start=getDesign().initial();
        SMVAgentInfo agentInfo = Env.getAll_agent_modules().get(agentName);
        BDDVarSet visVars = agentInfo.getVisVars_BDDVarSet();
        // X - agentName's visible variables
        BDDVarSet allInvisVars = Env.globalUnprimeVarsMinus(visVars);
        BDD FairReachStates = getFairStates().and(getReachableStates());
        BDD end = FairReachStates.imp(f).forAll(allInvisVars);
        BDD subpath[]=getDesign().shortestPath(start,end);
        return subpath;
    }
    public BDD[] EX_example(BDD s, BDD f) {
        BDD next=getDesign().succ(s).and(getDesign().feasible()).satOne(design.moduleUnprimeVars(),false);
        BDD[] returned_path = new BDD[2];
        returned_path = new BDD[20];
        returned_path[0]=s;
        returned_path[1]=next;
        return   returned_path;
    }
    public BDD[] EU_example(BDD s,BDD f,BDD g) {
        BDD[] Z=new BDD[100];
        Z[0]=g.id().and(getDesign().feasible());
        if (Z[0].equals(Env.FALSE())) return null;
        int i=0,n=0;
        BDD[] returned_path = new BDD[100];
        while (true)
        {
            if(!s.and(Z[i]).equals(Env.FALSE()))
            {	returned_path[0]=s;
                if(!s.and(Z[0]).equals(Env.FALSE()))
                {   returned_path[0]=returned_path[0].and(Z[0]).satOne(getDesign().moduleUnprimeVars(),false);
                    return returned_path;
                }
                else
                {n=i;break;}
            }
            Z[i+1]=f.and(getDesign().pred(Z[i]));
            i=i+1;
        }
        for(i=1;i<=n;i++)
            returned_path[i]=getDesign().succ(returned_path[i-1]).and(Z[n-i]).satOne(getDesign().moduleUnprimeVars(),false);
        return returned_path;
    }
    public BDD[] EG_example(BDD s,BDD f) {
        BDD feasible=getDesign().feasible().and(f);
        BDD temp, fulfill;
        // saving to the previous restriction state
        Vector<BDD> trans_restrictions = design
                .getAllTransRestrictions();

        // Lines 1-2 are handled by the caller. ("verify")

        // Line 3
        design.restrictTrans(feasible.and(Env.prime(feasible)));

        // Line 4
        //feasible.satOne(design.moduleUnprimeVars(), false); **************
        // BDD s = feasible.satOne();

        // Lines 5-6
        while (true) {
            temp = design.allSucc(s).and(
                    design.allPred(s).not());
            if (!temp.isZero())
                s = temp.satOne(design.moduleUnprimeVars(), false);
                // s = temp.satOne();
            else
                break;
        }
        // Lines 5-6 : better version.
        // temp = tester.allSucc(s).and(tester.allPred(s).not());
        // while (!temp.isZero()){
        // s = temp.satOne(tester.moduleUnprimeVars(), false);
        // temp = tester.allSucc(s).and(tester.allPred(s).not());
        // }

        // Line 7: Compute MSCS containing s.
        BDD feas = design.allSucc(s);

        // Line 9
        // Find prefix - shortest path from initial state to subgraph feas.
        design.removeAllTransRestrictions();
        Vector<BDD> prefix = new Vector<BDD>();
        BDD[] path = design.shortestPath(design.initial(),
                feas);
        for (int i = 0; i < path.length; i++)
            prefix.add(path[i]);

        // //// Calculate "_period".

        // Line 8: This has to come after line 9, because the way TS.tlv
        // implements restriction.
        design.restrictTrans(feas.and(Env.prime(feas)));

        // Line 10
        Vector<BDD> period = new Vector<BDD>();
        period.add(prefix.lastElement());

        // Since the last item of the prefix is the first item of
        // the period we don't need to print the last item of the prefix.
        temp = prefix.remove(prefix.size() - 1);

        // Lines 11-13
        if (design instanceof ModuleWithWeakFairness) {
            ModuleWithWeakFairness weakDes = (ModuleWithWeakFairness) design;
            for (int i = 0; i < weakDes.justiceNum(); i++) {
                // Line 12, check if j[i] already satisfied
                fulfill = Env.FALSE();
                for (int j = 0; j < period.size(); j++) {
                    fulfill = period.elementAt(j).and(weakDes.justiceAt(i))
                            .satOne(weakDes.moduleUnprimeVars(), false);
                    // fulfill =
                    // period.elementAt(j).and(design.justiceAt(i)).satOne();
                    if (!fulfill.isZero())
                        break;
                }
                // Line 13
                if (fulfill.isZero()) {
                    BDD from = period.lastElement();
                    BDD to = feas.and(weakDes.justiceAt(i));
                    path = weakDes.shortestPath(from, to);
                    // eliminate the edge since from is already in period
                    for (int j = 1; j < path.length; j++)
                        period.add(path[j]);
                }
            }
        }
        // Lines 14-16
        if (design instanceof ModuleWithStrongFairness) {
            ModuleWithStrongFairness strongDes = (ModuleWithStrongFairness) design;
            for (int i = 0; i < strongDes.compassionNum(); i++) {
                if (!feas.and(strongDes.pCompassionAt(i)).isZero()) {
                    // check if C requirement i is already satisfied
                    fulfill = Env.FALSE();
                    for (int j = 0; j < period.size(); j++) {
                        fulfill = period.elementAt(j).and(
                                strongDes.qCompassionAt(i)).satOne(
                                strongDes.moduleUnprimeVars(), false);
                        // fulfill =
                        // period.elementAt(j).and(design.qCompassionAt(i)).satOne();
                        if (!fulfill.isZero())
                            break;
                    }

                    if (fulfill.isZero()) {
                        BDD from = period.lastElement();
                        BDD to = feas.and(strongDes.qCompassionAt(i));
                        path = strongDes.shortestPath(from, to);
                        // eliminate the edge since from is already in period
                        for (int j = 1; j < path.length; j++)
                            period.add(path[j]);
                    }
                }
            }
        }

        //
        // Close cycle
        //

        // A period of length 1 may be fair, but it might be the case that
        // period[1] is not a successor of itself. The routine path
        // will add nothing. To solve this
        // case we add another state to _period, now it will be OK since
        // period[1] and period[n] will not be equal.

        // Line 17, but modified
        if (!period.firstElement().and(period.lastElement()).isZero()) {
            // The first and last states are already equal, so we do not
            // need to extend them to complete a cycle, unless period is
            // a degenerate case of length = 1, which is not a successor of
            // self.
            if (period.size() == 1) {
                // Check if _period[1] is a successor of itself.
                if (period.firstElement().and(
                        design.succ(period.firstElement())).isZero()) {
                    // period[1] is not a successor of itself: Add state to
                    // period.
                    period
                            .add(design
                                    .succ(period.firstElement())
                                    .satOne(
                                            design
                                                    .moduleUnprimeVars(), false));
                    // period.add(design.succ(period.firstElement()).satOne());

                    // Close cycle.
                    BDD from = period.lastElement();
                    BDD to = period.firstElement();
                    path = design.shortestPath(from, to);
                    // eliminate the edges since from and to are already in
                    // period
                    for (int i = 1; i < path.length - 1; i++)
                        period.add(path[i]);
                }
            }
        } else {
            BDD from = period.lastElement();
            BDD to = period.firstElement();
            path = design.shortestPath(from, to);
            // eliminate the edges since from and to are already in period
            for (int i = 1; i < path.length - 1; i++)
                period.add(path[i]);
        }

        // Yaniv - the last one is for closing the cycle. He won't be printed.
        period.add(period.firstElement());

        // There is no need to have the last state of the period
        // in the counterexample since it already appears in _period[1]
        // if (period.size() > 1)
        // temp = period.remove(period.size() -1);

        // Copy prefix and period.
        prefix.addAll(period);
        BDD[] returned_path = new BDD[prefix.size()];
        prefix.toArray(returned_path);
        for (int i = 0; i < returned_path.length; i++) {
            returned_path[i] = returned_path[i].satOne(getDesign().moduleUnprimeVars(), false);
        }
        // returning to the previous restriction state
        design.setAllTransRestrictions(trans_restrictions);
        return returned_path;
    }
    public BDD[] EBU_example(BDD s,int from, int to, BDD f, BDD g){
        BDD[] Z = new BDD[100];
        int m=0,n=from;
        BDD oldZ=null;
        Z[to]=g.id().and(getDesign().feasible());
        for(int i=to-1;i>=from;i--)
        {
            Z[i] = Z[i+1].or(f.and(design.pred(Z[i+1])));
            if(Z[i].equals(Z[i+1])) {n=i;break;}
            n=i;
        }
        oldZ=Z[n];
        for(int i=from-1;i>=0;i--)
        {
            Z[i] = f.and(design.pred(oldZ));
            if(Z[i].equals(oldZ)) {m=i;break;}
            oldZ=Z[i];
            m=i;
        }
        //System.out.println("--n--"+n+"--m--"+m);
        BDD [] return_path=new BDD[100];
        BDD c=s,next;
        if (Z[m]==null)
        {       m=n;
            return_path[0]=c.and(Z[m]).satOne(getDesign().moduleUnprimeVars(),false);
            return return_path;
        }
        else
        {
            for(int i=0;i<=m ;i++)//补齐0 ---- m
            {
                return_path[i]=c.and(f).and(Z[m]).satOne(getDesign().moduleUnprimeVars(),false);
                next=getDesign().succ(c);
                c=next;
                //System.out.println(i+"---"+return_path[i]);
            }
            for(int i=m+1;i<=from-1;i++)//补齐m+1 ---- from-1
            {
                return_path[i]=getDesign().succ(return_path[i-1]).and(Z[i]).satOne(getDesign().moduleUnprimeVars(),false);
                //System.out.println(i+"---"+return_path[i]);
            }
            if (from>0){////Uppdate by LS on : 2017/11/16
                BDD nextZ,nextg;
                for(int i=from;i<=to  ;i++)//补齐from ---- n --- to
                {
                    if(i<=n) nextZ=Z[n];
                    else nextZ=Z[i];
                    nextg=getDesign().succ(return_path[i-1]).and(nextZ).and(g.id());
                    if (!nextg.equals(Env.FALSE())) {
                        return_path[i]=nextg.satOne(getDesign().moduleUnprimeVars(),false);
                        break;

                    }
                    return_path[i]=getDesign().succ(return_path[i-1]).and(nextZ);
                    //System.out.println(i+"---"+return_path[i]);
                }
            }
            return return_path;
        }
    }
    public BDD[] EBG_example(BDD s,int from, int to, BDD f){
        BDD[] Z = new BDD[100];
        int m=0,n=0;
        BDD oldZ=null;
        Z[to]=f.id().and(getDesign().feasible());
        for(int i=to-1;i>=from;i--)
        {
            Z[i] = Z[i+1].or(f.and(design.pred(Z[i+1])));
            if(Z[i].equals(Z[i+1])) {n=i;break;}
            n=i;
        }
        oldZ=Z[n];
        for(int i=from-1;i>=0;i--)
        {
            Z[i] = design.pred(oldZ);
            if(Z[i].equals(oldZ)) {m=i;break;}
            oldZ=Z[i];
            m=i;
        }//from 为0跳过此步

        BDD [] return_path=new BDD[100];
        BDD c=s,next;
        System.out.println(m+"---"+n);
        if(Z[m]==null)//0..n
        {
            m=n;
            for(int i=0;i<=to ;i++)
            {
                return_path[i]=c.and(Z[m]).satOne(getDesign().moduleUnprimeVars(),false);
                next=getDesign().succ(c);
                c=next;
                //System.out.println(i+"---"+return_path[i]);
            }
            return return_path;
        }
        else
        {
            if (s.and(Z[m]).equals(Env.FALSE()))return null;
            for(int i=0;i<=m ;i++)//补齐0 ---- m
            {
                return_path[i]=c.and(Z[m]).satOne(getDesign().moduleUnprimeVars(),false);
                next=getDesign().succ(c);
                c=next;
                //System.out.println(i+"---"+return_path[i]);
            }
            for(int i=m+1;i<=from-1;i++)//补齐m+1 ---- from-1
            {
                return_path[i]=getDesign().succ(return_path[i-1]).and(Z[i]).satOne(getDesign().moduleUnprimeVars(),false);
                //System.out.println(i+"---"+return_path[i]);
            }
            int stop=0;
            for(int i=from;i<=n  ;i++)//补齐from ---- n
            {
                return_path[i]=getDesign().succ(return_path[i-1]).and(Z[n]).satOne(getDesign().moduleUnprimeVars(),false);
                //System.out.println(i+"---"+return_path[i]);
            }
            for(int i=n;i<=to-1;i++)//补齐n ---- to
            {
                return_path[i+1]=getDesign().succ(return_path[i]).and(Z[i+1]).satOne(getDesign().moduleUnprimeVars(),false);
                //System.out.println(i+"---"+return_path[i]);
            }
            return return_path;
        }
    }
      /*
    end RTCTLK counterexample
     */
}

