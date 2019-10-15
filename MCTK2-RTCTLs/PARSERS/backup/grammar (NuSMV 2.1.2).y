%{
/**CFile***********************************************************************

  FileName    [grammar.y]

  PackageName [parser]

  Synopsis    [Yacc for NuSMV input language parser]

  SeeAlso     [input.lex]

  Author      [Marco Roveri]

  Copyright   [
  This file is part of the ``parser'' package of NuSMV version 2. 
  Copyright (C) 1998-2001 by CMU and ITC-irst. 

  NuSMV version 2 is free software; you can redistribute it and/or 
  modify it under the terms of the GNU Lesser General Public 
  License as published by the Free Software Foundation; either 
  version 2 of the License, or (at your option) any later version.

  NuSMV version 2 is distributed in the hope that it will be useful, 
  but WITHOUT ANY WARRANTY; without even the implied warranty of 
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public 
  License along with this library; if not, write to the Free Software 
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA.

  For more information of NuSMV see <http://nusmv.irst.itc.it>
  or email to <nusmv-users@irst.itc.it>.
  Please report bugs to <nusmv-users@irst.itc.it>.

  To contact the NuSMV development board, email to <nusmv@irst.itc.it>. ]

******************************************************************************/

#include <setjmp.h>
#include <malloc.h>
#include "util.h"
#include "utils.h"
#include "node.h"
#include "opt.h"
#include "parserInt.h"

static char rcsid[] UTIL_UNUSED = "$Id: grammar.y,v 1.19 2002/06/18 09:22:02 nusmv Exp $";

node_ptr parse_tree;
node_ptr parse_tree_int;
int parse_command_flag;

extern FILE * nusmv_stderr;
    
void yyerror(char *s);

%}
/*
  The number of conflicts: shift/reduce expected.
  If more, than a warning message is printed out.
*/
%expect 1

%union {
  node_ptr node;
}

/*
  All of the terminal grammar symbols (tokens recognized by the lexical analyzer) 
  Note: all binary operators associate from left to right,  operators are
        listed from lowest to highest priority 

  Note: The following token are not used inside the grammar, but are
  used by other modules inside the system (i.e. the compiler, mc).
  STEP RESET ASYNC MODTYPE LAMBDA CONTEXT EU AU EBU ABU MINU MAXU
  FORMAT CONSTANT SCALAR CONS OVER BDD ATLINE APROPOS IFTHENELSE
  QUOTE DL_ATOM APATH EPATH 
*/

%left GOTO LET STEP EVAL RESET CONSTRAINT
%left ASYNC MODULE PROCESS MODTYPE LAMBDA CONTEXT EU AU EBU ABU MINU MAXU
%left VAR IVAR DEFINE INIT TRANS INVAR FORMAT SPEC LTLSPEC COMPUTE 
%left INVARSPEC FAIRNESS COMPASSION JUSTICE
%left ISA CONSTANT ASSIGN INPUT OUTPUT IMPLEMENTS
%left BOOLEAN ARRAY OF SCALAR CONS OVER BDD
%left SEMI LP RP LB RB LCB RCB LLCB RRCB
%left EQDEF TWODOTS ATLINE
%left <node> FALSEEXP TRUEEXP
%left APROPOS SELF SIGMA
%left CASE ESAC COLON
%left IF THEN ELSE IFTHENELSE
%left INCONTEXT SIMPWFF LTLWFF CTLWFF COMPWFF

%left <node> ATOM
%left <node> NUMBER
%left <node> QUOTE
%left <node> DL_ATOM

%left  COMMA
%right IMPLIES
%left  IFF
%left  OR XOR XNOR
%left  AND
%left  NOT
%left  EX AX EF AF EG AG EE AA SINCE UNTIL TRIGGERED RELEASES EBF EBG ABF ABG BUNTIL MMIN MMAX
%left  OP_NEXT OP_GLOBAL OP_FUTURE
%left  OP_PREC OP_NOTPRECNOT OP_HISTORICAL OP_ONCE
%left  APATH EPATH
%left  EQUAL NOTEQUAL LT GT LE GE
%left  UNION
%left  SETIN
%left  MOD
%left  PLUS MINUS
%left  TIMES DIVIDE
%left  UMINUS		/* supplies precedence for unary minus */
%left  NEXT SMALLINIT
%left  DOT

/* all nonterminals return a parse tree node */
%type <node> module_list module declarations declaration var input_var var_list
%type <node> ivar_list type itype isa init trans invar define define_list spec
%type <node> ltlspec compute fairness justice compassion invarspec 
%type <node> atom_list decl_var_id var_id subrange number simple_expr 
%type <node> assign_expr init_expr next_expr justice_expr compassion_expr 
%type <node> fair_expr invar_expr trans_expr ctl_expr compute_expr s_case_list 
%type <node> n_case_list constant ltl_expr ltl_orexpr ltl_andexpr define_expr
%type <node> ltl_binary_expr ltl_atomexpr ltl_case_expr ltl_simple_expr 
%type <node> module_sign module_type simple_expr_list assign assign_list
%type <node> assign_type simple_expr_set next_expr_set ctl_expr_set 
%type <node> ltl_expr_set input output implements term_list
%type <node> constant_list actual_params command command_case trace state
%type <node> context ctl_case_list

/* %type <node> trace state */

/* currently not used: SEE GRAMMAR RULES 
%type next_expr_list
*/

%start begin
%%
begin         : { 
                  if(parse_command_flag) {
		    yyerror("MODULE definition when command expected");
		    YYERROR;
		  }
                } 
                module_list {parse_tree = $2;}
              | { 
                  if(! parse_command_flag) {
		    yyerror("command found in an SMV file");
		    YYERROR;
		  }
                }
                command {parse_tree_int = $2;}
              ;

/*
 An NuSMV program is a repetition of modules.
 Each module has a signature and a body.
*/
module_list   : module {$$ = cons($1, Nil);}
              | module_list module {$$ = cons($2, $1);}
              ;
module        : MODULE module_sign declarations {$$ = new_node(MODULE, $2, $3);}
              ;
module_sign   : ATOM {$$ = new_node(MODTYPE, $1, Nil);}
              | ATOM LP RP {$$ = new_node(MODTYPE, $1, Nil);}
              | ATOM LP atom_list RP {$$ = new_node(MODTYPE, $1, $3);}
              ;
atom_list     : ATOM {$$ = cons(find_atom($1), Nil);}
              | atom_list COMMA ATOM {$$ = cons(find_atom($3), $1);}
              ;


/* The body of a module */
declarations  : {$$ = Nil;}
              | declarations declaration {$$ = cons($2, $1);}
              ;
declaration   : isa
              | var
              | input_var
              | assign 
              | init
              | invar
              | trans
              | define
              | fairness
              | justice
              | compassion
              | invarspec
              | spec
              | ltlspec
              | compute
              | implements
              | input
              | output
              ;

/* Module macro-expansion */
isa           : ISA ATOM {$$ = new_node(ISA, $2, Nil);}
              ;

/* parse an optional semicolon */
optsemi      : | SEMI;

/*
 Variable declarations:
 This includes also the instantiation of module
 (in synchronous and asynchronous product).
*/
var           : VAR var_list {$$ = new_node(VAR, $2, Nil);}
              ;
input_var     : IVAR ivar_list {$$ = new_node(IVAR, $2, Nil);}
              ;
var_list      : {$$ = Nil;}
              | var_list decl_var_id COLON type SEMI {$$ = cons(new_node(COLON, $2, $4), $1);}
              ;
ivar_list     : {$$ = Nil;}
              | ivar_list decl_var_id COLON itype SEMI {$$ = cons(new_node(COLON, $2, $4), $1);}
              ;
type          : BOOLEAN {$$ = new_node(BOOLEAN, Nil, Nil);}
              | subrange
              | LCB constant_list RCB {$$ = new_node(SCALAR, $2, Nil);}
              | ARRAY subrange OF type {$$ = new_node(ARRAY, $2, $4);}
              | module_type
              | PROCESS module_type {$$ = new_node(PROCESS, $2, Nil);}
              ;
itype         : BOOLEAN {$$ = new_node(BOOLEAN, Nil, Nil);}
              | subrange
              | LCB constant_list RCB {$$ = new_node(SCALAR, $2, Nil);}
              | ARRAY subrange OF itype {$$ = new_node(ARRAY, $2, $4);}
              ;
constant_list : constant {$$ = cons(find_atom($1), Nil);}
              | constant_list COMMA constant {$$ = cons(find_atom($3), $1);}
              ;
constant      : ATOM
              | number
	      | FALSEEXP
	      | TRUEEXP
              ;
subrange      : number TWODOTS number {$$ = new_node(TWODOTS, $1, $3);}
              ;
number        : NUMBER
	      | PLUS NUMBER %prec UMINUS { $$ = $2; }
	      | MINUS NUMBER %prec UMINUS {node_int_setcar($2, -(node_get_int($2))); $$ = $2;}
              ;
module_type   : ATOM {$$ = new_node(MODTYPE, $1, Nil);}
              | ATOM LP RP {$$ = new_node(MODTYPE, $1, Nil);}
              | ATOM LP actual_params RP {$$ = new_node(MODTYPE, $1, $3);}
              ;
actual_params : simple_expr_list
              ;

/* Assignments of initial, current or next value of variables */
assign        : ASSIGN assign_list {$$ = new_node(ASSIGN, $2, Nil);}
              ;
assign_list   : {$$ = Nil;}
              | assign_list assign_type {$$ = new_node(AND, $1, $2);}
              ;
assign_type   : var_id EQDEF simple_expr SEMI
                 {$$ = new_node(EQDEF, $1, $3);} 
              | SMALLINIT LP var_id RP EQDEF simple_expr SEMI
                { $$ = new_node(EQDEF, new_node(SMALLINIT, $3, Nil), $6);}
              | NEXT LP var_id RP EQDEF assign_expr SEMI
                { $$ = new_node(EQDEF, new_node(NEXT, $3, Nil), $6);}
              ;
assign_expr   : next_expr
              ;

context       : ATOM                      { $$ = find_atom($1); }
              | context DOT ATOM          { $$ = find_node(DOT, $1, $3); }
              | context LB simple_expr RB { $$ = find_node(ARRAY, $1, $3); }
              ;    

/* Direct finite state machine definition (init, invar, trans) */
init          : INIT init_expr optsemi {$$ = new_node(INIT, $2, Nil);}
              ;
init_expr     : simple_expr
              ;
invar         : INVAR invar_expr optsemi {$$ = new_node(INVAR, $2, Nil);}
              ;
invar_expr    : simple_expr
              ;
trans         : TRANS trans_expr optsemi {$$ = new_node(TRANS, $2, Nil);}
              ;
trans_expr    : next_expr
              ;

/* Definitions */
define        : DEFINE define_list {$$ = new_node(DEFINE, $2, Nil);}
              ;
define_list   : {$$ = Nil;}
              | define_list decl_var_id EQDEF define_expr SEMI {$$ = cons(new_node(EQDEF, $2, $4), $1);}
              ;
define_expr   : next_expr
              ;

/* Fairness declarations */
fairness      : FAIRNESS fair_expr optsemi {$$ = new_node(JUSTICE, $2, Nil);}
              ;
fair_expr     : simple_expr
              ;

justice       : JUSTICE  justice_expr optsemi {$$ = new_node(JUSTICE, $2, Nil);}
              ;
justice_expr  : simple_expr
              ;

compassion    : COMPASSION compassion_expr optsemi {$$ = new_node(COMPASSION, $2, Nil);}
              ;
compassion_expr: LP simple_expr COMMA simple_expr RP {$$ = cons($2,$4);} 
               ;

/* Specifications and computation of min and max distance */
spec          : SPEC ctl_expr optsemi {$$ = new_node(SPEC, $2, Nil);}
              ;

ltlspec       : LTLSPEC ltl_expr optsemi {$$ = new_node(LTLSPEC, $2, Nil);}
              ;

invarspec     : INVARSPEC invar_expr optsemi {$$ = new_node(INVARSPEC, $2, Nil);}
              ;
 
compute       : COMPUTE compute_expr optsemi {$$ = new_node(COMPUTE, $2, Nil);}
              ;
compute_expr  : MMIN LB ctl_expr COMMA ctl_expr RB { $$ = new_node(MINU, $3, $5); }
              | MMAX LB ctl_expr COMMA ctl_expr RB { $$ = new_node(MAXU, $3, $5); }
              ;

/* Variable identifiers.
   decl_var_id is used for declarations; self not allowed.
   var_id is used to reference variables, includes self.
 */

decl_var_id   : ATOM
              | decl_var_id DOT ATOM {$$ = new_node(DOT, $1, $3);}
              | decl_var_id DOT NUMBER {$$ = new_node(DOT, $1, $3);}
              | decl_var_id LB simple_expr RB {$$ = new_node(ARRAY, $1, $3);}
              ;
var_id        : ATOM
              | SELF {$$ = new_node(SELF,Nil,Nil);}
              | var_id DOT ATOM {$$ = new_node(DOT, $1, $3);}
              | var_id DOT NUMBER {$$ = new_node(DOT, $1, $3);}
              | var_id LB simple_expr RB {$$ = new_node(ARRAY, $1, $3);}
              ;


/* Simple expressions. Do not involve next state variables or CLT. */

simple_expr_list : simple_expr {$$ = cons($1,Nil);}
	         | simple_expr_list COMMA simple_expr {$$ = cons($3, $1);}
                 ;
simple_expr   : var_id
              | number
              | subrange
              | FALSEEXP
              | TRUEEXP
              | LP simple_expr RP { $$ = $2; }
	      | simple_expr IMPLIES simple_expr { $$ = new_node(IMPLIES, $1, $3); }
	      | simple_expr IFF simple_expr { $$ = new_node(IFF, $1, $3); }
	      | simple_expr OR simple_expr { $$ = new_node(OR,$1, $3); }
              | simple_expr XOR simple_expr { $$ = new_node(XOR,$1, $3); }
              | simple_expr XNOR simple_expr { $$ = new_node(XNOR,$1, $3); }
	      | simple_expr AND simple_expr { $$ = new_node(AND, $1, $3); }
	      | NOT simple_expr { $$ = new_node(NOT, $2, Nil); }
              | simple_expr PLUS simple_expr { $$ = new_node(PLUS, $1, $3); }
              | simple_expr MINUS simple_expr { $$ = new_node(MINUS, $1, $3); }
              | simple_expr TIMES simple_expr { $$ = new_node(TIMES, $1, $3); }
              | simple_expr DIVIDE simple_expr { $$ = new_node(DIVIDE, $1, $3); }
              | simple_expr MOD simple_expr { $$ = new_node(MOD, $1, $3); }
              | simple_expr EQUAL simple_expr { $$ = new_node(EQUAL, $1, $3); }
              | simple_expr NOTEQUAL simple_expr { $$ = new_node(NOTEQUAL, $1, $3); }
              | simple_expr LT simple_expr { $$ = new_node(LT, $1, $3); }
              | simple_expr GT simple_expr { $$ = new_node(GT, $1, $3); }
              | simple_expr LE simple_expr { $$ = new_node(LE, $1, $3); }
              | simple_expr GE simple_expr { $$ = new_node(GE, $1, $3); }
              | LCB simple_expr_set RCB { $$ = $2; } 
              | simple_expr UNION simple_expr { $$ = new_node(UNION, $1, $3); }
              | simple_expr SETIN simple_expr { $$ = new_node(SETIN, $1, $3); }
              | CASE s_case_list ESAC { $$ = $2; }
              | SIGMA LB ATOM EQUAL subrange RB simple_expr
                { $$ = new_node(SIGMA, new_node(EQUAL, $3, $5), $7); }
	      ;
simple_expr_set : simple_expr {$$ = $1;}
                | simple_expr_set COMMA simple_expr {$$ = new_node(UNION, $1, $3);}
                ;
s_case_list   : {$$=new_node(TRUEEXP, Nil, Nil);}
              | simple_expr COLON simple_expr SEMI s_case_list
                {
	          $$ = new_node(CASE, new_node(COLON, $1, $3), $5);
	        }
              ;



/* Next expressions. Contain next state variables, but no CTL. */
next_expr     : var_id
              | number
              | subrange
              | FALSEEXP
              | TRUEEXP
/*              | NEXT LP var_id RP { $$ = new_node(NEXT, $3, Nil); } */
/* We dont want expression of this kind: next(alpha & next(beta))   */
              | NEXT LP simple_expr RP { $$ = new_node(NEXT, $3, Nil); } 
              | LP next_expr RP { $$ = $2; }
	      | next_expr IMPLIES next_expr { $$ = new_node(IMPLIES, $1, $3); }
	      | next_expr IFF next_expr { $$ = new_node(IFF, $1, $3); }
	      | next_expr OR next_expr { $$ = new_node(OR, $1, $3); }
	      | next_expr XOR next_expr { $$ = new_node(XOR, $1, $3); }
	      | next_expr XNOR next_expr { $$ = new_node(XNOR, $1, $3); }
	      | next_expr AND next_expr { $$ = new_node(AND, $1, $3); }
	      | NOT next_expr { $$ = new_node(NOT, $2, Nil); }
              | next_expr PLUS next_expr { $$ = new_node(PLUS, $1, $3); }
              | next_expr MINUS next_expr { $$ = new_node(MINUS, $1, $3); }
              | next_expr TIMES next_expr { $$ = new_node(TIMES, $1, $3); }
              | next_expr DIVIDE next_expr { $$ = new_node(DIVIDE, $1, $3); }
              | next_expr MOD next_expr { $$ = new_node(MOD, $1, $3); }
              | next_expr EQUAL next_expr { $$ = new_node(EQUAL, $1, $3); }
              | next_expr NOTEQUAL next_expr { $$ = new_node(NOTEQUAL, $1, $3); }
              | next_expr LT next_expr { $$ = new_node(LT, $1, $3); }
              | next_expr GT next_expr { $$ = new_node(GT, $1, $3); }
              | next_expr LE next_expr { $$ = new_node(LE, $1, $3); }
              | next_expr GE next_expr { $$ = new_node(GE, $1, $3); }
              | LCB next_expr_set RCB { $$ = $2; } 
              | next_expr UNION next_expr { $$ = new_node(UNION, $1, $3); }
              | next_expr SETIN next_expr { $$ = new_node(SETIN, $1, $3); }
              | CASE n_case_list ESAC { $$ = $2; }
              | SIGMA LB ATOM EQUAL subrange RB next_expr
                { $$ = new_node(SIGMA, new_node(EQUAL, $3, $5), $7); }
              ;
next_expr_set : next_expr {$$ = $1;}
              | next_expr_set COMMA next_expr {$$ = new_node(UNION, $1, $3);}
              ;
n_case_list   : {$$=new_node(TRUEEXP, Nil, Nil);}
              | next_expr COLON next_expr SEMI n_case_list
                {
	          $$ = new_node(CASE, new_node(COLON, $1, $3), $5);
	        }
              ;

/* currently not used 
next_expr_list : next_expr {$$ = cons($1,Nil);}
	         | next_expr_list COMMA next_expr {$$ = cons($3, $1);}
                 ;
*/

/* (Bounded) CTL formulas. Contain CTL operators, but no next variables. */

ctl_expr      : var_id
              | number
              | subrange
              | FALSEEXP
              | TRUEEXP
              | LP ctl_expr RP { $$ = $2; }
	      | ctl_expr IMPLIES ctl_expr { $$ = new_node(IMPLIES, $1, $3); }
	      | ctl_expr IFF ctl_expr { $$ = new_node(IFF, $1, $3); }
	      | ctl_expr OR ctl_expr { $$ = new_node(OR, $1, $3); }
	      | ctl_expr XOR ctl_expr { $$ = new_node(XOR, $1, $3); }
	      | ctl_expr XNOR ctl_expr { $$ = new_node(XNOR, $1, $3); }
	      | ctl_expr AND ctl_expr { $$ = new_node(AND, $1, $3); }
	      | NOT ctl_expr { $$ = new_node(NOT, $2, Nil); }
              | ctl_expr PLUS ctl_expr { $$ = new_node(PLUS, $1, $3); }
              | ctl_expr MINUS ctl_expr { $$ = new_node(MINUS, $1, $3); }
              | ctl_expr TIMES ctl_expr { $$ = new_node(TIMES, $1, $3); }
              | ctl_expr DIVIDE ctl_expr { $$ = new_node(DIVIDE, $1, $3); }
              | ctl_expr MOD ctl_expr { $$ = new_node(MOD, $1, $3); }
              | ctl_expr EQUAL ctl_expr { $$ = new_node(EQUAL, $1, $3); }
              | ctl_expr NOTEQUAL ctl_expr { $$ = new_node(NOTEQUAL, $1, $3); }
              | ctl_expr LT ctl_expr { $$ = new_node(LT, $1, $3); }
              | ctl_expr GT ctl_expr { $$ = new_node(GT, $1, $3); }
              | ctl_expr LE ctl_expr { $$ = new_node(LE, $1, $3); }
              | ctl_expr GE ctl_expr { $$ = new_node(GE, $1, $3); }
              | LCB ctl_expr_set RCB { $$ = $2; } 
              | ctl_expr UNION ctl_expr { $$ = new_node(UNION, $1, $3); }
              | ctl_expr SETIN ctl_expr { $$ = new_node(SETIN, $1, $3); }
              | CASE ctl_case_list ESAC { $$ = $2; }
/*            | SIGMA LB ATOM EQUAL subrange RB ctl_expr */
/*                { $$ = new_node(SIGMA, new_node(EQUAL, $3, $5), $7); } */
              | EX ctl_expr { $$ = new_node(EX, $2, Nil); }
              | AX ctl_expr { $$ = new_node(AX, $2, Nil); }
              | EF ctl_expr { $$ = new_node(EF, $2, Nil); }
              | AF ctl_expr { $$ = new_node(AF, $2, Nil); }
              | EG ctl_expr { $$ = new_node(EG, $2, Nil); }
              | AG ctl_expr { $$ = new_node(AG, $2, Nil); }
	      | AA LB ctl_expr UNTIL ctl_expr RB { $$ = new_node(AU, $3, $5); }
	      | EE LB ctl_expr UNTIL ctl_expr RB { $$ = new_node(EU, $3, $5); }
	      | AA LB ctl_expr BUNTIL subrange ctl_expr RB
                       { $$ = new_node(ABU, new_node(AU, $3, $6), $5); }
	      | EE LB ctl_expr BUNTIL subrange ctl_expr RB
                       { $$ = new_node(EBU, new_node(EU, $3, $6), $5); }
              | EBF subrange ctl_expr { $$ = new_node(EBF, $3, $2); }
              | ABF subrange ctl_expr { $$ = new_node(ABF, $3, $2); }
              | EBG subrange ctl_expr { $$ = new_node(EBG, $3, $2); }
              | ABG subrange ctl_expr { $$ = new_node(ABG, $3, $2); }
	      ;
ctl_expr_set  : ctl_expr {$$ = $1;}
              | ctl_expr_set COMMA ctl_expr {$$ = new_node(UNION, $1, $3);}
              ;
ctl_case_list : {$$=new_node(TRUEEXP, Nil, Nil);}
              | ctl_expr COLON ctl_expr SEMI ctl_case_list
                {
	          $$ = new_node(CASE, new_node(COLON, $1, $3), $5);
	        }
              ;

/* LTL grammar */
ltl_expr      : ltl_orexpr {$$ = $1;}
              | ltl_orexpr IMPLIES ltl_expr {$$ = new_node(IMPLIES, $1, $3);}
              | ltl_orexpr IFF ltl_orexpr {$$ = new_node(IFF, $1, $3);}
              | ltl_orexpr XOR ltl_orexpr {$$ = new_node(XOR, $1, $3);}
              | ltl_orexpr XNOR ltl_orexpr {$$ = new_node(XNOR, $1, $3);}
              ;
ltl_orexpr    : ltl_andexpr {$$ = $1;}
              | ltl_orexpr OR ltl_andexpr {$$ = new_node(OR, $1, $3);}
              ;
ltl_andexpr   : ltl_binary_expr {$$ = $1;}
              | ltl_andexpr AND ltl_binary_expr {$$ = new_node(AND, $1, $3);}
              ;
ltl_binary_expr : ltl_atomexpr {$$ = $1;}
              | ltl_binary_expr UNTIL ltl_atomexpr {$$ = new_node(UNTIL, $1, $3);}
              | ltl_binary_expr SINCE ltl_atomexpr {$$ = new_node(SINCE, $1, $3);}
              | ltl_binary_expr RELEASES ltl_atomexpr 
                {$$ = new_node(NOT, new_node(UNTIL, new_node(NOT, $1, Nil), 
                new_node(NOT, $3, Nil)), Nil);}
              | ltl_binary_expr TRIGGERED ltl_atomexpr 
                {$$ = new_node(NOT, new_node(SINCE, new_node(NOT, $1, Nil), 
                new_node(NOT, $3, Nil)), Nil);}
              ;
ltl_atomexpr  : NOT ltl_atomexpr {$$ = new_node(NOT, $2, Nil);}
              | OP_NEXT       ltl_atomexpr {$$ = new_node(OP_NEXT, $2, Nil);} 
              | OP_PREC       ltl_atomexpr {$$ = new_node(OP_PREC, $2, Nil);} 
              | OP_NOTPRECNOT ltl_atomexpr {$$ = new_node(OP_NOTPRECNOT, $2, Nil);}               
              | OP_GLOBAL     ltl_atomexpr {$$ = new_node(OP_GLOBAL, $2, Nil);}
              | OP_HISTORICAL ltl_atomexpr {$$ = new_node(OP_HISTORICAL, $2, Nil);}
              | OP_FUTURE     ltl_atomexpr {$$ = new_node(OP_FUTURE, $2, Nil);}
              | OP_ONCE       ltl_atomexpr {$$ = new_node(OP_ONCE, $2, Nil);}
              | LP ltl_expr RP {$$ = $2;}
              | CASE ltl_case_expr ESAC {$$ = $2;}
              | ltl_simple_expr
              ;

ltl_case_expr : {$$=new_node(TRUEEXP, Nil, Nil);}
              | ltl_expr COLON ltl_expr SEMI ltl_case_expr
                {
	          $$ = new_node(CASE, new_node(COLON, $1, $3), $5);
	        }
              ;

ltl_simple_expr : var_id
                | number
                | subrange
                | FALSEEXP
                | TRUEEXP
                | LP ltl_simple_expr RP {$$ = $2;}
                | ltl_simple_expr PLUS ltl_simple_expr {$$ = new_node(PLUS, $1, $3);}
                | ltl_simple_expr MINUS ltl_simple_expr {$$ = new_node(MINUS, $1, $3);}
                | ltl_simple_expr TIMES ltl_simple_expr {$$ = new_node(TIMES, $1, $3);}
                | ltl_simple_expr DIVIDE ltl_simple_expr {$$ = new_node(DIVIDE, $1, $3);}
                | ltl_simple_expr MOD ltl_simple_expr {$$ = new_node(MOD, $1, $3);}
                | ltl_simple_expr EQUAL ltl_simple_expr {$$ = new_node(EQUAL, $1, $3);}
                | ltl_simple_expr NOTEQUAL ltl_simple_expr {$$ = new_node(NOTEQUAL, $1, $3);}
                | ltl_simple_expr LT ltl_simple_expr {$$ = new_node(LT, $1, $3);}
                | ltl_simple_expr GT ltl_simple_expr {$$ = new_node(GT, $1, $3);}
                | ltl_simple_expr LE ltl_simple_expr {$$ = new_node(LE, $1, $3);}
                | ltl_simple_expr GE ltl_simple_expr {$$ = new_node(GE, $1, $3);}
                | LCB ltl_expr_set RCB { $$ = $2; } 
                | ltl_simple_expr UNION ltl_simple_expr { $$ = new_node(UNION, $1, $3);}
                | ltl_simple_expr SETIN ltl_simple_expr { $$ = new_node(SETIN, $1, $3);}
                ;
ltl_expr_set    : ltl_expr {$$ = $1;}
                | ltl_expr_set COMMA ltl_expr {$$ = new_node(UNION, $1, $3);}
                ;
/*
   Only for homomorphism checking (currently disabled).
 */
implements    : IMPLEMENTS ATOM {$$ = new_node(IMPLEMENTS, $2, Nil);}
              ;
input         : INPUT var_list {$$ = new_node(INPUT, $2, Nil);}
              ;
output        : OUTPUT term_list SEMI {$$ = new_node(OUTPUT, $2, Nil);}
              ;
term_list     : var_id {$$ = cons($1, Nil);}
              | term_list COMMA var_id {$$ = cons($3, $1);}
              ;

command       : command_case {$$ = $1;}
              | error SEMI {return(1);}
              | error {return(1);}
              ;

command_case  : GOTO state SEMI
                 {parse_tree_int = new_node(GOTO, $2, Nil); return(0);}
              | LET var_id EQDEF simple_expr SEMI
                 {parse_tree_int = new_node(LET, new_node(EQDEF, $2, $4), Nil); return(0);}
              | EVAL ctl_expr SEMI
                 {parse_tree_int = new_node(EVAL, $2, Nil); return(0);}
              | INIT init_expr SEMI
                 {parse_tree_int = new_node(INIT, $2, Nil); return(0);}
              | FAIRNESS fair_expr SEMI
                 {parse_tree_int = new_node(FAIRNESS, $2, Nil); return(0);}
              | TRANS trans_expr SEMI
                 {parse_tree_int = new_node(TRANS, $2, Nil); return(0);}
              | CONSTRAINT simple_expr SEMI
                 {parse_tree_int = new_node(CONSTRAINT, $2, Nil); return(0);}
              | SIMPWFF simple_expr SEMI
                 {parse_tree_int =
                    new_node(SIMPWFF,
                             new_node(CONTEXT, Nil, $2), Nil); return(0);}
              | SIMPWFF simple_expr INCONTEXT context SEMI
                 {parse_tree_int =
                    new_node(SIMPWFF,
                             new_node(CONTEXT, find_node(DOT, Nil, $4), $2), Nil);
                 return(0);}
              | CTLWFF ctl_expr SEMI
                 {parse_tree_int =
                    new_node(CTLWFF,
                             new_node(CONTEXT, Nil, $2), Nil); return(0);}
              | CTLWFF ctl_expr INCONTEXT context SEMI
                 {parse_tree_int =
                    new_node(CTLWFF,
                             new_node(CONTEXT, find_node(DOT, Nil, $4), $2), Nil);
                 return(0);}
              | LTLWFF ltl_expr SEMI
                 {parse_tree_int =
                    new_node(LTLWFF,
                             new_node(CONTEXT, Nil, $2), Nil); return(0);}
              | LTLWFF ltl_expr INCONTEXT context SEMI
                 {parse_tree_int =
                    new_node(LTLWFF,
                             new_node(CONTEXT, find_node(DOT, Nil, $4), $2), Nil);
                 return(0);}
              | COMPWFF compute_expr SEMI
                 {parse_tree_int =
                    new_node(COMPWFF,
                             new_node(CONTEXT, Nil, $2), Nil); return(0);}
              | COMPWFF compute_expr INCONTEXT context SEMI
                 {parse_tree_int =
                    new_node(COMPWFF,
                             new_node(CONTEXT, find_node(DOT, Nil, $4), $2), Nil);
                 return(0);}
              ;

trace         : NUMBER {$$ = (node_ptr)find_atom($1);}
              | state DOT NUMBER {$$ = find_node(DOT, $1, (node_ptr)find_atom($3));}
              ;

state         : trace DOT NUMBER {$$ = find_node(DOT, $1, (node_ptr)find_atom($3));}
              ;


%%

/* Additional source code */
void yyerror(char *s)
{
    extern options_ptr options;
    extern char yytext[];
    
    start_parsing_err();
    fprintf(nusmv_stderr, "at token \"%s\": %s\n", yytext, s);
    if (opt_batch(options)) finish_parsing_err();
}

int yywrap()
{
  return(1);
}
