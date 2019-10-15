package edu.wis.jtlv.env.core.spec;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

public class InternalSpecAgentIdentifier extends InternalSpec {

	public InternalSpecAgentIdentifier(String agentName, Token start) {
		super(agentName, start);
	}

	public String getAgentName() {
		return this.exp_str;
	}

	@Override
	public void evalBDDChildrenExp(TokenStream input)
			throws SpecParseException {
		// do nothing.
		this.setChildrenBDDsAsEvaluated(true);
	}

	@Override
	public boolean isPropSpec() {
		return true;
	}

	@Override
	public boolean isCTLSpec() {
		return true;
	}

	@Override
	public boolean isRealTimeCTLSpec() {
		return true;
	}

	@Override
	public boolean isLTLSpec() {
		return true;
	}

	@Override
	public boolean isFutureLTLSpec() {
		return true;
	}

	@Override
	public boolean isPastLTLSpec() {
		return true;
	}

	@Override
	public boolean isCTLStarSpec() {
		return true;
	}

	@Override
	public boolean hasTemporalOperators() {
		return false;
	}

	@Override
	public boolean hasEpistemicOperators() {
		return true;
	}

	@Override
	public boolean hasRealTimeOperators() {
		return false;
	}

	@Override
	public String toString() {
		return this.getAgentName();
	}
}
