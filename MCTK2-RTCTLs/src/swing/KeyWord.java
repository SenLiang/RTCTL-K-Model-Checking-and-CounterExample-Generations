package swing;

public class KeyWord {
	private String[] smvKeyWords;
	private String[] spcKeyWords;
	private String[] operKeyWords;
	public KeyWord()
	{
		smvKeyWords=new String[]{ "abs",  "array", "ASSIGN", "bool", "boolean",
				"case", "COMPASSION", "COMPID","COMPUTE", "COMPWFF", "CONSTANTS", "CONSTARRAY", "CONSTRAINT", "count",  "CTLWFF", "DEFINE",
				 "esac","extend", "FAIRNESS", "FALSE", "floor", "FROZENVAR", "FUN",  "IN",
				"in", "INIT", "init", "Integer", "integer", "NVAR",  "ISA", "ITYPE", "IVAR", "JUSTICE",
				"LTLWFF", "MAX", "max", "MDEFINE", "MIN", "min", "MIRROR", "mod", "MODULE", "NAME", "next", "NEXTWFF", "of",
				"PRED", "PREDICATES", "Real", "real", "resize",  "self", "signed", "SIMPWFF", "sizeof",
				"swconst", "T", "toint", "TRANS", "TRUE", "typeof","union", "unsigned", "uwconst", "V", "VAR", "Word", "word",
				"word1",  "xnor", "xor","AGENT"};
		spcKeyWords=new String[]{
				"INVARSPEC","LTLSPEC", "SPEC","CTLSPEC","PSLSPEC","ATL","ATL*SPEC","RTCTL","RTCTL*SPEC"};
		operKeyWords=new String[]{
				"A", "ABF", "ABG", "AF", "AG", "AX",  "BU",
				"E", "EBF", "EBG", "EF", "EG",  "EX", "F", "G", "H",
				"next", "O", "S", "U",  "V", "X", "Y", "Z", "NOT", "FINALLY", "GLOBALLY",
				"HISTORICALLY", "NEXT", "NOT_PREV_NOT", "ONCE", "PREV",
				"EE", "AA", "CAN_ENFORCE", "CANNOT_AVOID,AND", "RELEASES", "SINCE", "TRIGGERED", "UNTIL", "AU",
				"EU", "B_FINALLY", "B_GLOBALLY", "KNOW", "NKNOW", "SKNOW", "NSKNOW" ,
				"ABU", "EBU", "B_UNTIL", "B_UNTIL0", "B_RELEASES"
		};
	}
	public boolean isSMV(String word)
	{
			for(int i=0;i<smvKeyWords.length;i++)
			{
				if(smvKeyWords[i].equals(word))
				{
					return true;
				}
			}
		return false;
	}
	public boolean isSPC(String word)
	{
		for(int i=0;i<spcKeyWords.length;i++)
		{
			if(spcKeyWords[i].equals(word))
			{
				return true;
			}
		}
		return false;
	}
	public boolean isOPER(String word)
	{
		for(int i=0;i<operKeyWords.length;i++)
		{
			if(operKeyWords[i].equals(word))
			{
				return true;
			}
		}
		return false;
	}
}
