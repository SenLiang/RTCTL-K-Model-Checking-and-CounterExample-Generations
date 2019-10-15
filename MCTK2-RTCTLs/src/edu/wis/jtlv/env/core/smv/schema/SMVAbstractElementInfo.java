package edu.wis.jtlv.env.core.smv.schema;

import edu.wis.jtlv.env.core.smv.SMVParseException;
import edu.wis.jtlv.env.module.SMVModule;

import static edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo.SMVElementCategory.STATE_VAR;

public abstract class SMVAbstractElementInfo implements Cloneable {

/*
	public enum SMVElementType {
		STATE_VAR("state variable", 1), INPUT_VAR("input variable", 2), ACTION_VAR("action variable", 3);

		private String name;
		private int index;

		private SMVElementType(String name, int index) {
			this.name = name;
			this.index = index;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}
*/

	public enum SMVElementCategory {
		NULL, STATE_VAR, INPUT_VAR, ACTION_VAR
	}

/*
	//SMV element category
	public final static int NULL = 0;
	public final static int STATE_VAR = 1;
	public final static int INPUT_VAR = 2;
	public final static int ACTION_VAR = 3;
*/


	protected SMVParsingInfo parse_info;
	public String name;
	protected SMVModuleInfo holder_desc;

	//LXY: for MAS
	public boolean visible; // if this is visible for the holder module
	public SMVElementCategory category; // STATE_VAR, INPUT_VAR, ACTION_VAR

	public SMVAbstractElementInfo(String a_name, SMVParsingInfo a_parse_info)
			throws SMVParseException {
		this.name = a_name;
		this.parse_info = a_parse_info;
		this.holder_desc = null;

		this.visible = false; //false by default
		this.category = STATE_VAR; // to be state variable by default
	}

	public SMVAbstractElementInfo(SMVElementCategory category, boolean visible, String a_name, SMVParsingInfo a_parse_info)
			throws SMVParseException {
		this.name = a_name;
		this.parse_info = a_parse_info;
		this.holder_desc = null;

		this.visible = visible; //false by default
		this.category = category;
	}

	public void set_holder(SMVModuleInfo an_holder) {
		this.holder_desc = an_holder;
	}
	
	public SMVModuleInfo get_holder() {
		return this.holder_desc;
	}

/*	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Boolean getVisible() {
		return this.visible;
	}
*/
	public String toString() {
		return name + " : " + typeString();
	}

	public abstract SMVAbstractElementInfo clone_element() throws SMVParseException;

	public abstract String typeString();

	// / all phases.
	
	// 0. during construction I need to set the arrays names.
	public abstract void mk_fix_names() throws SMVParseException;

	// 1. only modules.
	public abstract void mk_modules_skel(SMVModule instance_holder)
			throws SMVParseException;

	// 2. add define variables.
	public abstract void mk_defines(SMVModule instance_holder)
			throws SMVParseException;

	// 3. add variables (including state, input, action variables).
	public abstract void mk_variables(SMVModule instance_holder)
			throws SMVParseException;

/*
	//LXY: 3.1 add input variables (only unprime version of BDD variables created).
	public abstract void mk_input_variables(SMVModule instance_holder)
			throws SMVParseException;

	//LXY: 3.2 add action variables (only unprime version of BDD variables created).
	public abstract void mk_action_variables(SMVModule instance_holder)
			throws SMVParseException;
*/

	// 4. add module args.
	public abstract void mk_module_args(SMVModule instance_holder)
			throws SMVParseException;
}
