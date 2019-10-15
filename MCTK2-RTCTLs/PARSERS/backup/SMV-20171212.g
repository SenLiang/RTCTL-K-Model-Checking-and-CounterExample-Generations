grammar SMV;

options {
	output=AST;
	//backtrack = true;
	//k=4;
}

tokens {
	NOP; // an empty token to help identify components at the walker stage.
	PROGRAM_T;
	MODULE_SIGN_T;
	VAR_DECL_T;
	IVAR_DECL_T;
	DEFINE_DECL_T;
	ASSIGN_DECL_T;
	INIT_ASSIGN_DECL_T;
	NEXT_ASSIGN_DECL_T;
	JUSTICE_DECL_T;
	COMPASSION_DECL_T;
	SUBRANGE_T;
	TYPE_VALUE_LIST_T;
	SYNC_PROCESS_T;
	ASYNC_PROCESS_T;
	VALUE_T; 
	VALUE_LIST_T; // list of atoms... seperated with COMMA
	REF_T; 
	REF_LIST_T; // list of added references from the module sign
	SET_LIST_EXP_T;
	BLOCK_T;
	CASE_LIST_EXPR_T;
	CASE_ELEMENT_EXPR_T;
	BIT_SELECT_T;
	ARRAY_INDEX_T;
	TOK_UNARY_MINUS_T;
	PURE_CTL_T;
	PURE_LTL_T;
	
	//add in 1.5
	TOK_AND_FOR_T;
	TOK_OR_FOR_T;
	TOK_PLUS_FOR_T;
	
	//LXY: for MAS
	AGENT_SIGN_T;
	AGENT_REF_LIST_T; // list of added references from the agent sign
	AGENT_SIGN_ATOM_T;
	MODULE_DECL_VAR_T;
	AGENT_DECL_VAR_T;
	AGENT_DECL_INVISIBLE_VAR_T;
	AGENT_DECL_AVAR_T;
} // an imaginary node

@header {
package edu.wis.jtlv.env.core.smv;
import java.util.HashMap;
import java.util.Vector;
import edu.wis.jtlv.env.Env;
}
@members {
public static StructuralInit smv_initiator = StructuralInit.get_instance();
// for the module signature
//public static String curr_module_name;
public static Vector<String> curr_module_sign_vector = new Vector<String>(10);

//LXY: for MAS
//public static Vector<String> curr_agent_sign_vector = new Vector<String>(10);
//public static Vector<Boolean> curr_module_sign_visible_vector = new Vector<Boolean>(10);

// for the defines
//public static String curr_decl_var_id;
//public static CommonTree curr_simple_expression;
// for atoms to replace with defines
//public static String curr_primary_expr_helper1_pointer;
// for exception handling
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
public void emitErrorMessage(String msg) {
	// System.err.println(msg);
	// do nothing.
}

//// I don't like the printing...
    //public void recoverFromMismatchedToken(IntStream input,
    //		RecognitionException e, int ttype, BitSet follow)
    //		throws RecognitionException {
    //	//System.err.println("BR.recoverFromMismatchedToken");
    //	// if next token is what we are looking for then "delete" this token
    //	if (input.LA(2) == ttype) {
    //		reportError(e);
    //		/*
    //		 * System.err.println("recoverFromMismatchedToken deleting
    //		 * "+input.LT(1)+ " since "+input.LT(2)+" is what we want");
    //		 */
    //		beginResync();
    //		input.consume(); // simply delete extra token
    //		endResync();
    //		input.consume(); // move past ttype token as if all were ok
    //		return;
    //	}
    //	if (!recoverFromMismatchedElement(input, e, follow)) {
    //		throw e;
    //	}
    //}
}
@lexer::header {
package edu.wis.jtlv.env.core.smv;
import edu.wis.jtlv.env.Env;
//import edu.wis.jtlv.env.module.SMVParseException;
}
@lexer::members {
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
public void emitErrorMessage(String msg) {
	// System.err.println(msg);
	// do nothing.
}

}
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// SMV tree construction....
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// to the JTLV implementation many elements had been removed.
// they are all tagged with "not implemented in JTLV" 

program		: EOF
			| module_list EOF
			-> ^(PROGRAM_T module_list)
/* not implemented in JTLV */
		| command 
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV LTL statement", input); }
		ltl_expression!
/* -------------------- */
		;

/* --------------------------------------------------------------------- */
/* ---------------------------- EXPRESSION ----------------------------- */
/* --------------------------------------------------------------------- */
/* Later on unary plus must be implemented as a usual unary operator
   (as unary minus now)
*/
number		: TOK_NUMBER
		| TOK_PLUS! TOK_NUMBER;
integer		: TOK_NUMBER
		-> ^(TOK_PLUS TOK_NUMBER)
		| TOK_PLUS TOK_NUMBER
		-> ^(TOK_PLUS TOK_NUMBER)
		| TOK_MINUS TOK_NUMBER
		-> ^(TOK_MINUS TOK_NUMBER)
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

subrange : integer_or_atom TOK_TWODOTS integer_or_atom 
		-> ^( SUBRANGE_T integer_or_atom integer_or_atom )
		;
constant		: TOK_FALSEEXP
		| TOK_TRUEEXP
		| number
		| number_word
		//| number_frac { yyerror("fractional constants are not supported."); YYABORT;}
		//| number_exp {yyerror("exponential constants are not supported."); YYABORT;}
		//| number_real {yyerror("real constants are not supported."); YYABORT;}
		;
		
primary_expr_helper1	
		: constant primary_expr_select
		-> ^(VALUE_T constant NOP primary_expr_select)
		| primary_expr_helper1_pointer1
		| primary_expr_helper1_pointer2
		| TOK_LP basic_expr TOK_RP primary_expr_select
		-> ^(BLOCK_T basic_expr NOP primary_expr_select)
		| TOK_BOOL  TOK_LP basic_expr TOK_RP primary_expr_select
		-> ^(TOK_BOOL  basic_expr NOP primary_expr_select)
		| TOK_WORD1 TOK_LP basic_expr TOK_RP primary_expr_select
		-> ^(TOK_WORD1 basic_expr NOP primary_expr_select)
		| TOK_NEXT TOK_LP basic_expr TOK_RP primary_expr_select
		-> ^(TOK_NEXT basic_expr NOP primary_expr_select)
		| TOK_CASE case_element_list_expr TOK_ESAC primary_expr_select
		-> ^(CASE_LIST_EXPR_T case_element_list_expr NOP primary_expr_select)
		| TOK_WAREAD TOK_LP simple_expression TOK_COMMA simple_expression TOK_RP primary_expr_select
		-> ^(TOK_WAREAD simple_expression simple_expression NOP primary_expr_select)
		| TOK_WAWRITE TOK_LP simple_expression TOK_COMMA simple_expression TOK_COMMA simple_expression TOK_RP primary_expr_select
		-> ^(TOK_WAWRITE simple_expression simple_expression simple_expression NOP primary_expr_select)
		;
primary_expr_helper1_pointer1 
		: TOK_ATOM primary_expr_select
		-> ^(VALUE_T TOK_ATOM NOP primary_expr_select)
		;
primary_expr_helper1_pointer2 
		: TOK_SELF primary_expr_select
		-> ^(VALUE_T TOK_SELF NOP primary_expr_select)
		;
primary_expr_select	: (primary_expr_select_helper | primary_expr_ref)*
		; 
primary_expr_ref	: (TOK_DOT! (TOK_ATOM | TOK_NUMBER))
		;
//primary_expr_select_helper	: TOK_LB f=simple_expression 
//			( TOK_RB
//			-> ^(ARRAY_INDEX_T $f)
//			| TOK_COLON r=simple_expression TOK_RB)
//			-> ^(BIT_SELECT_T $f $r)
//		;
//update in 1.5
primary_expr_select_helper :  TOK_LB simple_expression TOK_RB
		-> ^( ARRAY_INDEX_T simple_expression )
		| TOK_LB f= number TOK_COLON s= number TOK_RB
		-> ^( BIT_SELECT_T $f $s)
		;
//cancel in 1.5
//primary_expr_select_helper_arr_suffix
//		: TOK_LB simple_expression TOK_RB
//		-> ^(ARRAY_INDEX_T simple_expression)
//		;
//primary_expr_select_helper_bit_suffix
//		: TOK_LB f=simple_expression TOK_COLON s=simple_expression TOK_RB
//		-> ^(BIT_SELECT_T $f $s)
//		;

primary_expr		: primary_expr_helper1
		| TOK_MINUS primary_expr
		-> ^(TOK_UNARY_MINUS_T primary_expr)
		| TOK_NOT primary_expr
		-> ^(TOK_NOT primary_expr)
		| constru_for_expr
		;

//add in 1.5
constru_for_expr 	:( TOK_AND TOK_FOR TOK_LP i_and= TOK_ATOM TOK_EQUAL init_and= simple_expression TOK_SEMI cond_and= simple_expression TOK_SEMI ii_and= TOK_ATOM TOK_EQUAL inc_and= simple_expression TOK_RP TOK_LCB loop_main= simple_expression TOK_RCB )
		-> ^( TOK_AND_FOR_T TOK_LP $i_and NOP $init_and NOP $cond_and NOP $inc_and NOP TOK_RP TOK_LCB $loop_main TOK_RCB ) 
		| ( TOK_OR TOK_FOR TOK_LP i_or= TOK_ATOM TOK_EQUAL init_or= simple_expression TOK_SEMI cond_or= simple_expression TOK_SEMI ii_or= TOK_ATOM TOK_EQUAL inc_or= simple_expression TOK_RP TOK_LCB loop_main= simple_expression TOK_RCB ) 
		-> ^( TOK_OR_FOR_T TOK_LP $i_or NOP $init_or NOP $cond_or NOP $inc_or NOP TOK_RP TOK_LCB $loop_main TOK_RCB ) 
		| ( TOK_PLUS TOK_FOR TOK_LP i_plus= TOK_ATOM TOK_EQUAL init_plus= simple_expression TOK_SEMI cond_plus= simple_expression TOK_SEMI ii_plus= TOK_ATOM TOK_EQUAL inc_plus= simple_expression TOK_RP TOK_LCB loop_main= simple_expression TOK_RCB ) 
		-> ^( TOK_PLUS_FOR_T TOK_LP $i_plus NOP $init_plus NOP $cond_plus NOP $inc_plus NOP TOK_RP TOK_LCB $loop_main TOK_RCB )
		;
		
/* last element in the list. Add FAILURE node */
case_element_list_expr	: case_element_expr (case_element_expr)*
		;
case_element_expr	: basic_expr TOK_COLON basic_expr TOK_SEMI
		-> ^(CASE_ELEMENT_EXPR_T basic_expr basic_expr)
		;
concatination_expr	: primary_expr (TOK_CONCATENATION^ primary_expr)*
		;
multiplicative_expr	: concatination_expr 
			( TOK_TIMES^ concatination_expr
			| TOK_DIVIDE^ concatination_expr)*
		;
additive_expr		: multiplicative_expr
			( TOK_PLUS^ multiplicative_expr
			| TOK_MINUS^ multiplicative_expr)*
		;
remainder_expr	: additive_expr (TOK_MOD^ additive_expr)*;
shift_expr		: remainder_expr
			( TOK_LSHIFT^ remainder_expr
			| TOK_RSHIFT^ remainder_expr)*
		/*
		| shift_expr TOK_LROTATE remainder_expr
		| shift_expr TOK_RROTATE remainder_expr */
		;
set_list_expr		: basic_expr (TOK_COMMA! basic_expr)*
		;
set_expr		: shift_expr
		| subrange
		| TOK_LCB set_list_expr TOK_RCB
		-> ^(SET_LIST_EXP_T set_list_expr)
		;
union_expr		: set_expr (TOK_UNION^ set_expr)*
		;
in_expr		: union_expr (TOK_SETIN^ union_expr)*
		;
relational_expr	: in_expr
			( TOK_EQUAL^ in_expr 
			| TOK_NOTEQUAL^ in_expr 
			| TOK_LT^ in_expr 
			| TOK_GT^ in_expr 
			| TOK_LE^ in_expr 
			| TOK_GE^ in_expr)*
		;
pure_ctl_expr		: TOK_EX^ ctl_expr
		| TOK_AX^ ctl_expr
		| TOK_EF^ ctl_expr
		| TOK_AF^ ctl_expr
		| TOK_EG^ ctl_expr
		| TOK_AG^ ctl_expr
		| TOK_AA^ TOK_LB! ctl_basic_expr (TOK_UNTIL | TOK_BUNTIL subrange) ctl_basic_expr TOK_RB!
		| TOK_EE^ TOK_LB! ctl_basic_expr (TOK_UNTIL | TOK_BUNTIL subrange) ctl_basic_expr TOK_RB!
		| TOK_EBF^ subrange ctl_expr
		| TOK_ABF^ subrange ctl_expr
		| TOK_EBG^ subrange ctl_expr
		| TOK_ABG^ subrange ctl_expr
		/* NOT is required here to allow such expr as "! EX a" */
		| TOK_NOT^ pure_ctl_expr
		;
ctl_expr		: (TOK_NOT* // resolving conflict with the TOK_NOT
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
		-> ^(PURE_CTL_T pure_ctl_expr)
		| relational_expr
		;
/* there are separate CTL rules for propositional expressions 
   to avoid ambiguity related to TOK_UNTIL token in LTL and CTL.
*/
ctl_and_expr		: ctl_expr (TOK_AND^ ctl_expr)*
		;
ctl_or_expr		: ctl_and_expr
			( TOK_OR^ ctl_and_expr 
			| TOK_XOR^ ctl_and_expr 
			| TOK_XNOR^ ctl_and_expr)*
		;
ctl_iff_expr		: ctl_or_expr (TOK_IFF^ ctl_or_expr)*
		;
ctl_implies_expr	: ctl_iff_expr (TOK_IMPLIES^ ctl_implies_expr)?;

ctl_basic_expr		: ctl_implies_expr
		;
/* pure ltl_unary_expr is introduced to allow NOT before the ltl expressions */
pure_ltl_unary_expr	: TOK_OP_NEXT^ ltl_unary_expr
		| TOK_OP_PREC^ ltl_unary_expr
		| TOK_OP_NOTPRECNOT^ ltl_unary_expr
		| TOK_OP_GLOBAL^ ltl_unary_expr
		| TOK_OP_HISTORICAL^ ltl_unary_expr
		| TOK_OP_FUTURE^ ltl_unary_expr
		| TOK_OP_ONCE^ ltl_unary_expr
		/* NOT is required here to allow such expr as "! X a" */
		| TOK_NOT^ pure_ltl_unary_expr
		;
/* LTL has to include CTL to allow paranthesis around CTL (and everything) */
ltl_unary_expr		: (TOK_NOT* // resolving conflict with the TOK_NOT
			( TOK_OP_NEXT
			| TOK_OP_PREC
			| TOK_OP_NOTPRECNOT
			| TOK_OP_GLOBAL
			| TOK_OP_HISTORICAL
			| TOK_OP_FUTURE
			| TOK_OP_ONCE)) => pure_ltl_unary_expr /* all unary LTL operators */ 
		-> ^(PURE_LTL_T pure_ltl_unary_expr)
		| ctl_expr
		; 
/* all LTL binary operators */
ltl_binary_expr	: ltl_unary_expr
			( TOK_UNTIL^ ltl_unary_expr 
			| TOK_SINCE^ ltl_unary_expr 
			| TOK_RELEASES^ ltl_unary_expr 
			| TOK_TRIGGERED^ ltl_unary_expr)*
		;
and_expr		: ltl_binary_expr (TOK_AND^ ltl_binary_expr)*
		;
or_expr		: and_expr
			( TOK_OR^ and_expr
			| TOK_XOR^ and_expr 
			| TOK_XNOR^ and_expr)*
		;
iff_expr		: or_expr (TOK_IFF^ or_expr)*
		;
/* right association */
implies_expr		: iff_expr (TOK_IMPLIES^ implies_expr)?
		;
basic_expr		: implies_expr
		;
/* every expression below, at first, remembers the current kind of 
   the parsed expression and then sets its own kind.
   After parsing the kind of expression is restored
*/
simple_expression	: basic_expr
		;
next_expression	: basic_expr
		;

/* not implemented in JTLV */ 
ctl_expression	: basic_expr
		;
psl_expression	: basic_expr
		;
ltl_expression		: basic_expr
		;
/* -------------------- */

/* ------------------------------------------------------------------------ */
/* ----------------------------  TYPES ------------------------------------ */
/* ------------------------------------------------------------------------ */

//cancel in 1.5
//itype		: TOK_BOOLEAN
/* not implemented in JTLV -- for some reason this creates a bug, since it is not implemented, I'm removeing it
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV type INTEGER", input); }
		TOK_INTEGER // { yyerror("given token is not supported."); YYABORT; }
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV type REAL", input); }
		TOK_REAL // { yyerror("given token is not supported."); YYABORT;}
/* -------------------- */
//		| TOK_WORD TOK_LB simple_expression TOK_RB
//		-> ^(TOK_WORD simple_expression)
//		| subrange
//		| TOK_LCB type_value_list TOK_RCB
//		-> ^(TYPE_VALUE_LIST_T type_value_list)
//		| TOK_ARRAY f=TOK_WORD TOK_LB simple_expression TOK_RB TOK_OF s=TOK_WORD TOK_LB simple_expression TOK_RB  
//		{ if (true) throw new SMVUnimplementedException("Unimplemented JTLV type " + $f.text + "[..] of " + $s.text + "[..]", input); }
//		-> ^(TOK_ARRAY ^(TOK_WORD simple_expression) ^(TOK_WORD simple_expression))
//		| TOK_ARRAY subrange TOK_OF itype
//		-> ^(TOK_ARRAY subrange itype)
//		;
//update in 1.5
type :  TOK_BOOLEAN 
	| TOK_WORD TOK_LB simple_expression TOK_RB 
		-> ^( TOK_WORD simple_expression ) 
	| subrange | TOK_LCB type_value_list TOK_RCB 
		-> ^( TYPE_VALUE_LIST_T type_value_list ) 
	| TOK_ATOM ( TOK_LP TOK_RP )? 
		-> ^( SYNC_PROCESS_T ^( VALUE_T TOK_ATOM ) ) 
	| TOK_ATOM TOK_LP simple_list_expression TOK_RP 
		-> ^( SYNC_PROCESS_T ^( VALUE_T TOK_ATOM simple_list_expression ) ) 
	| TOK_PROCESS TOK_ATOM ( TOK_LP TOK_RP )? 
		-> ^( ASYNC_PROCESS_T ^( VALUE_T TOK_ATOM ) ) 
	| TOK_PROCESS TOK_ATOM TOK_LP simple_list_expression TOK_RP 
		-> ^( ASYNC_PROCESS_T ^( VALUE_T TOK_ATOM simple_list_expression ) ) 
	| TOK_ARRAY subrange TOK_OF type 
		-> ^( TOK_ARRAY subrange type ) ;
type_value_list	: type_value (TOK_COMMA! type_value)*
		;
type_value		: complex_atom /* actually only process_selector can be declared with complex constants */
		-> ^(VALUE_T complex_atom)
		| integer
		| TOK_FALSEEXP
		| TOK_TRUEEXP
		;
complex_atom	: TOK_ATOM (TOK_DOT! TOK_ATOM)*
		;
//cancel in 1.5
//module_type		: TOK_ATOM
//		-> ^(VALUE_T TOK_ATOM)
//		| TOK_ATOM TOK_LP TOK_RP
//		-> ^(VALUE_T TOK_ATOM)
//		| TOK_ATOM TOK_LP simple_list_expression TOK_RP
//		-> ^(VALUE_T TOK_ATOM simple_list_expression)
//		| TOK_ARRAY subrange TOK_OF module_type
//		-> ^(TOK_ARRAY subrange module_type)
//		;
simple_list_expression	: simple_list_expression_helper
		-> ^(VALUE_LIST_T simple_list_expression_helper)
		;
simple_list_expression_helper
		: simple_expression (TOK_COMMA! simple_expression)*
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
module_list	: (module | game_definition | mas_agent) 
		  (module | game_definition | mas_agent)*
		;

module		: { curr_module_sign_vector.clear(); }
		TOK_MODULE module_sign
			{ //curr_module_name = curr_module_sign_vector.elementAt(0); 
		  	smv_initiator.add_module(curr_module_sign_vector, input); }
		declarations
			-> ^(TOK_MODULE module_sign declarations)
		;
module_sign	: TOK_ATOM
			{ curr_module_sign_vector.add($TOK_ATOM.text); //the module name
			  curr_module_sign_vector.add("0"); //"1" -- is Agent; "0" -- is normal module
			}
			-> ^(MODULE_SIGN_T TOK_ATOM)
		| TOK_ATOM TOK_LP TOK_RP
			{ curr_module_sign_vector.add($TOK_ATOM.text); //the module name
			  curr_module_sign_vector.add("0"); //"1" -- is Agent; "0" -- is normal module
			}
			-> ^(MODULE_SIGN_T TOK_ATOM)
		| TOK_ATOM 
			{ curr_module_sign_vector.add($TOK_ATOM.text); //the module name
			  curr_module_sign_vector.add("0"); //"1" -- is Agent; "0" -- is normal module
			}
		  TOK_LP sign_atom_list TOK_RP
			-> ^(MODULE_SIGN_T TOK_ATOM ^(REF_LIST_T sign_atom_list))
		;


/* The body of a module */
declarations	: (declaration)*;
sign_atom_list 	:  f=TOK_ATOM 
		{ curr_module_sign_vector.add($f.text); //the argument name
		  curr_module_sign_vector.add("0"); //"1" -- visible; "0" -- invisible
		}
		(TOK_COMMA! r=TOK_ATOM 
		{ curr_module_sign_vector.add($r.text); //the argument name
		  curr_module_sign_vector.add("0"); //"1" -- visible; "0" -- invisible
		}
		)*
		;
declaration	: var
//		  -> ^(MODULE_DECL_VAR_T var)	
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
/*		| ctlspec //not implemented in JTLV
		| ltlspec //not implemented in JTLV
		| pslspec // and I think cannot be implemented in JTLV
*/		| compute //not implemented in JTLV
		| constants //not implemented in JTLV
		| predicate // and I think cannot be implemented in JTLV
		| mirror // and I think cannot be implemented in JTLV
		;

// LXY: The agent module definition for Multi-agent systems 
// curr_agent_sign_vector: the first element is the name of the agent module;
//			   the following elements are the names of parameters
// curr_agent_sign_visible_vector: saving the visibility of each parameter

mas_agent	: { curr_module_sign_vector.clear(); }
		TOK_AGENT agent_sign
			{ //curr_module_name = curr_agent_sign_vector.elementAt(0); 
		  	smv_initiator.add_module(curr_module_sign_vector, input); }
		agent_declarations
			-> ^(TOK_AGENT agent_sign agent_declarations)
		;

agent_sign	: TOK_ATOM
			{ curr_module_sign_vector.add($TOK_ATOM.text); //the agent name
			  curr_module_sign_vector.add("1"); //"1" -- is Agent; "0" -- is normal module
			}
			-> ^(AGENT_SIGN_T TOK_ATOM)
		| TOK_ATOM TOK_LP TOK_RP
			{ curr_module_sign_vector.add($TOK_ATOM.text); //the agent name
			  curr_module_sign_vector.add("1"); //"1" -- is Agent; "0" -- is normal module
			}
			-> ^(AGENT_SIGN_T TOK_ATOM)
		| TOK_ATOM 
			{ curr_module_sign_vector.add($TOK_ATOM.text); //the agent name
			  curr_module_sign_vector.add("1"); //"1" -- is Agent; "0" -- is normal module
			}
		  TOK_LP agent_sign_atom_list TOK_RP
			-> ^(AGENT_SIGN_T TOK_ATOM ^(AGENT_REF_LIST_T agent_sign_atom_list))
		;

// The body of a module 
agent_declarations	: (agent_declaration)*;

agent_sign_atom_list 	:  agent_sign_atom 
			(TOK_COMMA! agent_sign_atom)* ;

//agent_sign_atom_list 	:  f=TOK_ATOM { curr_agent_sign_vector.add($f.text); }
//		(TOK_COMMA! r=TOK_ATOM { curr_agent_sign_vector.add($r.text); })*
//		;		
agent_sign_atom		: f=TOK_ATOM 
			{ curr_module_sign_vector.add($f.text); //the argument name
			  curr_module_sign_vector.add("0"); //"1" -- visible; "0" -- invisible
			}
//			-> ^(AGENT_SIGN_ATOM_T TOK_ATOM)
			| TOK_VISIBLE^ r=TOK_ATOM 
			{ curr_module_sign_vector.add($r.text); //the argument name
			  curr_module_sign_vector.add("1"); //"1" -- visible; "0" -- invisible
			}
//			-> ^(AGENT_SIGN_ATOM_T TOK_VISIBLE TOK_ATOM)
			;

agent_declaration		: 
		  var	// the variables declared here are visible by default
//		  -> ^(AGENT_DECL_VAR_T var)
		| agent_invisible_var	// the variables declared here are invisible
//		  -> ^(AGENT_DECL_INVISIBLE_VAR_T agent_invisible_var)
		| input_var
		| avar_decl
		| assign 
		| init
		| trans
		| define
		| justice
		| compassion
		;

agent_invisible_var		: TOK_INVISIBLE_VAR
				| TOK_INVISIBLE_VAR^ var_decl_list
				;

// avar_decl is for the declaration of action variable
avar_decl :  TOK_AVAR TOK_COLON avar_type TOK_SEMI 
			-> ^( AGENT_DECL_AVAR_T avar_type ) 
		;

avar_type : 	subrange 
		| TOK_LCB type_value_list TOK_RCB 
			-> ^( TYPE_VALUE_LIST_T type_value_list ) 
		;

// LXY: End of above definitions 


/* a game definition consists of GAME keyword, definitions of its two
   players and a list of usual and game specifications.
   NOTE: this rule returns value not by usual way 
   but through variables mbp_parser_spec_list,
   mbp_parser_player_1 and mbp_parser_player_2.
*/
game_definition	: TOK_GAME game_body
		-> ^(TOK_GAME game_body)
		;
game_body		: game_body_element (game_body)?;
/* a GAME definition consists of both players definitions,
   usual specifications and game specifications.
*/
game_body_element	: TOK_PLAYER_1 player_body
		-> ^(TOK_PLAYER_1 player_body)
		| TOK_PLAYER_2 player_body
		-> ^(TOK_PLAYER_2 player_body)
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
		| TOK_VAR^ var_decl_list
		;
/* opositive direction chosen for some reason */
var_decl_list		: var_decl (var_decl)* 
		;
var_decl :  decl_var_id TOK_COLON type TOK_SEMI 
		-> ^( VAR_DECL_T decl_var_id type ) 
	    | ( TOK_FOR TOK_LP i_f= TOK_ATOM TOK_EQUAL init_f= simple_expression TOK_SEMI cond_f= simple_expression TOK_SEMI ii= TOK_ATOM TOK_EQUAL inc_f= simple_expression TOK_RP TOK_LCB loop_main= var_decl_list TOK_RCB ) 
	    	-> ^( TOK_FOR TOK_LP $i_f NOP $init_f NOP $cond_f NOP $inc_f NOP TOK_RP TOK_LCB $loop_main TOK_RCB ) ;

input_var		: TOK_IVAR
		| TOK_IVAR^ ivar_decl_list
		;
/* opositive direction chosen for some reason */
ivar_decl_list		: ivar_decl (ivar_decl)*
		;
//update in 1.5
ivar_decl :  decl_var_id TOK_COLON type TOK_SEMI 
			-> ^( IVAR_DECL_T decl_var_id type ) 
		| ( TOK_FOR TOK_LP i_f= TOK_ATOM TOK_EQUAL init_f= simple_expression TOK_SEMI cond_f= simple_expression TOK_SEMI ii= TOK_ATOM TOK_EQUAL inc_f= simple_expression TOK_RP TOK_LCB loop_main= ivar_decl_list TOK_RCB ) 
			-> ^( TOK_FOR TOK_LP $i_f NOP $init_f NOP $cond_f NOP $inc_f NOP TOK_RP TOK_LCB $loop_main TOK_RCB ) 
		;
		

/* Definitions */
define		: TOK_DEFINE^ define_list
		;
define_list		: define_decl ( define_decl )* 
		;
define_decl : decl_var_id TOK_EQDEF simple_expression TOK_SEMI -> ^( DEFINE_DECL_T decl_var_id simple_expression ) | ( TOK_FOR TOK_LP i_f= TOK_ATOM TOK_EQUAL init_f= simple_expression TOK_SEMI cond_f= simple_expression TOK_SEMI ii= TOK_ATOM TOK_EQUAL inc_f= simple_expression TOK_RP TOK_LCB loop_main= define_list TOK_RCB ) -> ^( TOK_FOR TOK_LP $i_f NOP $init_f NOP $cond_f NOP $inc_f NOP TOK_RP TOK_LCB $loop_main TOK_RCB ) ;

/* Assignments of initial, current or next value of variables */
assign		: TOK_ASSIGN^ assign_list
		;
assign_list		: one_assign ( one_assign )*
		;

one_assign	:
		var_id TOK_EQDEF simple_expression TOK_SEMI
		-> ^( ASSIGN_DECL_T var_id simple_expression )
		| TOK_SMALLINIT TOK_LP var_id TOK_RP TOK_EQDEF simple_expression TOK_SEMI 
		-> ^( INIT_ASSIGN_DECL_T var_id simple_expression )
		| TOK_NEXT TOK_LP var_id TOK_RP TOK_EQDEF next_expression TOK_SEMI 
		-> ^( NEXT_ASSIGN_DECL_T var_id next_expression ) 
		| ( TOK_FOR TOK_LP i_f= TOK_ATOM TOK_EQUAL init_f= simple_expression TOK_SEMI cond_f= simple_expression TOK_SEMI ii= TOK_ATOM TOK_EQUAL inc_f= simple_expression TOK_RP TOK_LCB loop_main= assign_list TOK_RCB ) 
		-> ^( TOK_FOR TOK_LP $i_f NOP $init_f NOP $cond_f NOP $inc_f NOP TOK_RP TOK_LCB $loop_main TOK_RCB )
		;

/* Direct finite state machine definition (init, invar, trans) */
init		: TOK_INIT simple_expression optsemi
		-> ^(TOK_INIT simple_expression)
		;
/* not implemented in JTLV */

invar		: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement INVAR", input); }
		TOK_INVAR^ simple_expression! optsemi!
		;
/* -------------------- */

trans		: TOK_TRANS next_expression optsemi
		-> ^(TOK_TRANS next_expression)
		;

/* Fairness declarations */
/* not implemented in JTLV */
fairness		: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement FAIRNESS", input); }
		TOK_FAIRNESS^ simple_expression! optsemi!
		;
/* -------------------- */

justice		: TOK_JUSTICE^ justice_list
		;
justice_list	: justice_decl ( justice_decl )* 
		;
justice_decl	: simple_expression TOK_SEMI
		-> ^(JUSTICE_DECL_T simple_expression)
		| ( TOK_FOR TOK_LP i_f= TOK_ATOM TOK_EQUAL init_f= simple_expression TOK_SEMI cond_f= simple_expression TOK_SEMI ii= TOK_ATOM TOK_EQUAL inc_f= simple_expression TOK_RP TOK_LCB loop_main= justice_list TOK_RCB ) -> ^( TOK_FOR TOK_LP $i_f NOP $init_f NOP $cond_f NOP $inc_f NOP TOK_RP TOK_LCB $loop_main TOK_RCB ) 
		;
compassion	: TOK_COMPASSION^ compassion_list
		;
compassion_list	: compassion_decl ( compassion_decl )* 
		;
compassion_decl	: TOK_LP f=simple_expression TOK_COMMA s=simple_expression TOK_RP TOK_SEMI
		-> ^(COMPASSION_DECL_T $f TOK_COMMA $s)
		| ( TOK_FOR TOK_LP i_f= TOK_ATOM TOK_EQUAL init_f= simple_expression TOK_SEMI cond_f= simple_expression TOK_SEMI ii= TOK_ATOM TOK_EQUAL inc_f= simple_expression TOK_RP TOK_LCB loop_main= compassion_list TOK_RCB ) -> ^( TOK_FOR TOK_LP $i_f NOP $init_f NOP $cond_f NOP $inc_f NOP TOK_RP TOK_LCB $loop_main TOK_RCB ) 
		;
/* not implemented in JTLV */
invarspec		: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement INVAR", input); }
		TOK_INVARSPEC^ simple_expression! optsemi!
		;
/* -------------------- */

/* Specifications and computation of min and max distance */
/* not implemented in JTLV */
/*
ctlspec		:  { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement SPEC", input); }
		TOK_SPEC^ ctl_expression! optsemi!
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement CTLSPEC", input); }
		TOK_CTLSPEC^ ctl_expression! optsemi!
		;
ltlspec		: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement LTLSPEC", input); }
		TOK_LTLSPEC^ ltl_expression! optsemi!
		;
pslspec		: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement PSLSPEC", input); }
		TOK_PSLSPEC^ psl_expression! optsemi!
		;
*/
constants		: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement CONSTANTS", input); }
		TOK_CONSTANTS^ constants_expression! TOK_SEMI!
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
predicate		: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement PRED", input); }
		TOK_PRED^ simple_expression! optsemi!
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement PRED[..]", input); }
		TOK_PRED^ TOK_LB! TOK_NUMBER! TOK_RB! simple_expression! optsemi! 
		;
mirror		: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement MIRROR", input); }
		TOK_MIRROR^ decl_var_id! optsemi!
		;
/* -------------------- */
reachtarget		: TOK_REACHTARGET player_num simple_expression optsemi
		-> ^(TOK_REACHTARGET player_num simple_expression)
		;
avoidtarget		: TOK_AVOIDTARGET player_num simple_expression optsemi
		-> ^(TOK_AVOIDTARGET player_num simple_expression)
		;
reachdeadlock	: TOK_REACHDEADLOCK player_num optsemi
		-> ^(TOK_REACHDEADLOCK player_num)
		;
avoiddeadlock	: TOK_AVOIDDEADLOCK player_num optsemi
		-> ^(TOK_AVOIDDEADLOCK player_num)
		;
buchigame		: TOK_BUCHIGAME player_num TOK_LP simple_list_expression TOK_RP optsemi
		-> ^(TOK_BUCHIGAME player_num simple_list_expression)
		;
genreactivity		: TOK_GENREACTIVITY player_num TOK_LP simple_list_expression TOK_RP TOK_IMPLIES TOK_LP simple_list_expression TOK_RP optsemi
		-> ^(TOK_GENREACTIVITY player_num simple_list_expression TOK_IMPLIES simple_list_expression)
		;

/* not implemented in JTLV */
compute		: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement COMPUTE", input); }
		TOK_COMPUTE^ compute_expression! optsemi!
		;
compute_expression	: TOK_MMIN TOK_LB ctl_expression TOK_COMMA ctl_expression TOK_RB
		| TOK_MMAX TOK_LB ctl_expression TOK_COMMA ctl_expression TOK_RB
		;
/* -------------------- */

/* Module macro-expansion */
/* not implemented in JTLV */
isa		: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV statement ISA", input); }
		TOK_ISA^ TOK_ATOM!
		;
/* -------------------- */

/* parse an optional semicolon */
optsemi		: 
		| TOK_SEMI;


/* Variable identifiers.
   decl_var_id is used for declarations; self not allowed.
   var_id is used to reference variables in assignment, includes self.
 */
decl_var_id		: decl_var_id_helper
		-> ^(REF_T decl_var_id_helper)
		;
decl_var_id_helper	: TOK_ATOM
			( TOK_DOT! TOK_ATOM
			| TOK_DOT! TOK_NUMBER /// ????? not clear....
			| TOK_LB simple_expression TOK_RB )*///update for 1.5
		;
var_id		: var_id_helper
		-> ^(REF_T var_id_helper)
		;
var_id_helper		: (TOK_ATOM | TOK_SELF)
			( TOK_DOT! TOK_ATOM
			| TOK_DOT! TOK_NUMBER
			| TOK_LB simple_expression TOK_RB)*
		;


/* ------------------------------------------------------------------------ */
/* ----------------------------  COMMANDS  -------------------------------- */
/* ------------------------ NOT IMPLEMENTED IN JTLV ------------------------ */
/* ------------------------------------------------------------------------ */
command		: command_case
////???		| error TOK_SEMI {return(1);}
////???		| error {return(1);}
		;
command_case	: { if (true) throw new SMVUnimplementedException("Unimplemented JTLV command GOTO", input); }
		TOK_GOTO^ state! TOK_SEMI!
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV command INIT", input); }
		TOK_INIT^ simple_expression! TOK_SEMI!
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV command FAIRNESS", input); }
		TOK_FAIRNESS^ simple_expression! TOK_SEMI!
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV command TRANS", input); }
		TOK_TRANS^ next_expression! TOK_SEMI!
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV command CONSTRAINT", input); }
		TOK_CONSTRAINT^ simple_expression! TOK_SEMI!
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV command SIMPWFF", input); }
		TOK_SIMPWFF^ simple_expression! (TOK_INCONTEXT! context!)? TOK_SEMI!
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV command CTLWFF", input); }
		TOK_CTLWFF^ ctl_expression! (TOK_INCONTEXT! context!)? TOK_SEMI!
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV command LTLWFF", input); }
		TOK_LTLWFF^ ltl_expression! (TOK_INCONTEXT! context!)? TOK_SEMI!
		| { if (true) throw new SMVUnimplementedException("Unimplemented JTLV command COMPWFF", input); }
		TOK_COMPWFF^ compute_expression! (TOK_INCONTEXT! context!)? TOK_SEMI!
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


/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// LEXER
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
//???//^"#"" "[0-9]+.*\n       sscanf(yytext,"# %d",&yylineno);
//???//^"#".*\n                rpterr("Unexpected preprocessor directive:\n %s\n",yytext);
//add in 1.5
TOK_FOR		: 'for';
TOK_MODULE 	: 'MODULE';//                {yylval.lineno = yylineno; return(TOK_MODULE);}
TOK_PROCESS	: 'process';//               {yylval.lineno = yylineno; return(TOK_PROCESS);}
TOK_DEFINE		: 'DEFINE';//                {yylval.lineno = yylineno; return(TOK_DEFINE);}
TOK_VAR		: 'VAR';//                   {yylval.lineno = yylineno; return(TOK_VAR);}
TOK_IVAR		: 'IVAR';//                  {yylval.lineno = yylineno; return(TOK_IVAR);}
TOK_INIT		: 'INIT';//                  {yylval.lineno = yylineno; return(TOK_INIT);}
TOK_TRANS		: 'TRANS';//                 {yylval.lineno = yylineno; return(TOK_TRANS);}
TOK_INVAR		: 'INVAR';//                 {yylval.lineno = yylineno; return(TOK_INVAR);}
TOK_SPEC		: 'SPEC';//                  {yylval.lineno = yylineno; return(TOK_SPEC);}
TOK_CTLSPEC		: 'CTLSPEC';//               {yylval.lineno = yylineno; return(TOK_CTLSPEC);}
TOK_LTLSPEC		: 'LTLSPEC';//               {yylval.lineno = yylineno; return(TOK_LTLSPEC);}
TOK_PSLSPEC		: 'PSLSPEC';//               {yylval.lineno = yylineno; Parser_switch_to_psl(); return(TOK_PSLSPEC);}
TOK_COMPUTE	: 'COMPUTE';//               {yylval.lineno = yylineno; return(TOK_COMPUTE);}
TOK_INVARSPEC	: 'INVARSPEC';//             {yylval.lineno = yylineno; return(TOK_INVARSPEC);}
TOK_CONSTRAINT	: 'CONSTRAINT';//            {yylval.lineno = yylineno; return(TOK_CONSTRAINT);}
TOK_CONSTANTS	: 'CONSTANTS';//             {yylval.lineno = yylineno; return(TOK_CONSTANTS);}
TOK_SIMPWFF		: 'SIMPWFF';//               {yylval.lineno = yylineno; return(TOK_SIMPWFF);}
TOK_CTLWFF		: 'CTLWFF';//                {yylval.lineno = yylineno; return(TOK_CTLWFF);}
TOK_LTLWFF		: 'LTLWFF';//               {yylval.lineno = yylineno; return(TOK_LTLWFF);}
TOK_COMPWFF	: 'COMPWFF';//               {yylval.lineno = yylineno; return(TOK_COMPWFF);}
TOK_INCONTEXT	: 'IN';//                    {yylval.lineno = yylineno; return(TOK_INCONTEXT);}
TOK_FAIRNESS	: 'FAIRNESS';//              {yylval.lineno = yylineno; return(TOK_FAIRNESS);}
TOK_JUSTICE		: 'JUSTICE';//               {yylval.lineno = yylineno; return(TOK_JUSTICE);}
TOK_COMPASSION	: 'COMPASSION';//            {yylval.lineno = yylineno; return(TOK_COMPASSION);}
TOK_ISA		: 'ISA';//                   {yylval.lineno = yylineno; return(TOK_ISA);}
TOK_ASSIGN		: 'ASSIGN';//                {yylval.lineno = yylineno; return(TOK_ASSIGN);}
TOK_GOTO		: 'GOTO';//                  {yylval.lineno = yylineno; return(TOK_GOTO);}

// LXY: Tokens for MAS
TOK_AGENT 	: 'AGENT';//                {yylval.lineno = yylineno; return(TOK_AGENT);} //LXY added
TOK_VISIBLE 	: 'VISIBLE';//                {yylval.lineno = yylineno; return(TOK_AGENT);} //LXY added
TOK_INVISIBLE 	: 'INVISIBLE';//                {yylval.lineno = yylineno; return(TOK_AGENT);} //LXY added
TOK_INVISIBLE_VAR	: 'INVISIBLEVAR';
TOK_AVAR		: 'ACT';//                  {yylval.lineno = yylineno; return(TOK_AVAR);}

TOK_GAME		: 'GAME';//                  {yylval.lineno = yylineno; return(TOK_GAME);}
TOK_PLAYER_1	: 'PLAYER_1';//              {yylval.lineno = yylineno; return(TOK_PLAYER_1);}
TOK_PLAYER_2	: 'PLAYER_2';//              {yylval.lineno = yylineno; return(TOK_PLAYER_2);}
TOK_REACHTARGET	: 'REACHTARGET';//           {yylval.lineno = yylineno; return(TOK_REACHTARGET);}
TOK_AVOIDTARGET	: 'AVOIDTARGET';//           {yylval.lineno = yylineno; return(TOK_AVOIDTARGET);}
TOK_REACHDEADLOCK	: 'REACHDEADLOCK';//         {yylval.lineno = yylineno; return(TOK_REACHDEADLOCK);}
TOK_AVOIDDEADLOCK	: 'AVOIDDEADLOCK';//         {yylval.lineno = yylineno; return(TOK_AVOIDDEADLOCK);}
TOK_BUCHIGAME	: 'BUCHIGAME';//             {yylval.lineno = yylineno; return(TOK_BUCHIGAME);}
TOK_GENREACTIVITY	: 'GENREACTIVITY';//         {yylval.lineno = yylineno; return(TOK_GENREACTIVITY);}

TOK_ARRAY		: 'array';//                 {yylval.lineno = yylineno; return(TOK_ARRAY);}
TOK_OF		: 'of';//                    {yylval.lineno = yylineno; return(TOK_OF);}
TOK_BOOLEAN	: 'boolean';//               {yylval.lineno = yylineno; return(TOK_BOOLEAN);}
TOK_INTEGER		: 'integer' | 'Integer';//               {yylval.lineno = yylineno; return(TOK_INTEGER);}
TOK_REAL		: 'real' | 'Real';//                  {yylval.lineno = yylineno; return(TOK_REAL);}
TOK_WORD1		: 'word1';//                 {yylval.lineno = yylineno; return(TOK_WORD1);}
TOK_WORD		: 'word' | 'Word';//                  {yylval.lineno = yylineno; return(TOK_WORD);}
TOK_BOOL		: 'bool';//                  {yylval.lineno = yylineno; return(TOK_BOOL);}

TOK_WAREAD		: 'READ';//                  {yylval.lineno = yylineno; return(TOK_WAREAD);}
TOK_WAWRITE	: 'WRITE';//                 {yylval.lineno = yylineno; return(TOK_WAWRITE);}

TOK_EX		: 'EX';//                    {yylval.lineno = yylineno; return(TOK_EX);}
TOK_AX		: 'AX';//                    {yylval.lineno = yylineno; return(TOK_AX);}
TOK_EF		: 'EF';//                    {yylval.lineno = yylineno; return(TOK_EF);}
TOK_AF		: 'AF';//                    {yylval.lineno = yylineno; return(TOK_AF);}
TOK_EG		: 'EG';//                    {yylval.lineno = yylineno; return(TOK_EG);}
TOK_AG		: 'AG';//                    {yylval.lineno = yylineno; return(TOK_AG);}
TOK_EE		: 'E';//                     {yylval.lineno = yylineno; return(TOK_EE);}
TOK_OP_FUTURE	: 'F';//                     {yylval.lineno = yylineno; return(TOK_OP_FUTURE);}
TOK_OP_ONCE	: 'O';//                     {yylval.lineno = yylineno; return(TOK_OP_ONCE);}
TOK_OP_GLOBAL	: 'G';//                     {yylval.lineno = yylineno; return(TOK_OP_GLOBAL);}
TOK_OP_HISTORICAL	: 'H';//                     {yylval.lineno = yylineno; return(TOK_OP_HISTORICAL);}
TOK_OP_NEXT	: 'X';//                     {yylval.lineno = yylineno; return(TOK_OP_NEXT);}
TOK_OP_PREC		: 'Y';//                     {yylval.lineno = yylineno; return(TOK_OP_PREC);}
TOK_OP_NOTPRECNOT	: 'Z';//                     {yylval.lineno = yylineno; return(TOK_OP_NOTPRECNOT);}
TOK_AA		: 'A';//                     {yylval.lineno = yylineno; return(TOK_AA);}
TOK_UNTIL		: 'U';//                     {yylval.lineno = yylineno; return(TOK_UNTIL);}
TOK_SINCE		: 'S';//                     {yylval.lineno = yylineno; return(TOK_SINCE);}
TOK_RELEASES	: 'V';//                     {yylval.lineno = yylineno; return(TOK_RELEASES);}
TOK_TRIGGERED	: 'T';//                     {yylval.lineno = yylineno; return(TOK_TRIGGERED);}
TOK_BUNTIL		: 'BU';//                    {yylval.lineno = yylineno; return(TOK_BUNTIL);}
TOK_EBF		: 'EBF';//                   {yylval.lineno = yylineno; return(TOK_EBF);}
TOK_ABF		: 'ABF';//                   {yylval.lineno = yylineno; return(TOK_ABF);}
TOK_EBG		: 'EBG';//                   {yylval.lineno = yylineno; return(TOK_EBG);}
TOK_ABG		: 'ABG';//                   {yylval.lineno = yylineno; return(TOK_ABG);}
TOK_MMIN		: 'MIN';//                   {yylval.lineno = yylineno; return(TOK_MMIN);}
TOK_MMAX		: 'MAX';//                   {yylval.lineno = yylineno; return(TOK_MMAX);}
TOK_LP		: '(';//                     {yylval.lineno = yylineno; return(TOK_LP);}
TOK_RP		: ')';//                     {yylval.lineno = yylineno; return(TOK_RP);}
TOK_LB		: '[';//                     {yylval.lineno = yylineno; return(TOK_LB);}
TOK_RB		: ']';//                     {yylval.lineno = yylineno; return(TOK_RB);}
TOK_LCB		: '{';//                     {yylval.lineno = yylineno; return(TOK_LCB);}
TOK_RCB		: '}';//                     {yylval.lineno = yylineno; return(TOK_RCB);}
TOK_FALSEEXP	: 'FALSE';//                 {
		//		yylval.node = new_node(FALSEEXP,Nil,Nil);
		//		return(TOK_FALSEEXP);
		//	}
TOK_TRUEEXP		: 'TRUE';//                  {
		//		yylval.node = new_node(TRUEEXP,Nil,Nil);
		//		return(TOK_TRUEEXP);
		//	}
TOK_CASE		: 'case';//                  {yylval.lineno = yylineno; return(TOK_CASE);}
TOK_ESAC		: 'esac';//                  {yylval.lineno = yylineno; return(TOK_ESAC);}
TOK_EQDEF		: ':=';//                    {yylval.lineno = yylineno; return(TOK_EQDEF);}
TOK_PLUS		: '+';//                     {yylval.lineno = yylineno; return(TOK_PLUS);}
TOK_MINUS		: '-';//                     {yylval.lineno = yylineno; return(TOK_MINUS);}
TOK_TIMES		: '*';//                     {yylval.lineno = yylineno; return(TOK_TIMES);}
TOK_DIVIDE		: '/';//                     {yylval.lineno = yylineno; return(TOK_DIVIDE);}
TOK_MOD		: 'mod';//                   {yylval.lineno = yylineno; return(TOK_MOD);}
TOK_LSHIFT		: '<<';//                    {yylval.lineno = yylineno; return(TOK_LSHIFT);}
TOK_RSHIFT		: '>>';//                    {yylval.lineno = yylineno; return(TOK_RSHIFT);}
TOK_LROTATE	: '<<<';//                   {yylval.lineno = yylineno; return(TOK_LROTATE);}
TOK_RROTATE	: '>>>';//                   {yylval.lineno = yylineno; return(TOK_RROTATE);}
TOK_EQUAL		: '=';//                     {yylval.lineno = yylineno; return(TOK_EQUAL);}
TOK_NOTEQUAL	: '!=';//                    {yylval.lineno = yylineno; return(TOK_NOTEQUAL);}
TOK_LE		: '<=';//                    {yylval.lineno = yylineno; return(TOK_LE);}
TOK_GE		: '>=';//                    {yylval.lineno = yylineno; return(TOK_GE);}
TOK_LT		: '<';//                     {yylval.lineno = yylineno; return(TOK_LT);}
TOK_GT		: '>';//                     {yylval.lineno = yylineno; return(TOK_GT);}
TOK_NEXT		: 'next';//                  {yylval.lineno = yylineno; return(TOK_NEXT);}
TOK_SMALLINIT	: 'init';//                  {yylval.lineno = yylineno; return(TOK_SMALLINIT);}
TOK_SELF		: 'self';//                  {yylval.lineno = yylineno; return(TOK_SELF);}
TOK_UNION		: 'union';//                 {yylval.lineno = yylineno; return(TOK_UNION);}
TOK_SETIN		: 'in';//                    {yylval.lineno = yylineno; return(TOK_SETIN);}
TOK_TWODOTS	: '..';//                    {yylval.lineno = yylineno; return(TOK_TWODOTS);}
TOK_DOT		: '.';//                     {yylval.lineno = yylineno; return(TOK_DOT);}
TOK_IMPLIES		: '->';//                    {yylval.lineno = yylineno; return(TOK_IMPLIES);}
TOK_IFF		: '<->';//                   {yylval.lineno = yylineno; return(TOK_IFF);}
TOK_OR		: '|';//                     {yylval.lineno = yylineno; return(TOK_OR);}
TOK_AND		: '&';//                     {yylval.lineno = yylineno; return(TOK_AND);}
TOK_XOR		: 'xor';//                   {yylval.lineno = yylineno; return(TOK_XOR);}
TOK_XNOR		: 'xnor';//                  {yylval.lineno = yylineno; return(TOK_XNOR);}
TOK_NOT		: '!';//                     {yylval.lineno = yylineno; return(TOK_NOT);}

TOK_COMMA		: ',';//                     {yylval.lineno = yylineno; return(TOK_COMMA);}
TOK_COLON		: ':';//                     {yylval.lineno = yylineno; return(TOK_COLON);}
TOK_SEMI		: ';';//                     {yylval.lineno = yylineno; return(TOK_SEMI);}
TOK_CONCATENATION	: '::';//                    {yylval.lineno = yylineno; return(TOK_CONCATENATION);}
TOK_PRED		: 'PRED';//                  {yylval.lineno = yylineno; return(TOK_PRED);}
TOK_PREDSLIST	: 'PREDICATES';//            {yylval.lineno = yylineno; return(TOK_PREDSLIST);}
TOK_MIRROR		: 'MIRROR';//                {yylval.lineno = yylineno; return(TOK_MIRROR);}

 /* word constants */
TOK_NUMBER_WORD	: '0' ('b' | 'B' | 'o' | 'O' | 'd' | 'D' | 'h' | 'H') ('0'..'9')* '_' ('0'..'9' | 'a'..'f' | 'A'..'F') ('0'..'9' | 'a'..'f' | 'A'..'F' | '_')*;

 /* real, fractional and exponential constants */
TOK_NUMBER_FRAC	: ('f' | 'F') '\'' ('0'..'9')+ '/' ('0'..'9')+;
/* not implemented in JTLV, and make problems in the parsing, conflict with '0..8'
TOK_NUMBER_EXP	: ('0'..'9')+ ('.' ('0'..'9')+)? ('e' | 'E') ('+' | '-') ('0'..'9')+;
TOK_NUMBER_REAL	: ('0'..'9')+ '.' ('0'..'9')+;
*/
 /* integer number */
TOK_NUMBER		: ('0'..'9')+;

/* identifier */
TOK_ATOM		: ('a'..'z' | 'A'..'Z' | '_') ('a'..'z' | 'A'..'Z' | '0'..'9' | '_' | '\\' | '$' | '#' | '-')*;


// basic JTLV extension
JTOK_WS 		:   (   ' '
		|   '\t'
		|   '\r'
		|   '\n'
		)+
		{ $channel=HIDDEN; };
JTOK_MULTI_COMMENT	: ('/*' (
		options { greedy=false;}
		:  // '\r' '\n' |
		'\r'
		|   '\n'
		|   ~('\n'|'\r')
		)*
		'*/'
		{$channel=HIDDEN;});
JTOK_LINE_COMMENT	: ('--' (~('\n'|'\r'))* (('\n'|'\r'('\n')?))? {$channel=HIDDEN;})
		|
		('//' (~('\n'|'\r'))* (('\n'|'\r'('\n')?))? {$channel=HIDDEN;});

