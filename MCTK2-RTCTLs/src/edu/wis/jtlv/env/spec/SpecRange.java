package edu.wis.jtlv.env.spec;

import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.core.spec.InternalSpecLanguage;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDVarSet;

/**
 * <p>
 * This node is a range node for the Real Time CTL operators.<br>
 * Simple ranges are not assigned with such a node, and are evaluated as a smv
 * expressions.<br>
 * For consistency, this is not regarded as a temporal operator in the queries.
 * Its holder should mark it as a temporal operator.
 * </p>
 *
 * <p>
 * UPDATING20170428<br/>
 * RTLTL, RTCTL and RTCTL* all have range expression,
 * this node is not need to specific just for CTL(so change name to SpecRange)
 * </p>
 * 
 * @version {@value edu.wis.jtlv.env.Env#version}
 * @author yaniv sa'ar.
 * 
 */
public class SpecRange implements Spec {
	private int from;
	private int to;
	private InternalSpecLanguage language=InternalSpecLanguage.UNDEF;

	/**
	 * <p>
	 * A constructor for (Real Time) CTL range.
	 * </p>
	 * 
	 * @param from range start.
	 * @param to range end
	 */
	public SpecRange(int from, int to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * <p>
	 * Get the range start.
	 * </p>
	 * 
	 * @return The range start.
	 */
	public int getFrom() {
		return this.from;
	}

	/**
	 * <p>
	 * Get the range end.
	 * </p>
	 * 
	 * @return The range end.
	 */
	public int getTo() {
		return this.to;
	}

	@Override
	public InternalSpecLanguage getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(InternalSpecLanguage language) {
		this.language = language;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.env.spec.Spec#isPropSpec()
	 */
	public boolean isPropSpec() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.env.spec.Spec#isCTLSpec()
	 */
	public boolean isCTLSpec() {
		return true;
	}

	@Override
	public boolean isCTLKSpec() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.env.spec.Spec#isRTCTLSpec()
	 */
	public boolean isRTCTLSpec() {
		return true;
	}

	@Override
	public boolean isRTCTLKSpec() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.env.spec.Spec#isRTLTLSpec()
	 */
	public boolean isRTLTLSpec() {
		return true;
	}

	@Override
	public boolean isRTLTLKSpec() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.env.spec.Spec#isLTLSpec()
	 */
	public boolean isLTLSpec() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.env.spec.Spec#isFutureLTLSpec()
	 */
	public boolean isFutureLTLSpec() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.env.spec.Spec#isPastLTLSpec()
	 */
	public boolean isPastLTLSpec() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.env.spec.Spec#isCTLStarSpec()
	 */
	public boolean isCTLStarSpec() {
		return true;
	}

	public boolean isATLsKSpec() {
		return true;
	}

	@Override
	public boolean isStateSpec() {
		return true;
	}

	/* (non-Javadoc)
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
	public boolean hasCTLOperators() {
		return false;
	}
	@Override
	public boolean hasLTLOperators() {
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "#[" + this.getFrom() + ".." + this.getTo() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof SpecRange))
			return false;
		SpecRange otherCTLRange = (SpecRange) other;
		return ((this.from == otherCTLRange.from) && (this.to == otherCTLRange.to));
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.env.spec.Spec#releventVars()
	 */
	public BDDVarSet releventVars() {
		return Env.getEmptySet();
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.env.spec.Spec#toBDD()
	 */
	public BDD toBDD() throws SpecException {
		throw new SpecException("Cannot convert CTL range into BDD.");
	}

}
