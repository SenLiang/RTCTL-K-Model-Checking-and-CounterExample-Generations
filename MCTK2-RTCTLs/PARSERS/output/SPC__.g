lexer grammar SPC;
@members {
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
@header {
package edu.wis.jtlv.env.core.spec;
import edu.wis.jtlv.env.Env;
}

// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1431
TOK_CTL_SPEC				: 'CTLSPEC' | 'SPEC';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1432
TOK_CTL_STAR_SPEC			: 'CTL*SPEC';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1433
TOK_LTL_SPEC				: 'LTLSPEC';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1434
TOK_INVAR_SPEC				: 'INVARSPEC';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1435
TOK_ATL_STAR_SPEC			: 'ATL*SPEC';	

// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1437
TOK_EX						: 'EX';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1438
TOK_AX						: 'AX';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1439
TOK_EF						: 'EF';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1440
TOK_AF						: 'AF';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1441
TOK_EG						: 'EG';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1442
TOK_AG						: 'AG';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1443
TOK_EE						: 'E';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1444
TOK_AA						: 'A';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1445
TOK_BUNTIL					: 'BU';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1446
TOK_EBF						: 'EBF';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1447
TOK_ABF						: 'ABF';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1448
TOK_EBG						: 'EBG';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1449
TOK_ABG						: 'ABG';
// the last is the TLV notation.
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1451
TOK_OP_FINALLY				: 'F' | 'FINALLY' | 'EVENTUALLY';  // '<>' | 
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1452
TOK_OP_ONCE				: 'O' | 'ONCE';  // '<_>' | 
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1453
TOK_OP_GLOBALLY				: 'G' | 'GLOBALLY' | 'ALWAYS';  // '[]' | 
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1454
TOK_OP_HISTORICALLY			: 'H' | 'HISTORICALLY'; // '[_]' | 
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1455
TOK_OP_NEXT					: 'X' | 'NEXT'; // '()' | 
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1456
TOK_OP_PREV					: 'Y' | 'PREV'; // '(_)' | 
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1457
TOK_UNTIL					: 'Until' | 'U' | 'UNTIL';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1458
TOK_SINCE					: 'Since' | 'S' | 'SINCE';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1459
TOK_RELEASE				: 'Awaits' | 'R' | 'RELEASE';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1460
TOK_TRIGGERED				: 'Backto' | 'T' | 'TRIGGERED';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1461
TOK_OP_NOTPREVNOT			: 'Z';

// more bounded
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1464
TOK_OP_BFINALLY				: 'BF' | 'BFINALLY' | 'BEVENTUALLY';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1465
TOK_OP_BGLOBALLY			: 'BG' | 'BGLOBALLY' | 'BALWAYS';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1466
TOK_BRELEASE				: 'BR' | 'BRELEASE';


//epistemic
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1470
TOK_KNOW				: 'K' | 'KNOW' | 'Know';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1471
TOK_SKNOW				: 'SK' | 'SKNOW' | 'Sknow';

//TOK_MMIN					: 'MIN';// !!!
//TOK_MMAX					: 'MAX';// !!!

// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1476
TOK_LP						: '(';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1477
TOK_RP						: ')';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1478
TOK_LB						: '[';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1479
TOK_RB						: ']';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1480
TOK_LCB						: '{';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1481
TOK_RCB						: '}';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1482
TOK_FALSEEXP				: 'FALSE';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1483
TOK_TRUEEXP					: 'TRUE';

// ALL NON SIMPLE OPERATOR SHOULD BE REMOVED OR ELSE THEY
// WOULD NOT HAVE MEANING IN BETWEEN TL STATEMENTS
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1487
TOK_WORD1					: 'word1';// ???
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1488
TOK_WORD					: 'word' | 'Word';// ???
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1489
TOK_BOOL					: 'bool';// ???
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1490
TOK_WAREAD					: 'READ';// ???
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1491
TOK_WAWRITE					: 'WRITE';// ???

// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1493
TOK_CASE					: 'case';// ???
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1494
TOK_ESAC					: 'esac';// ???
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1495
TOK_PLUS					: '+';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1496
TOK_MINUS					: '-';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1497
TOK_TIMES					: '*';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1498
TOK_DIVIDE					: '/';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1499
TOK_MOD						: 'mod';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1500
TOK_LSHIFT					: '<<';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1501
TOK_RSHIFT					: '>>';
//TOK_LROTATE					: '<<<';
//TOK_RROTATE					: '>>>';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1504
TOK_EQUAL					: '=';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1505
TOK_NOTEQUAL				: '!=';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1506
TOK_LE						: '<=';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1507
TOK_GE						: '>=';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1508
TOK_LT						: '<';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1509
TOK_GT						: '>';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1510
TOK_NEXT					: 'next';
//TOK_SELF					: 'self';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1512
TOK_UNION					: 'union';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1513
TOK_SETIN					: 'in';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1514
TOK_TWODOTS					: '..';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1515
TOK_DOT						: '.';

// basic logic operators...
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1518
TOK_IMPLIES					: '->';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1519
TOK_IFF						: '<->';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1520
TOK_OR						: '|';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1521
TOK_AND						: '&';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1522
TOK_XOR						: 'xor';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1523
TOK_XNOR					: 'xnor';
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1524
TOK_NOT						: '!';

// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1526
TOK_COMMA					: ',';//                     {yylval.lineno = yylineno; return(TOK_COMMA);}
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1527
TOK_COLON					: ':';//                     {yylval.lineno = yylineno; return(TOK_COLON);}
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1528
TOK_SEMI					: ';';//                     {yylval.lineno = yylineno; return(TOK_SEMI);}
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1529
TOK_CONCATENATION			: '::';//                    {yylval.lineno = yylineno; return(TOK_CONCATENATION);}



/////////////////////////////////////////////////////////////////////
// basic JTLV extension - atoms, whitespaces and comments
/////////////////////////////////////////////////////////////////////

/* word constants */
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1538
TOK_NUMBER_WORD					: '0' ('b' | 'B' | 'o' | 'O' | 'd' | 'D' | 'h' | 'H') ('0'..'9')* '_' ('0'..'9' | 'a'..'f' | 'A'..'F') ('0'..'9' | 'a'..'f' | 'A'..'F' | '_')*;

 /* real, fractional and exponential constants */
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1541
TOK_NUMBER_FRAC					: ('f' | 'F') '\'' ('0'..'9')+ '/' ('0'..'9')+;

/* integer number */
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1544
TOK_NUMBER						: ('0'..'9')+;

/* identifier */
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1547
TOK_ATOM						: ('a'..'z' | 'A'..'Z' | '_') ('a'..'z' | 'A'..'Z' | '0'..'9' | '_' | '\\' | '$' | '#' | '-')*;


// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1550
JTOK_WS 						:   (   ' '
								|   '\t'
								|   '\r'
								|   '\n'
								)+
								{ $channel=HIDDEN; };
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1556
JTOK_MULTI_COMMENT				: ('/*' (
								options { greedy=false;}
								:  // '\r' '\n' |
								'\r'
								|   '\n'
								|   ~('\n'|'\r')
								)*
								'*/'
								{$channel=HIDDEN;});
// $ANTLR src "/Users/lxy/Documents/Doc-LXY-iMac/RecentDoc/Development/JTLV/MCTK2/PARSERS/SPC.g" 1565
JTOK_LINE_COMMENT				: ('--' (~('\n'|'\r'))* (('\n'|'\r'('\n')?))? {$channel=HIDDEN;})
								| ('//' (~('\n'|'\r'))* (('\n'|'\r'('\n')?))? {$channel=HIDDEN;});

