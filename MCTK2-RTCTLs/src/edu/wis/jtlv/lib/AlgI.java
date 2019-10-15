package edu.wis.jtlv.lib;

import edu.wis.jtlv.env.core.smv.SMVParseException;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.spec.SpecException;
import edu.wis.jtlv.old_lib.mc.ModelCheckException;
import org.graphstream.graph.Graph;

import javax.swing.*;

/**
 * <p>
 * Implement your algorithm, use or ignore what ever phases that you'd like.
 * </p>
 * 
 * @version {@value edu.wis.jtlv.env.Env#version}
 * @author yaniv sa'ar.
 * 
 */
public interface AlgI {

	/**
	 * <p>
	 * Pre algorithm phase.
	 * </p>
	 * 
	 * @return Any kind of results the algorithm choose to.
	 * @throws AlgExceptionI
	 *             Wherever the algorithm implementor choose to.
	 */
	public AlgResultI preAlgorithm() throws AlgExceptionI, SMVParseException, ModelCheckException, ModuleException;

	/**
	 * <p>
	 * The main algorithm phase.
	 * </p>
	 * 
	 * @return Any kind of results the algorithm choose to.
	 * @throws AlgExceptionI
	 *             Wherever the algorithm implementor choose to.
	 */
	public AlgResultI doAlgorithm() throws AlgExceptionI, ModelCheckException, ModuleException, SMVParseException, SpecException;
	/**
	 * <p>
	 * Post algorithm phase.
	 * </p>
	 * 
	 * @return Any kind of results the algorithm choose to.
	 * @throws AlgExceptionI
	 *             Wherever the algorithm implementor choose to.
	 */
	public AlgResultI postAlgorithm() throws AlgExceptionI;

}
