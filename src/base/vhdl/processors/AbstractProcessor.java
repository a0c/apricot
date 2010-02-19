package base.vhdl.processors;

import base.vhdl.structure.nodes.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 08.10.2008
 * <br>Time: 23:20:29
 */
public abstract class AbstractProcessor {

    public abstract void processIfNode(IfNode ifNode);

    public abstract void processCompositeNode(CompositeNode compositeNode);

    public abstract void processCaseNode(CaseNode caseNode);

    public abstract void processWhenNode(WhenNode whenNode);

    public abstract void processTransitionNode(TransitionNode transitionNode) throws Exception;
}
