package edu.wis.jtlv.env.spec;

import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.core.spec.InternalSpecLanguage;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDVarSet;

/**
 * <p>
 * Specification expression.
 * </p>
 * 
 * @version {@value edu.wis.jtlv.env.Env#version}
 * @author yaniv sa'ar.
 * 
 */
public class SpecExp implements Spec {
	private Operator theOp;
	private Spec[] elements;
	private InternalSpecLanguage language=InternalSpecLanguage.UNDEF;

	/**
	 * <p>
	 * A general purpose constructor.
	 * </p>
	 * 
	 * @param op
	 *            The operator.
	 * @param el
	 *            An array of children specification
	 */
	public SpecExp(Operator op, Spec[] el) throws SpecException {
		if(op == Operator.CAN_ENFORCE || op == Operator.CANNOT_AVOID) {
			if(el.length<1)
				throw new SpecException("Cannot instantiate operator " + op + " without operand.");
		} else if (op.numOfOperands() != el.length)
			throw new SpecException("Cannot instantiate operator " + op
					+ " with " + el.length + " operands.");
		this.theOp = op;
		this.elements = el;
	}

	/**
	 * <p>
	 * Constructor for an unary specification.
	 * </p>
	 * 
	 * @param op
	 *            The operator.
	 * @param e1
	 *            The sub specification.
	 */
	public SpecExp(Operator op, Spec e1) {
		this.theOp = op;
		this.elements = new Spec[] { e1 };
	}

	/**
	 * <p>
	 * Constructor for a binary specification.
	 * </p>
	 * 
	 * @param op
	 *            The operator.
	 * @param e1
	 *            The first sub specification.
	 * @param e2
	 *            The second sub specification.
	 */
	public SpecExp(Operator op, Spec e1, Spec e2) {
		this.theOp = op;
		this.elements = new Spec[] { e1, e2 };
	}

	/**
	 * <p>
	 * Constructor for a triplet specification.
	 * </p>
	 * 
	 * @param op
	 *            The operator.
	 * @param e1
	 *            The first sub specification.
	 * @param e2
	 *            The second sub specification.
	 * @param e3
	 *            The third sub specification.
	 */
	public SpecExp(Operator op, Spec e1, Spec e2, Spec e3) {
		this.theOp = op;
		this.elements = new Spec[] { e1, e2, e3  };
	}

	/**
	 * <p>
	 * The operator representing this node.
	 * </p>
	 * 
	 * @return An operator representing this node.
	 */
	public Operator getOperator() {
		return theOp;
	};

	/**
	 * <p>
	 * Get the children specification of this node.
	 * <p>
	 * 
	 * @return The children specification.
	 */
	public Spec[] getChildren() {
		return this.elements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isCTLSpec()
	 */
	public boolean isCTLSpec() {
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isCTLSpec())
				return false;
		// checking that I'm prop or CTL
		return this.getOperator().isPropOp()
				| this.getOperator().isCTLOp();
	}

	public boolean isCTLKSpec() {
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isCTLKSpec())
				return false;
		// checking that I'm prop or CTL
		return this.getOperator().isPropOp()
				| this.getOperator().isCTLOp()
				| this.getOperator().isEpistemicOp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isRTCTLSpec()
	 */
	public boolean isRTCTLSpec() {
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isRTCTLSpec())
				return false;
		// checking that I'm prop, CTL or RealTimeCTL
		return this.getOperator().isPropOp()
				| this.getOperator().isCTLOp()
				| this.getOperator().isRTCTLOp();
	}

	public boolean isRTCTLKSpec() {
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isRTCTLKSpec())
				return false;
		// checking that I'm prop, CTL or RealTimeCTL
		return this.getOperator().isPropOp()
				| this.getOperator().isCTLOp()
				| this.getOperator().isRTCTLOp()
				| this.getOperator().isEpistemicOp();
	}

	/*
 * (non-Javadoc)
 *
 * @see edu.wis.jtlv.env.spec.Spec#isRTLTLSpec()
 */
	public boolean isRTLTLSpec() {
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isRTLTLSpec())
				return false;
		// checking that I'm prop, LTL or RealTimeLTL
		return this.getOperator().isPropOp()
				| this.getOperator().isLTLOp()
				| this.getOperator().isRTLTLOp();
	}

	@Override
	public boolean isRTLTLKSpec() {
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isRTLTLKSpec())
				return false;
		// checking that I'm prop, LTL, RealTimeLTL or epistemic logic
		return this.getOperator().isPropOp()
				| this.getOperator().isLTLOp()
				| this.getOperator().isRTLTLOp()
				| this.getOperator().isEpistemicOp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isLTLSpec()
	 */
	public boolean isLTLSpec() {
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isLTLSpec())
				return false;
		// checking that I'm prop, FutureLTL or PastLTL
		return this.getOperator().isPropOp()
				| this.getOperator().isLTLOp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isFutureLTLSpec()
	 */
	public boolean isFutureLTLSpec() {
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isFutureLTLSpec())
				return false;
		// checking that I'm prop or FutureLTL
		return this.getOperator().isPropOp()
				| this.getOperator().isFutureLTLOp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isPastLTLSpec()
	 */
	public boolean isPastLTLSpec() {
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isPastLTLSpec())
				return false;
		// checking that I'm prop or pastLTL
		return this.getOperator().isPropOp()
				| this.getOperator().isPastLTLOp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isCTLStarSpec()
	 */
	public boolean isCTLStarSpec() {
		// this is a bit redundant... it suppose to always return true...
		// return true;
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isCTLStarSpec())
				return false;
		// checking that I'm prop, LTL, CTL or RealTimeCTL
		return this.getOperator().isPropOp()
				| this.getOperator().isLTLOp()
				| this.getOperator().isRTLTLOp()
				| this.getOperator().isCTLsPathOp();
	}

	public boolean isATLsKSpec() { // RTATL*K
		// this is a bit redundant... it suppose to always return true...
		// return true;
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isATLsKSpec())
				return false;
		Operator op = this.getOperator();
		return op.isPropOp()
				| op.isLTLOp()
				| op.isRTLTLOp()
				| op.isCTLsPathOp()
				| op.isATLsPathOp()
				| op.isEpistemicOp();
	}

	// return true if this is a state formula
	// return false if there exists temporal operators that are not restricted by path quantifiers
	public boolean isStateSpec() {
		Operator op = this.getOperator();
		if(op.isPropOp()){
			for (Spec s : this.getChildren())
				if(!s.isStateSpec()) return false;
			return true;
		}else if (op.isLTLOp() || op.isRTLTLOp()) // temporal operators
			return false;
		else { // other operators, including path quantifiers and epistemic modalities
			return true;
		}
	}

	@Override
	public InternalSpecLanguage getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(InternalSpecLanguage language) {
		this.language = language;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isPropSpec()
	 */
	public boolean isPropSpec() {
		// checking that all children are prop.
		for (Spec s : this.getChildren())
			if (!s.isPropSpec())
				return false;
		// checking that I'm prop
		return this.getOperator().isPropOp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#hasTemporalOperators()
	 */
	public boolean hasTemporalOperators() {
		// if one of my elements is temporal.
		for (Spec s : this.getChildren())
			if (s.hasTemporalOperators())
				return true;
		// or I'm temporal.
		return this.theOp.isTemporalOp();
	}


	public boolean hasEpistemicOperators() {
		// if one of my elements is epistemic.
		for (Spec s : this.getChildren())
			if (s.hasEpistemicOperators())
				return true;
		// or I'm epistemic.
		return this.theOp.isEpistemicOp();
	}

	public boolean hasObsEpistemicOperators() {
		// if one of my elements is syn epistemic.
		for (Spec s : this.getChildren())
			if (s.hasObsEpistemicOperators())
				return true;
		// or I'm syn epistemic.
		return this.theOp.isObsEpistemicOp();
	}

	public boolean hasSynEpistemicOperators() {
		// if one of my elements is syn epistemic.
		for (Spec s : this.getChildren())
			if (s.hasSynEpistemicOperators())
				return true;
		// or I'm syn epistemic.
		return this.theOp.isSynEpistemicOp();
	}

	@Override
	public boolean hasPathOperators() {
		return hasCTLsPathOperators() | hasATLsPathOperators();
	}

	@Override
	public boolean hasCTLOperators() {
		for (Spec s : this.getChildren())
			if (s.hasCTLOperators())
				return true;
		return this.theOp.isCTLOp()|this.theOp.isRTCTLOp();
	}

	@Override
	public boolean hasCTLsPathOperators() {
		for (Spec s : this.getChildren())
			if (s.hasCTLsPathOperators())
				return true;
		return this.theOp.isCTLsPathOp();
	}
	@Override
	public boolean hasLTLOperators() {
		for (Spec s : this.getChildren())
			if (s.hasLTLOperators())
				return true;
		return this.theOp.isLTLOp()|this.theOp.isRTLTLOp();
	}
	@Override
	public boolean hasATLsPathOperators() {
		for (Spec s : this.getChildren())
			if (s.hasATLsPathOperators())
				return true;
		return this.theOp.isATLsPathOp();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		Operator op = this.getOperator();
		Spec[] ch = this.getChildren();

		// special cases
		if (op == Operator.AU)
			return "(A[" + ch[0] + " UNTIL " + ch[1] + ")";
		if (op == Operator.EU)
			return "(E[" + ch[0] + " UNTIL " + ch[1] + ")";
		if (op == Operator.ABF)
			return "(ABF " + ch[0] + " " + ch[1] + ")";
		if (op == Operator.EBF)
			return "(EBF " + ch[0] + " " + ch[1] + ")";
		if (op == Operator.ABG)
			return "(ABG " + ch[0] + " " + ch[1] + ")";
		if (op == Operator.EBG)
			return "(EBG " + ch[0] + " " + ch[1] + ")";
		if (op == Operator.ABU)
			return "A[" + ch[0] + " BUNTIL " + ch[1] + " " + ch[2] + "]";
		if (op == Operator.EBU)
			return "E[" + ch[0] + " BUNTIL " + ch[1] + " " + ch[2] + "]";

		//special cases of path quantifiers of ATL* and CTL*
		if (op == Operator.EE)
			return "(E " + ch[0] + ")";
		if (op == Operator.AA)
			return "(A " + ch[0] + ")";
		if (op == Operator.CAN_ENFORCE) {
			String agt_list = "";
			if(ch.length>1) {
				agt_list+=ch[0];
				for (int i = 1; i < ch.length-1; i++)
					agt_list+=","+ch[i];
			}
			return "(<" + agt_list + "> " + ch[ch.length - 1] + ")";
		}
		if (op == Operator.CANNOT_AVOID) {
			String agt_list = "";
			if(ch.length>1) {
				agt_list+=ch[0];
				for (int i = 1; i < ch.length-1; i++)
					agt_list+=","+ch[i];
			}
			return "([" + agt_list + "] " + ch[ch.length - 1] + ")";
		}

		// epistemic
		if (op == Operator.KNOW)
			return "(" + ch[0] + " KNOW " + ch[1] + ")";
		if (op == Operator.NKNOW)
			return "(" + ch[0] + " NKNOW " + ch[1] + ")";
		if (op == Operator.SKNOW)
			return "(" + ch[0] + " SKNOW " + ch[1] + ")";
		if (op == Operator.NSKNOW)
			return "(" + ch[0] + " NSKNOW " + ch[1] + ")";

		//special cases of RTLTL
		if (op == Operator.B_FINALLY)
//			return "(BF " + ch[0] + " " + ((SpecRange)ch[0]).getOriginSpec() + ")";
			return "(BF " + ch[0] + " " + ch[1] + ")";
		if (op == Operator.B_GLOBALLY)
//			return "(BG " + ch[0] + " " + ((SpecRange)ch[0]).getOriginSpec() + ")";
			return "(BG " + ch[0] + " " + ch[1] + ")";
		if (op == Operator.B_UNTIL)
//			return "("+((SpecRange)ch[0]).getOriginLeftSpec()+" BU " +ch[0]+ " " + ((SpecRange)ch[0]).getOriginSpec() + ")";
			return "(" + ch[0] + " BUNTIL " + ch[1] + " " + ch[2] + ")";
		if (op == Operator.B_RELEASES)
			return "(" + ch[0] + " BRELEASE " + ch[1] + " " + ch[2] + ")";

		// simple unary
		if (op.isUnary())
			return "(" + op + " " + ch[0] + ")";
		// simple binary
		if (op.isBinary())
			return "(" + ch[0] + " " + op + " " + ch[1] + ")";

		return "[!#$! Cannot Identify Expression]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof SpecExp))
			return false;
		SpecExp otherExp = (SpecExp) other;
		if (this.getOperator() != otherExp.getOperator())
			return false;

		Spec[] this_children = this.getChildren();
		Spec[] other_children = otherExp.getChildren();
		if (this_children.length != other_children.length)
			return false;

		for (int i = 0; i < this_children.length; i++)
			if (!this_children[i].equals(other_children[i]))
				return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#releventVars()
	 */
	public BDDVarSet releventVars() {
		BDDVarSet res = Env.getEmptySet();
		for (Spec s : this.getChildren()) {
			res = res.id().union(s.releventVars());
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#toBDD()
	 */
	public BDD toBDD() throws SpecException {
		if (!this.getOperator().isPropOp())
			throw new SpecException("Cannot convert temporal expression into"
					+ " BDD in specification: " + this.toString());
		// else building the BDD.
		Spec[] child = this.getChildren();
		Operator op = this.getOperator();

		if (op == Operator.NOT)
			return child[0].toBDD().not();
		if (op == Operator.AND)
			return child[0].toBDD().and(child[1].toBDD());
		if (op == Operator.OR)
			return child[0].toBDD().or(child[1].toBDD());
		if (op == Operator.XOR)
			return child[0].toBDD().xor(child[1].toBDD());
		if (op == Operator.XNOR)
			return child[0].toBDD().xor(child[1].toBDD()).not();
		if (op == Operator.IFF)
			return child[0].toBDD().biimp(child[1].toBDD());
		if (op == Operator.IMPLIES)
			return child[0].toBDD().imp(child[1].toBDD());

		throw new SpecException("Cannot evaluate operator " + op
				+ " in specification: " + this.toString());
	}
}
