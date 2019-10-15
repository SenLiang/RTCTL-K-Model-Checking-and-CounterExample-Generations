package edu.wis.jtlv.env.core.spec;

public enum InternalOp {
	// UNARY............
	// Prop
	NOT,
	AA, // all path
	EE, // some path
	// LTL
	FINALLY, GLOBALLY, HISTORICALLY, NEXT, NOT_PREV_NOT, ONCE, PREV,

	// CTL
	EX, EF, EG, AX, AF, AG,

	// BINARY............
	// Prop
	AND, OR, XOR, XNOR, IFF, IMPLIES,
	// LTL
	RELEASES, SINCE, TRIGGERED, UNTIL,
	// CTL
	ABF, ABG, EBF, EBG, AU, EU,
	// RTLTL
	B_FINALLY, B_GLOBALLY, B_UNTIL,
	//Knowledge
	KNOW, SKNOW, NKNOW, NSKNOW,
	//ATL*
	CAN_ENFORCE, // ATL* can enforce
	CANNOT_AVOID, // ATL* cannot avoid

	// TRIPLET............
	// Prop
	// LTL
	// CTL
	ABU, EBU,
	//RTLTL
	B_RELEASES;


	// Extra Prop - Binary
	// EQ, NEQ, LT, GT, LE, GE, SETIN, UNION, LSHIFT, RSHIFT, MOD, PLUS, MINUS,
	// TIMES, DIVIDE, CONCATENATION;

	// ////////////////////////////////////////////////////////////////////////
	// Groupings //////////////////////////////////////////////////////////////
	// number of operands...
	public static final InternalOp[] unaryOp = { NOT, FINALLY, GLOBALLY, HISTORICALLY,
			NEXT, NOT_PREV_NOT, ONCE, PREV, EX, EF, EG, AX, AF, AG };
	public static final InternalOp[] binaryOp = { AND, OR, XOR, XNOR, IFF, IMPLIES,
			RELEASES, SINCE, TRIGGERED, UNTIL, ABF, ABG, EBF, EBG, AU, EU, B_FINALLY, B_GLOBALLY, KNOW, SKNOW, NKNOW, NSKNOW, CAN_ENFORCE, CANNOT_AVOID};
	public static final InternalOp[] tripletOp = { ABU, EBU, B_UNTIL, B_RELEASES};

	// is it propositional, or TL operator.
	public static final InternalOp[] propOp = { NOT, AND, OR, XOR, XNOR, IFF, IMPLIES };
	public static final InternalOp[] FutureLTLOp = { FINALLY, GLOBALLY, NEXT, RELEASES,
			UNTIL, B_FINALLY, B_GLOBALLY, B_UNTIL, B_RELEASES};
	public static final InternalOp[] PastLTLOp = { HISTORICALLY, NOT_PREV_NOT, ONCE,
			PREV, SINCE, TRIGGERED };
	public static final InternalOp[] CTLOp = { EX, EF, EG, AX, AF, AG, ABF, ABG, EBF,
			EBG, AU, EU, ABU, EBU };
	public static final InternalOp[] RealTimeCTLOp = { ABF, ABG, EBF, EBG, ABU, EBU };
	public static final InternalOp[] RealTimeLTLOp = { B_FINALLY, B_GLOBALLY, B_UNTIL, B_RELEASES};
	public static final InternalOp[] EpistemicOp = { KNOW, SKNOW, NKNOW, NSKNOW };
	public static final InternalOp[] SynEpistemicOp = { SKNOW, NSKNOW };

	private boolean in(InternalOp[] set) {
		for (InternalOp op : set)
			if (this == op)
				return true;
		return false;
	}

	// ////////////////////////////////////////////////////////////////////////

	public boolean isProp() {
		return this.in(propOp);
	}

	public boolean isLTLOp() {
		return this.in(FutureLTLOp) | this.in(PastLTLOp);
	}

	public boolean isFutureLTLOp() {
		return this.in(FutureLTLOp);
	}

	public boolean isPastLTLOp() {
		return this.in(PastLTLOp);
	}

	public boolean isCTLOp() {
		return this.in(CTLOp) | this.in(RealTimeCTLOp);
	}

	public boolean isRealTimeCTLOp() {
		return this.in(RealTimeCTLOp);
	}

	public boolean isRealTimeLTLOp() {
		return this.in(RealTimeLTLOp);
	}

	public boolean isEpistemicOp() {
		return this.in(EpistemicOp);
	}

	public boolean isSynEpistemicOp() {
		return this.in(SynEpistemicOp);
	}

	public boolean isTemporalOp() {
		return this.in(FutureLTLOp) | this.in(PastLTLOp) | this.in(CTLOp)
				| this.in(RealTimeCTLOp) | this.in(RealTimeLTLOp);
	}

	public boolean isUnary() {
		return this.in(unaryOp);
	}

	public boolean isBinary() {
		return this.in(binaryOp);
	}

	public boolean isTriplet() {
		return this.in(tripletOp);
	}

	public int numOfOperands() {
		if (this.in(unaryOp))
			return 1;
		if (this.in(binaryOp))
			return 2;
		if (this.in(tripletOp))
			return 3;
		return -1;
	}
	
	public String toString() {
		return super.toString();
	}
}
