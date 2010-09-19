package base.vhdl.visitors;

import base.vhdl.structure.Entity;
import base.vhdl.structure.Architecture;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.*;

import java.util.regex.Pattern;


/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 12.02.2008
 * <br>Time: 9:50:47
 */
public abstract class AbstractVisitor {
	private static final Pattern CLOCK_PATTERN = Pattern.compile(".*((CLOCK)|(CLK)).*", Pattern.CASE_INSENSITIVE);
	private static final Pattern RESET_PATTERN = Pattern.compile(".*RESET.*", Pattern.CASE_INSENSITIVE);

	/* Here only request processing of AbstractNodes(ParseTree) */
    public abstract void visitEntity(Entity entity) throws Exception;

    public abstract void visitArchitecture(Architecture architecture) throws Exception;

    public abstract void visitProcess(Process process) throws Exception;

    public abstract void visitIfNode(IfNode ifNode) throws Exception;

    public abstract void visitTransitionNode(TransitionNode transitionNode) throws Exception;

    public abstract void visitCaseNode(CaseNode caseNode) throws Exception;

    public abstract void visitWhenNode(WhenNode whenNode) throws Exception;

	/**
     * todo: see {@link ui.ConfigurationHandler#isStateName(String)}
     */
    @Deprecated
    static boolean isClockName(String varName) {
        return CLOCK_PATTERN.matcher(varName).matches(); /*varName.equalsIgnoreCase("CLOCK") || varName.equalsIgnoreCase("CLK");*/
    }

	static boolean isResetName(String operandName) {
        return RESET_PATTERN.matcher(operandName).matches();
    }
}