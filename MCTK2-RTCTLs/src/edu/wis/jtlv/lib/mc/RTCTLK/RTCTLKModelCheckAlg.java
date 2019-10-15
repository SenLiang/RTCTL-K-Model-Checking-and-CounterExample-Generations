package edu.wis.jtlv.lib.mc.RTCTLK;

import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.core.smv.schema.SMVAgentInfo;
import edu.wis.jtlv.env.module.Module;
import edu.wis.jtlv.env.module.ModuleWithStrongFairness;
import edu.wis.jtlv.env.module.ModuleWithWeakFairness;
import edu.wis.jtlv.env.spec.*;
import edu.wis.jtlv.lib.AlgExceptionI;
import edu.wis.jtlv.lib.AlgResultI;
import edu.wis.jtlv.lib.AlgResultString;
import edu.wis.jtlv.lib.mc.CTL.CTLModelCheckAlg;
import edu.wis.jtlv.lib.mc.ModelCheckAlgException;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDVarSet;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import swing.VerifyJPanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static swing.IndexJFrame.verifyPanel;

public class RTCTLKModelCheckAlg extends CTLModelCheckAlg{
    // variables for counterexamples generation
    private static int createdPathNumber=0; // the number of the paths currently created
    int specExplainToLevel = 1; // 0: trunk and all branches at all levels
                                // 1: trunk only
                                // 2: trunk and the branch at level 2
                                // i: trunk and all branches at the levels not larger than i

    private static int createdEpistemicEdgeNumber=0;
    GraphExplainRTCTLK graph;
    boolean isShowGraph;//控制只输出结果还是带图形化证据

    // for clock semantics computations
    private Vector<BDD> postImages;


    @Override
    public AlgResultI preAlgorithm() throws AlgExceptionI {
        if (getProperty() == null)
            VerifyJPanel.insertDocument(verifyPanel.ctext,  "\n Cannot model check a null specification.", Color.RED,1);
        else if (!getProperty().isRTCTLKSpec())
            VerifyJPanel.insertDocument(verifyPanel.ctext,  "\nCannot model check non RTCTLK specification: " + getProperty(), Color.RED,1);
        return null;
    }

    @Override
    public AlgResultI postAlgorithm() throws AlgExceptionI {
        return null;
    }

    public RTCTLKModelCheckAlg(ModuleWithStrongFairness design, Spec property) {
        super(design, property);
        postImages = new Vector<>();
    }

    // agentName KNOW p
    // forall(system_global_variables - agentName's visible_variables).((global_reachable_states & fair_states) -> p)
    public BDD know(String agentName, BDD p) throws ModelCheckAlgException {
        if(agentName.equals("")) throw new ModelCheckAlgException("The agent name of the knowledge formula is null.");

        int idx_dot = agentName.indexOf('.');
        if(idx_dot==-1)
            agentName = "main." + agentName;
        else if (!agentName.substring(0, idx_dot).equals("main"))
            throw new ModelCheckAlgException("The agent's name " + agentName + " is illegal.");

        SMVAgentInfo agentInfo = Env.getAll_agent_modules().get(agentName);
        if(agentInfo==null) throw new ModelCheckAlgException("Cannot find the information of agent " + agentName + ".");

        BDDVarSet visVars = agentInfo.getVisVars_BDDVarSet();
        // V - agentName's visible variables
        BDDVarSet allInvisVars = Env.globalUnprimeVarsMinus(visVars);

        BDD res = getFairReachableStates().imp(p).forAll(allInvisVars);

        return res;
    }

    // s |= agentName NKNOW p : agentName consider that it is possible that p holds on s
    // forall(system_global_variables - agentName's visible_variables).((global_reachable_states & fair_states) -> p)
    public BDD nknow(String agentName, BDD p) throws ModelCheckAlgException {
        if(agentName.equals("")) throw new ModelCheckAlgException("The agent name of the knowledge formula is null.");

        int idx_dot = agentName.indexOf('.');
        if(idx_dot==-1)
            agentName = "main." + agentName;
        else if (!agentName.substring(0, idx_dot).equals("main"))
            throw new ModelCheckAlgException("The agent's name " + agentName + " is illegal.");

        SMVAgentInfo agentInfo = Env.getAll_agent_modules().get(agentName);
        if(agentInfo==null) throw new ModelCheckAlgException("Cannot find the information of agent " + agentName + ".");

        BDDVarSet visVars = agentInfo.getVisVars_BDDVarSet();
        // V - agentName's visible variables
        BDDVarSet allInvisVars = Env.globalUnprimeVarsMinus(visVars);

        BDD res = getFairReachableStates().and(p).exist(allInvisVars);

        return res;
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
    /**A[p BU f..t q] is equivalent to
     ! ((EBF 0..(f - 1) !p)
     | EBG f..f ((EBG 0..(t - f) !q)
     | E[!q BU 0..(t - f) (!q & !p)]))
     **/

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

    public BDD satRTCTLK(Spec property) throws ModelCheckAlgException {
        if (property instanceof SpecBDD)
            return ((SpecBDD) property).getVal();
        // else it is SpecExp since this cannot be a Real Time CTL.
        // and it also cannot be a triplet operator.
        SpecExp propExp = (SpecExp) property;
        Operator op = propExp.getOperator();
        Spec[] child = propExp.getChildren();

        int noo = op.numOfOperands();
        SpecRange range = null;
        SpecAgentIdentifier agentId = null;
        BDD left=null;
        BDD right=null;

        if (noo==1) //EX, EF, EG, AX, AF, AG left
            left=satRTCTLK(child[0]);
        if (noo==2) {//ABF, ABG, EBF, EBG, KNOW
            if (child[0] instanceof SpecRange)
            {   range = (SpecRange) child[0];
                left= satRTCTLK(child[1]);
            }else if(child[0] instanceof SpecAgentIdentifier) { //KNOW
                agentId = (SpecAgentIdentifier) child[0];
                left = satRTCTLK(child[1]);
            }else{
                left=satRTCTLK(child[0]);//AU, EU
                right=satRTCTLK(child[1]);
            }
        }
        if (noo==3)// ABU, EBU, ABG, EBG
        {
            if (child[1] instanceof SpecRange)
            { range = (SpecRange) child[1];
                left=satRTCTLK(child[0]);
                right=satRTCTLK(child[2]);
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
            String agentName = agentId.toString();
            return know(agentName, left);
        }

        // something is wrong.
        throw new ModelCheckAlgException(
                "Cannot identify root operator for sub specification: " + property);
    }


    public void SetShowGraph(boolean isShowGraph) {
        this.isShowGraph = isShowGraph;
    }
    public GraphExplainRTCTLK GetGraph() {
        return  this.graph;
    }

    //@Override
    public AlgResultI No_Swing_doAlgorithm() throws AlgExceptionI {
        Spec origSpec = getProperty();
        System.out.println("model checking RTCTLK: " + simplifySpecString(origSpec.toString(),false));
        // could throw an exception...
        BDD satStates = synSatRTCTLK(0, origSpec); // satRTCTLK(origSpec);
        BDD fairInitStates = getDesign().initial().and(getFairStates());
        BDD fairInit_unSat = fairInitStates.and(satStates.not());
        if(fairInit_unSat.isZero()){
            return new AlgResultString(true, "*** Property is VALID ***");
        }else{
            GraphExplainRTCTLK G = new GraphExplainRTCTLK("A counterexample of " + simplifySpecString(origSpec.toString(),false), this);
            G.addAttribute("ui.label",G.getId());

            boolean ok = mainExplainRTCTLK(origSpec, fairInit_unSat, G);

            String returned_msg = "";
            if(ok) {
                returned_msg = "*** Property is NOT VALID and its counterexample is as follows ***\n ";
                new ViewerExplainRTCTLK(G);
            }else{
                returned_msg = "*** Property is NOT VALID ***\n ";
            }
            return new AlgResultString(false, returned_msg);
        }

    }

    //Using swing to show
    public AlgResultI doAlgorithm() throws AlgExceptionI {
        Spec origSpec = getProperty();
        // could throw an exception...
        BDD satStates = satRTCTLK(origSpec);
        BDD fairInitStates = getDesign().initial().and(getFairStates());
        BDD fairInit_unSat = fairInitStates.and(satStates.not());
        if(fairInit_unSat.isZero()){
            return new AlgResultString(true, "*** Property is VALID ***");
        }else{
            String returned_msg = "";
            if (isShowGraph == false) {//只输出结果
                returned_msg = "*** Property is NOT VALID ***";
                return new AlgResultString(false, returned_msg);
            } else {//输出反例
                GraphExplainRTCTLK G = new GraphExplainRTCTLK("A counterexample of " + simplifySpecString(origSpec.toString(), false), this);
                G.addAttribute("ui.label", G.getId());
                boolean ok = mainExplainRTCTLK(origSpec, fairInit_unSat, G);
                if (ok) {
                    returned_msg = "*** Property is NOT VALID and its counterexample is as follows ***";
                    this.graph = G;
                } else {
                    returned_msg = "*** Property is NOT VALID ***";
                }
                return new AlgResultString(false, returned_msg);
            }
        }
    }

    public String simplifySpecString(String spec, boolean delTrue) {
        String res = spec.replaceAll("main.", "");
        if(delTrue){
            res = res.replace("#[TRUE], \n", "");
            res = res.replace("#[TRUE]", "");
        }
        return res;
    }

    public boolean mainExplainRTCTLK(
        Spec spec,                          // the spec. under checked
        BDD FairInitStates_unsat_spec,       // the set of fair initial states that does not satisfy spec
        GraphExplainRTCTLK G                 // the graph that explains spec
    ) throws ModelCheckAlgException {
        createdPathNumber = 0;

        //create a new node as the first state of the counterexample
        BDD fromState = FairInitStates_unsat_spec.satOne(getDesign().moduleUnprimeVars(), false);
        Node n = G.addStateNode( 1, 0, fromState, null, 0); // create the first state 1.0 of G
        n.setAttribute("ui.class", "initialState");
        boolean ok = explainRTCTLK(false, spec, G, 1, 0, 0);

        return true;
    }

    //-------------------------------------------------------------------------------------------------------
    // generating a witness/counterexample of spec from the created state pathNo.stateNo
    //-------------------------------------------------------------------------------------------------------
    public boolean explainRTCTLK(
            boolean getWitness,     // getWitness=true and pathNo.stateNo|=spec: get witness of spec,
                                    // getWitness=false and pathNo.stateNo=/=spec: get counterexample of spec
            Spec spec,              // the spec. under checked
            GraphExplainRTCTLK G,   // the graph that explains spec
            int pathNo,             // pathNo is the No. of the current path
            int stateNo,             // stateNo is the No. of the current state
            int layer               // the layer of the state pathNo.stateNo, the layer of the root of the graph is 0
    ) throws ModelCheckAlgException {
        String stateID = pathNo+"."+stateNo;
        BDD fromState = G.getNodeBDD(stateID);
        if(fromState==null || fromState.isZero()) return false;
        int fromStateLayer = G.getNodeLayer(stateID);

        if (spec instanceof SpecBDD) { // prop is an assertion
            //BDD specVal = ((SpecBDD) spec).getVal();
            if(getWitness) // generating a witness for an assertion
                G.addNodeSatSpec(stateID, spec, false);
            else // generating a counterexample for an assertion
                G.addNodeSatSpec(stateID, new SpecExp(Operator.NOT, spec), false);
            return true;
        }

        SpecExp origPropExp = (SpecExp) spec;
        Operator op = origPropExp.getOperator();
        Spec[] child = origPropExp.getChildren();

        //----------- erase operators IMPLIES, IFF, XOR, XNOR ----------
        if (op == Operator.IMPLIES) { // L->R == !L | R
            SpecExp newSpec = new SpecExp(  Operator.OR,
                    new SpecExp(Operator.NOT, child[0]),
                    child[1]  );
            return explainRTCTLK(getWitness, newSpec, G, pathNo, stateNo, layer);
        }else if (op == Operator.IFF) { // L<->R == (L & R) | (!L & !R)
            SpecExp newSpec = new SpecExp(  Operator.OR,
                    new SpecExp(Operator.AND, child[0], child[1]),
                    new SpecExp(Operator.AND,
                            new SpecExp(Operator.NOT, child[0]),
                            new SpecExp(Operator.NOT, child[1]))  );
            return explainRTCTLK(getWitness, newSpec, G, pathNo, stateNo, layer);
        }else if(op == Operator.XOR) {
            SpecExp newSpec = new SpecExp(Operator.NOT, new SpecExp(Operator.IFF, child[0], child[1]));
            return explainRTCTLK(getWitness, newSpec, G, pathNo, stateNo, layer);
        }else if(op == Operator.XNOR) {
            SpecExp newSpec = new SpecExp(Operator.NOT, new SpecExp(Operator.XOR, child[0], child[1]));
            return explainRTCTLK(getWitness, newSpec, G, pathNo, stateNo, layer);
        }

        if(getWitness) { // getWitness=true: generating a witness of spec
            if (op == Operator.NOT) {
                if(!(child[0] instanceof SpecBDD)) {
                    SpecExp leftExp = (SpecExp) child[0];
                    if(leftExp.getOperator() == Operator.KNOW) {
                        Spec knowChild[] = leftExp.getChildren();
                        SpecAgentIdentifier agentId = (SpecAgentIdentifier) knowChild[0];
                        return witnessNKnow(agentId.toString(), new SpecExp(Operator.NOT, knowChild[1]), G, pathNo, stateNo);
                    }else if(leftExp.getOperator() == Operator.SKNOW) {
                        Spec knowChild[] = leftExp.getChildren();
                        SpecAgentIdentifier agentId = (SpecAgentIdentifier) knowChild[0];
                        return witnessNSKnow(agentId.toString(), new SpecExp(Operator.NOT, knowChild[1]), G, pathNo, stateNo);
                    }
                }
                return explainRTCTLK(!getWitness, child[0], G, pathNo, stateNo, layer);
            }else if (op == Operator.AND) {
                boolean ret1=true, ret2=true;
                ret1 = explainRTCTLK(getWitness, child[0], G, pathNo, stateNo, layer);
                ret2 = explainRTCTLK(getWitness, child[1], G, pathNo, stateNo, layer);
                return ret1 && ret2;
            }else if (op == Operator.OR) {
                boolean ret1=true, ret2=true;
                if(child[0] instanceof SpecBDD) {
                    //BDD leftVal = ((SpecBDD) child[0]).getVal();
                    G.addNodeSatSpec(stateID, child[0], true); //leftVal.toString());
                    return true;
                }
                if(child[1] instanceof SpecBDD) {
                    //BDD rightVal = ((SpecBDD) child[1]).getVal();
                    G.addNodeSatSpec(stateID, child[1], true); //rightVal.toString());
                    return true;
                }
                BDD satLeft = synSatRTCTLK(fromStateLayer, child[0]);
                if(fromState.imp(satLeft).isOne())
                    return explainRTCTLK(getWitness, child[0], G, pathNo, stateNo, layer);
                else
                    return explainRTCTLK(getWitness, child[1], G, pathNo, stateNo, layer);
            }else if(op==Operator.AX || op==Operator.AF || op==Operator.AU || op==Operator.AG ||
                    op==Operator.ABF || op==Operator.ABU || op==Operator.ABG || op==Operator.KNOW || op==Operator.SKNOW) {
                G.addNodeSatSpec(stateID, spec, false);
                return true;
            }else if(op==Operator.EX) {
                return witnessEX(spec, G, pathNo, stateNo);
            }else if(op==Operator.EU) {
                witnessEU(spec, G, pathNo, stateNo);
            }else if(op==Operator.EF) {
                SpecBDD trueSpec = new SpecBDD(Env.TRUE());
                witnessEU(new SpecExp(Operator.EU, trueSpec, child[0]), G, pathNo, stateNo);
            }else if(op==Operator.EG) {
                witnessEG(spec, G, pathNo, stateNo);
            }else if(op==Operator.EBG) {//   new SpecExp(Operator.EBG, child[0],new SpecExp(Operator.NOT, child[1])), G, pathNo, stateNo);
                boolean child_isSyn = child[1].hasSynEpistemicOperators();
                if(!child_isSyn) witnessEBG(spec, G, pathNo, stateNo);
                else witnessSynEBG(spec, G, pathNo, stateNo);
            }else if(op==Operator.EBU) {//   new SpecExp(Operator.EBU, trueSpec,child[0],new SpecExp(Operator.NOT, child[1]))
                boolean left_isSyn = child[0].hasSynEpistemicOperators();
                boolean right_isSyn = child[2].hasSynEpistemicOperators();
                if(!left_isSyn && !right_isSyn) witnessEBU(spec, G, pathNo, stateNo);
                else witnessSynEBU(spec, G, pathNo, stateNo);
            }else if(op==Operator.EBF) {
                SpecBDD trueSpec = new SpecBDD(Env.TRUE());
                boolean left_isSyn = child[1].hasSynEpistemicOperators();
                if(!left_isSyn) witnessEBU(new SpecExp(Operator.EBU, trueSpec, child[0], child[1]), G, pathNo, stateNo);
                else witnessSynEBU(new SpecExp(Operator.EBU, trueSpec, child[0], child[1]), G, pathNo, stateNo);
            }

        }else{ // getWitness=false: generating a counterexample of spec
            if (op == Operator.NOT) {
                if(!(child[0] instanceof SpecBDD)) {
                    SpecExp leftExp = (SpecExp) child[0];
                    if(leftExp.getOperator() == Operator.KNOW || leftExp.getOperator() == Operator.SKNOW) {
                        G.addNodeSatSpec(stateID, child[0], false);
                        return true;
                    }
                }
                return explainRTCTLK(!getWitness, child[0], G, pathNo, stateNo, layer);
            }else if (op == Operator.OR) {
                boolean ret1=true, ret2=true;
                ret1 = explainRTCTLK(!getWitness, new SpecExp(Operator.NOT, child[0]), G, pathNo, stateNo, layer);
                ret2 = explainRTCTLK(!getWitness, new SpecExp(Operator.NOT, child[1]), G, pathNo, stateNo, layer);
                return ret1 && ret2;
            }else if (op == Operator.AND) {
                boolean ret1=true, ret2=true;
                if(child[0] instanceof SpecBDD) {
                    //BDD not_leftVal = ((SpecBDD) child[0]).getVal().not();
                    G.addNodeSatSpec(stateID, new SpecExp(Operator.NOT, child[0]), false); //not_leftVal.toString());
                    return true;
                }
                if(child[1] instanceof SpecBDD) {
                    //BDD not_rightVal = ((SpecBDD) child[1]).getVal().not();
                    G.addNodeSatSpec(stateID, new SpecExp(Operator.NOT, child[1]), false); //not_rightVal.toString());
                    return true;
                }
                BDD not_satLeft = getFairReachableStates().and(synSatRTCTLK(fromStateLayer, child[0]).not()); // fairReach - satLeft
                if(fromState.imp(not_satLeft).isOne())
                    return explainRTCTLK(!getWitness, new SpecExp(Operator.NOT, child[0]), G, pathNo, stateNo, layer);
                else
                    return explainRTCTLK(!getWitness, new SpecExp(Operator.NOT, child[1]), G, pathNo, stateNo, layer);
            }else if(op==Operator.EX || op==Operator.EF || op==Operator.EU || op==Operator.EG ||
                    op==Operator.EBF || op==Operator.EBU || op==Operator.EBG) {
                G.addNodeSatSpec(stateID, new SpecExp(Operator.NOT, spec), false);
                return true;
            }else if(op==Operator.AX) {
                SpecExp neg_spec = new SpecExp(Operator.EX,
                        new SpecExp(Operator.NOT, child[0]));
                return witnessEX(neg_spec, G, pathNo, stateNo);
            }else if(op==Operator.AU) { // CE(s, A[f U g]) = WIT(s, E[!g U (!f & !g)] \/ EG(!g))
                SpecExp EGng = new SpecExp(Operator.EG,
                        new SpecExp(Operator.NOT, child[1]));
                SpecExp EUspec = new SpecExp(Operator.EU,
                        new SpecExp(Operator.NOT,child[1]),
                        new SpecExp(Operator.NOT,
                                new SpecExp(Operator.OR, child[0], child[1])));
                return explainRTCTLK(!getWitness, new SpecExp(Operator.OR, EGng, EUspec), G, pathNo, stateNo, layer);
            }else if(op==Operator.AF) {
                return explainRTCTLK(!getWitness, new SpecExp(Operator.EG, new SpecExp(Operator.NOT, child[0])), G, pathNo, stateNo, layer);
            }else if(op==Operator.AG) {
                return explainRTCTLK(!getWitness, new SpecExp(Operator.EF, new SpecExp(Operator.NOT, child[0])), G, pathNo, stateNo, layer);
            }else if(op==Operator.ABF) {
                return explainRTCTLK(!getWitness, new SpecExp(Operator.EBG, child[0],new SpecExp(Operator.NOT, child[1])), G, pathNo, stateNo, layer);
            }else if(op==Operator.ABG) {
                SpecBDD trueSpec = new SpecBDD(Env.TRUE());
                return explainRTCTLK(!getWitness, new SpecExp(Operator.EBU, trueSpec,child[0],new SpecExp(Operator.NOT, child[1])), G, pathNo, stateNo, layer);
            }else if(op==Operator.ABU) { //  CE(s, A[f U a..b g]) = WIT(s, E[!g U a..b (!f & !g)] \/ EG a..b (!g))
                SpecExp EBGng = new SpecExp(Operator.EBG,child[1],
                        new SpecExp(Operator.NOT, child[2]));
                SpecExp EBUspec = new SpecExp(Operator.EBU,
                        new SpecExp(Operator.NOT,child[2]),child[1],
                        new SpecExp(Operator.AND, new SpecExp(Operator.NOT,child[0]), new SpecExp(Operator.NOT,child[2])));
                return explainRTCTLK(!getWitness, new SpecExp(Operator.OR, EBGng, EBUspec), G, pathNo, stateNo, layer);
                //return explainRTCTLK(!getWitness,  EBUspec, G, pathNo, stateNo);
            }else if(op==Operator.KNOW || op==Operator.SKNOW) {
                return explainRTCTLK(!getWitness, new SpecExp(Operator.NOT, spec), G, pathNo, stateNo, layer);
            }
        }
        return true;
    }

    // generating a witness of pathNo.stateNo |= spec, where spec = EX child[0]
    public boolean witnessEX(
            Spec spec,              // the spec. under checked
            GraphExplainRTCTLK G,   // the graph that explains spec
            int pathNo,             // pathNo is the No. of the current path
            int stateNo             // stateNo is the No. of the current state
    ) throws ModelCheckAlgException {
        String stateID = pathNo+"."+stateNo;
        BDD fromState = G.getNodeBDD(stateID);
        if(fromState==null || fromState.isZero()) return false;

        SpecExp origPropExp = (SpecExp) spec;
        Operator op = origPropExp.getOperator();
        Spec[] child = origPropExp.getChildren();

        if (spec instanceof SpecBDD) return false;
        if (op != Operator.EX) return false;

        BDD satLeft = satRTCTLK(child[0]);
        BDD nextState = getDesign().succ(fromState).and(satLeft).and(getFairReachableStates())
                .satOne(getDesign().moduleUnprimeVars(), false);
        if(nextState.isZero()) return false;

        createdPathNumber++;
        String nextStateId = createdPathNumber + "." + (stateNo + 1);
        G.addStateNode(createdPathNumber, stateNo+1, nextState, child[0], G.getNodeLayer(stateID)+1);
        Edge e = G.addEdge("Path #" + createdPathNumber + " |= X " + child[0], stateID, nextStateId, true);
        e.addAttribute("ui.label", e.getId());

        return true;
    }

    // generating a witness of pathNo.stateNo |= spec, where spec = E[ child[0] U child[1]]
    public boolean witnessEU(
            Spec spec,              // the spec. under checked
            GraphExplainRTCTLK G,   // the graph that explains spec
            int pathNo,             // pathNo is the No. of the current path
            int stateNo             // stateNo is the No. of the current state
    ) throws ModelCheckAlgException {
        String stateID = pathNo + "." + stateNo;
        BDD fromState = G.getNodeBDD(stateID);
        if (fromState == null || fromState.isZero()) return false;

        SpecExp origPropExp = (SpecExp) spec;
        Operator op = origPropExp.getOperator();
        Spec[] child = origPropExp.getChildren();
        ModuleWithStrongFairness design = getDesign();

        if (spec instanceof SpecBDD) return false;
        if (op != Operator.EU) return false;

        BDD f = satRTCTLK(child[0]);
        BDD g = satRTCTLK(child[1]);

        Vector<BDD> Z = new Vector<BDD>();
        Z.add(g.id().and(getFairStates()));
        if (Z.get(0).isZero()) return false;
        if (fromState.imp(Z.get(0)).isOne()) { // fromState |= g & fair, in this case don't need construct path
            G.addNodeSatSpec(stateID, child[1], true);
            return true;
        }

        // the path has at least 2 states, including fromState
        int i = 0, n = -1;
        while (true)
        {
            BDD predZ = f.and(design.pred(Z.get(i))); // predZ is the frontier of Z[i]
            if(fromState.imp(predZ).isOne()) { // fromState in predZ
                n = i;  // n is the nextZ of fromState
                break;
            }
            // fromState not in predZ
            Z.add(predZ);
            i++;
        }

        BDD[] path = new BDD[n+2];
        path[0] = fromState;
        G.addNodeSatSpec(stateID, child[0], true);
        int fromStateLayer = G.getNodeLayer(stateID);
        createdPathNumber++;

        String nid1, nid2;
        Edge e;
        for(i=1; i<=n+1; i++) {
            path[i] = design.succ(path[i - 1]).and(Z.get(n + 1 - i)).satOne(getDesign().moduleUnprimeVars(), false);

            if(i<=n) G.addStateNode(createdPathNumber, i, path[i], child[0], fromStateLayer+i);
            else G.addStateNode(createdPathNumber, i, path[i], child[1], fromStateLayer+i);

            if(i==1) {
                nid1=stateID; nid2=createdPathNumber+".1";
                e = G.addEdge("Path #" + createdPathNumber + " |= " + simplifySpecString(child[0].toString(),false) + " U " +
                        simplifySpecString(child[1].toString(),false), nid1, nid2, true);
                e.addAttribute("ui.label", e.getId());
            }else{
                nid1=createdPathNumber+"."+(i-1); nid2=createdPathNumber+"."+i;
                e = G.addEdge(nid1+"->"+nid2, nid1, nid2, true);
                //e.addAttribute("ui.label", e.getId());
            }
        }
        return true;
    }

    /**
     * <p>
     * This is essentially a variant of the algorithm "Witness", from the article: Yonit Ketsen,
     * Amir Pnueli, Li-on Raviv, Elad Shahar, "Model checking with strong
     * fairness".<br>
     * The line numbers are the line numbers of that algorithm. Read the article
     * for further details.
     * </p>
     *
     */
    public boolean witnessEG(
            Spec spec,              // the spec. under checked
            GraphExplainRTCTLK G,   // the graph that explains spec
            int pathNo,             // pathNo is the No. of the current path
            int stateNo             // stateNo is the No. of the current state
    ) throws ModelCheckAlgException {
        String stateID = pathNo + "." + stateNo;
        BDD fromState = G.getNodeBDD(stateID);
        if (fromState == null || fromState.isZero()) return false;

        SpecExp origPropExp = (SpecExp) spec;
        Operator op = origPropExp.getOperator();
        Spec[] child = origPropExp.getChildren();
        ModuleWithStrongFairness design = getDesign();

        if (spec instanceof SpecBDD) return false;
        if (op != Operator.EG) return false;

        BDD f = satRTCTLK(child[0]); // spec = EG f
        if(f==null) return false;
        BDD EGf = EfG(f);
        if(EGf.isZero()) return false;
        BDD feasible = EGf.and(getReachableStates()); //ce_fair_g(f).and(getReachableStates()); // feasible is the set of states satisfying f & fair
        if(fromState.and(feasible).isZero()) // fromState not in feasible
            return false;


        BDD temp, fulfill;
        // saving to the previous restriction state
        Vector<BDD> trans_restrictions = design.getAllTransRestrictions();

        // Lines 1-2 are handled by the caller. ("verify")

        // Line 3
        design.restrictTrans(feasible.and(Env.prime(feasible)));

        // Line 4
        // BDD s = feasible.satOne(design.moduleUnprimeVars(), false); // commented by LXY
        BDD s = fromState; // LXY: compute feas as the set of states in MSCS's from fromState
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
        BDD feas = design.allSucc(s); // LXY: feas is the set of states in MSCS's

        // Line 9
        // Find prefix - shortest path from initial state to subgraph feas.
        // design.removeAllTransRestrictions();  // LXY: keep each transition is restricted by feasible*feasible'
        Vector<BDD> prefix = new Vector<BDD>();
        BDD[] path = design.shortestPath(fromState, feas);
        String prefix_last_nodeId;
        String pred_nid, cur_nid;

        int currLayer = G.getNodeLayer(stateID);
        Edge e;
        boolean NotYetCreateEdge = true;
        createdPathNumber++;
        if (path.length >= 1) prefix.add(path[0]);
        if(path.length<=1) { // only include one state: fromState
            G.addNodeSatSpec(stateID, child[0], true);
            prefix_last_nodeId=stateID;
        }else { //path.length > 1
            pred_nid = stateID;
            G.addNodeSatSpec(stateID, child[0], true);
            for (int i = 1; i < path.length; i++) {
                cur_nid = createdPathNumber + "." + i;
                G.addStateNode(createdPathNumber, i, path[i], child[0], ++currLayer);

                if (NotYetCreateEdge) {
                    e = G.addEdge("Path #" + createdPathNumber + " |= G " + simplifySpecString(child[0].toString(),false), pred_nid, cur_nid, true);
                    e.addAttribute("ui.label", e.getId());
                    NotYetCreateEdge = false;
                } else {
                    e = G.addEdge(pred_nid + "->" + cur_nid, pred_nid, cur_nid, true);
                }

                pred_nid = cur_nid;

                prefix.add(path[i]);
            }
            prefix_last_nodeId = pred_nid; // prefix_last_nodeId is the first state in period
        }

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

        // LXY: Now period = { period[0]=prefix_last_node, ..., period[length-1]}, and
        // there is a transition from period[length-1] to period[0]
        pred_nid=prefix_last_nodeId;
        int state_idx = prefix.size()+1;
        for (int i=1; i<=period.size()-1; i++) {
            cur_nid=createdPathNumber+"."+state_idx;
            G.addStateNode(createdPathNumber,  state_idx, period.get(i), child[0], ++currLayer);

            if (NotYetCreateEdge) {
                e = G.addEdge("Path #" + createdPathNumber + " |= G " + simplifySpecString(child[0].toString(),false), pred_nid, cur_nid, true);
                e.addAttribute("ui.label", e.getId());
                NotYetCreateEdge=false;
            } else {
                G.addEdge(pred_nid+"->"+cur_nid, pred_nid, cur_nid, true);
            }

            state_idx++;
            pred_nid=cur_nid;
        }
        if(NotYetCreateEdge) { // period only has period[0], i.e., prefix_last_node
            e = G.addEdge("Path #" + createdPathNumber + " |= G " + simplifySpecString(child[0].toString(),false), pred_nid, prefix_last_nodeId, true);
            e.addAttribute("ui.label", e.getId());
            NotYetCreateEdge=false;
        }else
            G.addEdge(pred_nid+"->"+prefix_last_nodeId, pred_nid, prefix_last_nodeId, true); // close period


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

/*
        // Strip auxiliary variables introduced by tester.
        if (relevantVars != null) {
            BDDVarSet extraVars = Env.globalVarsMinus(relevantVars);
            // BDDVarSet extraVars = Env.globalVarsMinus(relevantVars);
            for (int i = 0; i < returned_path.length; i++) {
                returned_path[i] = returned_path[i].satOne(relevantVars, false)
                        .exist(extraVars);
            }
        }
*/

        // returning to the previous restriction state
        design.setAllTransRestrictions(trans_restrictions);

        // the construction of path done
        //return returned_path;


        return true;
    }

    public boolean witnessEBG(
            Spec spec,              // the spec. under checked
            GraphExplainRTCTLK G,   // the graph that explains spec
            int pathNo,             // pathNo is the No. of the current path
            int stateNo             // stateNo is the No. of the current state
    ) throws ModelCheckAlgException {
        String stateID = pathNo + "." + stateNo;
        BDD fromState = G.getNodeBDD(stateID);
        if (fromState == null || fromState.isZero()) return false;
        int fromStateLayer = G.getNodeLayer(stateID);

        SpecExp origPropExp = (SpecExp) spec;
        Operator op = origPropExp.getOperator();
        Spec[] child = origPropExp.getChildren();
        SpecRange range = (SpecRange) child[0];
        ModuleWithStrongFairness design = getDesign();

        if (spec instanceof SpecBDD) return false;
        if (op != Operator.EBG) return false;
        BDD f = satRTCTLK(child[1]); // spec = EBG a..b f
        if(f==null || f.isZero()) return false;
        int from=range.getFrom(), to=range.getTo();

        HashMap<Integer, BDD> Z = new HashMap<Integer, BDD>();

        if(isUsingReachableStates()) Z.put(to, getFairReachableStates().and(f));
        else Z.put(to, getFairStates().and(f));

        int n=from, m=0;
        for (int i=to-1;i>=from;i--){
            Z.put(i, f.and(EX(Z.get(i+1))));
            if (Z.get(i).equals(Z.get(i+1))) {n=i; break;}
        }

        BDD preZ = Z.get(n);
        for (int i=from-1;i>=0;i--){
            Z.put(i, EX(preZ));
            if (Z.get(i).equals(preZ)) {m=i; break;}
            preZ = Z.get(i);
        }
        if(!fromState.imp(Z.get(m)).isOne()) return false;

        int currLayer = G.getNodeLayer(stateID);
        createdPathNumber++;
        BDD pre_state = fromState, cur_state;
        for(int i=1; i<=to; i++) {
            if(i<m){
                cur_state = design.succ(pre_state).and(Z.get(m)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, null, ++currLayer);
            }else if(i>=m && i<=(from-1)){
                cur_state = design.succ(pre_state).and(Z.get(i)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, null, ++currLayer);
            }else if(i>=from && i<n){
                cur_state = design.succ(pre_state).and(Z.get(n)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, child[1], ++currLayer);
            }else{ // i>=n && i<=to
                cur_state = design.succ(pre_state).and(Z.get(i)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, child[1], ++currLayer);
            }

            String nid1,nid2;
            Edge e;
            if(i==1) {
                nid1=stateID; nid2=createdPathNumber+".1";
                e = G.addEdge("Path #" + createdPathNumber + " |= BG ["+child[0].toString()+"] " +  simplifySpecString(child[1].toString(),false), nid1, nid2, true);
                e.addAttribute("ui.label", e.getId());
            }else{
                nid1=createdPathNumber+"."+(i-1); nid2=createdPathNumber+"."+i;
                e = G.addEdge(nid1+"->"+nid2, nid1, nid2, true);
                //e.addAttribute("ui.label", e.getId());
            }

            pre_state = cur_state;
        }

        return true;
    }

    public boolean witnessSynEBG(
            Spec spec,              // the spec. under checked
            GraphExplainRTCTLK G,   // the graph that explains spec
            int pathNo,             // pathNo is the No. of the current path
            int stateNo             // stateNo is the No. of the current state
    ) throws ModelCheckAlgException {
        String stateID = pathNo + "." + stateNo;
        BDD fromState = G.getNodeBDD(stateID);
        if (fromState == null || fromState.isZero()) return false;
        int fromStateLayer = G.getNodeLayer(stateID);

        SpecExp origPropExp = (SpecExp) spec;
        Operator op = origPropExp.getOperator();
        Spec[] child = origPropExp.getChildren();
        SpecRange range = (SpecRange) child[0];
        int from=range.getFrom(), to=range.getTo();
        ModuleWithStrongFairness design = getDesign();

        if (spec instanceof SpecBDD) return false;
        if (op != Operator.EBG) return false;
        if (!child[1].hasSynEpistemicOperators()) return false;

        HashMap<Integer, BDD> Z = new HashMap<Integer, BDD>();

        //compute Z[to]
        BDD f = synSatRTCTLK(fromStateLayer+to, child[1]);  // f = child[1] at tick fromStateLayer+to
        if(f==null || f.isZero()) return false;
        if(isUsingReachableStates()) Z.put(to, getFairReachableStates().and(f));
        else Z.put(to, getFairStates().and(f));

        //compute Z[from..to-1]
        for (int i=to-1;i>=from;i--){
            f = synSatRTCTLK(fromStateLayer+i, child[1]); // f = child[1] at tick fromStateLayer+i
            Z.put(i, f.and(EX(Z.get(i+1))));  // Z[i] = f /\ EX Z[i+1]
        }

        //compute Z[m..from-1], where m>=0 is the tick at which fixpoint reaches
        int m=0;
        BDD preZ = Z.get(from);
        for (int i=from-1;i>=0;i--){
            Z.put(i, EX(preZ));
            if (Z.get(i).equals(preZ)) {m=i; break;}
            preZ = Z.get(i);
        }
        if(!fromState.imp(Z.get(m)).isOne()) return false;

        int currLayer = fromStateLayer;
        createdPathNumber++;
        BDD pre_state = fromState, cur_state;
        for(int i=1; i<=to; i++) {
            if(i<m){
                cur_state = design.succ(pre_state).and(Z.get(m)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, null, ++currLayer);
            }else if(i>=m && i<=(from-1)){
                cur_state = design.succ(pre_state).and(Z.get(i)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, null, ++currLayer);
            }else{ // i>=from && i<=to
                cur_state = design.succ(pre_state).and(Z.get(i)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, child[1], ++currLayer);
            }

            String nid1,nid2;
            Edge e;
            if(i==1) {
                nid1=stateID; nid2=createdPathNumber+".1";
                e = G.addEdge("Path #" + createdPathNumber + " |= BG ["+child[0].toString()+"] " +  simplifySpecString(child[1].toString(),false), nid1, nid2, true);
                e.addAttribute("ui.label", e.getId());
            }else{
                nid1=createdPathNumber+"."+(i-1); nid2=createdPathNumber+"."+i;
                e = G.addEdge(nid1+"->"+nid2, nid1, nid2, true);
                //e.addAttribute("ui.label", e.getId());
            }

            pre_state = cur_state;
        }

        return true;
    }

    public boolean witnessEBU(
            Spec spec,              // the spec. under checked
            GraphExplainRTCTLK G,   // the graph that explains spec
            int pathNo,             // pathNo is the No. of the current path
            int stateNo             // stateNo is the No. of the current state
    ) throws ModelCheckAlgException {
        String stateID = pathNo + "." + stateNo;
        BDD fromState = G.getNodeBDD(stateID);
        if (fromState == null || fromState.isZero()) return false;

        SpecExp origPropExp = (SpecExp) spec;
        Operator op = origPropExp.getOperator();
        Spec[] child = origPropExp.getChildren();
        SpecRange range = (SpecRange) child[1];
        ModuleWithStrongFairness design = getDesign();

        if (spec instanceof SpecBDD) return false;
        if (op != Operator.EBU) return false;
        BDD f = satRTCTLK(child[0]);
        BDD g = satRTCTLK(child[2]);
        int from = range.getFrom(), to = range.getTo();

        HashMap<Integer, BDD> Z = new HashMap<>();
        if(isUsingReachableStates()) Z.put(to, getFairReachableStates().and(g));
        else Z.put(to, getFairStates().and(g));

        int n=from, m=0;
        for (int i=to-1; i>=from; i--){
            Z.put(i, Z.get(i+1).or(f.and(EX(Z.get(i+1)))));
            if (Z.get(i).equals(Z.get(i+1))) {n=i; break;}
        }

        BDD preZ = Z.get(n);
        for (int i=from-1;i>=0;i--){
            Z.put(i, f.and(EX(preZ)));
            if (Z.get(i).equals(preZ)) {m=i; break;}
            preZ = Z.get(i);
        }
        if(!fromState.imp(Z.get(m)).isOne()) return false;

        int currLayer = G.getNodeLayer(stateID);
        createdPathNumber++;
        BDD pre_state = fromState, cur_state;
        boolean finished=false;
        for(int i=1; i<=to; i++) {
            if(i<m){
                cur_state = design.succ(pre_state).and(Z.get(m)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, child[0], ++currLayer);
            }else if(i>=m && i<=(from-1)){
                cur_state = design.succ(pre_state).and(Z.get(i)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, child[0], ++currLayer);
            }else{
                BDD cur_Z;
                if(i>=from && i<n) cur_Z=Z.get(n);
                else cur_Z = Z.get(i); // i>=n && i<=to
                BDD cur_to_g = design.succ(pre_state).and(cur_Z.and(g));
                if(!cur_to_g.isZero()) {
                    cur_state = cur_to_g.satOne(design.moduleUnprimeVars(), false);
                    G.addStateNode(createdPathNumber, i, cur_state, child[2], ++currLayer);
                    finished=true;
                } else { // g states can not reach from pre_state by one transition step
                    cur_state = design.succ(pre_state).and(cur_Z).satOne(design.moduleUnprimeVars(), false);
                    G.addStateNode(createdPathNumber, i, cur_state, child[0], ++currLayer);
                }
            }

            String nid1,nid2;
            Edge e;
            if(i==1) {
                nid1=stateID; nid2=createdPathNumber+".1";
                e = G.addEdge("Path #" + createdPathNumber + " |= "+ simplifySpecString(child[0].toString(),false) +
                        " BU "+ child[1].toString() +  simplifySpecString(child[2].toString(),false), nid1, nid2, true);
                e.addAttribute("ui.label", e.getId());
            }else{
                nid1=createdPathNumber+"."+(i-1); nid2=createdPathNumber+"."+i;
                e = G.addEdge(nid1+"->"+nid2, nid1, nid2, true);
                //e.addAttribute("ui.label", e.getId());
            }
            if(finished) return true;
            pre_state = cur_state;
        }

        return true;
    }

    public boolean witnessSynEBU(
            Spec spec,              // the spec. under checked
            GraphExplainRTCTLK G,   // the graph that explains spec
            int pathNo,             // pathNo is the No. of the current path
            int stateNo             // stateNo is the No. of the current state
    ) throws ModelCheckAlgException {
        String stateID = pathNo + "." + stateNo;
        BDD fromState = G.getNodeBDD(stateID);
        if (fromState == null || fromState.isZero()) return false;
        int fromStateLayer = G.getNodeLayer(stateID);

        SpecExp origPropExp = (SpecExp) spec;
        Operator op = origPropExp.getOperator();
        Spec[] child = origPropExp.getChildren();
        SpecRange range = (SpecRange) child[1];
        int from = range.getFrom(), to = range.getTo();
        ModuleWithStrongFairness design = getDesign();

        if (spec instanceof SpecBDD) return false;
        if (op != Operator.EBU) return false;
        boolean left_isSyn = child[0].hasSynEpistemicOperators();
        boolean right_isSyn = child[2].hasSynEpistemicOperators();
        if(!left_isSyn && !right_isSyn) return false;

        BDD f, g;
        if(!left_isSyn) f = satRTCTLK(child[0]); else f = Env.FALSE();
        if(!right_isSyn) g = satRTCTLK(child[2]); else g = Env.FALSE();

        HashMap<Integer, BDD> Z = new HashMap<>();

        BDD cur_f, cur_g;
        //compute Z[to]
        cur_g = right_isSyn ? synSatRTCTLK(fromStateLayer+to, child[2]) : g;
        if(isUsingReachableStates()) Z.put(to, getFairReachableStates().and(cur_g));
        else Z.put(to, getFairStates().and(cur_g));

        for (int i=to-1; i>=from; i--){
            cur_f = left_isSyn ? synSatRTCTLK(fromStateLayer+i, child[0]) : f;
            cur_g = right_isSyn ? synSatRTCTLK(fromStateLayer+i, child[2]) : g;

            Z.put(i, cur_g.or(cur_f.and(EX(Z.get(i+1)))));
        }

        int m=0;
        if(left_isSyn) {
            for (int i = from - 1; i >= 0; i--)
                Z.put(i, synSatRTCTLK(fromStateLayer + i, child[0]).and(EX(Z.get(i+1))));
        }else{
            BDD preZ = Z.get(from);
            for (int i = from - 1; i >= 0; i--) {
                Z.put(i, f.and(EX(preZ)));
                if (Z.get(i).equals(preZ)) {
                    m = i;
                    break;
                }
                preZ = Z.get(i);
            }
        }
        if(!fromState.imp(Z.get(m)).isOne()) return false;

        int currLayer = G.getNodeLayer(stateID);
        createdPathNumber++;
        BDD pre_state = fromState, cur_state;
        boolean finished=false;
        for(int i=1; i<=to; i++) {
            if(i<m){
                cur_state = design.succ(pre_state).and(Z.get(m)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, child[0], ++currLayer);
            }else if(i>=m && i<=(from-1)){
                cur_state = design.succ(pre_state).and(Z.get(i)).satOne(design.moduleUnprimeVars(), false);
                G.addStateNode(createdPathNumber, i, cur_state, child[0], ++currLayer);
            }else{// i>=from && i<=to
                BDD cur_Z = Z.get(i);
                cur_g = right_isSyn ? synSatRTCTLK(fromStateLayer+i, child[2]) : g;
                BDD cur_to_g = design.succ(pre_state).and(cur_Z.and(cur_g));
                if(!cur_to_g.isZero()) {
                    cur_state = cur_to_g.satOne(design.moduleUnprimeVars(), false);
                    G.addStateNode(createdPathNumber, i, cur_state, child[2], ++currLayer);
                    finished=true;
                } else { // g states can not reach from pre_state by one transition step
                    cur_state = design.succ(pre_state).and(cur_Z).satOne(design.moduleUnprimeVars(), false);
                    G.addStateNode(createdPathNumber, i, cur_state, child[0], ++currLayer);
                }
            }

            String nid1,nid2;
            Edge e;
            if(i==1) {
                nid1=stateID; nid2=createdPathNumber+".1";
                e = G.addEdge("Path #" + createdPathNumber + " |= "+ simplifySpecString(child[0].toString(),false) +
                        " BU "+ child[1].toString() +  simplifySpecString(child[2].toString(),false), nid1, nid2, true);
                e.addAttribute("ui.label", e.getId());
            }else{
                nid1=createdPathNumber+"."+(i-1); nid2=createdPathNumber+"."+i;
                e = G.addEdge(nid1+"->"+nid2, nid1, nid2, true);
                //e.addAttribute("ui.label", e.getId());
            }
            if(finished) return true;
            pre_state = cur_state;
        }

        return true;
    }

    // generating a witness for pathNo.stateNo |= agentId NKnow spec
    public boolean witnessNKnow(
            String agentId,         // the name of the agent
            Spec spec,              // the spec. under checked
            GraphExplainRTCTLK G,   // the graph that explains spec
            int pathNo,             // pathNo is the No. of the current path
            int stateNo             // stateNo is the No. of the current state
    ) throws ModelCheckAlgException {
        //System.out.println("witnessNKnow----"+spec);
        String stateId = pathNo+"."+stateNo;
        BDD fromState = G.getNodeBDD(stateId);
        if(fromState==null || fromState.isZero()) return false;

        BDD specStates = satRTCTLK(spec).and(getFairReachableStates());
        if(specStates==null || specStates.isZero()) return false;

        if(fromState.imp(specStates).isOne()) { // fromState in specStates, don't need to extend the witness
            G.addNodeSatSpec(stateId, spec, true);
            return true;
        }

        // there exists another state s', agentId consider possible from fromState, satisfies spec
        // (1) generating a path to s', the starting state is one of the states in INIT or the current states within G
        // (2) create an epistemic edge between stateId and s'

        // get agentId's invisible variables
        int idx_dot = agentId.indexOf('.');
        if(idx_dot==-1)
            agentId = "main." + agentId;
        else if (!agentId.substring(0, idx_dot).equals("main"))
            throw new ModelCheckAlgException("The agent's name " + agentId + " is illegal.");
        SMVAgentInfo agentInfo = Env.getAll_agent_modules().get(agentId);
        if(agentInfo==null) throw new ModelCheckAlgException("Cannot find the information of agent " + agentId + ".");
        BDDVarSet visVars = agentInfo.getVisVars_BDDVarSet();
        BDDVarSet allInvisVars = Env.globalUnprimeVarsMinus(visVars);

        // nknowStates_neg_s0 is the set of states agentId consider possible from s0,
        // on these states agentId consider spec can be true
        BDD nknowStates_neg_s0 = nknow(agentId, specStates);
        nknowStates_neg_s0 = nknowStates_neg_s0.id().and(fromState.exist(allInvisVars)).and(specStates).and(getFairReachableStates()).and(fromState.not());
        if(nknowStates_neg_s0.isZero()) return false;

        //(1)
        //(1.1) collect all states within G into startStates
        BDD startStates = Env.FALSE();
        for (Node n : G) {
            BDD state = n.getAttribute("BDD");
            startStates = startStates.id().or(state);
        }
        startStates = startStates.id().or(getDesign().initial()).and(getFairReachableStates());
        if(startStates==null || startStates.isZero()) return false;

        //(1.2) generating the path
        BDD[] path = getDesign().shortestPath(startStates, nknowStates_neg_s0);
        if(path.length<=0) return false;
        String fromNodeId = null;
        for (Node n : G) {
            BDD bdd = n.getAttribute("BDD");
            if(path[0].equals(bdd)) {
                fromNodeId=n.getId();
                break;
            }
        }

        int currLayer=0;
        createdPathNumber++;
        if(fromNodeId==null) { // the starting state path[0] is not in G and must be another initial state
            Node n = G.addStateNode(createdPathNumber, 0, path[0], null, 0);
            n.setAttribute("ui.class", "initialState");
            fromNodeId = createdPathNumber + ".0";
        }
        // generating the path: fromNodeId(in G) -> path[1] -> ... -> path[length-1]
        String pred_nodeId = fromNodeId, cur_nodeId;
        for(int i=1; i<path.length; i++) {
            if(i==path.length-1) { // path[length-1] is the epistemic reachability state from stateId
                Node n = G.addStateNode(createdPathNumber, i, path[i], spec, ++currLayer);
                n.setAttribute("ui.class", "epistemicState");
            }else
                G.addStateNode(createdPathNumber, i, path[i], null, ++currLayer);

            cur_nodeId = createdPathNumber+"."+i;
            Edge e = G.addEdge(pred_nodeId+"->"+cur_nodeId, pred_nodeId, cur_nodeId, true);
            if(i==1) {
                e.addAttribute("ui.label", "goes to state " +
                        createdPathNumber+"."+(path.length-1)+" where "+simplifySpecString(agentId,true)
                        +" consider "+simplifySpecString(spec.toString(),false)+" holds" );
            }

            pred_nodeId = cur_nodeId;
        }

        //(2)
        Edge e = G.addEdge("Agent " + simplifySpecString(agentId,true) + " [" + (++createdEpistemicEdgeNumber) + "]", stateId, pred_nodeId, false);
        e.addAttribute("ui.label", "Agent " + simplifySpecString(agentId,true));
        e.setAttribute("ui.class", "epistemicEdge");

        return true;
    }

    // generating a witness for pathNo.stateNo, pathNo.stateNo.layer |= agentId NSKnow spec
    public boolean witnessNSKnow(
            String agentId,         // the name of the agent
            Spec spec,              // the spec. under checked
            GraphExplainRTCTLK G,   // the graph that explains spec
            int pathNo,             // pathNo is the No. of the current path
            int stateNo             // stateNo is the No. of the current state
    ) throws ModelCheckAlgException {
        String stateId = pathNo+"."+stateNo;
        BDD fromState = G.getNodeBDD(stateId);
        if(fromState==null || fromState.isZero()) return false;

        int fromStateLayer = G.getNodeLayer(stateId);
        BDD specStates = synSatRTCTLK(fromStateLayer, spec);

        if(fromState.imp(specStates).isOne()) {
            // fromState in specStates, don't need to extend the witness
            G.addNodeSatSpec(stateId, spec, true);
            return true;
        }

        // there exists another state s', agentId consider possible from fromState, satisfies spec
        // (1) generating a path to s', the starting state is one of the states in INIT or the current states within G
        // (2) create an epistemic edge between stateId and s'

        // get agentId's invisible variables
        int idx_dot = agentId.indexOf('.');
        if(idx_dot==-1)
            agentId = "main." + agentId;
        else if (!agentId.substring(0, idx_dot).equals("main"))
            throw new ModelCheckAlgException("The agent's name " + agentId + " is illegal.");
        SMVAgentInfo agentInfo = Env.getAll_agent_modules().get(agentId);
        if(agentInfo==null) throw new ModelCheckAlgException("Cannot find the information of agent " + agentId + ".");
        BDDVarSet visVars = agentInfo.getVisVars_BDDVarSet();
        BDDVarSet allInvisVars = Env.globalUnprimeVarsMinus(visVars);

        // nknowStates_neg_s0 is the set of states agentId consider possible from s0,
        // on these states agentId consider spec can be true
        BDD synNKnowStates_neg_s0 = synNKnow(fromStateLayer, agentId, spec);
        synNKnowStates_neg_s0 = synNKnowStates_neg_s0.id().and(fromState.exist(allInvisVars)).and(specStates)
                .and(getFairReachableStates()).and(fromState.not());
        if(synNKnowStates_neg_s0.isZero()) return false;

        //(1)
        int currLayer=0;
        createdPathNumber++;

        //generate a path, from one of initial states to one state in synNKnowStates_neg_s0, with fromStateLayer+1 nodes
        BDD Z[] = new BDD[fromStateLayer+1];
        Z[fromStateLayer] = synNKnowStates_neg_s0.and(getFairReachableStates());
        for(int i=fromStateLayer-1; i>=0; i--) Z[i] = EX(Z[i+1]);
        BDD pre_state = Z[0].and(getDesign().initial()).and(getFairStates());
        BDD cur_state;
        if(pre_state.isZero()) return false;

        pre_state = pre_state.id().satOne(getDesign().moduleUnprimeVars(), false);
        Node n = G.addStateNode(createdPathNumber, 0, pre_state, null, 0);
        n.setAttribute("ui.class", "initialState");

        String pre_nid=createdPathNumber+".0", cur_nid;
        for(int i=1; i<=fromStateLayer; i++) {
            //add a node and an edge
            cur_state = getDesign().succ(pre_state).and(Z[i]).satOne(getDesign().moduleUnprimeVars(),false);

            if(i==fromStateLayer) { // path[fromStateLayer] is the epistemic reachability state from stateId
                n = G.addStateNode(createdPathNumber, i, cur_state, spec, i);
                n.setAttribute("ui.class", "epistemicState");
            }else
                G.addStateNode(createdPathNumber, i, cur_state, null, i);
            cur_nid = createdPathNumber+"."+i;

            Edge e = G.addEdge(pre_nid+"->"+cur_nid, pre_nid, cur_nid, true);
            if(i==1) {
                e.addAttribute("ui.label", "goes to state " +
                        createdPathNumber+"."+(fromStateLayer)+" where "+simplifySpecString(agentId,true)
                        +" consider "+simplifySpecString(spec.toString(),false)+" holds" );
            }

            pre_nid = cur_nid;
            pre_state = cur_state;
        }
        //(2)
        Edge e = G.addEdge("Agent " + simplifySpecString(agentId,true) + " [" + (++createdEpistemicEdgeNumber) + "]", stateId, pre_nid, false);
        e.addAttribute("ui.label", "Agent " + simplifySpecString(agentId,true));
        e.setAttribute("ui.class", "epistemicEdge");

        return true;
    }

    public boolean explainOneGraphNode(GraphExplainRTCTLK G, String nodeId) throws ModelCheckAlgException {
        Node n = G.getNode(nodeId); if(n==null) return false;
        Queue<Spec> Q = n.getAttribute("queue_satSpec"); if(Q==null) return false;
        int pathNo=n.getAttribute("pathNo");
        int stateNo=n.getAttribute("stateNo");

        boolean ret=true;
        while(!Q.isEmpty()) {
            Spec spec = Q.poll();
            if(spec!=null) {
                int layer =  n.getAttribute("layer");
                ret = explainRTCTLK(true, spec, G, pathNo, stateNo, layer);
            }
            ret = ret && ret;
        }

        return ret;
    }

    public BDD synSatRTCTLK(int tick, Spec spec) throws ModelCheckAlgException {
        if (spec instanceof SpecBDD)
            return ((SpecBDD) spec).getVal();
        // else it is SpecExp since this cannot be a Real Time CTL.
        // and it also cannot be a triplet operator.
        SpecExp propExp = (SpecExp) spec;
        Operator op = propExp.getOperator();
        Spec[] child = propExp.getChildren();

        int noo = op.numOfOperands();
        SpecRange range = null;
        SpecAgentIdentifier agentId = null;
        Spec left=null;
        Spec right=null;
        boolean left_isSyn, right_isSyn=false;

        if (noo==1) {//EX, EF, EG, AX, AF, AG left
            left = child[0];
            left_isSyn = left.hasSynEpistemicOperators();
        }else if (noo==2) {//AU, EU, ABF, ABG, EBF, EBG, KNOW, SKNOW
            if (child[0] instanceof SpecRange) //ABF, ABG, EBF, EBG
            {   range = (SpecRange) child[0];
                left = child[1];
                left_isSyn = left.hasSynEpistemicOperators();
            }else if(child[0] instanceof SpecAgentIdentifier) { //KNOW, SKNOW
                agentId = (SpecAgentIdentifier) child[0];
                left = child[1];
                left_isSyn = left.hasSynEpistemicOperators();
            }else{ //AU, EU
                left = child[0];    left_isSyn = left.hasSynEpistemicOperators();
                right = child[1];   right_isSyn = right.hasSynEpistemicOperators();
            }
        }else if (noo==3) { // ABU, EBU
            range = (SpecRange) child[1];
            left = child[0];    left_isSyn = left.hasSynEpistemicOperators();
            right = child[2];   right_isSyn = right.hasSynEpistemicOperators();
        }else {
            throw new ModelCheckAlgException(
                    "Cannot check a specification with more than 2 operands: " + spec);
        }

        // propositional
        if (op == Operator.NOT)
            return synSatRTCTLK(tick, left).not();
        if (op == Operator.AND)
            return synSatRTCTLK(tick, left).and(synSatRTCTLK(tick, right));
        if (op == Operator.OR)
            return synSatRTCTLK(tick, left).or(synSatRTCTLK(tick, right));
        if (op == Operator.XOR)
            return synSatRTCTLK(tick, left).xor(synSatRTCTLK(tick, right));
        if (op == Operator.XNOR)
            return synSatRTCTLK(tick, left).xor(synSatRTCTLK(tick, right)).not();
        if (op == Operator.IFF)
            return synSatRTCTLK(tick, left).biimp(synSatRTCTLK(tick, right));
        if (op == Operator.IMPLIES)
            return synSatRTCTLK(tick, left).imp(synSatRTCTLK(tick, right));

        // unbounded CTL temporal
        if (op == Operator.EX)
            return synEfX(tick, left);
        if (op == Operator.AX)
            return synAfX(tick, left);


        if (op == Operator.EF) {
            if(left_isSyn) throw new ModelCheckAlgException(
                    "Cannot check EF operator because there exists synchronous epistemic modality in its sub specification: " + left);
            return EfF(satRTCTLK(left));
        }
        if (op == Operator.AF) {
            if(left_isSyn) throw new ModelCheckAlgException(
                    "Cannot check AF operator because there exists synchronous epistemic modality in its sub specification: " + left);
            return AfF(satRTCTLK(left));
        }
        if (op == Operator.EG) {
            if(left_isSyn) throw new ModelCheckAlgException(
                    "Cannot check EG operator because there exists synchronous epistemic modality in its sub specification: " + left);
            return EfG(satRTCTLK(left));
        }
        if (op == Operator.AG) {
            if(left_isSyn) throw new ModelCheckAlgException(
                    "Cannot check AG operator because there exists synchronous epistemic modality in its sub specification: " + left);
            return AfG(satRTCTLK(left));
        }
        if (op == Operator.EU) {
            if(left_isSyn || right_isSyn) throw new ModelCheckAlgException(
                    "Cannot check EU operator because there exists synchronous epistemic modality in its sub specifications: " + left);
            return EfU(satRTCTLK(left), satRTCTLK(right));
        }
        if (op == Operator.AU) {
            if(left_isSyn || right_isSyn) throw new ModelCheckAlgException(
                    "Cannot check AU operator because there exists synchronous epistemic modality in its sub specifications: " + left);
            return AfU(satRTCTLK(left), satRTCTLK(right));
        }

        // bounded CTL temporal
        if (op == Operator.EBU)
            return synEfBU(tick, range.getFrom(), range.getTo(), left, right);//EfBU(int from, int to, BDD p, BDD q)
        if (op == Operator.ABU)  //AfBU(int from, int to, BDD p, BDD q)
            return synAfBU(tick, range.getFrom(), range.getTo(), left, right);
        if (op == Operator.EBF)//EfBF(int from, int to, BDD p)
            return synEfBF(tick, range.getFrom(), range.getTo(), left);
        if (op == Operator.ABF)//(int from, int to, BDD p)
            return synAfBF(tick, range.getFrom(), range.getTo(), left);
        if (op == Operator.EBG)//(int from, int to, BDD p)
            return synEfBG(tick, range.getFrom(), range.getTo(), left);
        if (op == Operator.ABG)//AfBG(int from, int to, BDD p)
            return synAfBG(tick, range.getFrom(), range.getTo(), left);

        // epistemic
        if (op == Operator.KNOW) {
            String agentName = agentId.toString();
            if(left_isSyn)
                throw new ModelCheckAlgException(
                    "Cannot check KNOW modality because there exists synchronous epistemic modality in its sub specifications: " + left);
            else
                return know(agentName, satRTCTLK(left));
        }
        if (op == Operator.SKNOW) {
            String agentName = agentId.toString();
            return synKnow(tick, agentName, left);
        }

        // something is wrong.
        throw new ModelCheckAlgException(
                "Cannot identify root operator for sub specification: " + spec);
    }


    // return the set of states r(from) such that (r,from)|= EX f
    public BDD synEfX(int tick, Spec f) throws ModelCheckAlgException {
        BDD fs;
        boolean f_isSyn = f.hasSynEpistemicOperators();
        BDD f_bdd = f_isSyn ? synSatRTCTLK(tick+1, f) : satRTCTLK(f);
        return EfX(f_bdd);
    }

    public BDD synAfX(int tick, Spec f) throws ModelCheckAlgException {
        return synEfX(tick, new SpecExp(Operator.NOT, f)).not();
    }  	// AX f = !EX(!f) under fairness

    // r,tick |= E[f BU from..to g] under fairness
    public BDD synEfBU(int tick, int from, int to, Spec f, Spec g) throws ModelCheckAlgException {
        if(tick<0 || from<0 || to<0 || from>to) return null;
        boolean f_isSyn = f.hasSynEpistemicOperators(), g_isSyn = g.hasSynEpistemicOperators();
        BDD f_bdd, g_bdd;

        if(from==0 && to==0) {
            return g_isSyn ? synSatRTCTLK(tick, g) : satRTCTLK(g);
        }else if(from==0) { // from=0 & to>0
            if(!f_isSyn && !g_isSyn) return EfBU(from, to, satRTCTLK(f), satRTCTLK(g));
            f_bdd = f_isSyn ? synSatRTCTLK(tick, f) : satRTCTLK(f);
            g_bdd = g_isSyn ? synSatRTCTLK(tick, g) : satRTCTLK(g);
            return g_bdd.or(f_bdd.and(EX(synEfBU(tick+1, from, to-1, f, g))));
        }else{ // from>0 & to>=from
            if(!f_isSyn && !g_isSyn) return EfBU(from, to, satRTCTLK(f), satRTCTLK(g));
            f_bdd = f_isSyn ? synSatRTCTLK(tick, f) : satRTCTLK(f);
            return f_bdd.and(EX(synEfBU(tick+1, from-1, to-1, f, g)));
        }
    }

    // r,tick |= A[f BU from..to g] under fairness
    public BDD synAfBU(int tick, int from, int to, Spec f, Spec g) throws ModelCheckAlgException {
        if(tick<0 || from<0 || to<0 || from>to) return null;
        boolean f_isSyn = f.hasSynEpistemicOperators(), g_isSyn = g.hasSynEpistemicOperators();
        BDD f_bdd, g_bdd;

        if(from==0 && to==0) {
            return g_isSyn ? synSatRTCTLK(tick, g) : satRTCTLK(g);
        }else if(from==0) { // from=0 & to>0
            if(!f_isSyn && !g_isSyn) return AfBU(from, to, satRTCTLK(f), satRTCTLK(g));
            f_bdd = f_isSyn ? synSatRTCTLK(tick, f) : satRTCTLK(f);
            g_bdd = g_isSyn ? synSatRTCTLK(tick, g) : satRTCTLK(g);
            return g_bdd.or(f_bdd.and(AX(synAfBU(tick+1, from, to-1, f, g))));
        }else{ // from>0 & to>=from
            if(!f_isSyn && !g_isSyn) return AfBU(from, to, satRTCTLK(f), satRTCTLK(g));
            f_bdd = f_isSyn ? synSatRTCTLK(tick, f) : satRTCTLK(f);
            return f_bdd.and(AX(synAfBU(tick+1, from-1, to-1, f, g)));
        }
    }

    // r,tick |= EBF from..to f under fairness
    public BDD synEfBF(int tick, int from, int to, Spec f) throws ModelCheckAlgException {
        SpecBDD trueSpec = new SpecBDD(Env.TRUE());
        return synEfBU(tick, from, to, trueSpec, f);
    }

    // r,tick |= ABF from..to f under fairness
    public BDD synAfBF(int tick, int from, int to, Spec f) throws ModelCheckAlgException {
        return synEfBG(tick, from, to, new SpecExp(Operator.NOT, f)).not();
    }

    // r,tick |= EBG from..to f under fairness
    public BDD synEfBG(int tick, int from, int to, Spec f) throws ModelCheckAlgException {
        if(tick<0 || from<0 || to<0 || from>to) return null;
        boolean f_isSyn = f.hasSynEpistemicOperators();
        BDD f_bdd;

        if(from==0 && to==0) {
            return f_isSyn ? synSatRTCTLK(tick, f) : satRTCTLK(f);
        }else if(from==0) { // from=0 & to>0
            if(!f_isSyn) return EfBG(from, to, satRTCTLK(f));
            f_bdd = synSatRTCTLK(tick, f);
            return f_bdd.and(EX(synEfBG(tick+1, from, to-1, f)));
        }else{ // from>0 & to>=from
            if(!f_isSyn) return EfBG(from, to, satRTCTLK(f));
            f_bdd = synSatRTCTLK(tick, f);
            return f_bdd.and(EX(synEfBG(tick+1, from-1, to-1, f)));
        }
    }

    // r,tick |= ABG from..to f under fairness
    public BDD synAfBG(int tick, int from, int to, Spec f) throws ModelCheckAlgException {
        return synEfBF(tick, from, to, new SpecExp(Operator.NOT, f)).not();
    }

    BDD getPostImage(int tick) {
        if(tick<0) return null;
        BDD res;
        if(tick<this.postImages.size()) {
            res = this.postImages.get(tick);
            if(res==null) return Env.FALSE(); else return res;
        }
        //tick>=postImage.size()
        //compute the post image at clock "tick", allow "null" image to be stored in postImages
        BDD image;
        if(this.postImages.size()==0) {
            image = getDesign().initial();
            postImages.add(image);
        }else{
            image = this.postImages.get(postImages.size()-1);
        }

        int i = postImages.size()-1;
        while(i<tick && image!=null && image!=Env.FALSE()) {
            image = getDesign().succ(image.id());
            postImages.add(image);
            i++;
        }

        return image;
    }

    // return the set of states (r,tick) such that r,tick |= agentName KNOW f
    // forall(system_global_variables - agentName's visible_variables).((postimage(from) & fair_states) -> p)
    public BDD synKnow(int tick, String agentName, Spec f) throws ModelCheckAlgException {
        if(agentName.equals("")) throw new ModelCheckAlgException("The agent name of the knowledge formula is null.");

        int idx_dot = agentName.indexOf('.');
        if(idx_dot==-1)
            agentName = "main." + agentName;
        else if (!agentName.substring(0, idx_dot).equals("main"))
            throw new ModelCheckAlgException("The agent's name " + agentName + " is illegal.");

        SMVAgentInfo agentInfo = Env.getAll_agent_modules().get(agentName);
        if(agentInfo==null) throw new ModelCheckAlgException("Cannot find the information of agent " + agentName + ".");

        BDDVarSet visVars = agentInfo.getVisVars_BDDVarSet();
        // V - agentName's visible variables
        BDDVarSet allInvisVars = Env.globalUnprimeVarsMinus(visVars);


        BDD fairImageAtTick = getPostImage(tick);
        if(fairImageAtTick==null) {
            fairImageAtTick = Env.FALSE();
        }else {
            fairImageAtTick = fairImageAtTick.id().and(getFairReachableStates());
        }

        return fairImageAtTick.imp(synSatRTCTLK(tick, f)).forAll(allInvisVars);
    }

    public BDD synNKnow(int tick, String agentName, Spec f) throws ModelCheckAlgException {
        return synKnow(tick, agentName, new SpecExp(Operator.NOT, f)).not();
    }
}

