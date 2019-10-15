package edu.wis.jtlv.lib.mc.RTLTLK;

import edu.wis.jtlv.env.spec.Spec;
import edu.wis.jtlv.lib.mc.LTL.LTLTester;
import edu.wis.jtlv.lib.mc.ModelCheckAlgException;

public class RTLTLKTester extends LTLTester {
    /**
     * <p>
     * Constructor for the tester.
     * </p>
     *
     * @param root_spec The specification to construct tester for.
     * @param isWeak    Whether to add initial or not.
     * @throws ModelCheckAlgException
     */
    public RTLTLKTester(Spec root_spec, boolean isWeak) throws ModelCheckAlgException {
        super(root_spec, isWeak);
    }

}
