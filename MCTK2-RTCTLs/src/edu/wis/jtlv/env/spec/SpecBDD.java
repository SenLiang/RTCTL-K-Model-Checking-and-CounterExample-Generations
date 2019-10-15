package edu.wis.jtlv.env.spec;

import edu.wis.jtlv.env.core.spec.InternalSpecLanguage;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDVarSet;
import edu.wis.jtlv.env.Env;

/**
 * <p>
 * First order specification which is parsed as an SMV expression.
 * </p>
 * 
 * @version {@value edu.wis.jtlv.env.Env#version}
 * @author yaniv sa'ar.
 * 
 */
public class SpecBDD implements Spec {
	private BDD val = null;
	private String identifying_expr = "";
	private InternalSpecLanguage language=InternalSpecLanguage.UNDEF;

	/**
	 * <p>
	 * A constructor for a leaf BDD specification, with a string representation.
	 * </p>
	 * 
	 * @param identifying_expr
	 *            The string from which this leaf was constructed.
	 * @param v
	 *            The BDD value (if already evaluated).
	 */
	public SpecBDD(String identifying_expr, BDD v) {
		this.identifying_expr = identifying_expr;
		this.val = v;
	}

	/**
	 * <p>
	 * A constructor for a leaf BDD specification, without a string
	 * representation.
	 * </p>
	 * 
	 * @param v
	 *            The evaluated BDD.
	 */
	public SpecBDD(BDD v) {
		this("", v);
	}

	/**
	 * <p>
	 * Getter for the BDD value.
	 * </p>
	 * 
	 * @return The BDD value.
	 * 
	 * @see SpecBDD#toBDD()
	 */
	public BDD getVal() {
		return this.val;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isCTLSpec()
	 */
	public boolean isCTLSpec() {
		return true;
	}

	@Override
	public boolean isCTLKSpec() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isRTCTLSpec()
	 */
	public boolean isRTCTLSpec() {
		return true;
	}

	@Override
	public boolean isRTCTLKSpec() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see edu.wis.jtlv.env.spec.Spec#isRTLTLSpec()
	 */
	public boolean isRTLTLSpec() {
		return true;
	}

	@Override
	public boolean isRTLTLKSpec() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isCTLStarSpec()
	 */
	public boolean isCTLStarSpec() {
		return true;
	}

	public boolean isATLsKSpec() {
		return true;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isLTLSpec()
	 */
	public boolean isLTLSpec() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isFutureLTLSpec()
	 */
	public boolean isFutureLTLSpec() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#isPastLTLSpec()
	 */
	public boolean isPastLTLSpec() {
		return true;
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
		return true;
	}

	public boolean isStateSpec() { return true; }

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#hasTemporalOperators()
	 */
	public boolean hasTemporalOperators() {
		return false;
	}

	public boolean hasEpistemicOperators() {
		return false;
	}
	public boolean hasObsEpistemicOperators() { return false; }
	public boolean hasSynEpistemicOperators() { return false; }

	@Override
	public boolean hasPathOperators() {
		return false;
	}

	@Override
	public boolean hasLTLOperators() {
		return false;
	}
	@Override
	public boolean hasCTLOperators() {
		return false;
	}

	@Override
	public boolean hasCTLsPathOperators() {
		return false;
	}

	@Override
	public boolean hasATLsPathOperators() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (this.getVal() == null)
			if (this.identifying_expr != "")
				return "\"" + this.identifying_expr + "\"";
			else
				return "[!$#! Cannot Identify BDD Expression]";
		return "#[" + Env.toNiceSignleLineString(null, this.getVal()) + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof SpecBDD))
			return false;
		SpecBDD otherBDD = (SpecBDD) other;
		if ((this.val == null) || (otherBDD.val == null))
			return false;
		return this.val.equals(otherBDD.val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#releventVars()
	 */
	public BDDVarSet releventVars() {
		return this.getVal().support();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wis.jtlv.env.spec.Spec#toBDD()
	 */
	public BDD toBDD() throws SpecException {
		return this.getVal();
	}
}
