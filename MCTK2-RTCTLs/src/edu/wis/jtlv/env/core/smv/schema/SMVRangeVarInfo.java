package edu.wis.jtlv.env.core.smv.schema;

import edu.wis.jtlv.env.core.smv.SMVParseException;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.module.SMVModule;

import static edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo.SMVElementCategory.ACTION_VAR;
import static edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo.SMVElementCategory.INPUT_VAR;
import static edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo.SMVElementCategory.STATE_VAR;

public class SMVRangeVarInfo extends SMVVarInfo {
	public int from;
	public int to;

	public SMVRangeVarInfo(SMVElementCategory category, boolean visible, String a_name, SMVParsingInfo an_info, int from,
			int to) throws SMVParseException {
		super(category, visible, a_name, an_info);
		this.from = from;
		this.to = to;
	}

	@Override
	public SMVAbstractElementInfo clone_element() throws SMVParseException {
		return new SMVRangeVarInfo(this.category, this.visible, this.name, this.parse_info, this.from,
				this.to);
	}

	@Override
	public String typeString() {
		return "[" + this.from + ".." + this.to + "]";
	}


	// LXY: for MAS
	// currently, action variable supports the following two types: RangeVar and ValueVar
	@Override
	public void mk_variables(SMVModule instance_holder)
			throws SMVParseException {
		String[] names = new String[this.to - this.from + 1];
		for (int i = 0; i < names.length; i++) {
			names[i] = "";
			int val = (i + this.from);
			names[i] += val;
		}
		try {
			if (this.category == STATE_VAR) //LXY
				instance_holder.addVar(this.name, this.from, this.to); //JTLV
			else if (this.category == INPUT_VAR || this.category == ACTION_VAR){ // LXY: this.category == INPUT_VAR
				instance_holder.addVar_unprime_only(this.name, this.from, this.to);
			} else { //LXY: this.category == NULL
				throw new SMVParseException("The category of this range variable is NULL.", parse_info);
			}
		} catch (ModuleException me) {
			throw new SMVParseException(me.getMessage(), parse_info);
		}
	}

}
