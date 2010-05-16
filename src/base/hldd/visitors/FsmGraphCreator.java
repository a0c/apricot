package base.hldd.visitors;

import base.HLDDException;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.FSMNode;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.nodes.fsm.Transitions;
import base.hldd.structure.variables.GraphVariable;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.ConstantVariable;

import java.util.Stack;

import parsers.Beh2RtlTransformer.ControlPartManager;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 15.02.2008
 * <br>Time: 12:12:46
 */
public class FsmGraphCreator implements HLDDVisitor {
    private Context context;
    private GraphVariable graphVariable;

    private ControlPartManager controlPartManager;

    public FsmGraphCreator(ControlPartManager controlPartManager) {
        this.controlPartManager = controlPartManager;
        context = new Context();
    }

    public void visitNode(Node node) throws Exception {

        /* Process STATE variable differently from all others GraphVariables */
        if (graphVariable.isState()) {
            AbstractVariable absDepVariable = node.getDependentVariable();
            if (node.isTerminalNode()) {
                /* Create a new Terminal Node (FSMNode) */
                if (absDepVariable instanceof ConstantVariable) {
                    ConstantVariable constDepVariable = (ConstantVariable) absDepVariable;
                    Transitions newTransitions = controlPartManager.createTransition();
                    controlPartManager.insertTransition(newTransitions, graphVariable, constDepVariable.getValue().intValue());
                    context.fillCurrentContext(new FSMNode(newTransitions));

                } else {
                    /* Create a stub Terminal Node (FSMNode) */
                    context.fillCurrentContext(new FSMNode(controlPartManager.createTransition()));
//                    /* Ignore current context */
//                    context.dismissCurrentContext();
                }

            } else {
                /* Create a new Control Node (Node) */
                int conditionsCount = node.getConditionsCount();
				Node newControlNode = new Node.Builder(absDepVariable).partedIndices(node.getPartedIndices()).createSuccessors(node.getConditionValuesCount()).build();
                for (int idx = 0; idx < conditionsCount; idx++) {
					Condition condition = node.getCondition(idx);
					Node successor = node.getSuccessor(condition);
                    /* Create new Context and hash it */
                    context.addContext(newControlNode, condition);
                    successor.traverse(this);
                }
                context.fillCurrentContext(newControlNode);
            }
        }
    }

    public void visitGraphVariable(GraphVariable graphVariable) throws Exception {
        /* Remember graphVariable to:
        * 1) derive a place for transitions to be inserted into
        * 2) traverse STATE graph differently from all other graphs
        * 3) extract the graphRootNode during graph merging */
        this.graphVariable = graphVariable;
        if (graphVariable.isState()) {
            graphVariable.getGraph().getRootNode().traverse(this);
//            getRootNode().trimTerminals(0);
        } else {
            graphVariable.traverse(new FSMGraphMerger(graphVariable));
//            graphVariable.getGraph().getRootNode().traverse(new FSMGraphMerger());
            /* todo: trim? */
        }
    }

    public Node getRootNode() {
        return context.getRootNode();
    }

    private class Context {
        private Stack<ContextItem> contextStack;
        private Node rootNode;

        public Context() {
            contextStack = new Stack<ContextItem>();
            rootNode = null;
        }

        public void addContext(Node controlNode, Condition condition) throws Exception {
            if (controlNode.isTerminalNode()) {
                throw new Exception("Terminal node is being hashed as a CONTEXT: " + controlNode +
                        "\nOnly Control Nodes and their condition can be hashed as a context.");
            }
            contextStack.add(new ContextItem(controlNode, condition));
        }

        /**
         * @param newNode node to fill the current context with
         * @throws Exception {@link ContextItem#fillContext(base.hldd.structure.nodes.Node)}.
         */
        public void fillCurrentContext(Node newNode) throws Exception {
            if (contextStack.isEmpty()) {
                rootNode = newNode;
//                if (rootNode == null) {
//                } else throw new Exception("Root node is being RESET in the Context:" +
//                        "\nCurrent root node: " + rootNode +
//                        "\nResetting (new) root node: " + newNode);
            } else {
                ContextItem currentContext = contextStack.pop();
                currentContext.fillContext(newNode);
            }
        }

//        public void dismissCurrentContext() {
//            contextStack.pop();
//        }

        public Node getRootNode() {
            return rootNode;
        }

        private class ContextItem {
            private final Node controlNode;
            private final Condition condition;


            public ContextItem(Node controlNode, Condition condition) {
                this.controlNode = controlNode;
                this.condition = condition;
            }

            /**
             * @param newNode node to fill the current context with
             * @throws Exception Causes: {@link Node#setSuccessor(Condition, base.hldd.structure.nodes.Node) cause }
             */
            public void fillContext(Node newNode) throws Exception {
                controlNode.setSuccessor(condition, newNode);
            }
        }
    }

    private class FSMGraphMerger implements HLDDVisitor {
        /* todo: NB! Both FsmContext and ResetTracker could be completely removed,
         * if the following two fields would've been added to the Node class:
          * 1) a link to the parent Node of this Node
          * 2) condition of the parent node that leads to this Node
          * todo: Actually, only ResetTracker can be removed... really?? */
        /* Current context of FSM Graph traversal */
        private FsmContext fsmContext;
        /* Current context of GraphVariable traversal */
        private ResetTracker resetTracker;
        private final GraphVariable graphVariable; // todo: can be removed from here, since the upper graphVariable is accessible from inner classes

        public FSMGraphMerger(GraphVariable graphVariable) {
            this.graphVariable = graphVariable;
            fsmContext = new FsmContext();
            resetTracker = new ResetTracker();
        }

        public void visitGraphVariable(GraphVariable graphVariable) throws Exception {

            /* Collect FSMNode transitions from all the GRAPHS into FSM Graph
            * and add additional conditions to FSM Graph if needed: */
            fsmContext.addRootContext(getRootNode());
            graphVariable.getGraph().getRootNode().traverse(this);

        }        

        public void visitNode(Node node) throws Exception {
            Node fsmNode = fsmContext.getCurrentContext().fsmNode;

            if (areControlNodesIdentical(node)) {
				/* Both nodes are CONTROL nodes with the SAME CONTROL VARIABLE */
				if (node.hasIdenticalConditionsWith(fsmNode)) { // try to benefit from complex Conditions
					int conditionsCount = node.getConditionsCount();
					for (int idx = 0; idx < conditionsCount; idx++) {
						Condition condition = node.getCondition(idx);
						Node successor = node.getSuccessor(condition);
						/* Traverse successors */
						traverseSuccessors(successor, fsmNode.getSuccessor(condition), condition, node);
					}
				} else { // no benefit from complex Conditions. process value-by-value.
					int conditionValuesCount = node.getConditionValuesCount();
					for (int idx = 0; idx < conditionValuesCount; idx++) {
						Condition condition = Condition.createCondition(idx);
						Node successor = node.getSuccessorInternal(condition);
						/* Decompact successors */
						fsmNode.decompact(condition);
						/* Traverse successors */
						traverseSuccessors(successor, fsmNode.getSuccessor(condition), condition, node); // now that fsmNode has been decompacted fsmNode.getSuccessor() can be used instead of fsmNode.getSuccessorInternal()
					}
				}
			} else {

                if (node.isTerminalNode() && fsmNode.isTerminalNode()) {
                    /* Both nodes are TERMINAL */

                    /* Insert Control Part Output into Existing FSM transitions */
                    mergeControlPartOutput(node, fsmNode); // todo: call propagateControlPartOutput(node, fsmNode) and replace this and the next conditions with single " if (node.isTerminalNode())" 

                } else if (node.isTerminalNode() && fsmNode.isControlNode()) {
                    /* Graph node is TERMINAL, EXISTING node is CONTROL */

                    /* Propagate Control Part Output amongst all the CONTROL NODE SUCCESSORS */
                    for (Node successor : fsmNode.getSuccessors()) {
                        propagateControlPartOutput(node, successor);
                    }

                } else if (node.isControlNode() && fsmNode.isTerminalNode()) {
                    /* Graph node is CONTROL, EXISTING node is TERMINAL */

                    /* Insert new CONTROL NODE into EXIST, propagate EXISTING node amongst all the REG CONTROL NODE SUCCESSORS:*/
                    addControlNode(node, fsmNode); // todo: replace this and the next conditions with single " if (node.isControlNode())" 

                } else {
                    /* Both nodes are CONTROL, but different CONTROL VARIABLES (dependent variables) */

                    /* Traverse GraphNode for every successor of FSMNode */
                    addControlNode(node, fsmNode);
//                    for (int index = 0; index < fsmNode.getSuccessors().length; index++) {
//                        Node fsmSuccessor = fsmNode.getSuccessors()[index];
//                        fsmContext.addContext(fsmSuccessor, index);
//                        node.traverse(this);
//                    }
                }

            }

        }

		private void traverseSuccessors(Node successor, Node fsmSuccessor, Condition condition, Node parentNode) throws Exception {
			fsmContext.addContext(fsmSuccessor, condition);
			resetTracker.trackCondition(parentNode.getDependentVariable(), condition);
			successor.traverse(this);
			resetTracker.dismissCurrentContext();
			fsmContext.dismissCurrentContext();
			// dismiss contexts here! Always dismiss them at the place they are added,
			// in order not to lose the control.
		}

		private void addControlNode(Node graphControlNode, Node fsmNode) throws Exception {
            /* Create new Control Node */
            Node newControlNode = new Node.Builder(graphControlNode.getDependentVariable())
                    .partedIndices(graphControlNode.getPartedIndices())
					.createSuccessors(graphControlNode.getConditionValuesCount()).build();

            FsmGraphCreator.FSMGraphMerger.FsmContext.Context currentContext = fsmContext.getCurrentContext();
            if (currentContext.isRootContext()) {
                fsmContext.dismissCurrentContext();/* Dismiss this fsmNode */
                /* Set new FSM current context (add new Control Node to fsmContext, since it's being processed from this point forward) */
                fsmContext.addRootContext(newControlNode);
            } else {
                /* In the parent of this fsmNode, replace fsmNode with new Control Node */
                Condition fsmParentCondition = currentContext.nodeSourceCondition;
                fsmContext.dismissCurrentContext();/* Dismiss this fsmNode */
                Node fsmParent = fsmContext.getCurrentContext().fsmNode;
                fsmParent.setSuccessor(fsmParentCondition, newControlNode);
                /* Replace FSM current context (add new Control Node to fsmContext, since it's being processed from this point forward) */
                fsmContext.addContext(newControlNode, fsmParentCondition);
            }
            /* Fill new Control Node with clones of fsmNode */
			int conditionsCount = graphControlNode.getConditionsCount();
			for (int idx = 0; idx < conditionsCount; idx++) {
				Condition condition = graphControlNode.getCondition(idx);
				/* Create a clone of fsmNode */
                Node fsmTerminalClone = Node.clone(fsmNode); //todo: why naming fsmTerminalClone???
                /* Fill new Control Node with the clone */
                newControlNode.setSuccessor(condition, fsmTerminalClone);
                /* Traverse the new successor of new Control Node */
                fsmContext.addContext(fsmTerminalClone, condition);
                resetTracker.trackCondition(graphControlNode.getDependentVariable(), condition);
                graphControlNode.getSuccessor(condition).traverse(this);
                resetTracker.dismissCurrentContext();
                fsmContext.dismissCurrentContext();

            }

        }

        private void propagateControlPartOutput(Node graphTerminalNode, Node fsmNode) throws Exception {
            if (fsmNode == null) return;
            if (fsmNode.isControlNode()) {
                for (Node successor : fsmNode.getSuccessors()) {
                    propagateControlPartOutput(graphTerminalNode, successor);
                }
            } else {
                mergeControlPartOutput(graphTerminalNode, fsmNode);
            }
        }

        private void mergeControlPartOutput(Node node, Node fsmNode) throws Exception {

            Transitions transitions = ((FSMNode) fsmNode).getTransitions();
            controlPartManager.insertTransition(transitions, node, graphVariable, resetTracker.isResetingTerminalNode());

        }

        private boolean areControlNodesIdentical(Node graphNode) {
            return fsmContext.getCurrentContext().fsmNode.isControlNode() && graphNode.isControlNode()
                    && fsmContext.getCurrentContext().fsmNode.getDependentVariable() == graphNode.getDependentVariable();
        }

		private class FsmContext {
            private Stack<Context> contextStack;

            public FsmContext() {
                contextStack = new Stack<Context>();
            }

            /**
             *
             * @param fsmNode non-root node of FsmGraph currently under consideration
             * @param nodeSourceCondition nodeSourceCondition value of parent node, through which <code>fsmNode</code>
             *                  is accessible from its parent. Condition is used to replace <code>fsmNode</code> in its
             *                  parent with other nodes (method {@link FSMGraphMerger#addControlNode(Node, Node)}).
             *
             */
            public void addContext(Node fsmNode, Condition nodeSourceCondition) {
                contextStack.push(new Context(fsmNode, nodeSourceCondition));
            }

            /**
             * @param fsmNode rootNode of FsmGraph currently under consideration
             */
            public void addRootContext(Node fsmNode) {
                contextStack.push(new Context(fsmNode));
                try {
                    context.fillCurrentContext(fsmNode);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public Context getCurrentContext() {
                return contextStack.isEmpty() ? null : contextStack.peek();
            }

            public void dismissCurrentContext() {
                contextStack.pop();
            }
            private class Context {
                private final Node fsmNode;
                private final Condition nodeSourceCondition;

                public Context(Node fsmNode, Condition nodeSourceCondition) {
                    this.fsmNode = fsmNode;
                    this.nodeSourceCondition = nodeSourceCondition;
                }

                public Context(Node fsmNode) {
                    this(fsmNode, null);
                }

                public boolean isRootContext() {
                    return nodeSourceCondition == null;
                }
            }
        }

        /**
         * Tracks RESET in the Graph
         */
        private class ResetTracker {
            private Stack<Context> contextStack;

            public ResetTracker() {
                contextStack = new Stack<Context>();
            }

            public void trackCondition(AbstractVariable parentConditionVar, Condition parentCondition) {
                contextStack.push(new Context(parentConditionVar, parentCondition));
            }

            public boolean isResetingTerminalNode() throws HLDDException {
                if (contextStack.isEmpty()) return false;
                Context currentContext = contextStack.peek();
                return currentContext.parentConditionVariable.isReset() && currentContext.parentCondition.getValue() == 1;
            }

            public void dismissCurrentContext() {
                contextStack.pop();
            }


            private class Context {
                private final AbstractVariable parentConditionVariable;
                private final Condition parentCondition;

                public Context(AbstractVariable parentConditionVar, Condition parentCondition) {
                    this.parentConditionVariable = parentConditionVar;
                    this.parentCondition = parentCondition;
                }
            }
        }
    }
}
