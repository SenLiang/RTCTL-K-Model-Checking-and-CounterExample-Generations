package edu.wis.jtlv.lib;

import edu.wis.jtlv.env.Env;
import net.sf.javabdd.BDD;

/**
 * <p>
 * BDD array results (Path).
 * </p>
 * 
 * @version {@value edu.wis.jtlv.env.Env#version}
 * @author yaniv sa'ar.
 * 
 */
public class AlgResultPath implements AlgResultI {
	private ResultStatus stat;
	private BDD[] result;

	/**
	 * <p>
	 * A constructor for a path results, with a general flag is was
	 * successful.
	 * </p>
	 * 
	 * @param was_successful
	 *            Did the algorithm succeed.
	 * @param res
	 *            The results themselves.
	 */
	public AlgResultPath(boolean was_successful, BDD[] res) {
		this.result = res;
		this.stat = was_successful ? ResultStatus.succeed : ResultStatus.failed;
	}

	/**
	 * <p>
	 * A constructor for a path results, with a general flag is was
	 * successful.
	 * </p>
	 * 
	 * @param fin_status
	 *            The algorithm finishing status.
	 * @param res
	 *            The results themselves.
	 */
	public AlgResultPath(ResultStatus fin_status, BDD[] res) {
		this.result = res;
		this.stat = fin_status;
	}

	/**
	 * <p>
	 * A constructor for a path results, without successfulness status.
	 * </p>
	 * 
	 * @param res
	 *            The results themselves.
	 */
	public AlgResultPath(BDD[] res) {
		this.result = res;
		this.stat = ResultStatus.undecided;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.lib.AlgResultI#getResultStat()
	 */
	@Override
	public ResultStatus getResultStat() {
		return stat;
	}

	/**
	 * <p>
	 * A BDD[] object representation of the results.
	 * </p>
	 * 
	 * @return A BDD[] object representing the results.
	 */
	@Override
	public Object resultObject() {
		return this.result;
	}

	/* (non-Javadoc)
	 * @see edu.wis.jtlv.lib.AlgResultI#resultString()
	 */
	//@Override
	public String Prime_resultString() {
		String res = "";
		res += "   Counter Example \n";
		res += "=====================\n";
		for (int i = 0; i < this.result.length; i++) {
			if(this.result[i]==null) break;
			res += "[" + i + "] " + Env.toNiceString(this.result[i]);
			res += ((i == (this.result.length - 1)) ? "" : "\n");
		}
		return res;
	}


	public String resultString() {
		String res ="";
		if ((this.result == null) || (this.result.length == 0))
			return res + "No Counter Example Exists.";
		res += " Counter Example\n";
		res += "=================\n";
		// the last is not printed. It is only to point to the cycle.
		BDD last = this.result[this.result.length - 1];
		int loop_index = -1;
		boolean loop_exists = false;
		for (int i = 0; i < this.result.length - 1; i++) {
			boolean loop_here = this.result[i].biimp(last).isOne();
			if ((loop_here) && (loop_index == -1)) {
				loop_index = i + 1;
				res += "[[" + (i + 1) + "]]";
				loop_exists = true;
			} else {
				res += "  " + (i + 1) + "  ";
			}
			res += " \t: " + this.result[i].toString() + "\n";
		}
		if (loop_exists)
			res += "Loop back to state " + loop_index;
		else
			res += "  " + this.result.length + "   \t: "
					+ this.result[this.result.length - 1].toString() + "\n";

		return res;
	}
}
