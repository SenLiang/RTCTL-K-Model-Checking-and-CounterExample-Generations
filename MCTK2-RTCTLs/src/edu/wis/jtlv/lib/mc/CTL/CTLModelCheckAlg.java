package edu.wis.jtlv.lib.mc.CTL;

import java.util.Vector;

import net.sf.javabdd.BDD;
import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.module.Module;
import edu.wis.jtlv.env.module.ModuleWithStrongFairness;
import edu.wis.jtlv.env.spec.Operator;
import edu.wis.jtlv.env.spec.Spec;
import edu.wis.jtlv.env.spec.SpecBDD;
import edu.wis.jtlv.env.spec.SpecExp;
import edu.wis.jtlv.lib.AlgExceptionI;
import edu.wis.jtlv.lib.AlgResultI;
import edu.wis.jtlv.lib.AlgResultString;
import edu.wis.jtlv.lib.FixPoint;
import edu.wis.jtlv.lib.mc.ModelCheckAlgException;
import edu.wis.jtlv.lib.mc.ModelCheckAlgI;

/**
 * <p>
 * An implementation for CTL model checking.
 * </p>
 * 
 * <p>
 * checking CTL with fairness based on Li-On's algorithm. These have been
 * checked (with TLV) on the dining philosophers problem for various number of
 * philosophers (up to 9).
 * </p>
 * <p>
 * Fairness:<br>
 * ce_fair_g(p): handles both justice and compassion using Lions algorithm<br>
 * 
 * EfX(p), EfU(,q), EfG(p), EfF(p), AfX(p), AfU(p,q), AfG(p), AfF(p) ( justice
 * only )<br>
 * 
 * agptoafq(p,q): states satisfying AG(p --> AF q) over fair paths
 * </p>
 * 
 * @version {@value edu.wis.jtlv.env.Env#version}
 * @author yaniv sa'ar.
 * 
 */
public class CTLModelCheckAlg extends ModelCheckAlgI {
	private Spec property;
	public Spec getProperty() {
		return property;
	}
	public void setProperty(Spec property) {
		this.property = property;
	}

	private BDD FairStates = null;
	public void setFairStates(BDD fairStates) {
		this.FairStates = fairStates;
	}
	public BDD getFairStates() {
		if(this.FairStates == null)
			this.FairStates = ce_fair_g(Env.TRUE());
		return this.FairStates;
	}

	private BDD ReachableStates = null;
	public BDD getReachableStates() {
		if(this.ReachableStates == null)
			this.ReachableStates = this.getDesign().reachable();
		return ReachableStates;
	}
	public void setReachableStates(BDD reachableStates) {
		ReachableStates = reachableStates;
	}

	public boolean isUsingReachableStates() {
		return UsingReachableStates;
	}

	public void setUsingReachableStates(boolean usingReachableStates) {
		UsingReachableStates = usingReachableStates;
	}

	private boolean UsingReachableStates=false;

	public BDD getFairReachableStates() {
		if(FairReachableStates == null)
			FairReachableStates = getFairStates().and(getReachableStates());
		return FairReachableStates;
	}

	public void setFairReachableStates(BDD fairReachableStates) {
		FairReachableStates = fairReachableStates;
	}

	private BDD FairReachableStates = null;

	public CTLModelCheckAlg(ModuleWithStrongFairness design, Spec property) {
		super(design);
		if (property == null)
			throw new RuntimeException("Cannot model check a null"
					+ " specification.");
		this.property = property;
		this.FairStates = null;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.lib.mc.ModelCheckAlgI#getDesign()
	 */
	public ModuleWithStrongFairness getDesign() {
		return (ModuleWithStrongFairness) super.getDesign();
	}

	/**
	 * <p>
	 * Check that the property is a CTL property.
	 * </p>
	 * 
	 * @throws AlgExceptionI
	 *             If The specification is not a CTL specification.
	 * 
	 * @return Nothing.
	 * 
	 * @see edu.wis.jtlv.lib.AlgI#preAlgorithm()
	 */
	@Override
	public AlgResultI preAlgorithm() throws AlgExceptionI {
		if (!property.isCTLSpec()) // Real Time CTL is not included here.
			throw new ModelCheckAlgException("Cannot model check non CTL"
					+ " specification: " + property);
		return null;
	}

	/**
	 * <p>
	 * Given a specification \phi (as a formula in temporal logic) we want to
	 * decide whether \phi is valid over finite state program P , i.e. whether
	 * all the computations of the design satisfy \phi.
	 * </p>
	 * 
	 * @throws ModelCheckAlgException
	 *             When cannot identify one of the operators.
	 * 
	 * @return a string result with "VALID", or "NOT VALID" (i.e.
	 *         {@link edu.wis.jtlv.lib.AlgResultString}).
	 * 
	 * @see edu.wis.jtlv.lib.AlgI#postAlgorithm()
	 */
	@Override
	public AlgResultI doAlgorithm() throws AlgExceptionI {
		System.out.println("model checking: " + property);
		//setFairStates(Env.TRUE());

		// could throw an exception...
		BDD calculateStates = satCTL(property);
		BDD FairInitStates = getDesign().initial().and(getFairStates());
//		if (!getDesign().initial().imp(calculateStates).not().isZero()) {
		if(FairInitStates.imp(calculateStates).isOne()){
			return new AlgResultString(true, "*** Property is VALID ***");
		}else{
			return new AlgResultString(false, "*** Property is NOT VALID ***");
		}
	}

	/**
	 * <p>
	 * Does nothing.
	 * </p>
	 * 
	 * @throws AlgExceptionI
	 *             Never.
	 * 
	 * @return Nothing.
	 * 
	 * @see edu.wis.jtlv.lib.AlgI#postAlgorithm()
	 */
	@Override
	public AlgResultI postAlgorithm() throws AlgExceptionI {
		return null;
	}

	public BDD satCTL(Spec property) throws ModelCheckAlgException {
		if (property instanceof SpecBDD)
			return ((SpecBDD) property).getVal();
		// else it is SpecExp since this cannot be a Real Time CTL.
		// and it also cannot be a triplet operator.
		SpecExp propExp = (SpecExp) property;
		Operator op = propExp.getOperator();
		Spec[] child = propExp.getChildren();
		BDD left = satCTL(child[0]);
		BDD right = (op.isBinary()) ? satCTL(child[1]) : null;

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

		// temporal
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

		// something is wrong.
		throw new ModelCheckAlgException(
				"Cannot identify root operator for sub" + " specification: "
						+ property);
	}

	public BDD EX(BDD f) {
		return getDesign().pred(f);
	}

	public BDD AX(BDD f) {
		return EX(f.not()).not();
	}  	// AX f = !EX(!f)

	public BDD AfX(BDD f) {
		return EfX(f.not()).not();
	}  	// AX f = !EX(!f) under fairness

	public BDD AfF(BDD f) {
		return EfG(f.not()).not();
	}	// AF f = !EG !f

	public BDD AfG(BDD f) {
		return EfF(f.not()).not();
	}	// AG f = !EF !f

	public BDD AfU(BDD f, BDD g) {	// AU(f,g) = !EU(!g, !f & !g) & !EG !g
		return EfU(g.id().not(), f.id().not().and(g.id().not())).not().and(
				EfG(g.id().not()).not());
	}

	public BDD EfX(BDD f) {
		BDD fs;
		if(isUsingReachableStates()) {
			fs = getFairReachableStates();
			return getDesign().pred(f.and(fs)).and(getReachableStates());
		} else {
			fs = getFairStates();
			return getDesign().pred(f.and(fs));
		}
	}

	public BDD EfF(BDD f) {
		return EfU(Env.TRUE(), f);
	}

	public BDD EfG_JTLV(BDD f) {
		/*
		 * Dealing with FAIRNESS
		 * 
		 * Standard model-checking function ce_fair_gj(p) for check \E_fair\G p
		 * using only justice.
		 */
		ModuleWithStrongFairness design = getDesign();
		BDD old_z, z = Env.TRUE();

		for (FixPoint<BDD> ires = new FixPoint<BDD>(); ires.advance(z);) {
			old_z = z.id();
			z = f.id();
			for (int i = design.justiceNum() - 1; i >= 0; i--) {
				BDD oldAndJust = design.justiceAt(i).and(old_z);
				z = z.id().and(design.pred(allPredsIn(f, oldAndJust)));
			}
		}
		return z;
	}

	public BDD EfG(BDD f) {
		ModuleWithStrongFairness design = getDesign();
		if(f.isOne()) {
			if (isUsingReachableStates()) return getFairReachableStates();
			else return getFairStates();
		}
		// f is not TRUE
		if(isUsingReachableStates()) f = f.id().and(getReachableStates());

		if(design.compassionNum()>0) return ce_fair_g(f);

		// compassion is empty
		BDD oldZ, Z = Env.TRUE();
		for (FixPoint<BDD> ires = new FixPoint<BDD>(); ires.advance(Z);) {
			oldZ = Z.id();
			Z = f.id();
			for (int i = design.justiceNum() - 1; i >= 0; i--) {
				BDD oldAndJust = design.justiceAt(i).and(oldZ);
				Z = Z.id().and(allPredsIn(f, oldAndJust));
			}
			Z = design.pred(Z.id());
		}
		return Z;
	}

	public BDD EfU(BDD f, BDD g) {
		BDD fs;
		if(isUsingReachableStates()) fs = getFairReachableStates(); else fs = getFairStates();
		return allPredsIn(f, g.id().and(fs));
	}

	public BDD allPredsIn(BDD p, BDD q) {
		Module design = getDesign();
		for (FixPoint<BDD> ires = new FixPoint<BDD>(); ires.advance(q);)
			q = q.id().or(p.and(design.pred(q.id())));
		return q;
	}


	public static boolean printable = false;

	/**
	 * <p>
	 * Li-on's ce_fair_g package <br>
	 * Compute all accessible states satisfying e_fair_g p
	 * </p>
	 * Handles both justice and compassion using Lions algorithm.
	 * 
	 * @param p
	 * @return
	 */
	public BDD ce_fair_g(BDD p) {
		// some kind of variant to feasible algorithm.
		ModuleWithStrongFairness design = getDesign();
		// saving the previous restriction state.
		Vector<BDD> trans_restriction = design.getAllTransRestrictions();
		BDD res = design.allSucc(design.initial()).and(p);  // Line 2

		// Line 3
		design.restrictTrans(res.id().and(Env.prime(res.id())));

		for (FixPoint<BDD> ires = new FixPoint<BDD>(); ires.advance(res);) {
			// I'm doing reverse so it will be completely identical to the
			// original TLV implementation.
			for (int i = design.justiceNum() - 1; i >= 0; i--) {
				res = res.id().and(design.justiceAt(i));
				res = design.allPred(res.id()).and(design.allSucc(res.id())); // res is the set of states in the SCC, in which each circle path must past Justice i
				if (printable)
					System.out.println("justice No. " + i);
				design.restrictTrans(res.id().and(Env.prime(res.id())));
			}

			for (int i = design.compassionNum() - 1; i >= 0; i--) {
				BDD tmp = res.id().and(design.qCompassionAt(i));
				tmp = design.allPred(tmp.id()).and(design.allSucc(tmp.id()));
				res = tmp.or(res.id().and(design.pCompassionAt(i).not()));
				if (printable)
					System.out.println("compassion No. " + i);
				design.restrictTrans(res.id().and(Env.prime(res.id())));
			}

			design.removeAllTransRestrictions();
			BDD resPreds = design.pred(res.id());
			BDD resSuccs = design.succ(res.id());
			res = res.id().and(resSuccs).and(resPreds);
			design.restrictTrans(res.id().and(Env.prime(res.id())));
		}
		design.removeAllTransRestrictions();

		// returning to the previous restriction state.
		design.setAllTransRestrictions(trans_restriction);
		return this.allPredsIn(p.id(), res.id());
	}
}
