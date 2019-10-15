package edu.wis.jtlv.lib.mc.LTL;

import java.util.HashMap;
import java.util.Set;

import edu.wis.jtlv.env.core.smv.SMVParseException;
import edu.wis.jtlv.env.core.smv.eval.*;
import edu.wis.jtlv.env.spec.*;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;
import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.module.ModuleBDDField;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.module.SMVModule;
import edu.wis.jtlv.lib.mc.ModelCheckAlgException;

/**
 * <p>
 * Construct a tester for a given LTL specification.
 * test commit
 * </p>
 *
 * @author yaniv sa'ar
 */
public class LTLTester {
    private static int tester_id = 0;
    private static int field_id = 0;

    private Spec root;
    private SMVModule tester;
    private HashMap<SpecExp, ModuleBDDField> spec2field = new HashMap<SpecExp, ModuleBDDField>();

    /**
     * <p>
     * Constructor for the tester.
     * </p>
     *
     * @param root_spec The specification to construct tester for.
     * @param isWeak    Whether to add initial or not.
     * @throws ModelCheckAlgException
     */
    public LTLTester(Spec root_spec, boolean isWeak)
            throws ModelCheckAlgException {
        this.root = root_spec;
        if ((root == null) || (!root.isLTLSpec()))
            throw new ModelCheckAlgException("Cannot construct a tester for"
                    + "specification: " + root);

        this.tester = new SMVModule("LTLTester_" + (++tester_id));
        createAuxVariable(root);
        constructModule(root, isWeak);
    }

    /**
     * <p>
     * Getter for the tester which where constructed from this specification.
     * </p>
     *
     * @return The tester which where constructed from this specification.
     */
    public SMVModule getTester() {
        return this.tester;
    }

    /**
     * <p>
     * Returns a BDD variable representing the given specification in this
     * tester.
     * </p>
     *
     * @param root The specification.
     * @return A BDD variable representing the given specification in this
     * tester.
     * @throws ModelCheckAlgException When failed to find the cooresponding BDD variable.
     */
    public BDD getSpec2BDD(Spec root) throws ModelCheckAlgException {
        if (root instanceof SpecBDD)
            return ((SpecBDD) root).getVal();
        // else it is SpecExp (cannot be a SpecCTLRange)
        SpecExp se = (SpecExp) root;
        Spec[] child = se.getChildren();
        Operator op = se.getOperator();

        if (op == Operator.NOT)
            return getSpec2BDD(child[0]).not();
        if (op == Operator.AND)
            return getSpec2BDD(child[0]).and(getSpec2BDD(child[1]));
        if (op == Operator.OR)
            return getSpec2BDD(child[0]).or(getSpec2BDD(child[1]));
        if (op == Operator.XOR)
            return getSpec2BDD(child[0]).xor(getSpec2BDD(child[1]));
        if (op == Operator.XNOR)
            return getSpec2BDD(child[0]).xor(getSpec2BDD(child[1])).not();
        if (op == Operator.IFF)
            return getSpec2BDD(child[0]).biimp(getSpec2BDD(child[1]));
        if (op == Operator.IMPLIES)
            return getSpec2BDD(child[0]).imp(getSpec2BDD(child[1]));
        if (op.isLTLOp()) {
            ModuleBDDField f = spec2field.get(root);
            if ((f != null) && (f.getDomain().size().intValue() == 2))
                return f.getDomain().ithVar(1);
        }
        // something is wrong
        throw new ModelCheckAlgException("Failed to find corresponding bdd"
                + " to specification: " + root.toString());
    }

    private void createAuxVariable(Spec s) throws ModelCheckAlgException {
        if (!(s instanceof SpecExp))
            return;
        // else
        SpecExp se = (SpecExp) s;
        if (se.getOperator().isLTLOp()) {
            try {
                // String name = se.getOperator().toString() + "_"
                // + (++field_id);
                String name = "AUX[" + (++field_id) + "]";
                ModuleBDDField f = tester.addVar(name);
                spec2field.put(se, f);
            } catch (ModuleException e) {
                throw new ModelCheckAlgException("Failed naming the extra "
                        + "auxiliary fields");
            }
        }

        Spec[] children = se.getChildren();
        for (int i = 0; i < children.length; i++) {
            createAuxVariable(children[i]);
        }
    }

    private void constructModule(Spec root, boolean isWeak)
            throws ModelCheckAlgException {
        BDD p_c1, p_c2, p_aux;

        Set<SpecExp> specifications = spec2field.keySet();
        //to solve case:LTLSpec x;(x means some logic without ltl　operator)
        if (specifications.size() == 0 && root instanceof SpecExp) {
            String name = "AUX[" + (++field_id) + "]";
            ModuleBDDField f = null;
            try {
                f = tester.addVar(name);
            } catch (ModuleException e) {
                e.printStackTrace();
            }
            tester.conjunctTrans(getSpec2BDD(root).biimp(f.getDomain().ithVar(1)));
        }

        for (SpecExp spec : specifications) {
            try {
                Operator op = spec.getOperator();
                Spec[] child = spec.getChildren();
                BDD aux = getSpec2BDD(spec);
                int noo = op.numOfOperands();

                //UPDATING20170428(add range)
                SpecRange range = null;
                BDD c1 = null;
                if (child[0] instanceof SpecRange)
                    range = (SpecRange) child[0];
                else
                    c1 = (noo > 0) ? getSpec2BDD(child[0]) : null;

                BDD c2 = (noo > 1) ? getSpec2BDD(child[1]) : null;
                BDD c3 = (noo > 2) ? getSpec2BDD(child[2]) : null;
                switch (op) {
                    case NEXT:
                        p_c1 = Env.prime(c1);
                        tester.conjunctTrans(aux.biimp(p_c1)); // aux <-> c1'
                        break;
                    case FINALLY:
                        p_aux = Env.prime(aux);
                        tester.conjunctTrans(aux.biimp(c1.or(p_aux))); // aux <-> (c1 \/ aux')
                        tester.addJustice(c1.or(aux.not()));  // aux -> c1
                        break;
                    case GLOBALLY:
                        p_aux = Env.prime(aux);
                        tester.conjunctTrans(aux.biimp(c1.and(p_aux))); // aux <-> (c1 /\ aux')
                        tester.addJustice(c1.not().or(aux)); // c1 -> aux
                        break;
                    case PREV:
                        p_aux = Env.prime(aux);
                        tester.addInitial(aux.not());
                        tester.conjunctTrans(p_aux.biimp(c1));
                        break;
                    // no BEFORE
                    case ONCE:
                        p_c1 = Env.prime(c1);
                        p_aux = Env.prime(aux);
                        tester.addInitial(aux.biimp(c1));
                        tester.conjunctTrans(p_aux.biimp(aux.or(p_c1)));
                        break;
                    case HISTORICALLY:
                        p_c1 = Env.prime(c1);
                        p_aux = Env.prime(aux);
                        tester.addInitial(aux.biimp(c1));
                        tester.conjunctTrans(p_aux.biimp(aux.and(p_c1)));
                        break;
                    case UNTIL:
                        p_aux = Env.prime(aux);
                        tester.conjunctTrans(aux.biimp(c2.or(c1.and(p_aux)))); // aux <-> (c2 \/ (c1 /\ aux'))
                        tester.addJustice(aux.not()); // !aux
                        break;
                    case RELEASES:
                        p_aux = Env.prime(aux);
                        tester.conjunctTrans(aux.biimp(c2.or(c1.and(p_aux))));
                        tester.addJustice(c1.not().and(c2.not()).or(aux));
                        break;
                    case SINCE:
                        p_c1 = Env.prime(c1);
                        p_c2 = Env.prime(c2);
                        p_aux = Env.prime(aux);
                        tester.addInitial(aux.biimp(c2));
                        tester.conjunctTrans(p_aux.biimp(p_c2.or(p_c1.and(aux))));
                        break;
                    case TRIGGERED:
                        p_c1 = Env.prime(c1);
                        p_c2 = Env.prime(c2);
                        p_aux = Env.prime(aux);
                        tester.addInitial(aux.biimp(c1.or(c2)));
                        tester.conjunctTrans(p_aux.biimp(p_c2.or(p_c1.and(aux))));
                        break;
                    // NOT_PREV_NOT,

                    // UPDATING20170428(add RTLTL operator BF、BG、BU )
                    case B_FINALLY:
                        tester.conjunctTrans(aux.biimp(c2));//translation algorithm
                        break;
                    case B_GLOBALLY:
                        tester.conjunctTrans(aux.biimp(c2));//translation algorithm
                        break;
                    case B_UNTIL:
                        tester.conjunctTrans(aux.biimp(c2));//translation algorithm
                        break;
                    case B_UNTIL0:
                        //01:Var step
                        int b = range.getTo() - range.getFrom(),fieldId=(++field_id);
                        SMVModule main = (SMVModule) Env.getModule("main");
                        ModuleBDDField x = main.addVar("x["+ fieldId + "]");
                        ModuleBDDField t = main.addVar("t["+ fieldId + "]", 0, b);

                        //02:Define step
                        BDD s1 = c2.and(c3.not()).and(x.getDomain().ithVar(0));
                        BDD s2 = c2.and(c3.not()).and(x.getDomain().ithVar(1));
                        BDD s3 = c3.and(x.getDomain().ithVar(1));
                        BDD s4 = c2.not().and(c3.not()).and(x.getDomain().ithVar(0));
                        BDD NtE0 = null;//next(t)=0
                        BDD tGE0 = null;//t>-0
                        BDD tLTb = null;//t<b
                        BDD NtEtP1 = null;//next(t)=t+1
                        BDD tGEb = null;//t>=b
                        try {
                            NtE0 = new StmtOperator(tester, new OpEqual(new OpNext(new ValueDomStmt(tester, t)), new ValueConsStrStmt(tester, new String[]{"0"}))).eval_stmt().toBDD();
                            tGE0 = new StmtOperator(tester, new OpGE(new ValueDomStmt(tester, t), new ValueConsStrStmt(tester, new String[]{"0"}))).eval_stmt().toBDD();
                            tLTb = new StmtOperator(tester, new OpLT(new ValueDomStmt(tester, t), new ValueConsStrStmt(tester, new String[]{b + ""}))).eval_stmt().toBDD();
                            NtEtP1 = new StmtOperator(tester, new OpEqual(new OpNext(new ValueDomStmt(tester, t)), new OpPlus(new ValueDomStmt(tester, t), new ValueConsStrStmt(tester, new String[]{"1"})))).eval_stmt().toBDD();
                            tGEb = new StmtOperator(tester, new OpGE(new ValueDomStmt(tester, t), new ValueConsStrStmt(tester, new String[]{b + ""}))).eval_stmt().toBDD();
                        } catch (SMVParseException e) {
                            e.printStackTrace();
                        }

                        //03:Trans step
                        main.conjunctTrans(
                                (s1.and(Env.prime(s1)))
                                        .or(s1.and(NtE0).and(Env.prime(s2)))
                                        .or(s1.and(Env.prime(s4)))
                                        .or(s2.and(tGE0).and(tLTb).and(Env.prime(s2)).and(NtEtP1))
                                        .or(s2.and(tGEb).and(Env.prime(s3)))
                                        .or(s3.and(Env.prime(s1)))
                                        .or(s3.and(Env.prime(s2)))
                                        .or(s3.and(Env.prime(s3)))
                                        .or(s3.and(Env.prime(s4)))
                                        .or(s4.and(Env.prime(s1)))
                                        .or(s4.and(Env.prime(s2)))
                                        .or(s4.and(Env.prime(s3)))
                                        .or(s4.and(Env.prime(s4)))
                        );

                        //05 tester:add trans
                        tester.conjunctTrans(aux.biimp(x.getDomain().ithVar(1)));
                        break;

                    default:
                        break;
                }
            } catch (ModuleException e) {
                throw new ModelCheckAlgException("Failed to construct BDD "
                        + "assertion for specification: " + spec.toString());
            } catch (BDDException e) {
                throw new ModelCheckAlgException("Failed to prime BDD "
                        + "assertion for specification: " + spec.toString());
            }
        }
        if (!isWeak) {
            try {
                tester.addInitial(getSpec2BDD(root).not());
            } catch (ModuleException e) {
                throw new ModelCheckAlgException(
                        "Failed to add roots BDD assertion.");
            }
        }
    }
}
