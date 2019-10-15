tree grammar FDSVarWalker;

options {
	tokenVocab=FDS;
	ASTLabelType=CommonTree;
	backtrack = true;
}

@header {
package edu.wis.jtlv.env.core.fds;

import java.util.HashMap;
import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.module.FDSModule;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.module.ModuleVariableException;
}

@members {
public static String curr_module_name = "global";
}

/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// Tree walker for initiating the variables
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////

// main program.
program	: ^(PROG_T module+);

module	: ^(MODULE_T m_name=ID {curr_module_name = $m_name.text; Env.putModule(curr_module_name, new FDSModule($m_name.text));} section*);

// all sections....
section	: (var_sec | init_sec | trans_sec | just_sec);
var_sec	: ^(VAR_T var_decl*);
init_sec	: ^(INIT_T init_decl*);
trans_sec	: ^(TRANS_T trans_decl*);
just_sec	: ^(JUST_T just_decl*);

// all declarations....
// every type of declaration is respossible on a different type of operation.
var_decl	: ^(VAR_DECL_T a_name=ID type)
	{ try {
		Env.getModule(curr_module_name).addVar($a_name.text);
	} catch (ModuleVariableException e) {
		throw new FDSParseException(e.getMessage(), input);
	} };
	//{Env.getModule(curr_module_name).addVar($a_name.text);};
	catch [ModuleException e] {e.printStackTrace();}
	catch [RecognitionException re] {reportError(re); recover(input,re);}
init_decl	: /* init_decl_assi | */ init_decl_expr;
// I'm converting it to explicit expr.
//init_decl_assi	: (INIT_DECL_ASSI_T ID value);
init_decl_expr	: ^(INIT_DECL_EXPR_T bool_expr);
trans_decl	: ^(TRANS_DECL_T bool_expr);
just_decl	: ^(JUST_DECL_T bool_expr);

// all expresions....
bool_expr	: binor_expr;
binor_expr	: ^(BINOR binor_expr binor_expr) | and_expr;
and_expr	: ^(AND and_expr and_expr) | implies_expr;
implies_expr	: ^(IMPLIES implies_expr implies_expr) | biimp_expr;
biimp_expr	: ^(BIIMP biimp_expr biimp_expr) | eq_expr;
eq_expr	: ^(EQ eq_expr eq_expr) | neq_expr;
neq_expr	: ^(NEQ neq_expr neq_expr) | not_expr;
not_expr	: ^(NOT block_expr) | block_expr;
block_expr	: ^(BLOCK_T bool_expr) | atom_expr;

atom_expr	: any_ref
	| value;

value	: TRUE | FALSE; // | ...other values will be added later.

// references....
any_ref	: unprime_ref | prime_ref;
prime_ref	: ^(REF_T ^(PRIME_T native_ref));
unprime_ref	: ^(REF_T native_ref);
native_ref	: loc_ref | far_ref;
loc_ref	: ID;
far_ref	: ^(DOT ID native_ref);

// others....
type	: BOOL_T; // | ...other types will be added later.

//// all expresions.... -BAD PERFORMANCE
//bool_expr	: binor_expr;
//binor_expr	: ^(BINOR_T and_expr bool_expr) | and_expr;
//and_expr	: ^(AND_T implies_expr bool_expr) | implies_expr;
//implies_expr	: ^(IMPLIES_T biimp_expr bool_expr) | biimp_expr;
//biimp_expr	: ^(BIIMP_T eq_expr bool_expr) | eq_expr;
//eq_expr	: ^(EQ_T neq_expr bool_expr) | neq_expr;
//neq_expr	: ^(NEQ_T not_expr bool_expr) | not_expr;
//not_expr	: ^(NOT_T block_expr) | block_expr;
//block_expr	: ^(BLOCK_T bool_expr) | atom_expr;
//
//atom_expr	: any_ref
//	| value;
//
//value	: TRUE | FALSE; // | ...other values will be added later.
//
//// references....
//any_ref	: unprime_ref | prime_ref;
//prime_ref	: ^(REF_T ^(PRIME_T native_ref));
//unprime_ref	: ^(REF_T native_ref);
//native_ref	: loc_ref | far_ref;
//loc_ref	: ID;
//far_ref	: ^(DOT_T ID ID);
//
//// others....
//type	: BOOL_T; // | ...other types will be added later.

