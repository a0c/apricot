package base.vhdl.processors;

import base.vhdl.structure.nodes.*;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractProcessor {

	public abstract void processIfNode(IfNode ifNode);

	public abstract void processCompositeNode(CompositeNode compositeNode);

	public abstract void processCaseNode(CaseNode caseNode);

	public abstract void processWhenNode(WhenNode whenNode);

	public abstract void processTransitionNode(TransitionNode transitionNode) throws Exception;
}
