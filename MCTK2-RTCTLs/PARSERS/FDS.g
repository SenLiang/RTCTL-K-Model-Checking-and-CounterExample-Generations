grammar FDS;

options {
	output=AST;
	backtrack = true;
	//k=5;
}

tokens {
	PROG_T;
	MODULE_T;
	VAR_T;
	INIT_T;
	TRANS_T;
	JUST_T;
	VAR_DECL_T;
	//INIT_DECL_ASSI_T;
	INIT_DECL_EXPR_T;
	TRANS_DECL_T;
	JUST_DECL_T;
	//AND_T;
	//BINOR_T;
	//IMPLIES_T;
	//BIIMP_T;
	//EQ_T;
	//NEQ_T;
	//NOT_T;
	BLOCK_T;
	REF_T;
	PRIME_T;
	BOOL_T;
	//DOT_T;
} // an imaginary node

@header {
package edu.wis.jtlv.env.core.fds;

//import java.util.HashMap;
}
@lexer::header {
package edu.wis.jtlv.env.core.fds;
}

/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// FDS tree construction....
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////

// main program.
program	: (module)+ EOF
	-> ^(PROG_T module+);

module	: MODULE ID LEFT_CURLY (section)* RIGHT_CURLY 
	-> ^(MODULE_T ID section*);

// all sections....
section	: (var_expl_sec | var_sec | init_sec | trans_sec | just_sec);
var_sec	: (var_decl)+
	-> ^(VAR_T var_decl*);
var_expl_sec	: VAR LEFT_CURLY (var_decl)* RIGHT_CURLY
	-> ^(VAR_T var_decl*);
init_sec	: INIT LEFT_CURLY init_decl* RIGHT_CURLY
	-> ^(INIT_T init_decl*);
trans_sec	: TRANS LEFT_CURLY (trans_decl)* RIGHT_CURLY
	-> ^(TRANS_T trans_decl*);
just_sec	: JUST LEFT_CURLY (just_decl)* RIGHT_CURLY
	-> ^(JUST_T just_decl*);

// all declarations....
var_decl	: ID COLON type SEMICOLON
	-> ^(VAR_DECL_T ID type);
init_decl	: init_decl_assi | init_decl_expr;
// I'm converting it to explicit expr.
//init_decl_assi	: ID ASSIGN value SEMICOLON
//	-> ^(INIT_DECL_ASSI_T ID value);
init_decl_assi	: init_decl_t | init_decl_f;
init_decl_t	: ID ASSIGN TRUE SEMICOLON
	-> ^(INIT_DECL_EXPR_T ^(REF_T ID));
init_decl_f	: ID ASSIGN FALSE SEMICOLON
	-> ^(INIT_DECL_EXPR_T ^(NOT ^(REF_T ID)));
init_decl_expr	: bool_expr SEMICOLON
	-> ^(INIT_DECL_EXPR_T bool_expr);
trans_decl	: bool_expr SEMICOLON
	 -> ^(TRANS_DECL_T bool_expr);
just_decl	: bool_expr SEMICOLON
	 -> ^(JUST_DECL_T bool_expr);

// all exppesions....
bool_expr	: binor_expr; // the order is important!!!!!
binor_expr	: and_expr (BINOR^ and_expr)*;
and_expr	: implies_expr (AND^ implies_expr)*;
implies_expr	: biimp_expr (IMPLIES^ biimp_expr)*;
biimp_expr	: eq_expr (BIIMP^ eq_expr)*;
eq_expr	: neq_expr (EQ^ neq_expr)*;
neq_expr	: not_expr (NEQ^ not_expr)*;
not_expr	: block_expr | (NOT^ block_expr);
block_expr	: LEFT_BRACES bool_expr RIGHT_BRACES -> ^(BLOCK_T bool_expr)
	| atom_expr;
atom_expr	: any_ref
	| value;

value 	: TRUE | FALSE; // | ...other values will be added later.

// references....
any_ref	: unprime_ref | prime_ref_a | prime_ref_b;
prime_ref_a	: (native_ref PRIME)
	-> ^(REF_T ^(PRIME_T native_ref));
prime_ref_b	: (NEXT LEFT_BRACES native_ref RIGHT_BRACES)
	-> ^(REF_T ^(PRIME_T native_ref));
unprime_ref	: native_ref
	-> ^(REF_T native_ref);
native_ref	: ID (DOT^ ID)*;

// others....
type : BOOL_TYPE -> ^(BOOL_T); // | ...other types will be added later.

// all exppesions.... - BAD PERFORMANCE!!!
//bool_expr	: binor_expr; // the order is important!!!!!
//binor_expr	: and_expr BINOR bool_expr -> ^(BINOR_T and_expr bool_expr)
//	| and_expr; //-> do nothing;
//and_expr	: implies_expr AND bool_expr -> ^(AND_T implies_expr bool_expr)
//	| implies_expr; //-> do nothing
//implies_expr	: biimp_expr IMPLIES bool_expr -> ^(IMPLIES_T biimp_expr bool_expr)
//	| biimp_expr;
//biimp_expr	: eq_expr BIIMP bool_expr -> ^(BIIMP_T eq_expr bool_expr)
//	| eq_expr;
//eq_expr	: neq_expr EQ bool_expr -> ^(EQ_T neq_expr bool_expr)
//	| neq_expr;
//neq_expr	: not_expr NEQ bool_expr -> ^(NEQ_T not_expr bool_expr)
//	| not_expr;
//not_expr	: NOT block_expr -> ^(NOT_T block_expr)
//	| block_expr; //-> do nothing
//block_expr	: LEFT_BRACES bool_expr RIGHT_BRACES -> ^(BLOCK_T bool_expr)
//	| atom_expr;
//atom_expr	: any_ref
//	| value;
//
//value 	: TRUE | FALSE; // | ...other values will be added later.
//
//// references....
//any_ref	: unprime_ref | prime_ref_a | prime_ref_b;
//prime_ref_a	: (native_ref PRIME)
//	-> ^(REF_T ^(PRIME_T native_ref));
//prime_ref_b	: (NEXT LEFT_BRACES native_ref RIGHT_BRACES)
//	-> ^(REF_T ^(PRIME_T native_ref));
//unprime_ref	: native_ref
//	-> ^(REF_T native_ref);
//native_ref	: loc_ref | far_ref;
//loc_ref	: ID;
//far_ref	: ID DOT ID
//	-> ^(DOT_T ID ID);
//
//// others....
//type : BOOL_TYPE -> ^(BOOL_T); // | ...other types will be added later.
//
//// others....
//type : BOOL_TYPE -> ^(BOOL_T); // | ...other types will be added later.


/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// LEXER
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
// general 
MODULE		: 'MODULE' | 'Module' | 'PLAYER' | 'Player';
VAR		: 'VAR' | 'Var';
INIT		: 'INIT' | 'Init';
TRANS		: 'TRANS' | 'Trans';
JUST		: 'JUST' | 'Just' | 'JUSTICE' | 'Justice';
COMPASSION		: 'COMP' | 'Comp' | 'COMPASSION' | 'Compassion';
NEXT		: 'next';
PRIME		: '\'';
LEFT_BRACES		: '(' ;
LEFT_CURLY		: '{';
RIGHT_CURLY		: '}';
RIGHT_BRACES	: ')' ;
SEMICOLON		: ';' ;
COLON		: ':' ;
ASSIGN		: ':=';
DOT		: '.';

// boolean operators
AND		: '/\\' | '&' | 'and';
BINOR		: '\\/' | '|' | 'or';
IMPLIES		:  '->' ; 
BIIMP		: '<->';
EQ		: '==';
NEQ		: '!=';
NOT		: '!' ;

// types and values
BOOL_TYPE		: 'bool' | 'boolean';
TRUE		: 'T' | 'TRUE';
FALSE		: 'F' | 'FALSE';


// basic
ID		: (('a'..'z') | ('A'..'Z') | '_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;
WS 		:   (   ' '
		|   '\t'
		|   '\r'
		|   '\n'
		)+
		{ $channel=HIDDEN; };
MULTI_COMMENT	: ('/*' (
		options { greedy=false; /*generateAmbigWarnings=false;*/}
		:  // '\r' '\n' |
		'\r'
		|   '\n'
		|   ~('\n'|'\r')
		)*
		'*/'
		{$channel=HIDDEN;});
LINE_COMMENT	: ('--' (~('\n'|'\r'))* (('\n'|'\r'('\n')?))? {$channel=HIDDEN;})
		|
		('//' (~('\n'|'\r'))* (('\n'|'\r'('\n')?))? {$channel=HIDDEN;});
