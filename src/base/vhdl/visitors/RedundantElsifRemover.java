package base.vhdl.visitors;

import base.vhdl.structure.Entity;
import base.vhdl.structure.*;
import base.vhdl.structure.nodes.*;

/**
 * Replaces all ELSIFs with ELSEs, if possible.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 12.02.2008
 * <br>Time: 15:18:26
 */
public class RedundantElsifRemover extends AbstractVisitor {

    /* Here only request processing of AbstractNodes(ParseTree) */
    public void visitEntity(Entity entity) throws Exception {}

    public void visitArchitecture(Architecture architecture) throws Exception {}

    public void visitProcess(base.vhdl.structure.Process process) throws Exception {
        process.getRootNode().traverse(this);
    }

    public void visitIfNode(IfNode ifNode) throws Exception {
        Expression condition = ifNode.getConditionExpression();

        /*##########################################
        *    P r o c e s s    T R U E   P A R T
        * ##########################################*/
        ifNode.getTruePart().traverse(this);

        /*##########################################
        *    P r o c e s s    F A L S E   P A R T
        *           and       T H I S   N O D E
        * ##########################################*/
        if (ifNode.getFalsePart() != null) {
            CompositeNode falsePart = ifNode.getFalsePart();
            /* Traverse FALSE part */
            falsePart.traverse(this);

            /* Substitute ELSIF with ELSE */
            if (falsePart.getChildren().size() == 1) {
                /* Substitute FALSE part */
                AbstractNode firstChild = falsePart.getChildren().get(0);
                if (firstChild instanceof IfNode) {
                    if (condition.isInverseOf(((IfNode) firstChild).getConditionExpression())) {
                        /* Set new FALSE part */
                        ifNode.setFalsePart(((IfNode) firstChild).getTruePart(), ifNode);
                        /* NB! Obviously, child doesn't contain falsePart at all */
                    }
                }
            }
        }

    }

    public void visitTransitionNode(TransitionNode transitionNode) throws Exception {}

    public void visitCaseNode(CaseNode caseNode) throws Exception {
        for (WhenNode whenNode : caseNode.getConditions()) {
            whenNode.traverse(this);
        }
    }

    public void visitWhenNode(WhenNode whenNode) throws Exception {
        whenNode.getTransitions().traverse(this);
    }
}
