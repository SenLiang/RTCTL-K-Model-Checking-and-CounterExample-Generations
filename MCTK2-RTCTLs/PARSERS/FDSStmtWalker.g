tree grammar FDSStmtWalker;

options {
	tokenVocab=FDS;
	ASTLabelType=CommonTree;
	backtrack = true;
}

@header {
package edu.wis.jtlv.env.core.fds;

import java.util.HashMap;
import net.sf.javabdd.BDD;
import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.module.ModuleBDDField;
import edu.wis.jtlv.env.module.FDSModule;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.module.ModuleVariableException;
}

@members {
public static FDSModule curr_module = null;
}

/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// PARSER
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////

// main program.
program	: ^(PROG_T module+);

module	: ^(MODULE_T m_name=ID {curr_module = ((FDSModule) Env.getModule($m_name.text));} section*);

// all sections....
section	: (var_sec | init_sec | trans_sec | just_sec);
var_sec	: ^(VAR_T var_decl*);
init_sec	: ^(INIT_T init_decl*);
trans_sec	: ^(TRANS_T trans_decl*);
just_sec	: ^(JUST_T just_decl*);

// all declarations....
// every type of declaration is respossible on a different type of operation.
var_decl	: ^(VAR_DECL_T ID type);
init_decl	: /* init_decl_assi | */ init_decl_expr;
//init_decl_assi	: ^(INIT_DECL_ASSI_T a_name=ID value)
//	{curr_modulel.addInitial($value.ret);};
//	catch [ModuleException e] {e.printStackTrace();}
//	catch [RecognitionException re] {reportError(re); recover(input,re);}
init_decl_expr	: ^(INIT_DECL_EXPR_T bool_expr)
	{curr_module.addInitial($bool_expr.ret);};
	catch [ModuleException e] {e.printStackTrace();}
	catch [RecognitionException re] {reportError(re); recover(input,re);}
trans_decl	: ^(TRANS_DECL_T bool_expr)
	{curr_module.conjunctTrans($bool_expr.ret);};
	catch [ModuleException e] {e.printStackTrace();}
	catch [RecognitionException re] {reportError(re); recover(input,re);}
just_decl	: ^(JUST_DECL_T bool_expr)
	{curr_module.addJustice($bool_expr.ret);};
	catch [ModuleException e] {e.printStackTrace();}
	catch [RecognitionException re] {reportError(re); recover(input,re);}

// all expresions....
bool_expr returns[BDD ret]
	: binor_expr {$ret = $binor_expr.ret;};
binor_expr returns[BDD ret]
	: ^(BINOR first=binor_expr second=binor_expr) {$ret = $first.ret.or($second.ret);}
	| and_expr {$ret = $and_expr.ret;};
and_expr returns[BDD ret]
	: ^(AND first=and_expr second=and_expr) {$ret = $first.ret.and($second.ret);}
	| implies_expr {$ret = $implies_expr.ret;};
implies_expr returns[BDD ret]
	: ^(IMPLIES first=implies_expr second=implies_expr) {$ret = $first.ret.imp($second.ret);}
	| biimp_expr {$ret = $biimp_expr.ret;};
biimp_expr returns[BDD ret]
	: ^(BIIMP first=biimp_expr second=biimp_expr) {$ret = $first.ret.biimp($second.ret);}
	| eq_expr {$ret = $eq_expr.ret;};
eq_expr returns[BDD ret]
	: ^(EQ first=eq_expr second=eq_expr) {$ret = $first.ret.biimp($second.ret);}
	| neq_expr {$ret = $neq_expr.ret;};
neq_expr returns[BDD ret]
	: ^(NEQ first=neq_expr second=neq_expr) {$ret = $first.ret.biimp($second.ret).not();}
	| not_expr {$ret = $not_expr.ret;};
not_expr returns[BDD ret]
	: ^(NOT first=not_expr) {$ret = $first.ret.not();}
	| block_expr {$ret = $block_expr.ret;};
block_expr returns[BDD ret]
	: ^(BLOCK_T bool_expr) {$ret = $bool_expr.ret;}
	| atom_expr {$ret = $atom_expr.ret;};
atom_expr returns[BDD ret]
	: any_ref {$ret = $any_ref.ret;}
	| value {$ret = $value.ret;};

value returns[BDD ret] 
	: TRUE {$ret = edu.wis.jtlv.env.Env.TRUE();}
	| FALSE {$ret = edu.wis.jtlv.env.Env.FALSE();};
	// | ...other values will be added later.

// references....
any_ref returns[BDD ret]
	: unprime_ref {$ret = $unprime_ref.ret;}
	| prime_ref {$ret = $prime_ref.ret;};
prime_ref returns[BDD ret]
	: ^(REF_T ^(PRIME_T native_ref)) {$ret = $native_ref.ret.prime().getDomain().ithVar(1);};
unprime_ref returns[BDD ret]
	: ^(REF_T native_ref) {$ret = $native_ref.ret.getDomain().ithVar(1);};
native_ref returns[ModuleBDDField ret]
	: construct_name {
if ($construct_name.ret.indexOf("-->>") == -1)
	$ret = Env.getVar(curr_module.getFullInstName(), $construct_name.ret);
else
	$ret = edu.wis.jtlv.env.Env.getVar($construct_name.ret.substring(0, $construct_name.ret.indexOf("-->>")), $construct_name.ret.substring($construct_name.ret.indexOf("-->>") + 4));
	if ($ret == null) 
		throw new FDSParseException("Could not find variable " + $construct_name.ret, input);
	//$ret = edu.wis.jtlv.env.Env.getVar($construct_name.ret.substring(0, $construct_name.ret.indexOf("-->>")), $construct_name.ret.substring($construct_name.ret.indexOf("-->>") + 4));
};
construct_name returns[String ret]
	: loc_ref {$ret = $loc_ref.ret;}
	| far_ref {$ret = $far_ref.ret;};
loc_ref returns[String ret]
	: ID {$ret = $ID.text;};
far_ref returns[String ret]
	: ^(DOT ID loc_ref) {$ret = $ID.text + "-->>" + $loc_ref.ret;} 
	| ^(DOT ID far_ref) {$ret = $ID.text + $far_ref.ret;};


// others....
type 	: BOOL_T; // | ...other types will be added later.


//// all expresions.... - BAD PERFORMANCE!!
//bool_expr returns[BDD ret]
//	: binor_expr {$ret = $binor_expr.ret;};
//binor_expr returns[BDD ret]
//	: ^(BINOR_T and_expr bool_expr) {$ret = $and_expr.ret.or($bool_expr.ret);}
//	| and_expr {$ret = $and_expr.ret;};
//and_expr returns[BDD ret]
//	: ^(AND_T implies_expr bool_expr) {$ret = $implies_expr.ret.and($bool_expr.ret);}
//	| implies_expr {$ret = $implies_expr.ret;};
//implies_expr returns[BDD ret]
//	: ^(IMPLIES_T biimp_expr bool_expr) {$ret = $biimp_expr.ret.imp($bool_expr.ret);}
//	| biimp_expr {$ret = $biimp_expr.ret;};
//biimp_expr returns[BDD ret]
//	: ^(BIIMP_T eq_expr bool_expr) {$ret = $eq_expr.ret.biimp($bool_expr.ret);}
//	| eq_expr {$ret = $eq_expr.ret;};
//eq_expr returns[BDD ret]
//	: ^(EQ_T neq_expr bool_expr) {$ret = $neq_expr.ret.biimp($bool_expr.ret);}
//	| neq_expr {$ret = $neq_expr.ret;};
//neq_expr returns[BDD ret]
//	: ^(NEQ_T not_expr bool_expr) {$ret = $not_expr.ret.biimp($bool_expr.ret).not();}
//	| not_expr {$ret = $not_expr.ret;};
//not_expr returns[BDD ret]
//	: ^(NOT_T block_expr) {$ret = $block_expr.ret.not();}
//	| block_expr {$ret = $block_expr.ret;};
//block_expr returns[BDD ret]
//	: ^(BLOCK_T bool_expr) {$ret = $bool_expr.ret;}
//	| atom_expr {$ret = $atom_expr.ret;};
//atom_expr returns[BDD ret]
//	: any_ref {$ret = $any_ref.ret;}
//	| value {$ret = $value.ret;};
//
//value returns[BDD ret] 
//	: TRUE {$ret = edu.wis.jtlv.env.Env.TRUE();}
//	| FALSE {$ret = edu.wis.jtlv.env.Env.FALSE();};
//	// | ...other values will be added later.
//
//// references....
//any_ref returns[BDD ret]
//	: unprime_ref {$ret = $unprime_ref.ret;}
//	| prime_ref {$ret = $prime_ref.ret;};
//prime_ref returns[BDD ret]
//	: ^(REF_T ^(PRIME_T native_ref)) {$ret = $native_ref.ret.prime().toBDD();};
//unprime_ref returns[BDD ret]
//	: ^(REF_T native_ref) {$ret = $native_ref.ret.toBDD();};
//native_ref returns[ModuleBDDField ret]
//	: loc_ref {$ret = $loc_ref.ret;}
//	| far_ref {$ret = $far_ref.ret;};
//loc_ref returns[ModuleBDDField ret]
//	: a_name=ID {$ret = curr_module.get_var($a_name.text);};
//far_ref returns[ModuleBDDField ret]
//	: ^(DOT_T a_module=ID a_name=ID) {$ret = edu.wis.jtlv.env.Env.get_var($a_module.text, $a_name.text);};
//
//// others....
//type 	: BOOL_T; // | ...other types will be added later.
