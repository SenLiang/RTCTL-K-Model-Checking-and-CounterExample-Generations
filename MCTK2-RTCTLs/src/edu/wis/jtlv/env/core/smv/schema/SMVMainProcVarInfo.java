package edu.wis.jtlv.env.core.smv.schema;

import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.core.smv.SMVParseException;
import edu.wis.jtlv.env.module.ModuleBDDField;
import edu.wis.jtlv.env.module.ModuleParamHolder;
import edu.wis.jtlv.env.module.SMVModule;
import net.sf.javabdd.BDDVarSet;

import java.util.HashSet;
import java.util.Vector;

import static edu.wis.jtlv.env.Env.getModule;
import static edu.wis.jtlv.env.Env.getVar;
import static edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo.SMVElementCategory.ACTION_VAR;
import static edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo.SMVElementCategory.INPUT_VAR;
import static edu.wis.jtlv.env.core.smv.schema.SMVAbstractElementInfo.SMVElementCategory.NULL;

public class SMVMainProcVarInfo extends SMVContainerElementInfo {
	public SMVModuleInfo proc_desc;

	public SMVMainProcVarInfo(SMVModuleInfo proc_desc) throws SMVParseException {
		super(NULL, false,"main", null); // this module have no initiate info...
		this.proc_desc = proc_desc;
	}

	@Override
	public SMVAbstractElementInfo clone_element() throws SMVParseException {
		throw new SMVParseException(
				"Cannot have more then a single main instance.");
	}

	@Override
	public String typeString() {
		return "";
	}

	@Override
	public void mk_fix_names() throws SMVParseException {
		this.proc_desc.mk_fix_names();
	}

	@Override
	public void mk_modules_skel(SMVModule instance_holder)
			throws SMVParseException {
		String inst_full_name = this.name; // "main"
		SMVModule inst = new SMVModule(this.proc_desc, inst_full_name);
		Env.putModule(inst_full_name, inst); // registering
		this.proc_desc.mk_modules_skel(inst);
	}

	@Override
	public void mk_defines(SMVModule instance_holder) throws SMVParseException {
		String inst_full_name = this.name; // "main"
		SMVModule inst = (SMVModule) Env.getModule(inst_full_name);
		this.proc_desc.mk_defines(inst);
	}

	@Override
	public void mk_variables(SMVModule instance_holder)
			throws SMVParseException {
		String inst_full_name = this.name; // "main"
		SMVModule inst = (SMVModule) Env.getModule(inst_full_name);
		this.proc_desc.mk_variables(inst);
	}

	@Override
	public void mk_module_args(SMVModule instance_holder)
			throws SMVParseException {
		String inst_full_name = this.name; // "main"
		SMVModule inst = (SMVModule) Env.getModule(inst_full_name);
		this.proc_desc.mk_module_args(inst);
	}

	// this function is an example of walking through an instance
	// walk through from the instance of the module
	// if the instance is null, then walk through the module
	public void walkthrough_module_structure(
			SMVModule module,
			SMVAbstractElementInfo instance // an instance of the module
	) {
		Vector<SMVAbstractElementInfo> insts;
		if(instance!=null) { // walk through the module from the inputted instance
			insts = new Vector<SMVAbstractElementInfo>();
			insts.add(instance);
		}else // instance==null
			insts = module.getModuleInfo().getSub_elements();

		for (SMVAbstractElementInfo inst : insts) {
			String type = inst.getClass().getName();
			int dot_idx = type.lastIndexOf('.');
			if (dot_idx != -1) type = type.substring(dot_idx + 1);

			String inst_fullname = module.getFullInstName() + "." + inst.name;
			if (type.equals("SMVProcVarInfo")) {  // inst is a module instance
				SMVProcVarInfo instProc = (SMVProcVarInfo) inst;
				SMVModule instModule = (SMVModule) Env.getModule(inst_fullname);
				walkthrough_module_structure(instModule, null);
			}else if (type.equals("SMVArrayVarInfo")) { // inst is an array of some type， could be multi-dimension array...
				SMVArrayVarInfo instArray = (SMVArrayVarInfo) inst;
				SMVAbstractElementInfo instArrayProto = instArray.proto;
				for (int i=instArray.from; i<=instArray.to; i++)
					walkthrough_module_structure(module, instArray.real_list[i-instArray.from]);
			}else {
				// Else: other types that can not expanded
				// deal with the instance

				System.out.println(module.getFullInstName() + ": " +inst.toString());
			}
		}
	}

	// collect an agent's information
	public void collect_agent_information_recur(
			SMVAgentInfo agentInfo, 		// agent's infor stored here
			SMVModule module,				// maybe a module instance of the agent, and initially it is the module holding the agent
			boolean module_visible, 		// true -- the module is visible for its holder; false -- the module is invisible for its holder
			SMVAbstractElementInfo instance // an instance of the module, initially its is null
	) throws SMVParseException {
		Vector<SMVAbstractElementInfo> insts;
		if(instance!=null) { // walk through the module from the inputted instance
			insts = new Vector<SMVAbstractElementInfo>();
			insts.add(instance);
		}else // instance==null
			insts = module.getModuleInfo().getSub_elements();

		for (SMVAbstractElementInfo inst : insts) {
			String type = inst.getClass().getName();
			int dot_idx = type.lastIndexOf('.');
			if (dot_idx != -1) type = type.substring(dot_idx + 1);

			String inst_fullname = module.getFullInstName() + "." + inst.name;
			if (type.equals("SMVProcVarInfo")) {  // inst is a module instance
				SMVProcVarInfo instProc = (SMVProcVarInfo) inst;
				if (instProc.proc_desc.isAgent && !agentInfo.name.equals("main")) {
					// the normal agent's local module instance inst is also an normal agent, this is not allowed
					throw new SMVParseException("The agent " + agentInfo.name + "'s local module instance " + inst_fullname + " is also an agent, this is not allowed.");
				}else if (instProc.proc_desc.isAgent && agentInfo.name.equals("main")) {
					// the inst is an agent of the main agent
					collect_agent_information(inst_fullname);
					// when the inst agent is visible to the main agent, add all local variables of the inst agent to the visible variables set of the main agent
					SMVAgentInfo instAgentInfo = Env.getAll_agent_modules().get(inst_fullname);
					SMVAgentInfo mainAgentInfo = Env.getAll_agent_modules().get("main");
					HashSet<ModuleBDDField> instLvars = new HashSet<ModuleBDDField>();
					if(instAgentInfo.VisLocalVars!=null) instLvars.addAll(instAgentInfo.VisLocalVars);
					if(instAgentInfo.InvisLocalVars!=null) instLvars.addAll(instAgentInfo.InvisLocalVars);
					HashSet<ModuleBDDField> instIvars = new HashSet<ModuleBDDField>();
					if(instAgentInfo.VisInputVars!=null) instIvars.addAll(instAgentInfo.VisInputVars);
					if(instAgentInfo.InvisInputVars!=null) instIvars.addAll(instAgentInfo.InvisInputVars);
					if (inst.visible) {
						mainAgentInfo.VisLocalVars.addAll(instLvars);
						mainAgentInfo.VisInputVars.addAll(instIvars);
					}else{
						mainAgentInfo.InvisLocalVars.addAll(instLvars);
						mainAgentInfo.InvisInputVars.addAll(instIvars);
					}

				}else { // the inst module is not agent
					SMVModule instModule = (SMVModule) Env.getModule(inst_fullname);
					collect_agent_information_recur(agentInfo, instModule, inst.visible, null);
				}
			}else if (type.equals("SMVArrayVarInfo")) { // inst is an array of some type， could be multi-dimension array...
				SMVArrayVarInfo instArray = (SMVArrayVarInfo) inst;
				SMVAbstractElementInfo instArrayProto = instArray.proto;
				for (int i=instArray.from; i<=instArray.to; i++)
					collect_agent_information_recur(agentInfo, module, inst.visible, instArray.real_list[i-instArray.from]);
			}else {
				// Else: other types that can not expanded
				// deal with the instance

				// System.out.println(module.getFullInstName() + ": " +inst.toString());

				if (inst.category == ACTION_VAR) {
					if(module.getFullInstName().equals(agentInfo.name)) { //inst is the action variable of the agent
						if(module.getAction_variable()==null)
							throw new SMVParseException("Cannot find the action variable "+inst_fullname+".");
						agentInfo.VisLocalVars.add(module.getAction_variable());  // the action variable is visible by default
					} else { //inst is the action variable of a local module instance in the agent's module, this is not allowed
						throw new SMVParseException(inst_fullname +
								" is the action variable of a local module instance in the agent's module, this is not allowed.");
					}
				}else if (inst.category == INPUT_VAR) {
					Vector<ModuleBDDField> ivars = module.getAll_input_variables();
					boolean found = false; int i = 0;
					while(i<ivars.size()) {
						if (inst_fullname.equals(ivars.elementAt(i).getPath() + "." + ivars.elementAt(i).getName()))
						{ found = true; break; }
						i++;
					}
					if(!found) throw new SMVParseException("can not find out the field of input variable " + inst_fullname);

					if(module_visible && inst.visible)  // inst is visible
						agentInfo.VisInputVars.add(ivars.elementAt(i));
					else // inst is invisible
						agentInfo.InvisInputVars.add(ivars.elementAt(i));

				}else { // inst is a normal state variable
					ModuleBDDField[] vars = module.getAllFields() ;
					boolean found = false; int i = 0;
					while(i<vars.length) {
						if (inst_fullname.equals(vars[i].getPath() + "." + vars[i].getName()))
						{ found = true; break; }
						i++;
					}
					if(!found) throw new SMVParseException("can not find out the field of state variable " + inst_fullname);

					if(module_visible && inst.visible)  // inst is visible
						agentInfo.VisLocalVars.add(vars[i]);
					else // inst is invisible
						agentInfo.InvisLocalVars.add(vars[i]);

				}
			}
		}
	}

	private void collect_agent_information(String agent_fullname) throws SMVParseException { // agentName is with path "main."
		SMVAgentInfo agentInfo = new SMVAgentInfo(agent_fullname);
		SMVModule agentModule = (SMVModule) agentInfo.module;

		Env.getAll_agent_modules().put(agent_fullname, agentInfo);

		//(1) collect visible and invisible local variables of the agent
		collect_agent_information_recur(agentInfo, agentModule,true, null);

		//(2) collect visible parameters of the agent
		ModuleParamHolder[] params = agentModule.getAllParams();
		boolean[] params_visible_list = agentModule.getModuleInfo().arg_visible_list;
		if(params.length != params_visible_list.length) {
			throw new SMVParseException("There is some problem with the number of parameters of agent " + agent_fullname);
		}

		HashSet<ModuleBDDField> visible_actual_params = new HashSet<>(10);
		for (int i = 0; i < params_visible_list.length; i++) {
			// any actual parameter can not be one of the local variables of the agent
			if (agentInfo.VisLocalVars.contains(params[i].getVar()) || agentInfo.InvisLocalVars.contains(params[i].getVar())) {
				throw new SMVParseException("The actual parameter " +
						params[i].getVar().getPath()+"."+params[i].getVar().getName() +
						" is a local variable of agent " + agent_fullname + ". This is not allowed.");
			}

			if(params_visible_list[i]) { //params[i] is visible
				visible_actual_params.add(params[i].getVar());
			}
		}
		agentInfo.VisActualParams = visible_actual_params;
		// Till now the construction of agent_info finished


		System.out.println("-------------------------------------------------");
		System.out.println(agent_fullname + "'s visible variables:");
		String[] vnames = agentInfo.getVisVarNames();
		int i=1;
		for (String vname : vnames) {
			System.out.println( "\t" + (i++) + ". " + vname);
		}
		System.out.println(agent_fullname + "'s invisible variables:");
		HashSet<ModuleBDDField> ivs = new HashSet<ModuleBDDField>();
		if(agentInfo.InvisLocalVars!=null) ivs.addAll(agentInfo.InvisLocalVars);
		if(agentInfo.InvisInputVars!=null) ivs.addAll(agentInfo.InvisInputVars);
		i=1;
		for (ModuleBDDField v : ivs) {
			System.out.println( "\t" + (i++) + ". " + v.getPath()+"."+v.getName());
		}


	}


	public void walkthrough_main_module( // the main module must be NOT agent
			SMVModule module,				// initially it is the main module
			SMVAbstractElementInfo instance // an instance of the module, initially is null
	) throws SMVParseException {
		Vector<SMVAbstractElementInfo> insts;
		if(instance!=null) { // walk through the module from the inputted instance
			insts = new Vector<SMVAbstractElementInfo>();
			insts.add(instance);
		}else // instance==null
			insts = module.getModuleInfo().getSub_elements();

		for (SMVAbstractElementInfo inst : insts) {
			String type = inst.getClass().getName();
			int dot_idx = type.lastIndexOf('.');
			if (dot_idx != -1) type = type.substring(dot_idx + 1);

			String inst_fullname = module.getFullInstName() + "." + inst.name;
			if (type.equals("SMVProcVarInfo")) {  // inst is a module instance
				SMVProcVarInfo instProc = (SMVProcVarInfo) inst;
				if (instProc.proc_desc.isAgent)
					collect_agent_information(inst_fullname);
			}else if (type.equals("SMVArrayVarInfo")) { // inst is an array of some type， could be multi-dimension array...
				SMVArrayVarInfo instArray = (SMVArrayVarInfo) inst;
				SMVAbstractElementInfo instArrayProto = instArray.proto;
				for (int i=instArray.from; i<=instArray.to; i++)
					walkthrough_main_module(module, instArray.real_list[i-instArray.from]);
			}else {
				// Else: other types that can not expanded
				// deal with the instance

				// System.out.println(module.getFullInstName() + ": " +inst.toString());
			}
		}
	}

	// LXY: for MAS
	// this method constructs the information of all agent module instances Env.all_agent_modules.
	// It must be executed after Env.all_modules is constructed by the above methods
	public void mk_agent_modules() throws SMVParseException {
		if (this.proc_desc.isAgent) { // the main module is an agent, i.e. the environment of a MAS
			collect_agent_information("main");
		}else { // the main module is a normal module, not agent
			walkthrough_main_module((SMVModule) Env.getModule("main"), null);
		}
	}


	private void old_collect_agent_information_recur(String module_name, // the name of a module instance
												 boolean module_visible_for_its_holder,
												 SMVAgentInfo agent_info)
			throws SMVParseException{
		SMVModule module = (SMVModule) Env.getModule(module_name);

		// check each local variable of the module
		Vector<SMVAbstractElementInfo> module_lvars = module.getModuleInfo().getSub_elements();
		for (SMVAbstractElementInfo lvar : module_lvars) {
			// get the datatype of lvar
			String lvar_type = lvar.getClass().getName();
			int dot_idx = lvar_type.lastIndexOf('.');
			if(dot_idx!=-1)  lvar_type = lvar_type.substring(dot_idx+1);

			String lvar_fullname = module_name + "." + lvar.name;
			if(lvar_type.equals("SMVProcVarInfo")) {  // lvar is a module instance
				SMVProcVarInfo lvar_proc = (SMVProcVarInfo) lvar;
				if (lvar_proc.proc_desc.isAgent) {
					// the agent's local module instance lvar is also an agent, this is not allowed
					//System.out.println("The agent's local module instance " + lvar_fullname + " is also an agent, this is not allowed.");
					throw new SMVParseException("The agent's local module instance " + lvar_fullname + " is also an agent, this is not allowed.");
				}

				old_collect_agent_information_recur(lvar_fullname,
											module_visible_for_its_holder && lvar.visible,
												agent_info);

			} else if (lvar.category == ACTION_VAR) { //lvar is the action variable
				if(module_name.equals(agent_info.name)) { //lvar is the action variable of the agent
					// check the syntax of the action variable

					//agent_info.actVar = module.getAll_action_variables().get(lvar_fullname);
					ModuleBDDField actVar = Env.getVar(module_name, lvar.name);
					if(actVar==null)
						throw new SMVParseException("Cannot find the action variable "+lvar_fullname+".");
					agent_info.VisLocalVars.add(actVar);  // the action variable is visible by default

                } else { //lvar is the action variable of a local module instance in the agent's module, this is not allowed
                    throw new SMVParseException(lvar_fullname +
                            " is the action variable of a local module instance in the agent's module, this is not allowed.");
                }
			} else if (lvar_type.equals("SMVArrayVarInfo")) { // lvar is an array of some type
				SMVArrayVarInfo lvarArray = (SMVArrayVarInfo) lvar;
				String arrType = lvarArray.proto.getClass().getName();
				dot_idx = arrType.lastIndexOf('.');
				if(dot_idx!=-1)  arrType = arrType.substring(dot_idx+1);

				if(arrType.equals("SMVProcVarInfo")) {  // the array type is a Module or Agent
					SMVProcVarInfo elem_proc = (SMVProcVarInfo) lvarArray.proto;
					if (elem_proc.proc_desc.isAgent) { // elem_proc is an agent, elem_proc.name does not include path
						for(int i=lvarArray.from; i<=lvarArray.to; i++) {
							old_collect_agent_information("main." + elem_proc.name + "[" + i + "]");
						}
					}
				}


				// find the couple of lvar
				ModuleBDDField[] all_lvar_fields = module.getAllFields();
				boolean found = false;
				int i = 0;
				while(i<all_lvar_fields.length) {
					if (lvar_fullname.equals(all_lvar_fields[i].getPath() + "." + all_lvar_fields[i].getName())) {
						found = true;
						break;
					}
					i++;
				}
				if(!found) throw new SMVParseException("can not find out the field of local variable " + lvar_fullname);

				// collect the field of lvar_fullname
				if(module_visible_for_its_holder && lvar.visible) { // lvar is visible
					agent_info.VisLocalVars.add(all_lvar_fields[i]);
				}else{ // lvar is invisible
					agent_info.InvisLocalVars.add(all_lvar_fields[i]);
				}
			} else { // lvar is a normal variable (category==STATE_VAR)
				// find the couple of lvar
				ModuleBDDField[] all_lvar_fields = module.getAllFields();
				boolean found = false;
				int i = 0;
				while(i<all_lvar_fields.length) {
					if (lvar_fullname.equals(all_lvar_fields[i].getPath() + "." + all_lvar_fields[i].getName())) {
						found = true;
						break;
					}
					i++;
				}
				if(!found) throw new SMVParseException("can not find out the field of local variable " + lvar_fullname);

				// collect the field of lvar_fullname
				if(module_visible_for_its_holder && lvar.visible) { // lvar is visible
					agent_info.VisLocalVars.add(all_lvar_fields[i]);
				}else{ // lvar is invisible
					agent_info.InvisLocalVars.add(all_lvar_fields[i]);
				}
			}
		}

	}


	private void old_collect_agent_information(String agent_fullname) throws SMVParseException { // agentName is with path "main."
		SMVAgentInfo agent_info = new SMVAgentInfo(agent_fullname);
		SMVModule agent_module = (SMVModule) agent_info.module;

		//(1) collect visible and invisible local variables of the agent
		old_collect_agent_information_recur(agent_fullname, true, agent_info);

		//(2) collect visible parameters of the agent
		ModuleParamHolder[] params = agent_module.getAllParams();
		boolean[] params_visible_list = agent_module.getModuleInfo().arg_visible_list;
		if(params.length != params_visible_list.length) {
			throw new SMVParseException("There is some problem with the number of parameters of agent " + agent_fullname);
		}

		HashSet<ModuleBDDField> visible_actual_params = new HashSet<>(10);
		for (int i = 0; i < params_visible_list.length; i++) {
			// any actual parameter can not be one of the local variables of the agent
			if (agent_info.VisLocalVars.contains(params[i].getVar()) || agent_info.InvisLocalVars.contains(params[i].getVar())) {
				throw new SMVParseException("The actual parameter " +
						params[i].getVar().getPath()+"."+params[i].getVar().getName() +
						" is a local variable of agent " + agent_fullname + ". This is not allowed.");
			}

			if(params_visible_list[i]) { //params[i] is visible
				visible_actual_params.add(params[i].getVar());
			}
		}
		agent_info.VisActualParams = visible_actual_params;
		// Till now the construction of agent_info finished

		Env.getAll_agent_modules().put(agent_fullname, agent_info);

		System.out.println("-------------------------------------------------");
		System.out.println(agent_fullname + "'s visible variables:");
		String[] vnames = agent_info.getVisVarNames();
		int i=1;
		for (String vname : vnames) {
			System.out.println( "\t" + (i++) + ". " + vname);
		}

	}




	// LXY: for MAS
	// this method constructs the information of all agent module instances Env.all_agent_modules.
	// It must be executed after Env.all_modules is constructed by the above methods
	public void old_mk_agent_modules() throws SMVParseException {

		Vector<SMVAbstractElementInfo> main_insts = this.proc_desc.getSub_elements();
		for (SMVAbstractElementInfo main_inst : main_insts) {
			String main_inst_type;
			main_inst_type = main_inst.getClass().getName();
			int dot_idx = main_inst_type.lastIndexOf('.');
			if(dot_idx!=-1)  //found '.'
				main_inst_type = main_inst_type.substring(dot_idx+1);

			if(main_inst_type.equals("SMVProcVarInfo")) {  // the current main_inst is a Module or Agent
				SMVProcVarInfo main_inst_proc = (SMVProcVarInfo)main_inst;
				if(main_inst_proc.proc_desc.isAgent) { // main_inst is an agent, main_inst.name does not include path
					String agent_name = "main." + main_inst.name;
					old_collect_agent_information(agent_name);
				}

			}else if (main_inst_type.equals("SMVArrayVarInfo")) {
				SMVArrayVarInfo main_inst_array = (SMVArrayVarInfo) main_inst;
				String arrType = main_inst_array.proto.getClass().getName();
				dot_idx = arrType.lastIndexOf('.');
				if(dot_idx!=-1)  arrType = arrType.substring(dot_idx+1);

				if(arrType.equals("SMVProcVarInfo")) {  // the array type is a Module or Agent
					SMVProcVarInfo elem_proc = (SMVProcVarInfo) main_inst_array.proto;
					if (elem_proc.proc_desc.isAgent) { // elem_proc is an agent, elem_proc.name does not include path
						for(int i=main_inst_array.from; i<=main_inst_array.to; i++) {
							old_collect_agent_information("main." + elem_proc.name + "[" + i + "]");
						}
					}
				}

			}

		}


	}
}
