package edu.wis.jtlv.env.core.smv.schema;

import edu.wis.jtlv.env.core.smv.SMVParseException;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.module.SMVModule;

import static edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo.SMVElementCategory.ACTION_VAR;
import static edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo.SMVElementCategory.INPUT_VAR;
import static edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo.SMVElementCategory.STATE_VAR;

public class SMVValueVarInfo extends SMVVarInfo {
	public String[] values;

	public SMVValueVarInfo(SMVElementCategory category, boolean visible, String a_name, SMVParsingInfo an_info,
			String[] values) throws SMVParseException {
		super(category, visible, a_name, an_info);
		this.values = values;
	}

	@Override
	public SMVAbstractElementInfo clone_element() throws SMVParseException {
		return new SMVValueVarInfo(this.category, this.visible, this.name, this.parse_info, this.values);
	}

	@Override
	public String typeString() {
		String res = "[";
		boolean is_first = true;
		for (int i = 0; i < this.values.length; i++) {
			if (!is_first) {
				res += ", ";
			}
			res += this.values[i];
			is_first = false;
		}
		return res + "]";
	}

	// LXY: for MAS
	// currently, action variable supports the following two types: RangeVar and ValueVar
	@Override
	public void mk_variables(SMVModule instance_holder)
			throws SMVParseException {
		try {
			if (this.category == STATE_VAR) //LXY
				instance_holder.addVar(this.name, values); //JTLV
			else if (this.category == INPUT_VAR || this.category == ACTION_VAR){ // LXY: this.category == INPUT_VAR
				instance_holder.addVar_unprime_only(this.name, values);
			} else { //LXY: this.category == NULL
				throw new SMVParseException("The category of this value variable is NULL.", parse_info);
			}
		} catch (ModuleException me) {
			throw new SMVParseException(me.getMessage(), parse_info);
		}
	}

}
