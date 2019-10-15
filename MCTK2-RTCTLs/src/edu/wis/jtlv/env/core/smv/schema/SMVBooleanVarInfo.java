package edu.wis.jtlv.env.core.smv.schema;

import edu.wis.jtlv.env.core.smv.SMVParseException;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.module.SMVModule;

public class SMVBooleanVarInfo extends SMVVarInfo {
	public SMVBooleanVarInfo(SMVElementCategory category, boolean visible, String a_name, SMVParsingInfo an_info)
			throws SMVParseException {
		super(category, visible, a_name, an_info);
	}

	@Override
	public SMVAbstractElementInfo clone_element() throws SMVParseException {
		return new SMVBooleanVarInfo(this.category, this.visible, this.name, this.parse_info);
	}

	@Override
	public String typeString() {
		return "Boolean";
	}

	@Override
	public void mk_variables(SMVModule instance_holder)
			throws SMVParseException {
		try {
			if (this.category == SMVElementCategory.INPUT_VAR || this.category == SMVElementCategory.ACTION_VAR)
				instance_holder.addVar_unprime_only(this.name);
			else // category == STATE_VAR
				instance_holder.addVar(this.name);
		} catch (ModuleException me) {
			throw new SMVParseException(me.getMessage(), parse_info);
		}
	}
}
