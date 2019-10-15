package edu.wis.jtlv.env.spec;

import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.core.spec.InternalSpecLanguage;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDVarSet;

public class SpecAgentIdentifier implements Spec {
	private String agentName;
	private Spec originSpec;//the spec without translating
	private Spec originLeftSpec;//the spec without translating
	private InternalSpecLanguage language=InternalSpecLanguage.UNDEF;

	public SpecAgentIdentifier(String agentName) {
		this.agentName = agentName;
	}

	public String getAgentName() { return this.agentName; }

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
		return true;
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
		return false;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getAgentName();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof SpecAgentIdentifier))
			return false;
		SpecAgentIdentifier otherCTLAgentId = (SpecAgentIdentifier) other;
		return (this.agentName.equals(otherCTLAgentId.getAgentName()));
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
		throw new SpecException("Cannot convert Agent Identifier into BDD.");
	}

	public Spec getOriginSpec() {
		return originSpec;
	}

	public void setOriginSpec(Spec originSpec) {
		this.originSpec = originSpec;
	}

	public Spec getOriginLeftSpec() {
		return originLeftSpec;
	}

	public void setOriginLeftSpec(Spec originLeftSpec) {
		this.originLeftSpec = originLeftSpec;
	}
}
