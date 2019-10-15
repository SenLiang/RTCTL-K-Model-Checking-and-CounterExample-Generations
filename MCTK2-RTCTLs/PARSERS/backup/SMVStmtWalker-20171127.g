tree grammar SMVStmtWalker;

options {
	tokenVocab=SMV;
	ASTLabelType=CommonTree;
	//backtrack = true;
}

@header {
package edu.wis.jtlv.env.core.smv;

import java.util.Vector;
import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.core.smv.Initiator.CaseElement;
import edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo;
//import java.lang.Boolean;
}

@members {
public static String curr_module_name = "main";
public static Initiator intr = Initiator.get_instance();
public static StringArrayWA curr_var_name = null;

//LXY: for MAS
public static Boolean curr_module_is_agent = false;
public static Boolean curr_var_decl_is_visible = true;

public String getErrorMessage(RecognitionException e, String[] tokenNames) {
	String msg = null;
	if ( e instanceof SMVUnimplementedException) {
		msg = e.toString();
    		Env.doError(e, msg);
	} else if ( e instanceof SMVParseException) {
		msg = e.toString();
    		Env.doError(e, msg);
	} else if ( e instanceof SMVInternalParseException) {
		msg = e.toString();
    		Env.doError(e, msg);
	} else {
		msg = super.getErrorMessage(e, tokenNames);
    		Env.doError(e, msg);
	}
	return msg;
}
public String getErrorHeader(RecognitionException e) {
	// I don't like Parse tree error header.
	// I prefer the BaseRecognizer one...
	return "line "+e.line+":"+e.charPositionInLine;
}
public void emitErrorMessage(String msg) {
	// System.err.println(msg);
	// do nothing.
}
}

/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// Tree walker for initiating the variables
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// to the JTLV implementation many elements had been removed.
// they are all tagged with "not implemented in JTLV" 

program		: // nothing or...
		| ^(PROGRAM_T module_list)
/* not implemented in JTLV */
		| command
		//| ltl_expression // was removed
/* -------------------- */
		;

/* --------------------------------------------------------------------- */
/* ---------------------------- EXPRESSION ----------------------------- */
/* --------------------------------------------------------------------- */
/* Later on unary plus must be implemented as a usual unary operator
   (as unary minus now)
*/
number		: TOK_NUMBER;
integer 	: ^(TOK_PLUS TOK_NUMBER)
		| ^(TOK_MINUS TOK_NUMBER)
		;
number_word		: TOK_NUMBER_WORD
		;
/* not implemented in JTLV	
number_frac		: TOK_NUMBER_FRAC
		;
number_real		: TOK_NUMBER_REAL
		;
number_exp		: TOK_NUMBER_EXP
		;
*/
//add in 1.5
integer_or_atom :  integer | TOK_ATOM ;
subrange returns[Vector<String> ret]
		: ^(SUBRANGE_T f=integer_or_atom s=integer_or_atom)
		{ret = new Vector<String>(2);}
		{ret.add($f.text);}
		{ret.add($s.text);}
		;
constant returns[StmtValueArrayWA ret]
		: TOK_FALSEEXP
		{ $ret = intr.mk_constant_false_val(curr_module_name, input); }
		| TOK_TRUEEXP
		{ $ret = intr.mk_constant_true_val(curr_module_name, input); }
		| number
		{ $ret = intr.mk_constant_numeric_val(curr_module_name, $number.text, input); }
		| number_word
		{ $ret = intr.mk_constant_word_val(curr_module_name, $number_word.text, input); }
		//| number_frac { yyerror("fractional constants are not supported."); YYABORT;}
		//| number_exp {yyerror("exponential constants are not supported."); YYABORT;}
		//| number_real {yyerror("real constants are not supported."); YYABORT;}
		;
		
primary_expr_helper1 returns[StmtValueArrayWA ret]
		: ^(VALUE_T constant NOP primary_expr_select)
		{ $ret = $constant.ret; } // primary_expr_select has no meaning here.
		| ^(VALUE_T TOK_ATOM NOP primary_expr_select)
		{ $ret = intr.mk_ref_val(curr_module_name, $TOK_ATOM.text, $primary_expr_select.ret, input); }
		| ^(VALUE_T TOK_SELF NOP primary_expr_select) // has been converted
		{ $ret = intr.mk_self_ref_val(curr_module_name, $primary_expr_select.ret, input); }
		| ^(BLOCK_T basic_expr NOP primary_expr_select)
		{ $ret = $basic_expr.ret; } // I don't see how could primary_expr_select has any meaning here.
		| ^(TOK_BOOL basic_expr NOP primary_expr_select)
		{ $ret = $basic_expr.ret; } // I don't see how could primary_expr_select has any meaning here.
		| ^(TOK_WORD1 basic_expr NOP primary_expr_select)
		{ $ret = $basic_expr.ret; } // I don't see how could primary_expr_select has any meaning here.
		| ^(TOK_NEXT basic_expr NOP primary_expr_select)
		{ $ret = intr.NEXT_val($basic_expr.ret); }
		| ^(CASE_LIST_EXPR_T case_element_list_expr NOP primary_expr_select)
		{ $ret = $case_element_list_expr.ret; } // I don't see how could primary_expr_select has any meaning here.
		| ^(TOK_WAREAD simple_expression simple_expression NOP primary_expr_select)
		// ????
		| ^(TOK_WAWRITE simple_expression simple_expression simple_expression NOP primary_expr_select)
		// ????
		;
primary_expr_select returns [StringArrayWA ret] 
		: { $ret = new StringArrayWA(intr, "", "", null, "");  }
		( ^( ARRAY_INDEX_T f1= simple_expression
		{ StringArrayWA temp =  new StringArrayWA(intr,"[" + $f1.text + "]","[",f1.ret,"]"); }
		{  $ret = ret.concat(temp);   } ) 		
		| ^( BIT_SELECT_T f2= simple_expression s= simple_expression 
		{StringArrayWA first = new StringArrayWA(intr, "[" + $f2.text + ":", "[", f2.ret, ":");}
            	{StringArrayWA second = new StringArrayWA(intr, $s.text + "]", "", s.ret, "]"); }
            	{StringArrayWA temp = first.concat(second); }
            	{ret = ret.concat(temp); })
		| TOK_ATOM 
		{StringArrayWA temp = new StringArrayWA(intr, "." + $TOK_ATOM.text, "", null, ""); }
		{ ret = ret.concat(temp);}
		| TOK_NUMBER
		{StringArrayWA temp = new StringArrayWA(intr, "." + $TOK_NUMBER.text, "", null, ""); }
		{ ret = ret.concat(temp);})* 
		;
//primary_expr_select returns[String ret]
	//	: { $ret = ""; }
	//	( primary_expr_select_helper { $ret += "[" + $primary_expr_select_helper.ret + "]"; }
	//	| primary_expr_ref { $ret += "." + $primary_expr_ref.ret; }
	//	)*
	//	; 
//cancel in 1.5
//primary_expr_ref returns[String ret]
	//	: { $ret = ""; }
	//	 ( TOK_ATOM { $ret = $TOK_ATOM.text; } 
	//	 | TOK_NUMBER { $ret = $TOK_NUMBER.text; })
	//	;
//primary_expr_select_helper returns[String ret]
	//	: { $ret = ""; }
	//	^(ARRAY_INDEX_T simple_expression { $ret += $simple_expression.text; } )
	//	| ^(BIT_SELECT_T f=simple_expression s=simple_expression { $ret += $f.text + ":" + $s.text; } )
	//	;
primary_expr returns[StmtValueArrayWA ret]
		: primary_expr_helper1
		{ $ret = $primary_expr_helper1.ret; }
		| ^(TOK_UNARY_MINUS_T f=primary_expr)
		{ $ret = intr.UNARY_MINUS_val($f.ret); }
		| ^(TOK_NOT f=primary_expr)
		{ $ret = intr.NOT_val($f.ret); }
		|constru_for_expr
		{ $ret = $constru_for_expr.ret; }
		;
constru_for_expr returns [StmtValueArrayWA ret]
@init{
		int init_marker = -1;
        	int cond_marker = -1;
        	int inc_marker = -1;
        	int expr_marker = -1;
        	int end_marker = -1;
}
		: ^(TOK_AND_FOR_T{ intr.startDummyPhase(); } TOK_LP TOK_ATOM NOP{init_marker = input.mark(); } simple_expression NOP{cond_marker = input.mark(); } simple_expression NOP{inc_marker = input.mark(); } simple_expression NOP TOK_RP TOK_LCB{expr_marker = input.mark(); } simple_expression TOK_RCB{end_marker = input.mark();}
                {
		intr.endDummyPhase();
		$ret = intr.evalConstructorForLoop("AND", $TOK_ATOM.text,
		init_marker, cond_marker, inc_marker, expr_marker, end_marker, input, this); 
		}) 
		| ^( TOK_OR_FOR_T{ intr.startDummyPhase(); } TOK_LP TOK_ATOM NOP{init_marker = input.mark(); } simple_expression NOP{cond_marker = input.mark(); } simple_expression NOP{inc_marker = input.mark(); } simple_expression NOP TOK_RP TOK_LCB{expr_marker = input.mark(); } simple_expression TOK_RCB{end_marker = input.mark();}
		{
		intr.endDummyPhase();
		$ret = intr.evalConstructorForLoop("OR", $TOK_ATOM.text,
		init_marker, cond_marker, inc_marker, expr_marker, end_marker, input, this); 
		}) 
		| ^( TOK_PLUS_FOR_T{ intr.startDummyPhase(); } TOK_LP TOK_ATOM NOP{init_marker = input.mark(); } simple_expression NOP{cond_marker = input.mark(); } simple_expression NOP{inc_marker = input.mark(); } simple_expression NOP TOK_RP TOK_LCB{expr_marker = input.mark(); } simple_expression TOK_RCB{end_marker = input.mark();}
		{
		intr.endDummyPhase();
		$ret = intr.evalConstructorForLoop("PLUS", $TOK_ATOM.text,
		init_marker, cond_marker, inc_marker, expr_marker, end_marker, input, this); 
		});

/* last element in the list. Add FAILURE node */
case_element_list_expr returns[StmtValueArrayWA ret]
		: { Vector<CaseElement> all_elems = new Vector<CaseElement>(5); }
		(f=case_element_expr { all_elems.add($f.ret); } )+
		{ $ret = intr.mk_case_val(curr_module_name, all_elems, input); }
		;
case_element_expr returns[CaseElement ret]
		: ^(CASE_ELEMENT_EXPR_T f=basic_expr s=basic_expr)
		{ $ret = intr.new CaseElement($f.ret, $s.ret); }
		;
concatination_expr returns[StmtValueArrayWA ret]
		: ^(TOK_CONCATENATION f=concatination_expr s=concatination_expr)
		{ $ret = intr.CONCAT_val($f.ret, $s.ret); }
		| primary_expr
		{ $ret = $primary_expr.ret; }
		;
multiplicative_expr returns[StmtValueArrayWA ret]
		: ^(TOK_TIMES f=multiplicative_expr s=multiplicative_expr)
		{ $ret = intr.TIMES_val($f.ret, $s.ret); }
		| ^(TOK_DIVIDE f=multiplicative_expr s=multiplicative_expr)
		{ $ret = intr.DIVIDE_val($f.ret, $s.ret); }
		| concatination_expr 
		{ $ret = $concatination_expr.ret; }
		;
additive_expr returns[StmtValueArrayWA ret]
		: ^(TOK_PLUS f=additive_expr s=additive_expr)
		{ $ret = intr.PLUS_val($f.ret, $s.ret); }
		| ^(TOK_MINUS f=additive_expr s=additive_expr)
		{ $ret = intr.MINUS_val($f.ret, $s.ret); }
		| multiplicative_expr
		{ $ret = $multiplicative_expr.ret; }
		;
remainder_expr returns[StmtValueArrayWA ret]
		: ^(TOK_MOD f=remainder_expr s=remainder_expr)
		{ $ret = intr.MOD_val($f.ret, $s.ret); }
		| additive_expr
		{ $ret = $additive_expr.ret; }
		;
shift_expr returns[StmtValueArrayWA ret]
		: ^(TOK_LSHIFT f=shift_expr s=shift_expr)
		{ $ret = intr.LSHIFT_val($f.ret, $s.ret); }
		| ^(TOK_RSHIFT f=shift_expr s=shift_expr)
		{ $ret = intr.RSHIFT_val($f.ret, $s.ret); }
		| remainder_expr
		{ $ret = $remainder_expr.ret; }
		/*
		| shift_expr TOK_LROTATE remainder_expr
		| shift_expr TOK_RROTATE remainder_expr */
		;
set_list_expr returns[StmtValueArrayWA ret]
		: f=basic_expr { $ret = $f.ret; }
		(r=basic_expr { intr.UNION_val($ret, $r.ret); })*
		;
set_expr returns[StmtValueArrayWA ret]
		: shift_expr
		{ $ret = $shift_expr.ret; }
		| subrange
		{ $ret = intr.mk_range_val(curr_module_name, $subrange.ret, input); }
		| ^(SET_LIST_EXP_T set_list_expr)
		{ $ret = $set_list_expr.ret; }
		;
union_expr returns[StmtValueArrayWA ret]
		: ^(TOK_UNION f=union_expr s=union_expr)
		{ $ret = intr.UNION_val($f.ret, $s.ret); }
		| set_expr
		{ $ret = $set_expr.ret; }
		;
in_expr returns[StmtValueArrayWA ret]
		: ^(TOK_SETIN f=in_expr s=in_expr)
		{ $ret = intr.IN_val($f.ret, $s.ret); }
		| union_expr
		{ $ret = $union_expr.ret; }
		;

relational_expr returns[StmtValueArrayWA ret]
		: ^(TOK_EQUAL f=relational_expr s=relational_expr)
		{ $ret = intr.EQUAL_val($f.ret, $s.ret); }
		| ^(TOK_NOTEQUAL f=relational_expr s=relational_expr)
		{ $ret = intr.NOTEQUAL_val($f.ret, $s.ret); }
		| ^(TOK_LT f=relational_expr s=relational_expr)
		{ $ret = intr.LT_val($f.ret, $s.ret); }
		| ^(TOK_GT f=relational_expr s=relational_expr)
		{ $ret = intr.GT_val($f.ret, $s.ret); }
		| ^(TOK_LE f=relational_expr s=relational_expr)
		{ $ret = intr.LE_val($f.ret, $s.ret); }
		| ^(TOK_GE f=relational_expr s=relational_expr)
		{ $ret = intr.GE_val($f.ret, $s.ret); }
		| in_expr
		{ $ret = $in_expr.ret; }
		;
		
pure_ctl_expr returns[StmtValueArrayWA ret]
		: ^(TOK_EX f=ctl_expr) 
		{ $ret = intr.CTL_EX_val($f.ret); }
		| ^(TOK_AX f=ctl_expr)
		{ $ret = intr.CTL_AX_val($f.ret); }
		| ^(TOK_EF f=ctl_expr)
		{ $ret = intr.CTL_EF_val($f.ret); }
		| ^(TOK_AF f=ctl_expr)
		{ $ret = intr.CTL_AF_val($f.ret); }
		| ^(TOK_EG f=ctl_expr)
		{ $ret = intr.CTL_EG_val($f.ret); }
		| ^(TOK_AG f=ctl_expr)
		{ $ret = intr.CTL_AG_val($f.ret); }
		| ^(TOK_AA f=ctl_basic_expr (TOK_UNTIL | TOK_BUNTIL subrange) s=ctl_basic_expr)
		{ $ret = intr.CTL_AA_val($f.ret, "__", $subrange.ret, $s.ret); }
		| ^(TOK_EE f=ctl_basic_expr (TOK_UNTIL | TOK_BUNTIL subrange) s=ctl_basic_expr)
		{ $ret = intr.CTL_EE_val($f.ret, "__", $subrange.ret, $s.ret); }
		| ^(TOK_EBF r=subrange f=ctl_expr)
		{ $ret = intr.CTL_EBF_val($f.ret, $r.ret); }
		| ^(TOK_ABF r=subrange f=ctl_expr)
		{ $ret = intr.CTL_ABF_val($f.ret, $r.ret); }
		| ^(TOK_EBG r=subrange f=ctl_expr)
		{ $ret = intr.CTL_EBG_val($f.ret, $r.ret); }
		| ^(TOK_ABG r=subrange f=ctl_expr)
		{ $ret = intr.CTL_ABG_val($f.ret, $r.ret); }
		/* NOT is required here to allow such expr as "! EX a" */
		| ^(TOK_NOT f=pure_ctl_expr)
		{ $ret = intr.CTL_NOT_val($f.ret); }
		;

ctl_expr returns[StmtValueArrayWA ret]
		: ^(PURE_CTL_T pure_ctl_expr)
		{ $ret = $pure_ctl_expr.ret; }
		| relational_expr
		{ $ret = $relational_expr.ret; }
		;
//ctl_expr		: pure_ctl_expr
//		| relational_expr
//		;

/* there are separate CTL rules for propositional expressions 
   to avoid ambiguity related to TOK_UNTIL token in LTL and CTL.
*/
ctl_and_expr returns[StmtValueArrayWA ret]
		: ^(TOK_AND f=ctl_and_expr s=ctl_and_expr)
		{ $ret = intr.CTL_AND_val($f.ret, $s.ret); }
		| ctl_expr
		{ $ret = $ctl_expr.ret; }
		;
ctl_or_expr returns[StmtValueArrayWA ret]
		: ^(TOK_OR f=ctl_or_expr s=ctl_or_expr)
		{ $ret = intr.CTL_OR_val($f.ret, $s.ret); }
		| ^(TOK_XOR f=ctl_or_expr s=ctl_or_expr)
		{ $ret = intr.CTL_XOR_val($f.ret, $s.ret); }
		| ^(TOK_XNOR f=ctl_or_expr s=ctl_or_expr)
		{ $ret = intr.CTL_XNOR_val($f.ret, $s.ret); }
		| ctl_and_expr
		{ $ret = $ctl_and_expr.ret; }
		;
ctl_iff_expr returns[StmtValueArrayWA ret]
		: ^(TOK_IFF f=ctl_iff_expr s=ctl_iff_expr)
		{ $ret = intr.CTL_IFF_val($f.ret, $s.ret); }
		| ctl_or_expr
		{ $ret = $ctl_or_expr.ret; }
		;
ctl_implies_expr returns[StmtValueArrayWA ret]
		: ^(TOK_IMPLIES f=ctl_iff_expr s=ctl_implies_expr)
		{ $ret = intr.CTL_IMPLIES_val($f.ret, $s.ret); }
		| ctl_iff_expr
		{ $ret = $ctl_iff_expr.ret; }
		;
ctl_basic_expr returns[StmtValueArrayWA ret]
		: ctl_implies_expr
		{ $ret = $ctl_implies_expr.ret; }
		;
/* pure ltl_unary_expr is introduced to allow NOT before the ltl expressions */
pure_ltl_unary_expr returns[StmtValueArrayWA ret]
		: ^(TOK_OP_NEXT f=ltl_unary_expr)
		{ $ret = intr.OP_NEXT_val($f.ret); }
		| ^(TOK_OP_PREC f=ltl_unary_expr)
		{ $ret = intr.OP_PREC_val($f.ret); }
		| ^(TOK_OP_NOTPRECNOT f=ltl_unary_expr)
		{ $ret = intr.OP_NOTPRECNOT_val($f.ret); }
		| ^(TOK_OP_GLOBAL f=ltl_unary_expr)
		{ $ret = intr.OP_GLOBAL_val($f.ret); }
		| ^(TOK_OP_HISTORICAL f=ltl_unary_expr)
		{ $ret = intr.OP_HISTORICAL_val($f.ret); }
		| ^(TOK_OP_FUTURE f=ltl_unary_expr)
		{ $ret = intr.OP_FUTURE_val($f.ret); }
		| ^(TOK_OP_ONCE f=ltl_unary_expr)
		{ $ret = intr.OP_ONCE_val($f.ret); }
		/* NOT is required here to allow such expr as "! X a" */
		| ^(TOK_NOT f=pure_ltl_unary_expr)
		{ $ret = intr.LTL_NOT_val($f.ret); }
		;
/* LTL has to include CTL to allow paranthesis around CTL (and everything) */
ltl_unary_expr returns[StmtValueArrayWA ret]
		: ^(PURE_LTL_T pure_ltl_unary_expr) /* all unary LTL operators */ 
		{ $ret = $pure_ltl_unary_expr.ret; }
		| ctl_expr
		{ $ret = $ctl_expr.ret; }
		; 
/* all LTL binary operators */
ltl_binary_expr returns[StmtValueArrayWA ret]
		: ^(TOK_UNTIL f=ltl_binary_expr s=ltl_binary_expr)
		{ $ret = intr.UNTIL_val($f.ret, $s.ret); }
		| ^(TOK_SINCE f=ltl_binary_expr s=ltl_binary_expr)
		{ $ret = intr.SINCE_val($f.ret, $s.ret); }
		| ^(TOK_RELEASES f=ltl_binary_expr s=ltl_binary_expr)
		{ $ret = intr.RELEASES_val($f.ret, $s.ret); }
		| ^(TOK_TRIGGERED f=ltl_binary_expr s=ltl_binary_expr)
		{ $ret = intr.TRIGGERED_val($f.ret, $s.ret); }
		| ltl_unary_expr
		{ $ret = $ltl_unary_expr.ret; }
		;
and_expr returns[StmtValueArrayWA ret]
		: ^(TOK_AND f=and_expr s=and_expr)
		{ $ret = intr.AND_val($f.ret, $s.ret); }
		| ltl_binary_expr
		{ $ret = $ltl_binary_expr.ret; }
		;
or_expr returns[StmtValueArrayWA ret]
		: ^(TOK_OR f=or_expr s=or_expr)
		{ $ret = intr.OR_val($f.ret, $s.ret); }
		| ^(TOK_XOR f=or_expr s=or_expr)
		{ $ret = intr.XOR_val($f.ret, $s.ret); }
		| ^(TOK_XNOR f=or_expr s=or_expr)
		{ $ret = intr.XNOR_val($f.ret, $s.ret); }
		| and_expr
		{ $ret = $and_expr.ret; }
		;
iff_expr returns[StmtValueArrayWA ret]
		: ^(TOK_IFF f=iff_expr s=iff_expr)
		{ $ret = intr.IFF_val($f.ret, $s.ret); }
		| or_expr
		{ $ret = $or_expr.ret; }
		;
/* right association */
implies_expr returns[StmtValueArrayWA ret]
		: ^(TOK_IMPLIES f=iff_expr s=implies_expr)
		{ $ret = intr.IMPLIES_val($f.ret, $s.ret); }
		| iff_expr
		{ $ret = $iff_expr.ret; }
		;
basic_expr returns[StmtValueArrayWA ret]
		: implies_expr
		{ $ret = $implies_expr.ret; }
		;
/* every expression below, at first, remembers the current kind of 
   the parsed expression and then sets its own kind.
   After parsing the kind of expression is restored
*/
simple_expression returns [StmtValueArrayWA ret]
		: basic_expr
		{ $ret = $basic_expr.ret; }
		;
next_expression returns[StmtValueArrayWA ret]
		: basic_expr
		{ $ret = $basic_expr.ret; }
//		{ $ret = intr.NEXT_val($basic_expr.ret); }
		;
/* not implemented in JTLV */
ctl_expression returns[StmtValueArrayWA ret]
		: basic_expr
		{ $ret = $basic_expr.ret; }
		;
ltl_expression	 returns[StmtValueArrayWA ret]
		: basic_expr
		{ $ret = $basic_expr.ret; }
		;
/* -------------------- */

/* ------------------------------------------------------------------------ */
/* ----------------------------  TYPES ------------------------------------ */
/* ------------------------------------------------------------------------ */

//itype 		: TOK_BOOLEAN
/* not implemented in JTLV -- for some reason this creates a bug, since it is not implemented, I'm removeing it
		| TOK_INTEGER // { yyerror("given token is not supported."); YYABORT; }
		| TOK_REAL // { yyerror("given token is not supported."); YYABORT;}
/* -------------------- */
	//	| ^(TOK_WORD simple_expression)
	//	| subrange
	//	| ^(TYPE_VALUE_LIST_T type_value_list)
	//	| ^(TOK_ARRAY ^(TOK_WORD simple_expression) ^(TOK_WORD simple_expression))
	//	| ^(TOK_ARRAY subrange itype)
	//	;
//type 		: ^(SYNC_PROCESS_T module_type)
	//	| itype
	//	| ^(ASYNC_PROCESS_T module_type)
	//	;
type returns [SMVAbstractElementInfo ret] 
		: TOK_BOOLEAN 
		{$ret = intr.mk_boolean_var(curr_var_decl_is_visible, curr_var_name, input); }
		| ^( TOK_WORD se= simple_expression ) 
		{$ret = intr.mk_word_var(curr_var_decl_is_visible, curr_var_name, "2",$se.text,input);}
		| sr= subrange 
		{$ret = intr.mk_range_var(curr_var_decl_is_visible, curr_var_name, sr.elementAt(0), sr.elementAt(1), input); }
		| ^( TYPE_VALUE_LIST_T tvl= type_value_list)
		{$ret = intr.mk_values_var(curr_var_decl_is_visible, curr_var_name, $tvl.ret, input); }
		| ^( SYNC_PROCESS_T ^( VALUE_T TOK_ATOM ) ) 
		{$ret = intr.mk_process_var(curr_var_decl_is_visible, curr_var_name, $TOK_ATOM.text, true, new Vector<String>(1), new Vector<StmtValueArrayWA>(1), input); }
		| ^( SYNC_PROCESS_T ^( VALUE_T TOK_ATOM{ intr.startAllDefineScope(); } simple_list_expression{ intr.attachParamDefine(curr_var_name, $simple_list_expression.ret); } ) ) 		
		{$ret = intr.mk_process_var(curr_var_decl_is_visible, curr_var_name, $TOK_ATOM.text, true, $simple_list_expression.ret_str, $simple_list_expression.ret, input); }
		| ^( ASYNC_PROCESS_T ^( VALUE_T TOK_ATOM ) ) 
		{$ret = intr.mk_process_var(curr_var_decl_is_visible, curr_var_name, $TOK_ATOM.text, false, new Vector<String>(1), new Vector<StmtValueArrayWA>(1), input); }
		| ^( ASYNC_PROCESS_T ^( VALUE_T TOK_ATOM{intr.startAllDefineScope(); } simple_list_expression{intr.attachParamDefine(curr_var_name, $simple_list_expression.ret); } ) ) 
		{$ret = intr.mk_process_var(curr_var_decl_is_visible, curr_var_name, $TOK_ATOM.text, false, $simple_list_expression.ret_str, $simple_list_expression.ret, input);}
		| ^( TOK_ARRAY sr= subrange ri= type )
		{$ret = intr.mk_range_array_var(curr_var_decl_is_visible, curr_var_name, $ri.ret, sr.elementAt(0), sr.elementAt(1), input); }
		;
		
type_value_list returns [Vector<String> ret] 
		: {$ret = new Vector<String>(10);}( type_value {$ret.add($type_value.text);})+ 
		;
type_value		: ^(VALUE_T complex_atom) /* actually only process_selector can be declared with complex constants */
		| integer
		| TOK_FALSEEXP
		| TOK_TRUEEXP
		;
complex_atom	: TOK_ATOM+
		;
//module_type		: ^(VALUE_T TOK_ATOM)
	//	| ^(VALUE_T TOK_ATOM 
	//	{ intr.startAllDefineScope(); }
	//	simple_list_expression
	//	{ intr.attachParamDefine(curr_var_name, $simple_list_expression.ret); } )
	//	| ^(TOK_ARRAY subrange module_type)
	//	;
simple_list_expression returns [Vector<StmtValueArrayWA> ret, Vector<String> ret_str]
		: ^( VALUE_LIST_T simple_list_expression_helper 
		{ $ret = $simple_list_expression_helper.ret;
		$ret_str = $simple_list_expression_helper.ret_str;  })		
		;
simple_list_expression_helper returns [Vector<StmtValueArrayWA> ret, Vector<String> ret_str]
		: { $ret = new Vector<StmtValueArrayWA>(5);
		$ret_str = new Vector<String>(5);  } 
		(simple_expression { $ret.add($simple_expression.ret);
		$ret_str.add($simple_expression.text); } )+
		;


/* ------------------------------------------------------------------------ */
/* ---------------------------- DECLARATIONS  ----------------------------- */
/* ------------------------------------------------------------------------ */

/*
 An NuSMV program is a repetition of modules. Also there can be one
 game denition.  Each module has a signature and a body. Game
 definition constructs are passed through variables mbp_parser_spec_list,
 mbp_parser_player_1 and mbp_parser_player_2  and not
 returned by this rule.
*/

module_list		: (module | game_definition | mas_agent ) 
			( module 
			| game_definition
			| mas_agent )*
			;

/*
module_list		: (module | game_definition ) 
			( module 
			| game_definition )*
			;
*/			
module		: ^(TOK_MODULE
			module_sign 
			{ intr.collect_module_inst(curr_module_name); }
			declarations
			{ intr.finalize_module_inst(); }
			)
		;
module_sign		: ^(MODULE_SIGN_T TOK_ATOM)
		{ curr_module_name = $TOK_ATOM.text; curr_module_is_agent = false; }
		| ^(MODULE_SIGN_T TOK_ATOM 
		{ curr_module_name = $TOK_ATOM.text; curr_module_is_agent = false; }
		^(REF_LIST_T sign_atom_list))
		;
sign_atom_list	: TOK_ATOM+
		;

/* The body of a module */
declarations		: (declaration)*;
declaration		: { curr_var_decl_is_visible = true; } var
//		^(MODULE_DECL_VAR_T { curr_var_decl_is_visible = false; } var)
		| isa //not implemented in JTLV
		| input_var
		| assign 
		| init
		| invar //not implemented in JTLV
		| trans
		| define
		| fairness //not implemented in JTLV
		| justice
		| compassion
		| invarspec //not implemented in JTLV
		| ctlspec //not implemented in JTLV
		| ltlspec //not implemented in JTLV
		| pslspec // and I think cannot be implemented in JTLV
		| compute //not implemented in JTLV
		| constants //not implemented in JTLV
		| predicate // and I think cannot be implemented in JTLV
		| mirror // and I think cannot be implemented in JTLV
		;

// LXY: The agent module definition for Multi-agent systems 
mas_agent		: ^(TOK_AGENT
			agent_sign 
			{ intr.collect_module_inst(curr_module_name); }
			agent_declarations
			{ intr.finalize_module_inst(); }
			)
			;


agent_sign		: ^(AGENT_SIGN_T TOK_ATOM)
		{ curr_module_name = $TOK_ATOM.text; curr_module_is_agent = true; }
		| ^(AGENT_SIGN_T TOK_ATOM 
		{ curr_module_name = $TOK_ATOM.text; curr_module_is_agent = true; }
		^(AGENT_REF_LIST_T agent_sign_atom_list))
		;

// The body of a module 
agent_declarations		: (agent_declaration)*;
agent_sign_atom_list 	:  agent_sign_atom+; 

agent_sign_atom		: TOK_ATOM 
			| ^(TOK_VISIBLE TOK_ATOM) 
			;
			
agent_declaration		: 
		 { curr_var_decl_is_visible = true; } var 
//		^(AGENT_DECL_VAR_T { curr_var_decl_is_visible = true; } var)
//		| ^(AGENT_DECL_INVISIBLE_VAR_T { curr_var_decl_is_visible = false; } agent_invisible_var)	// the variables declared here are invisible
		| { curr_var_decl_is_visible = false; } agent_invisible_var
		| input_var
		| assign 
		| init
		| trans
		| define
		| justice
		| compassion
		;

agent_invisible_var		: TOK_INVISIBLE_VAR
				| ^(TOK_INVISIBLE_VAR var_decl_list)
				;

// LXY: End of above definitions 


/* a game definition consists of GAME keyword, definitions of its two
   players and a list of usual and game specifications.
   NOTE: this rule returns value not by usual way 
   but through variables mbp_parser_spec_list,
   mbp_parser_player_1 and mbp_parser_player_2.
*/
game_definition	: ^(TOK_GAME game_body)
		;
game_body		: game_body_element (game_body)?;
/* a GAME definition consists of both players definitions,
   usual specifications and game specifications.
*/
game_body_element	: ^(TOK_PLAYER_1 player_body)
		| ^(TOK_PLAYER_2 player_body)
// not implemented    | invarspec
// not implemented    | ctlspec
// not implemented    | ltlspec
// not implemented    | pslspec
// not implemented    | compute
		| reachtarget
		| avoidtarget
		| reachdeadlock
		| avoiddeadlock
		| buchigame
		| genreactivity
		;
/* a player's body is the same as module's body except the player
   cannot have ISA declaration and specifications 
*/
player_body		: (player_body_element)*
		;
player_body_element	: var
// not implemented : | input_var
		| assign 
		| init
		| invar //not implemented in JTLV
		| trans
		| define
// not implemented :    | fairness
// not implemented :    | justice
// not implemented :    | compassion
		;
/*
 Variable declarations:
 This includes also the instantiation of module
 (in synchronous and asynchronous product).
*/
/* Do we realy want to have empty VAR declarations */
var		: TOK_VAR
		| ^(TOK_VAR var_decl_list)
		;
/* opositive direction chosen for some reason */
var_decl_list		: var_decl (var_decl)* 
		;
//var_decl		: ^(VAR_DECL_T decl_var_id type)
var_decl
@init{
		boolean old_init_trans_phase_val = false;
        	int init_marker = -1;
        	int cond_marker = -1;
        	int inc_marker = -1;
        	int expr_marker = -1;
        	int end_marker = -1;
} 		:  ^( VAR_DECL_T var_id{ curr_var_name = $var_id.ret; } type ) 
		{intr.add_element(curr_module_name, $type.ret, curr_var_decl_is_visible, input); }
		| ^( TOK_FOR{intr.startDummyPhase();
		
		old_init_trans_phase_val = intr.isInitTransPhase();
                intr.endInitTransPhase();
                } TOK_LP TOK_ATOM NOP{init_marker = input.mark(); } simple_expression NOP{cond_marker = input.mark(); } simple_expression NOP{inc_marker = input.mark(); } simple_expression NOP TOK_RP TOK_LCB{expr_marker = input.mark(); } var_decl_list TOK_RCB
                {end_marker = input.mark(); 

                 if(old_init_trans_phase_val)
                    intr.startInitTransPhase();

                intr.endDummyPhase();
                intr.evalSimpleForLoop("VAR", $TOK_ATOM.text, init_marker,
                    cond_marker, inc_marker, expr_marker, end_marker, input, this, old_init_trans_phase_val); 
                }) 
		;
		
input_var		: TOK_IVAR
		| ^(TOK_IVAR ivar_decl_list)
		;
/* opositive direction chosen for some reason */
ivar_decl_list		: ivar_decl (ivar_decl)*
		;
ivar_decl 
@init{
		boolean old_init_trans_phase_val = false;
        	int init_marker = -1;
        	int cond_marker = -1;
        	int inc_marker = -1;
        	int expr_marker = -1;
        	int end_marker = -1;
} 		:  ^( IVAR_DECL_T var_id type ) 
		| ^( TOK_FOR{intr.startDummyPhase();
		
		old_init_trans_phase_val = intr.isInitTransPhase();
                intr.endInitTransPhase();
                } TOK_LP TOK_ATOM NOP{init_marker = input.mark(); } simple_expression NOP{cond_marker = input.mark(); } simple_expression NOP{inc_marker = input.mark(); } simple_expression NOP TOK_RP TOK_LCB{expr_marker = input.mark(); } ivar_decl_list TOK_RCB 
                {end_marker = input.mark(); 

                 if(old_init_trans_phase_val)
                    intr.startInitTransPhase();

                intr.endDummyPhase();
                intr.evalSimpleForLoop("IVAR", $TOK_ATOM.text, init_marker,
                    cond_marker, inc_marker, expr_marker, end_marker, input, this, old_init_trans_phase_val); 
                }) ;

/* Definitions */
define		: ^(TOK_DEFINE define_list)
		;
define_list		: define_decl ( define_decl )* 
		;
define_decl 
@init{
		boolean old_init_trans_phase_val = false;
        	int init_marker = -1;
        	int cond_marker = -1;
        	int inc_marker = -1;
        	int expr_marker = -1;
        	int end_marker = -1;
} 		: ^( DEFINE_DECL_T{intr.startAllDefineScope(); } var_id{curr_var_name = $var_id.ret;} simple_expression{intr.attachDefine($var_id.ret, $simple_expression.ret);} ) 
		{intr.add_element(curr_module_name, intr.mk_define_var(curr_var_name, input), false, input); }
		| ^( TOK_FOR{intr.startDummyPhase();
		
		old_init_trans_phase_val = intr.isInitTransPhase();
                intr.endInitTransPhase();
                } TOK_LP TOK_ATOM NOP{init_marker = input.mark(); } simple_expression NOP{cond_marker = input.mark(); } simple_expression NOP{inc_marker = input.mark();} simple_expression NOP TOK_RP TOK_LCB{expr_marker = input.mark();} define_list TOK_RCB
                {end_marker = input.mark(); 

                 if(old_init_trans_phase_val)
                    intr.startInitTransPhase();

                intr.endDummyPhase();
                intr.evalSimpleForLoop("DEFINE", $TOK_ATOM.text, init_marker,
                    cond_marker, inc_marker, expr_marker, end_marker, input, this, old_init_trans_phase_val); 
                });

/* Assignments of initial, current or next value of variables */
assign		: ^(TOK_ASSIGN assign_list)
		;
assign_list	: one_assign ( one_assign )*
		;
one_assign 
@init{
		boolean old_init_trans_phase_val = false;
        	int init_marker = -1;
        	int cond_marker = -1;
        	int inc_marker = -1;
        	int expr_marker = -1;
        	int end_marker = -1;
} 		: ^( ASSIGN_DECL_T var_id simple_expression {intr.attachASSIGNStmt($var_id.ret, $simple_expression.ret);}) 
		| ^( INIT_ASSIGN_DECL_T var_id simple_expression {intr.attachINITStmt($var_id.ret, $simple_expression.ret); }) 
		| ^( NEXT_ASSIGN_DECL_T var_id next_expression {intr.attachNEXTStmt($var_id.ret, $next_expression.ret);}) 
		| ^( TOK_FOR{intr.startDummyPhase();
		
		old_init_trans_phase_val = intr.isInitTransPhase();
                intr.endInitTransPhase();
                } TOK_LP TOK_ATOM NOP{init_marker = input.mark();} simple_expression NOP{cond_marker = input.mark();} simple_expression NOP{inc_marker = input.mark();} simple_expression NOP TOK_RP TOK_LCB{expr_marker = input.mark(); } assign_list TOK_RCB 
                {end_marker = input.mark(); 

                 if(old_init_trans_phase_val)
                    intr.startInitTransPhase();

                intr.endDummyPhase();
                intr.evalSimpleForLoop("ASSIGN", $TOK_ATOM.text, init_marker,
                    cond_marker, inc_marker, expr_marker, end_marker, input, this, old_init_trans_phase_val); 
                })
		;

/* Direct finite state machine definition (init, invar, trans) */
init		: ^(TOK_INIT simple_expression { intr.attachINIT($simple_expression.ret); } )
		;

/* not implemented in JTLV */
invar		: TOK_INVAR
		;
/* -------------------- */

trans		: ^(TOK_TRANS next_expression 
		{ intr.attachTRANS($next_expression.ret); }
		)
		;

/* Fairness declarations */
/* not implemented in JTLV */
fairness		: TOK_FAIRNESS
		;
/* -------------------- */
justice		: ^(TOK_JUSTICE justice_list)
		;
justice_list	: justice_decl ( justice_decl )*
		;
justice_decl 
@init{
		boolean old_init_trans_phase_val = false;
        	int init_marker = -1;
        	int cond_marker = -1;
        	int inc_marker = -1;
        	int expr_marker = -1;
        	int end_marker = -1;
} 		: ^( JUSTICE_DECL_T simple_expression{intr.attachJUSTStmt($simple_expression.ret);}) 
		| ^( TOK_FOR{intr.startDummyPhase();
		
		old_init_trans_phase_val = intr.isInitTransPhase();
                intr.endInitTransPhase();
                } TOK_LP TOK_ATOM NOP{init_marker = input.mark();} simple_expression NOP{cond_marker = input.mark();} simple_expression NOP{inc_marker = input.mark();} simple_expression NOP TOK_RP TOK_LCB{expr_marker = input.mark();} justice_list TOK_RCB
                {end_marker = input.mark(); 

                 if(old_init_trans_phase_val)
                    intr.startInitTransPhase();

                intr.endDummyPhase();
                intr.evalSimpleForLoop("JUSTICE", $TOK_ATOM.text, init_marker,
                    cond_marker, inc_marker, expr_marker, end_marker, input, this, old_init_trans_phase_val); 
                })
		;
compassion	: ^(TOK_COMPASSION compassion_list)
		;
compassion_list	: compassion_decl ( compassion_decl )*
		;
compassion_decl 
@init{
		boolean old_init_trans_phase_val = false;
        	int init_marker = -1;
        	int cond_marker = -1;
        	int inc_marker = -1;
        	int expr_marker = -1;
        	int end_marker = -1;
} 		: ^( COMPASSION_DECL_T f= simple_expression TOK_COMMA s= simple_expression{intr.attachCOMPStmt(f.ret, s.ret);} ) 
		| ^( TOK_FOR{intr.startDummyPhase();
		
		old_init_trans_phase_val = intr.isInitTransPhase();
                intr.endInitTransPhase();
                } TOK_LP TOK_ATOM NOP{init_marker = input.mark();} simple_expression NOP{cond_marker = input.mark();} simple_expression NOP{inc_marker = input.mark();} simple_expression NOP TOK_RP TOK_LCB{expr_marker = input.mark();} compassion_list TOK_RCB
                {end_marker = input.mark(); 

                 if(old_init_trans_phase_val)
                    intr.startInitTransPhase();

                intr.endDummyPhase();
                intr.evalSimpleForLoop("COMPASSION", $TOK_ATOM.text, init_marker,
                    cond_marker, inc_marker, expr_marker, end_marker, input, this, old_init_trans_phase_val); 
                })
		;
/* not implemented in JTLV */
invarspec		: TOK_INVARSPEC
		;

/* Specifications and computation of min and max distance */
ctlspec		: TOK_SPEC
		| TOK_CTLSPEC
		;
ltlspec		: TOK_LTLSPEC
		;
pslspec		: TOK_PSLSPEC
		;
constants		: TOK_CONSTANTS
		;
/* -------------------- */

//!!!!! I think its a bug in NuSMV parser..... it allows ",,, <complex_atom>, <complex_atom>"
// I'm leaving it consistant with this bug....
constants_expression	: 
		| complex_atom (TOK_COMMA complex_atom)*
		;

player_num		: TOK_PLAYER_1
		| TOK_PLAYER_2
		;

/* not implemented in JTLV */
predicate		: TOK_PRED
		;
mirror		: TOK_MIRROR
		;
/* -------------------- */

reachtarget		: ^(TOK_REACHTARGET player_num simple_expression)
		;
avoidtarget		: ^(TOK_AVOIDTARGET player_num simple_expression)
		;
reachdeadlock	: ^(TOK_REACHDEADLOCK player_num)
		;
avoiddeadlock	: ^(TOK_AVOIDDEADLOCK player_num)
		;
buchigame		: ^(TOK_BUCHIGAME player_num simple_list_expression)
		;
genreactivity		: ^(TOK_GENREACTIVITY player_num simple_list_expression TOK_IMPLIES simple_list_expression)
		;

/* not implemented in JTLV */
compute		: TOK_COMPUTE
		;
compute_expression	: TOK_MMIN TOK_LB ctl_expression TOK_COMMA ctl_expression TOK_RB
		| TOK_MMAX TOK_LB ctl_expression TOK_COMMA ctl_expression TOK_RB
		;

/* Module macro-expansion */
isa		: TOK_ISA
		;
/* -------------------- */

/* parse an optional semicolon */
optsemi		: 
		| TOK_SEMI;


/* Variable identifiers.
   decl_var_id is used for declarations; self not allowed.
   var_id is used to reference variables in assignment, includes self.
 */
//decl_var_id returns[String ret]
	//	: ^(REF_T decl_var_id_helper)
	//	{ $ret = $decl_var_id_helper.ret; }
	//	;
//decl_var_id_helper returns[String ret]
	//	: f=TOK_ATOM
	//	{ $ret = $f.text; }
	//		( ra=TOK_ATOM
	//		{ $ret = $ret + "." + $ra.text; }
	//		| rn=TOK_NUMBER
	//		{ $ret = $ret + "." + $rn.text; }
	//		| ri=integer
	//		{ $ret = $ret + "[" + $ri.text + "]"; }
	//		)*
	//	;

var_id returns [StringArrayWA ret] : ^( REF_T var_id_helper )
		{$ret=$var_id_helper.ret;}
		;
var_id_helper returns [StringArrayWA ret] 
		: f= TOK_ATOM 
		{$ret = new StringArrayWA(intr, $f.text, "", null, "");}
		(ra= TOK_ATOM 
		{StringArrayWA temp = new StringArrayWA(intr, "." + $ra.text, "", null, ""); 
            	 $ret = ret.concat(temp); }
		| rn= TOK_NUMBER 
		{StringArrayWA temp = new StringArrayWA(intr, "." + $rn.text, "", null, ""); 
            	 $ret = ret.concat(temp); }
		| TOK_LB rs= simple_expression TOK_RB 
		{StringArrayWA temp = new StringArrayWA(intr, "[" + $rs.text + "]", "[", $rs.ret , "]"); 
		$ret = ret.concat(temp); })* ;


/* ------------------------------------------------------------------------ */
/* ----------------------------  COMMANDS  -------------------------------- */
/* ------------------------ NOT IMPLEMENTED IN JTLV ------------------------ */
/* ------------------------------------------------------------------------ */
command		: command_case
////???		| error TOK_SEMI {return(1);}
////???		| error {return(1);}
		;
command_case	: TOK_GOTO
		| TOK_INIT
		| TOK_FAIRNESS
		| TOK_TRANS
		| TOK_CONSTRAINT
		| TOK_SIMPWFF
		| TOK_CTLWFF
		| TOK_LTLWFF
		| TOK_COMPWFF
		//| TOK_PREDSLIST predicate_list { yyerror("given token is not supported."); YYABORT; }                
		;
context		: TOK_ATOM
			( TOK_DOT TOK_ATOM 
			| TOK_LB simple_expression TOK_RB)*
		;
trace		: TOK_NUMBER (TOK_DOT TOK_NUMBER)*
		;
state		: trace TOK_DOT TOK_NUMBER
		;

