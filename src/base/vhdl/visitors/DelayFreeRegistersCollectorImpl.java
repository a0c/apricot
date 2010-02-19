package base.vhdl.visitors;

import base.vhdl.structure.*;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.WhenNode;

/**
 * todo: ......
 * 
 * <br><br>User: Anton Chepurov
 * <br>Date: 10.10.2008
 * <br>Time: 13:01:48
 */
public class DelayFreeRegistersCollectorImpl extends AbstractVisitor {
    private boolean isWithinClockNode = false;

    /* Here only request processing of AbstractNodes(ParseTree) */
    public void visitEntity(Entity entity) throws Exception {}

    public void visitArchitecture(Architecture architecture) throws Exception {}

    public void visitProcess(base.vhdl.structure.Process process) throws Exception {
        process.getRootNode().traverse(this);
    }

    public void visitIfNode(IfNode ifNode) throws Exception {
        if (ClockEventRemover.isClockingExpression(ifNode.getConditionExpression())) {
            isWithinClockNode = true;

        }
    }

    public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void visitCaseNode(CaseNode caseNode) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void visitWhenNode(WhenNode whenNode) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
