package base.vhdl.processors;

import base.vhdl.structure.nodes.*;

import java.util.List;
import java.util.LinkedList;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 08.10.2008
 * <br>Time: 23:25:07
 */
public class NodeReplacerImpl extends AbstractProcessor {
    private final AbstractNode nodeToReplace;
    private final AbstractNode replacingNode;

    private boolean isReplaced = false;

    public NodeReplacerImpl(AbstractNode nodeToReplace, AbstractNode replacingNode) {
        this.nodeToReplace = nodeToReplace;
        this.replacingNode = replacingNode;
    }

    public void processIfNode(IfNode ifNode) {
        /* At first, try to replace in True Part. If don't success, then try to replace in False Part. */
        ifNode.getTruePart().process(this);
        if (!isReplaced) {
            if (ifNode.getFalsePart() != null) {
                ifNode.getFalsePart().process(this);
            }
        }
    }

    public void processCompositeNode(CompositeNode compositeNode) {
        List<AbstractNode> oldChildren = compositeNode.getChildren();
        AbstractNode parentNode = compositeNode.getParentNode();
        if (oldChildren.contains(nodeToReplace)) {

            List<AbstractNode> newChildren = new LinkedList<AbstractNode>();

            for (AbstractNode oldChild : oldChildren) {
                if (oldChild == nodeToReplace) {
                    if (replacingNode instanceof CompositeNode) {
                        /* Merge the contents of replacing CompositeNode with the new children being created */
                        for (AbstractNode replChild : ((CompositeNode) replacingNode).getChildren()) {
                            replChild.setParent(parentNode);
                            newChildren.add(replChild);
                        }
                    } else {
                        replacingNode.setParent(parentNode);
                        newChildren.add(replacingNode);
                    }
                    isReplaced = true;
                } else {
                    newChildren.add(oldChild);
                }
            }

            compositeNode.setChildren(newChildren);
        }
    }

    public void processCaseNode(CaseNode caseNode) {
        for (WhenNode whenNode : caseNode.getConditions()) {
            whenNode.process(this);
        }
    }

    public void processWhenNode(WhenNode whenNode) {
        whenNode.getTransitions().process(this);        
    }

    public void processTransitionNode(TransitionNode transitionNode) throws Exception {
        throw new Exception("TerminalNode currently cannot be replaced for another node.");
    }
}
