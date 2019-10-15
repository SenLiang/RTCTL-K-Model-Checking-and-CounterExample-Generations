grammar SPC;

options {
	output=AST;
	//backtrack = true;
	//k=4;
}

tokens {
	NOP; // an empty token to help identify components at the walker stage.
	SPEC_LIST_T;
	SUBRANGE_T;
	VALUE_T; 
	SET_LIST_EXP_T;
	BLOCK_T;
	CASE_LIST_EXPR_T;
	CASE_ELEMENT_EXPR_T;
	BIT_SELECT_T;
	ARRAY_INDEX_T;
	TOK_UNARY_MINUS_T;
	PURE_CTL_T;
	PURE_LTL_T;
} // an imaginary node

@header {
package edu.wis.jtlv.env.core.spec;
import edu.wis.jtlv.env.Env;
}
@members {
// for exception handling
public String getErrorMessage(RecognitionException e, String[] tokenNames) {
	String msg = null;
	if (e instanceof SpecParseException) {
		msg = e.toString();
		Env.doError(e, msg);
	} else {
		msg = super.getErrorMessage(e, tokenNames);
		Env.doError(e, msg);
	}
	return msg;
}

public void emitErrorMessage(String msg) {
	// System.err.println(msg);
	// do nothing.
}

// I don't like the printing...
public void recoverFromMismatchedToken(IntStream input,
		RecognitionException e, int ttype, BitSet follow)
		throws RecognitionException {
	//System.err.println("BR.recoverFromMismatchedToken");
	// if next token is what we are looking for then "delete" this token
	if (input.LA(2) == ttype) {
		reportError(e);
		/*
		 * System.err.println("recoverFromMismatchedToken deleting
		 * "+input.LT(1)+ " since "+input.LT(2)+" is what we want");
		 */
		beginResync();
		input.consume(); // simply delete extra token
		endResync();
		input.consume(); // move past ttype token as if all were ok
		return;
	}
	if (!recoverFromMismatchedElement(input, e, follow)) {
		throw e;
	}
}

public static boolean in_my_recovery_mode = false;
public boolean er() {
	//if (input.LA(1) == TOK_SEMI)
	//	in_my_recovery_mode = true;
	return errorRecovery | in_my_recovery_mode;
}
public void recover(IntStream input, RecognitionException re) {
	in_my_recovery_mode = true;
	super.recover(input, re);
}
public void consumeUntilSpecStart(TokenStream input) throws SpecParseException {
	int ttype = input.LA(1);
	
	Token tstart = input.LT(1);
	Token tstop = null;
	while (ttype != Token.EOF && ttype != SPCLexer.TOK_INVAR_SPEC && ttype != SPCLexer.TOK_CTL_SPEC && ttype != SPCLexer.TOK_LTL_SPEC && ttype != SPCLexer.TOK_CTL_STAR_SPEC) {
		tstop = input.LT(1);
		input.consume();
		ttype = input.LA(1);
	}
	// if there is something to  throw, i.e. there was a problem.
	if (tstop != null) {
		throw new SpecParseException("Failed to parse expression '" + input.toString(tstart, tstop) + "'" , input, tstart, tstop);
	}
}
}

@lexer::header {
package edu.wis.jtlv.env.core.spec;
import edu.wis.jtlv.env.Env;
}
@lexer::members {
public String getErrorMessage(RecognitionException e, String[] tokenNames) {
	String msg = null;
	if (e instanceof SpecParseException) {
		msg = e.toString();
		Env.doError(e, msg);
	} else {
		msg = super.getErrorMessage(e, tokenNames);
		Env.doError(e, msg);
	}
	return msg;
}
public void emitErrorMessage(String msg) {
	// System.err.println(msg);
	// do nothing.
}
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// SPEC tree construction....
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
spec						returns[WAArrayOfSpec ret]
								: EOF
								| spec_list EOF
								{ $ret = $spec_list.ret; }
								-> ^(SPEC_LIST_T spec_list)
								;
spec_list					returns[WAArrayOfSpec ret]
@init {$ret = new WAArrayOfSpec(); }
								: f=spec_element { if(!er()) $ret.specs.add($f.ret); else $ret.specs.add(null); in_my_recovery_mode = false; }
								( s=spec_element { if(!er()) $ret.specs.add($s.ret); else $ret.specs.add(null); in_my_recovery_mode = false; }
								)*
								;
spec_element				returns[InternalSpec ret]
								: invar_spec {if(!er()) $ret = $invar_spec.ret; consumeUntilSpecStart(input); }
								| ctl_spec {if(!er()) $ret = $ctl_spec.ret; consumeUntilSpecStart(input); }
								| ltl_spec {if(!er()) $ret = $ltl_spec.ret; consumeUntilSpecStart(input); }
								| ctls_spec {if(!er()) $ret = $ctls_spec.ret; consumeUntilSpecStart(input); }
								;

//spec_list					returns[WAArrayOfSpec ret]
//								: {$ret = new WAArrayOfSpec(); }
//								| fi=invar_spec r=spec_list { if(!er()) $ret = $r.ret; if(!er()) $ret.specs.add($fi.ret); else $ret.specs.add(null); }
//								| fc=ctl_spec r=spec_list { if(!er()) $ret = $r.ret; if(!er()) if(!errorRecovery) $ret.specs.add($fc.ret); else $ret.specs.add(null); }
//								| fl=ltl_spec r=spec_list { if(!er()) $ret = $r.ret; if(!er()) $ret.specs.add($fl.ret); else $ret.specs.add(null); }
//								| fcs=ctls_spec r=spec_list { if(!er()) $ret = $r.ret; if(!er()) $ret.specs.add($fcs.ret); else $ret.specs.add(null); }
//								;


invar_spec					returns[InternalSpec ret]
@after { if (!er() && ($ret instanceof InternalSpecBDD)) ((InternalSpecBDD) $ret).evalBDDExp(input); }
								: TOK_INVAR_SPEC^ simple_root_expr optsemi! {if(!er()) $ret = $simple_root_expr.ret; }
								;
ctl_spec					returns[InternalSpec ret]
@after { if (!er() && ($ret instanceof InternalSpecBDD)) ((InternalSpecBDD) $ret).evalBDDExp(input); }
								: TOK_CTL_SPEC^ ctl_root_expr optsemi! {if(!er()) $ret = $ctl_root_expr.ret; }
								;
ltl_spec					returns[InternalSpec ret]
@after { if (!er() && ($ret instanceof InternalSpecBDD)) ((InternalSpecBDD) $ret).evalBDDExp(input); }
								: TOK_LTL_SPEC^ ltl_root_expr optsemi! {if(!er()) $ret = $ltl_root_expr.ret; }
								;
ctls_spec					returns[InternalSpec ret]
@after { if (!er() && ($ret instanceof InternalSpecBDD)) ((InternalSpecBDD) $ret).evalBDDExp(input); }
								: TOK_CTL_STAR_SPEC^ ctls_root_expr optsemi! {if(!er()) $ret = $ctls_root_expr.ret; }
								;

/* --------------------------------------------------------------------- */
/* ---------------------------- EXPRESSION ----------------------------- */
/* --------------------------------------------------------------------- */
///////////////////////////////////////////////////////////////////////////////
// SIMPLE NON TEMPORAL ROOT EXPRESSION ////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
simple_root_expr			returns[InternalSpec ret]
								: implies_expr {if(!er()) $ret = $implies_expr.ret; }
								;
implies_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=iff_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_IMPLIES^ s=implies_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_imply(input, $start, exp_str, $ret, $s.ret); }
								)?
								; /* right association */
iff_expr					returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=or_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_IFF^ s=or_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_iff(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
or_expr						returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=and_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_OR^ s=and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_or(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_XOR^ s=and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_xor(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_XNOR^ s=and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_xnor(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
and_expr					returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=relational_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_AND^ s=relational_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_and(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
relational_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=in_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_EQUAL^ s=in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_eq(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_NOTEQUAL^ s=in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_neq(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_LT^ s=in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_lt(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_GT^ s=in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_gt(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_LE^ s=in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_le(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_GE^ s=in_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ge(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
in_expr						returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=union_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_SETIN^ s=union_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_setin(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
union_expr					returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=set_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_UNION^ s=set_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_union(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
set_expr					returns[InternalSpec ret]
@init {boolean append_end = false;}
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: shift_expr
								{ if(!er()) $ret = $shift_expr.ret; }
								| subrange 
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_range(input, $start, $subrange.text); }
								| TOK_LCB set_list_expr TOK_RCB
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_set(input, $start, $TOK_LCB.text + " " + $set_list_expr.text + " " + $TOK_RCB.text); }
								-> ^(SET_LIST_EXP_T set_list_expr)
								;
set_list_expr				// do nothing...
								: simple_root_expr (TOK_COMMA! simple_root_expr)*
								;
shift_expr					returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=remainder_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_LSHIFT^ s=remainder_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_lshift(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_RSHIFT^ s=remainder_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_rshift(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
remainder_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=additive_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_MOD^ s=additive_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_mod(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
additive_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=multiplicative_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_PLUS^ s=multiplicative_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_plus(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_MINUS^ s=multiplicative_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_minus(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
multiplicative_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=concatination_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_TIMES^ s=concatination_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_times(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_DIVIDE^ s=concatination_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_divide(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
concatination_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=primary_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_CONCATENATION^ s=primary_expr
								{ if (!er()) exp_str += $op.text + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_concatenation(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
								
primary_expr				returns[InternalSpec ret]
@init {boolean append_end = false; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: primary_expr_helper1
								{ if(!er()) $ret = $primary_expr_helper1.ret; }
								// there is no need to work with primary expr...
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1.text); }
								| op=TOK_MINUS v=primary_expr
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_unary_minus(input, $start, $op.text + " " + $v.text, $v.ret); }
								-> ^(TOK_UNARY_MINUS_T primary_expr)
								| op=TOK_NOT v=primary_expr
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_not(input, $start, $op.text + " " + $v.text, $v.ret); }
								-> ^(TOK_NOT primary_expr)
								;

primary_expr_helper1		returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: constant primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1.text); }
								-> ^(VALUE_T constant NOP primary_expr_select)
								| primary_expr_helper1_pointer1
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1.text); }
//								| primary_expr_helper1_pointer2
								/* simple paren are the only case where we should start over
								from the "real" begining of the parsed expression... */
								| TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1.text); }
								-> ^(BLOCK_T simple_root_expr NOP primary_expr_select)
								| TOK_BOOL  TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1.text); }
								-> ^(TOK_BOOL  simple_root_expr NOP primary_expr_select)
								| TOK_WORD1 TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1.text); }
								-> ^(TOK_WORD1 simple_root_expr NOP primary_expr_select)
								| TOK_NEXT TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1.text); }
								-> ^(TOK_NEXT simple_root_expr NOP primary_expr_select)
								| TOK_CASE case_element_list_expr TOK_ESAC primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1.text); }
								-> ^(CASE_LIST_EXPR_T case_element_list_expr NOP primary_expr_select)
								| TOK_WAREAD TOK_LP simple_root_expr TOK_COMMA simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1.text); }
								-> ^(TOK_WAREAD simple_root_expr simple_root_expr NOP primary_expr_select)
								| TOK_WAWRITE TOK_LP simple_root_expr TOK_COMMA simple_root_expr TOK_COMMA simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1.text); }
								-> ^(TOK_WAWRITE simple_root_expr simple_root_expr simple_root_expr NOP primary_expr_select)
								;

///////////////////////////////////////////////////////////////////////////////
// ROOT OF CTL TEMPORAL EXPRESSIONS ///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
ctl_root_expr				returns[InternalSpec ret]
								: ctl_implies_expr {if(!er()) $ret = $ctl_implies_expr.ret; }
								;
ctl_implies_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_iff_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_IMPLIES^ s=ctl_implies_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_imply(input, $start, exp_str, $ret, $s.ret); }
								)?
								; /* right association */
ctl_iff_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_or_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_IFF^ s=ctl_or_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_iff(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctl_or_expr					returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_and_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_OR^ s=ctl_and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_or(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_XOR^ s=ctl_and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_xor(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_XNOR^ s=ctl_and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_xnor(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctl_and_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_AND^ s=ctl_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_and(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctl_expr					returns[InternalSpec ret]
								: (TOK_NOT* // resolving conflict with the TOK_NOT
									( TOK_EX 
									| TOK_AX 
									| TOK_EF 
									| TOK_AF 
									| TOK_EG 
									| TOK_AG 
									| TOK_AA 
									| TOK_EE 
									| TOK_EBF 
									| TOK_ABF 
									| TOK_EBG 
									| TOK_ABG)) => pure_ctl_expr
								{ if(!er()) $ret = $pure_ctl_expr.ret; }
								-> ^(PURE_CTL_T pure_ctl_expr)
								| ctl_relational_expr
								{ if(!er()) $ret = $ctl_relational_expr.ret; }
								;
pure_ctl_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_EX^ f=ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EX(input, $start, exp_str, $f.ret); }
								| op=TOK_AX^ f=ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_AX(input, $start, exp_str, $f.ret); }
								| op=TOK_EF^ f=ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EF(input, $start, exp_str, $f.ret); }
								| op=TOK_AF^ f=ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_AF(input, $start, exp_str, $f.ret); }
								| op=TOK_EG^ f=ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EG(input, $start, exp_str, $f.ret); }
								| op=TOK_AG^ f=ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_AG(input, $start, exp_str, $f.ret); }
								//| (TOK_AA TOK_LB ctl_root_expr TOK_UNTIL) => op=TOK_AA^ lb=TOK_LB! fre=ctl_root_expr opu=TOK_UNTIL sre=ctl_root_expr rb=TOK_RB!
								//{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + " " + $opu.text + " " + $sre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_AU(input, $start, exp_str, $fre.ret, $sre.ret); }
								//| (TOK_AA TOK_LB ctl_root_expr TOK_BUNTIL) => op=TOK_AA^ lb=TOK_LB! fre=ctl_root_expr opu=TOK_BUNTIL msr=subrange sre=ctl_root_expr rb=TOK_RB!
								//{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + " " + $opu.text + " " + $msr.text + " " + $sre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ABU(input, $start, exp_str, $fre.ret, $msr.ret, $sre.ret); }
								//| (TOK_EE TOK_LB ctl_root_expr TOK_UNTIL) => op=TOK_EE^ lb=TOK_LB! fre=ctl_root_expr opu=TOK_UNTIL sre=ctl_root_expr rb=TOK_RB!
								//{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + " " + $opu.text + " " + $sre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EU(input, $start, exp_str, $fre.ret, $sre.ret); }
								//| (TOK_EE TOK_LB ctl_root_expr TOK_BUNTIL) => op=TOK_EE^ lb=TOK_LB! fre=ctl_root_expr opu=TOK_BUNTIL msr=subrange sre=ctl_root_expr rb=TOK_RB!
								//{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + " " + $opu.text + " " + $msr.text + " " + $sre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EBU(input, $start, exp_str, $fre.ret, $msr.ret, $sre.ret); }
								| (TOK_AA TOK_LB ctl_root_expr TOK_UNTIL) => ctl_au { if(!er()) $ret = $ctl_au.ret; }
								| (TOK_AA TOK_LB ctl_root_expr TOK_BUNTIL) => ctl_abu { if(!er()) $ret = $ctl_abu.ret; }
								| (TOK_EE TOK_LB ctl_root_expr TOK_UNTIL) => ctl_eu { if(!er()) $ret = $ctl_eu.ret; }
								| (TOK_EE TOK_LB ctl_root_expr TOK_BUNTIL) => ctl_ebu { if(!er()) $ret = $ctl_ebu.ret; }
								| op=TOK_EBF^ fsr=subrange s=ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EBF(input, $start, exp_str, $fsr.ret, $s.ret); }
								| op=TOK_ABF^ fsr=subrange s=ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ABF(input, $start, exp_str, $fsr.ret, $s.ret); }
								| op=TOK_EBG^ fsr=subrange s=ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EBG(input, $start, exp_str, $fsr.ret, $s.ret); }
								| op=TOK_ABG^ fsr=subrange s=ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ABG(input, $start, exp_str, $fsr.ret, $s.ret); }
								// know
								//| TOK_LP! var_id opk=TOK_KNOW^ f=ctl_root_expr TOK_RP!
								//{ if (!er()) exp_str = $agent.text + " " + $opk.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ctl_know(input, $start, exp_str, $agent.ret, $f.ret); }
								| (TOK_LP var_id TOK_KNOW) => ctl_know { if(!er()) $ret = $ctl_know.ret; }
								/* NOT is required here to allow such expr as "! EX a" */
								| op=TOK_NOT^ fp=pure_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $fp.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_not(input, $start, exp_str, $fp.ret); }
								;
ctl_au						returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_AA^ lb=TOK_LB! fre=ctl_root_expr opu=TOK_UNTIL sre=ctl_root_expr rb=TOK_RB!
								{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + " " + $opu.text + " " + $sre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_AU(input, $start, exp_str, $fre.ret, $sre.ret); }
								;
ctl_eu						returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_EE^ lb=TOK_LB! fre=ctl_root_expr opu=TOK_UNTIL sre=ctl_root_expr rb=TOK_RB!
								{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + " " + $opu.text + " " + $sre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EU(input, $start, exp_str, $fre.ret, $sre.ret); }
								;
ctl_abu						returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_AA^ lb=TOK_LB! fre=ctl_root_expr opu=TOK_BUNTIL msr=subrange sre=ctl_root_expr rb=TOK_RB!
								{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + " " + $opu.text + " " + $msr.text + " " + $sre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ABU(input, $start, exp_str, $fre.ret, $msr.ret, $sre.ret); }
								;
ctl_ebu						returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_EE^ lb=TOK_LB! fre=ctl_root_expr opu=TOK_BUNTIL msr=subrange sre=ctl_root_expr rb=TOK_RB!
								{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + " " + $opu.text + " " + $msr.text + " " + $sre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EBU(input, $start, exp_str, $fre.ret, $msr.ret, $sre.ret); }
								;
/*
ctl_know						returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: agent=ctl_root_expr opk=TOK_KNOW f=ctl_root_expr
								{ if (!er()) exp_str = $agent.text + " " + $opk.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ctl_know(input, $start, exp_str, $agent.ret, $f.ret); }
								;
*/	
ctl_know				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								//: TOK_LP! agent=var_id op=TOK_KNOW^ f=ctl_root_expr TOK_RP! 
								: TOK_LP agent=var_id op=TOK_KNOW^ f=ctl_root_expr TOK_RP
								{ if (!er()) exp_str += $agent.text + " " + $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ctl_know(input, $start, exp_str, $agent.ret, $f.ret); }
								;

var_id		returns[String ret]
		: TOK_ATOM
		( TOK_DOT! TOK_ATOM
		| TOK_DOT! TOK_NUMBER
		| TOK_LB simple_root_expr TOK_RB)*
		{ if(!er()) $ret = $text; }
		;
							
ctl_relational_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_in_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_EQUAL^ s=ctl_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_eq(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_NOTEQUAL^ s=ctl_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_neq(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_LT^ s=ctl_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_lt(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_GT^ s=ctl_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_gt(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_LE^ s=ctl_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_le(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_GE^ s=ctl_in_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ge(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctl_in_expr					returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_union_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_SETIN^ s=ctl_union_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_setin(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctl_union_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_set_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_UNION^ s=ctl_set_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_union(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctl_set_expr				returns[InternalSpec ret]
@init {boolean append_end = false; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: ctl_shift_expr
								{ if(!er()) $ret = $ctl_shift_expr.ret; }
								| subrange 
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_range(input, $start, $subrange.text); }
								| TOK_LCB ctl_set_list_expr TOK_RCB
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_set(input, $start, $TOK_LCB.text + " " + $ctl_set_list_expr.text + " " + $TOK_RCB.text); }
								-> ^(SET_LIST_EXP_T ctl_set_list_expr)
								;
ctl_set_list_expr			// do nothing...
								: simple_root_expr (TOK_COMMA! simple_root_expr)*
								; /* these are simple expression, not ctl expressions */
ctl_shift_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_remainder_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_LSHIFT^ s=ctl_remainder_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_lshift(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_RSHIFT^ s=ctl_remainder_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_rshift(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctl_remainder_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_additive_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_MOD^ s=ctl_additive_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_mod(input, $start, exp_str, $ret, $s.ret); }
								)*
								;


ctl_additive_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_multiplicative_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_PLUS^ s=ctl_multiplicative_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_plus(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_MINUS^ s=ctl_multiplicative_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_minus(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctl_multiplicative_expr		returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_concatination_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_TIMES^ s=ctl_concatination_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_times(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_DIVIDE^ s=ctl_concatination_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_divide(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctl_concatination_expr		returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctl_primary_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_CONCATENATION^ s=ctl_primary_expr
								{ if (!er()) exp_str += $op.text + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_concatenation(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
								
ctl_primary_expr			returns[InternalSpec ret]
@init {boolean append_end = false; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: ctl_primary_expr_helper1
								{ if(!er()) $ret = $ctl_primary_expr_helper1.ret; }
								| op=TOK_MINUS v=ctl_primary_expr
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_unary_minus(input, $start, $op.text + " " + $v.text, $v.ret); }
								-> ^(TOK_UNARY_MINUS_T $v)
								| op=TOK_NOT v=ctl_primary_expr
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_not(input, $start, $op.text + " " + $v.text, $v.ret); }
								-> ^(TOK_NOT $v)
								;

ctl_primary_expr_helper1	returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: constant primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $constant.text + " " + $primary_expr_select.text); }
								-> ^(VALUE_T constant NOP primary_expr_select)
								| primary_expr_helper1_pointer1
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1_pointer1.text); }
//								| primary_expr_helper1_pointer2
//								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctl_primary_expr_helper1.text); }
//								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1_pointer2.text); }
								
								/* simple paren are the only case where we should start over
								from the "real" begining of the parsed expression... */
								| TOK_LP ctl_root_expr TOK_RP primary_expr_select
								{ if(!er()) $ret = $ctl_root_expr.ret; } // primary_expr_select should be null...
								-> ^(BLOCK_T ctl_root_expr NOP primary_expr_select)
								/* ------------------------------------------------------- */
								
								// cast has no meaning in a CTL exp. (using the common simple)
								| TOK_BOOL TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_BOOL.text + $TOK_LP.text + $simple_root_expr.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_BOOL simple_root_expr NOP primary_expr_select)
								| TOK_WORD1 TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_WORD1.text + $TOK_LP.text + $simple_root_expr.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_WORD1 simple_root_expr NOP primary_expr_select)
								// next cannot enforced on a CTL formula. 'AX' should be used. 
								| TOK_NEXT TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_NEXT.text + $TOK_LP.text + $simple_root_expr.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_NEXT simple_root_expr NOP primary_expr_select)
								// case has no meaning in a CTL exp. (using the common simple)
								| TOK_CASE case_element_list_expr TOK_ESAC primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_CASE.text + " " + $case_element_list_expr.text + " " + $TOK_ESAC.text + " " + $primary_expr_select.text); }
								-> ^(CASE_LIST_EXPR_T case_element_list_expr NOP primary_expr_select)
								// word op has no meaning in a CTL exp. (using the common simple)
								| TOK_WAREAD TOK_LP f=simple_root_expr TOK_COMMA s=simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_WAREAD.text + $TOK_LP.text + $f.text + $TOK_COMMA.text + $s.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_WAREAD $f $s NOP primary_expr_select)
								// word op has no meaning in a CTL exp. (using the common simple)
								| TOK_WAWRITE TOK_LP f=simple_root_expr tc1=TOK_COMMA m=simple_root_expr tc2=TOK_COMMA s=simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_WAWRITE.text + $TOK_LP.text + $f.text + $tc1.text + $m.text + $tc2.text + $s.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_WAWRITE $f $m $s NOP primary_expr_select)
								;

///////////////////////////////////////////////////////////////////////////////
// ROOT OF LTL TEMPORAL EXPRESSIONS ///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
ltl_root_expr				returns[InternalSpec ret]
								: ltl_implies_expr {if(!er()) $ret = $ltl_implies_expr.ret; }
								;
ltl_implies_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_iff_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_IMPLIES^ s=ltl_implies_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_imply(input, $start, exp_str, $ret, $s.ret); }
								)?
								; /* right association */
ltl_iff_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_or_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_IFF^ s=ltl_or_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_iff(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ltl_or_expr					returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_and_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_OR^ s=ltl_and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_or(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_XOR^ s=ltl_and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_xor(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_XNOR^ s=ltl_and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_xnor(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ltl_and_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_binary_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_AND^ s=ltl_binary_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_and(input, $start, exp_str, $ret, $s.ret); }
								)*
								;


ltl_binary_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: (TOK_LP var_id TOK_KNOW) => ltl_know_expr { if (!er()) exp_str += $ltl_know_expr.text; if(!er()) $ret = $ltl_know_expr.ret; }
								| (f=ltl_unary_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_UNTIL^ s=ltl_unary_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_until(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_SINCE^ s=ltl_unary_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_since(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_RELEASES^ s=ltl_unary_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_releases(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_TRIGGERED^ s=ltl_unary_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_triggered(input, $start, exp_str, $ret, $s.ret); }
								//RTLTL binary operator
								| op=TOK_BUNTIL^ fsr=subrange s=ltl_unary_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_bound_until(input, $start, exp_str, $fsr.ret, $f.ret, $s.ret); }
//								 | op=TOK_KNOW^ s=ltl_unary_expr 
//								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ltl_know(input, $start, exp_str, $f.ret, $s.ret); }
								)*)
								;
								
ltl_know_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								//: TOK_LP! agent=var_id op=TOK_KNOW^ f=ctl_root_expr TOK_RP! 
								: TOK_LP agent=var_id op=TOK_KNOW^ f=ltl_unary_expr TOK_RP
								{ if (!er()) exp_str += $agent.text + " " + $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ltl_know(input, $start, exp_str, $agent.ret, $f.ret); }
								;

ltl_unary_expr				returns[InternalSpec ret]
								: (TOK_NOT* // resolving conflict with the TOK_NOT
									( TOK_OP_NEXT
									| TOK_OP_PREV
									| TOK_OP_NOTPREVNOT
									| TOK_OP_GLOBALLY
									| TOK_OP_HISTORICALLY
									| TOK_OP_FINALLY
									| TOK_OP_ONCE
									| TOK_OP_BFINALLY
									| TOK_OP_BGLOBALLY )) => ltl_pure_unary_expr /* all unary LTL operators */ 
								{ if(!er()) $ret = $ltl_pure_unary_expr.ret; }
								-> ^(PURE_LTL_T ltl_pure_unary_expr)
								| ltl_relational_expr
								{ if(!er()) $ret = $ltl_relational_expr.ret; }
								; 
ltl_pure_unary_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_OP_NEXT^ f=ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_next(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_PREV^ f=ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_prev(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_NOTPREVNOT^ f=ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_notprevnot(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_GLOBALLY^ f=ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_globally(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_HISTORICALLY^ f=ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_historically(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_FINALLY^ f=ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_finally(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_ONCE^ f=ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_once(input, $start, exp_str, $f.ret); }
								/*RTLTL Spec*/
								| op=TOK_OP_BFINALLY^ fsr=subrange s=ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_bound_finally(input, $start, exp_str, $fsr.ret, $s.ret); }
								| op=TOK_OP_BGLOBALLY^ fsr=subrange s=ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_bound_globally(input, $start, exp_str, $fsr.ret, $s.ret); }
								/* NOT is required here to allow such expr as "! X a" */
								| op=TOK_NOT^ fp=ltl_pure_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $fp.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_not(input, $start, exp_str, $fp.ret); }
								;

ltl_relational_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_in_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_EQUAL^ s=ltl_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_eq(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_NOTEQUAL^ s=ltl_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_neq(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_LT^ s=ltl_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_lt(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_GT^ s=ltl_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_gt(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_LE^ s=ltl_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_le(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_GE^ s=ltl_in_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ge(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ltl_in_expr					returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_union_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_SETIN^ s=ltl_union_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_setin(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ltl_union_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_set_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_UNION^ s=ltl_set_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_union(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ltl_set_expr				returns[InternalSpec ret]
@init {boolean append_end = false; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: ltl_shift_expr
								{ if(!er()) $ret = $ltl_shift_expr.ret; }
								| subrange 
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_range(input, $start, $subrange.text); }
								| TOK_LCB ltl_set_list_expr TOK_RCB
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_set(input, $start, $TOK_LCB.text + " " + $ltl_set_list_expr.text + " " + $TOK_RCB.text); }
								-> ^(SET_LIST_EXP_T ltl_set_list_expr)
								;
ltl_set_list_expr			// do nothing...
								: simple_root_expr (TOK_COMMA! simple_root_expr)*
								; /* these are simple expression, not ltl expressions */
ltl_shift_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_remainder_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_LSHIFT^ s=ltl_remainder_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_lshift(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_RSHIFT^ s=ltl_remainder_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_rshift(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ltl_remainder_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_additive_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_MOD^ s=ltl_additive_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_mod(input, $start, exp_str, $ret, $s.ret); }
								)*
								;


ltl_additive_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_multiplicative_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_PLUS^ s=ltl_multiplicative_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_plus(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_MINUS^ s=ltl_multiplicative_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_minus(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ltl_multiplicative_expr		returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_concatination_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_TIMES^ s=ltl_concatination_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_times(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_DIVIDE^ s=ltl_concatination_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_divide(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ltl_concatination_expr		returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ltl_primary_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_CONCATENATION^ s=ltl_primary_expr
								{ if (!er()) exp_str += $op.text + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_concatenation(input, $start, exp_str, $ret, $s.ret); }
								)*
								;

ltl_primary_expr			returns[InternalSpec ret]
@init {boolean append_end = false; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: ltl_primary_expr_helper1
								{ if(!er()) $ret = $ltl_primary_expr_helper1.ret; }
								| op=TOK_MINUS v=ltl_primary_expr
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_unary_minus(input, $start, $op.text + " " + $v.text, $v.ret); }
								-> ^(TOK_UNARY_MINUS_T $v)
								| op=TOK_NOT v=ltl_primary_expr
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_not(input, $start, $op.text + " " + $v.text, $v.ret); }
								-> ^(TOK_NOT $v)
								;

ltl_primary_expr_helper1	returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: constant primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ltl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $constant.text + " " + $primary_expr_select.text); }
								-> ^(VALUE_T constant NOP primary_expr_select)
								| primary_expr_helper1_pointer1
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ltl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1_pointer1.text); }
//								| primary_expr_helper1_pointer2
//								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ltl_primary_expr_helper1.text); }
//								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1_pointer2.text); }
								
								/* simple paren are the only case where we should start over
								from the "real" begining of the parsed expression... */
								| TOK_LP ltl_root_expr TOK_RP primary_expr_select
								{ if(!er()) $ret = $ltl_root_expr.ret; } // primary_expr_select should be null...
								-> ^(BLOCK_T ltl_root_expr NOP primary_expr_select)
								/* ------------------------------------------------------- */
								
								// cast has no meaning in a ltl exp. (using the common simple)
								| TOK_BOOL TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ltl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_BOOL.text + $TOK_LP.text + $simple_root_expr.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_BOOL simple_root_expr NOP primary_expr_select)
								| TOK_WORD1 TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ltl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_WORD1.text + $TOK_LP.text + $simple_root_expr.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_WORD1 simple_root_expr NOP primary_expr_select)
								// next cannot enforced on a LTL formula. 'AX' should be used. 
								| TOK_NEXT TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ltl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_NEXT.text + $TOK_LP.text + $simple_root_expr.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_NEXT simple_root_expr NOP primary_expr_select)
								// case has no meaning in a LTL exp. (using the common simple)
								| TOK_CASE case_element_list_expr TOK_ESAC primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ltl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_CASE.text + " " + $case_element_list_expr.text + " " + $TOK_ESAC.text + " " + $primary_expr_select.text); }
								-> ^(CASE_LIST_EXPR_T case_element_list_expr NOP primary_expr_select)
								// word op has no meaning in a LTL exp. (using the common simple)
								| TOK_WAREAD TOK_LP f=simple_root_expr TOK_COMMA s=simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ltl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_WAREAD.text + $TOK_LP.text + $f.text + $TOK_COMMA.text + $s.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_WAREAD $f $s NOP primary_expr_select)
								// word op has no meaning in a LTL exp. (using the common simple)
								| TOK_WAWRITE TOK_LP f=simple_root_expr tc1=TOK_COMMA m=simple_root_expr tc2=TOK_COMMA s=simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ltl_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_WAWRITE.text + $TOK_LP.text + $f.text + $tc1.text + $m.text + $tc2.text + $s.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_WAWRITE $f $m $s NOP primary_expr_select)
								;


///////////////////////////////////////////////////////////////////////////////
// ROOT OF CTL STAR TEMPORAL EXPRESSIONS //////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
ctls_root_expr				returns[InternalSpec ret]
								: ctls_implies_expr {if(!er()) $ret = $ctls_implies_expr.ret; }
								;
ctls_implies_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_iff_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_IMPLIES^ s=ctls_implies_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_imply(input, $start, exp_str, $ret, $s.ret); }
								)?
								; /* right association */
ctls_iff_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_or_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_IFF^ s=ctls_or_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_iff(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctls_or_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_and_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_OR^ s=ctls_and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_or(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_XOR^ s=ctls_and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_xor(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_XNOR^ s=ctls_and_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_xnor(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctls_and_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_ltl_binary_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_AND^ s=ctls_ltl_binary_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_and(input, $start, exp_str, $ret, $s.ret); }
								)*
								;

/* all LTL binary operators */
ctls_ltl_binary_expr		returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_ltl_unary_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_UNTIL^ s=ctls_ltl_unary_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_until(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_SINCE^ s=ctls_ltl_unary_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_since(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_RELEASES^ s=ctls_ltl_unary_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_releases(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_TRIGGERED^ s=ctls_ltl_unary_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_triggered(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctls_ltl_unary_expr			returns[InternalSpec ret]
								: (TOK_NOT* // resolving conflict with the TOK_NOT
									( TOK_OP_NEXT
									| TOK_OP_PREV
									| TOK_OP_NOTPREVNOT
									| TOK_OP_GLOBALLY
									| TOK_OP_HISTORICALLY
									| TOK_OP_FINALLY
									| TOK_OP_ONCE )) => ctls_ltl_pure_unary_expr /* all unary LTL operators */ 
								{ if(!er()) $ret = $ctls_ltl_pure_unary_expr.ret; }
								-> ^(PURE_LTL_T ctls_ltl_pure_unary_expr)
								| ctls_ctl_expr
								{ if(!er()) $ret = $ctls_ctl_expr.ret; }
								; 
ctls_ltl_pure_unary_expr	returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_OP_NEXT^ f=ctls_ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_next(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_PREV^ f=ctls_ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_prev(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_NOTPREVNOT^ f=ctls_ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_notprevnot(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_GLOBALLY^ f=ctls_ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_globally(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_HISTORICALLY^ f=ctls_ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_historically(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_FINALLY^ f=ctls_ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_finally(input, $start, exp_str, $f.ret); }
								| op=TOK_OP_ONCE^ f=ctls_ltl_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_once(input, $start, exp_str, $f.ret); }
								/* NOT is required here to allow such expr as "! X a" */
								| op=TOK_NOT^ fp=ctls_ltl_pure_unary_expr
								{ if (!er()) exp_str = $op.text + " " + $fp.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_not(input, $start, exp_str, $fp.ret); }
								;

/* all CTL binary operators */
ctls_ctl_expr				returns[InternalSpec ret]
								: (TOK_NOT* // resolving conflict with the TOK_NOT
									( TOK_EX 
									| TOK_AX 
									| TOK_EF 
									| TOK_AF 
									| TOK_EG 
									| TOK_AG 
									| TOK_AA 
									| TOK_EE 
									| TOK_EBF 
									| TOK_ABF 
									| TOK_EBG 
									| TOK_ABG)) => ctls_pure_ctl_expr
								{ if(!er()) $ret = $ctls_pure_ctl_expr.ret; }
								-> ^(PURE_CTL_T ctls_pure_ctl_expr)
								| ctls_relational_expr
								{ if(!er()) $ret = $ctls_relational_expr.ret; }
								;
ctls_pure_ctl_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_EX^ f=ctls_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EX(input, $start, exp_str, $f.ret); }
								| op=TOK_AX^ f=ctls_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_AX(input, $start, exp_str, $f.ret); }
								| op=TOK_EF^ f=ctls_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EF(input, $start, exp_str, $f.ret); }
								| op=TOK_AF^ f=ctls_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_AF(input, $start, exp_str, $f.ret); }
								| op=TOK_EG^ f=ctls_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EG(input, $start, exp_str, $f.ret); }
								| op=TOK_AG^ f=ctls_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $f.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_AG(input, $start, exp_str, $f.ret); }

								| (TOK_AA) => ctls_aa { if(!er()) $ret = $ctls_aa.ret; }
								| (TOK_EE) => ctls_ee { if(!er()) $ret = $ctls_ee.ret; }

								| op=TOK_EBF^ fsr=subrange s=ctls_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EBF(input, $start, exp_str, $fsr.ret, $s.ret); }
								| op=TOK_ABF^ fsr=subrange s=ctls_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ABF(input, $start, exp_str, $fsr.ret, $s.ret); }
								| op=TOK_EBG^ fsr=subrange s=ctls_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EBG(input, $start, exp_str, $fsr.ret, $s.ret); }
								| op=TOK_ABG^ fsr=subrange s=ctls_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $fsr.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ABG(input, $start, exp_str, $fsr.ret, $s.ret); }
								/* NOT is required here to allow such expr as "! EX a" */
								| op=TOK_NOT^ fp=ctls_pure_ctl_expr
								{ if (!er()) exp_str = $op.text + " " + $fp.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_not(input, $start, exp_str, $fp.ret); }
								;
ctls_aa						returns[InternalSpec ret]
@after { if(!er()) $ret.evalBDDChildrenExp(input); }
								: (TOK_AA TOK_LB ctls_root_expr TOK_BUNTIL) => ctls_abu { if(!er()) $ret = $ctls_abu.ret; }
								// otherwise, it must be an AU
								| (TOK_AA TOK_LB) => ctls_au { if(!er()) $ret = $ctls_au.ret; }
								// other just faking an error state, so we won't have two errors for this failures
								//| (TOK_AA) => { in_my_recovery_mode = true; }
								;
ctls_ee						returns[InternalSpec ret]
@after { if(!er()) $ret.evalBDDChildrenExp(input); }
								: (TOK_EE TOK_LB ctls_root_expr TOK_BUNTIL) => ctls_ebu { if(!er()) $ret = $ctls_ebu.ret; }
								// otherwise, it must be a BU
								| (TOK_EE TOK_LB) => ctls_eu { if(!er()) $ret = $ctls_eu.ret; }
								// other just faking an error state, so we won't have two errors for this failures
								//| (TOK_EE) => { in_my_recovery_mode = true; }
								;

ctls_au						returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_AA^ lb=TOK_LB! fre=ctls_root_expr rb=TOK_RB!
								{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_AU(input, $start, exp_str, $fre.ret); }
								;
ctls_eu						returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_EE^ lb=TOK_LB! fre=ctls_root_expr rb=TOK_RB!
								{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EU(input, $start, exp_str, $fre.ret); }
								;
ctls_abu					returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_AA^ lb=TOK_LB! fre=ctls_root_expr opu=TOK_BUNTIL msr=subrange sre=ctls_root_expr rb=TOK_RB!
								{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + " " + $opu.text + " " + $msr.text + " " + $sre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ABU(input, $start, exp_str, $fre.ret, $msr.ret, $sre.ret); }
								;
ctls_ebu					returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: op=TOK_EE^ lb=TOK_LB! fre=ctls_root_expr opu=TOK_BUNTIL msr=subrange sre=ctls_root_expr rb=TOK_RB!
								{ if (!er()) exp_str = $op.text + $lb.text + $fre.text + " " + $opu.text + " " + $msr.text + " " + $sre.text + $rb.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_EBU(input, $start, exp_str, $fre.ret, $msr.ret, $sre.ret); }
								;

ctls_relational_expr		returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_in_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_EQUAL^ s=ctls_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_eq(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_NOTEQUAL^ s=ctls_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_neq(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_LT^ s=ctls_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_lt(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_GT^ s=ctls_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_gt(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_LE^ s=ctls_in_expr 
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_le(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_GE^ s=ctls_in_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ge(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctls_in_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_union_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_SETIN^ s=ctls_union_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_setin(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctls_union_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_set_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_UNION^ s=ctls_set_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_union(input, $start, exp_str, $ret, $s.ret); }
								)*
								;

ctls_set_expr				returns[InternalSpec ret]
@init {boolean append_end = false; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: ctls_shift_expr
								{ if(!er()) $ret = $ctls_shift_expr.ret; }
								| subrange 
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_range(input, $start, $subrange.text); }
								| TOK_LCB ctls_set_list_expr TOK_RCB
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_set(input, $start, $TOK_LCB.text + " " + $ctls_set_list_expr.text + " " + $TOK_RCB.text); }
								-> ^(SET_LIST_EXP_T ctls_set_list_expr)
								;
ctls_set_list_expr			// do nothing...
								: simple_root_expr (TOK_COMMA! simple_root_expr)*
								; /* these are simple expression, not ctl expressions */
ctls_shift_expr				returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_remainder_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_LSHIFT^ s=ctls_remainder_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_lshift(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_RSHIFT^ s=ctls_remainder_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_rshift(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctls_remainder_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_additive_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_MOD^ s=ctls_additive_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_mod(input, $start, exp_str, $ret, $s.ret); }
								)*
								;



ctls_additive_expr			returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_multiplicative_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_PLUS^ s=ctls_multiplicative_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_plus(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_MINUS^ s=ctls_multiplicative_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_minus(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctls_multiplicative_expr	returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_concatination_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_TIMES^ s=ctls_concatination_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_times(input, $start, exp_str, $ret, $s.ret); }
								| op=TOK_DIVIDE^ s=ctls_concatination_expr
								{ if (!er()) exp_str += " " + $op.text + " " + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_divide(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
ctls_concatination_expr		returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=ctls_primary_expr { if (!er()) exp_str += $f.text; if(!er()) $ret = $f.ret; }
								( op=TOK_CONCATENATION^ s=ctls_primary_expr
								{ if (!er()) exp_str += $op.text + $s.text; if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_concatenation(input, $start, exp_str, $ret, $s.ret); }
								)*
								;
								
ctls_primary_expr			returns[InternalSpec ret]
@init {boolean append_end = false; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: ctls_primary_expr_helper1
								{ if(!er()) $ret = $ctls_primary_expr_helper1.ret; }
								| op=TOK_MINUS v=ctls_primary_expr
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_unary_minus(input, $start, $op.text + " " + $v.text, $v.ret); }
								-> ^(TOK_UNARY_MINUS_T $v)
								| op=TOK_NOT v=ctls_primary_expr
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_not(input, $start, $op.text + " " + $v.text, $v.ret); }
								-> ^(TOK_NOT $v)
								;

ctls_primary_expr_helper1	returns[InternalSpec ret]
@init {boolean append_end = false; String exp_str = ""; }
@after { if(append_end) $ret.setEndToken($stop); if(!er()) $ret.evalBDDChildrenExp(input); }
								: constant primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctls_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $constant.text + " " + $primary_expr_select.text); }
								-> ^(VALUE_T constant NOP primary_expr_select)
								| primary_expr_helper1_pointer1
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctls_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1_pointer1.text); }
//								| primary_expr_helper1_pointer2
//								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctls_primary_expr_helper1.text); }
//								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $primary_expr_helper1_pointer2.text); }
								
								/* simple paren are the only case where we should start over
								from the "real" begining of the parsed expression... */
								| TOK_LP ctls_root_expr TOK_RP primary_expr_select
								{ if(!er()) $ret = $ctls_root_expr.ret; } // primary_expr_select should be null...
								-> ^(BLOCK_T ctls_root_expr NOP primary_expr_select)
								/* ------------------------------------------------------- */
								
								// cast has no meaning in a CTL exp. (using the common simple)
								| TOK_BOOL TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctls_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_BOOL.text + $TOK_LP.text + $simple_root_expr.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_BOOL simple_root_expr NOP primary_expr_select)
								| TOK_WORD1 TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctls_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_WORD1.text + $TOK_LP.text + $simple_root_expr.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_WORD1 simple_root_expr NOP primary_expr_select)
								// next cannot enforced on a CTL formula. 'AX' should be used. 
								| TOK_NEXT TOK_LP simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctls_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_NEXT.text + $TOK_LP.text + $simple_root_expr.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_NEXT simple_root_expr NOP primary_expr_select)
								// case has no meaning in a CTL exp. (using the common simple)
								| TOK_CASE case_element_list_expr TOK_ESAC primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctls_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_CASE.text + " " + $case_element_list_expr.text + " " + $TOK_ESAC.text + " " + $primary_expr_select.text); }
								-> ^(CASE_LIST_EXPR_T case_element_list_expr NOP primary_expr_select)
								// word op has no meaning in a CTL exp. (using the common simple)
								| TOK_WAREAD TOK_LP f=simple_root_expr TOK_COMMA s=simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctls_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_WAREAD.text + $TOK_LP.text + $f.text + $TOK_COMMA.text + $s.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_WAREAD $f $s NOP primary_expr_select)
								// word op has no meaning in a CTL exp. (using the common simple)
								| TOK_WAWRITE TOK_LP f=simple_root_expr tc1=TOK_COMMA m=simple_root_expr tc2=TOK_COMMA s=simple_root_expr TOK_RP primary_expr_select
								{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $ctls_primary_expr_helper1.text); }
								//{ if(!er()) append_end = true; if(!er()) $ret = InitSpec.mk_ref(input, $start, $TOK_WAWRITE.text + $TOK_LP.text + $f.text + $tc1.text + $m.text + $tc2.text + $s.text + $TOK_RP.text + $primary_expr_select.text); }
								-> ^(TOK_WAWRITE $f $m $s NOP primary_expr_select)
								;



///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
/* simple and common basic subtrees to all expressions (operands...) */
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
primary_expr_helper1_pointer1	: TOK_ATOM primary_expr_select
								-> ^(VALUE_T TOK_ATOM NOP primary_expr_select)
								;
//primary_expr_helper1_pointer2	: TOK_SELF primary_expr_select
//								-> ^(VALUE_T TOK_SELF NOP primary_expr_select)
//								;
primary_expr_select				: (primary_expr_select_helper | primary_expr_ref)*
								; 
primary_expr_ref				: (TOK_DOT! (TOK_ATOM | TOK_NUMBER))
								;
primary_expr_select_helper		: (TOK_LB simple_root_expr TOK_RB) => primary_expr_select_helper_arr_suffix
								| primary_expr_select_helper_bit_suffix
								;
primary_expr_select_helper_arr_suffix
								: TOK_LB simple_root_expr TOK_RB
								-> ^(ARRAY_INDEX_T simple_root_expr)
								;
primary_expr_select_helper_bit_suffix
								: TOK_LB f=simple_root_expr TOK_COLON s=simple_root_expr TOK_RB
								-> ^(BIT_SELECT_T $f $s)
								;
								
case_element_expr				: simple_root_expr TOK_COLON simple_root_expr TOK_SEMI
								-> ^(CASE_ELEMENT_EXPR_T simple_root_expr simple_root_expr)
								;
case_element_list_expr			: case_element_expr (case_element_expr)*
								;

number							: TOK_NUMBER
								| TOK_PLUS! TOK_NUMBER;
integer							: TOK_NUMBER
								-> ^(TOK_PLUS TOK_NUMBER)
								| TOK_PLUS TOK_NUMBER
								-> ^(TOK_PLUS TOK_NUMBER)
								| TOK_MINUS TOK_NUMBER
								-> ^(TOK_MINUS TOK_NUMBER)
								;
number_word						: TOK_NUMBER_WORD
								;

subrange					returns[InternalSpecRange ret]
@after { if(!er()) $ret.evalBDDChildrenExp(input); }
								: f=integer TOK_TWODOTS s=integer
								{ if(!er()) $ret = new InternalSpecRange($f.text, $s.text, $start); }
								-> ^(SUBRANGE_T integer integer)
								;
constant						: TOK_FALSEEXP
								| TOK_TRUEEXP
								| number
								| number_word
								;

/* parse an optional semicolon */
optsemi							: TOK_SEMI*
								;

/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// LEXER
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
//TOK_ANY_START				: 'CTLSPEC' | 'SPEC' | 'CTL*SPEC' | 'LTLSPEC' | 'INVARSPEC';
TOK_CTL_SPEC				: 'CTLSPEC' | 'SPEC';
TOK_CTL_STAR_SPEC			: 'CTL*SPEC';
TOK_LTL_SPEC				: 'LTLSPEC';
TOK_INVAR_SPEC				: 'INVARSPEC';

TOK_EX						: 'EX';
TOK_AX						: 'AX';
TOK_EF						: 'EF';
TOK_AF						: 'AF';
TOK_EG						: 'EG';
TOK_AG						: 'AG';
TOK_EE						: 'E';
TOK_AA						: 'A';
TOK_BUNTIL					: 'BU';
TOK_EBF						: 'EBF';
TOK_ABF						: 'ABF';
TOK_EBG						: 'EBG';
TOK_ABG						: 'ABG';
// the last is the TLV notation.
TOK_OP_FINALLY				: '<>' | 'F' | 'FINALLY' | 'EVENTUALLY';
TOK_OP_ONCE					: '<_>' | 'O' | 'ONCE';
TOK_OP_GLOBALLY				: '[]' | 'G' | 'GLOBALLY' | 'ALWAYS';
TOK_OP_HISTORICALLY			: '[_]' | 'H' | 'HISTORICALLY';
TOK_OP_NEXT					: '()' | 'X' | 'NEXT';
TOK_OP_PREV					: '(_)' | 'Y' | 'PREV';
TOK_UNTIL					: 'Until' | 'U' | 'UNTIL';
TOK_SINCE					: 'Since' | 'S' | 'SINCE';
TOK_RELEASES				: 'Awaits' | 'V' | 'RELEASES';
TOK_TRIGGERED				: 'Backto' | 'T' | 'TRIGGERED';
TOK_OP_NOTPREVNOT			: 'Z';
//RTLTL
TOK_OP_BFINALLY			: 'BF' ;
TOK_OP_BGLOBALLY			: 'BG' ;

//knowledge
TOK_KNOW					:'K'|'KNOW'|'Know';
//TOK_MMIN					: 'MIN';// !!!
//TOK_MMAX					: 'MAX';// !!!

TOK_LP						: '(';
TOK_RP						: ')';
TOK_LB						: '[';
TOK_RB						: ']';
TOK_LCB						: '{';
TOK_RCB						: '}';
TOK_FALSEEXP				: 'FALSE';
TOK_TRUEEXP					: 'TRUE';

// ALL NON SIMPLE OPERATOR SHOULD BE REMOVED OR ELSE THEY
// WOULD NOT HAVE MEANING IN BETWEEN TL STATEMENTS
TOK_WORD1					: 'word1';// ???
TOK_WORD					: 'word' | 'Word';// ???
TOK_BOOL					: 'bool';// ???
TOK_WAREAD					: 'READ';// ???
TOK_WAWRITE					: 'WRITE';// ???

TOK_CASE					: 'case';// ???
TOK_ESAC					: 'esac';// ???
TOK_PLUS					: '+';
TOK_MINUS					: '-';
TOK_TIMES					: '*';
TOK_DIVIDE					: '/';
TOK_MOD						: 'mod';
TOK_LSHIFT					: '<<';
TOK_RSHIFT					: '>>';
//TOK_LROTATE					: '<<<';
//TOK_RROTATE					: '>>>';
TOK_EQUAL					: '=';
TOK_NOTEQUAL				: '!=';
TOK_LE						: '<=';
TOK_GE						: '>=';
TOK_LT						: '<';
TOK_GT						: '>';
TOK_NEXT					: 'next';
//TOK_SELF					: 'self';
TOK_UNION					: 'union';
TOK_SETIN					: 'in';
TOK_TWODOTS					: '..';
TOK_DOT						: '.';

// basic logic operators...
TOK_IMPLIES					: '->';
TOK_IFF						: '<->';
TOK_OR						: '|';
TOK_AND						: '&';
TOK_XOR						: 'xor';
TOK_XNOR					: 'xnor';
TOK_NOT						: '!';

TOK_COMMA					: ',';//                     {yylval.lineno = yylineno; return(TOK_COMMA);}
TOK_COLON					: ':';//                     {yylval.lineno = yylineno; return(TOK_COLON);}
TOK_SEMI					: ';';//                     {yylval.lineno = yylineno; return(TOK_SEMI);}
TOK_CONCATENATION			: '::';//                    {yylval.lineno = yylineno; return(TOK_CONCATENATION);}



/////////////////////////////////////////////////////////////////////
// basic JTLV extension - atoms, whitespaces and comments
/////////////////////////////////////////////////////////////////////

/* word constants */
TOK_NUMBER_WORD					: '0' ('b' | 'B' | 'o' | 'O' | 'd' | 'D' | 'h' | 'H') ('0'..'9')* '_' ('0'..'9' | 'a'..'f' | 'A'..'F') ('0'..'9' | 'a'..'f' | 'A'..'F' | '_')*;

 /* real, fractional and exponential constants */
TOK_NUMBER_FRAC					: ('f' | 'F') '\'' ('0'..'9')+ '/' ('0'..'9')+;

/* integer number */
TOK_NUMBER						: ('0'..'9')+;

/* identifier */
TOK_ATOM						: ('a'..'z' | 'A'..'Z' | '_') ('a'..'z' | 'A'..'Z' | '0'..'9' | '_' | '\\' | '$' | '#' | '-')*;


JTOK_WS 						:   (   ' '
								|   '\t'
								|   '\r'
								|   '\n'
								)+
								{ $channel=HIDDEN; };
JTOK_MULTI_COMMENT				: ('/*' (
								options { greedy=false;}
								:  // '\r' '\n' |
								'\r'
								|   '\n'
								|   ~('\n'|'\r')
								)*
								'*/'
								{$channel=HIDDEN;});
JTOK_LINE_COMMENT				: ('--' (~('\n'|'\r'))* (('\n'|'\r'('\n')?))? {$channel=HIDDEN;})
								| ('//' (~('\n'|'\r'))* (('\n'|'\r'('\n')?))? {$channel=HIDDEN;});

