package base.hldd.visitors;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.FSMNode;
import base.hldd.structure.nodes.utils.Utility;
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
                Node[] successors = node.getSuccessors();
                Node newControlNode = new Node.Builder(absDepVariable).partedIndices(node.getPartedIndices()).successorsCount(successors.length).build();
                for (int condition = 0; condition < successors.length; condition++) {
                    Node successor = successors[condition];
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

        public void addContext(Node controlNode, int condition) throws Exception {
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
            private final int condition;


            public ContextItem(Node controlNode, int condition) {
                this.controlNode = controlNode;
                this.condition = condition;
            }

            /**
             * @param newNode node to fill the current context with
             * @throws Exception Causes: {@link Node#setSuccessor(int, base.hldd.structure.nodes.Node) cause }
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
                for (int index = 0; index < node.getSuccessors().length; index++) {
                    Node successor = node.getSuccessors()[index];
                    /* Traverse successors */
                    fsmContext.addContext(fsmNode.getSuccessors()[index], index);
                    resetTracker.trackCondition(node.getDependentVariable(), index);
                    successor.traverse(this);
                    resetTracker.dismissCurrentContext();
                    fsmContext.dismissCurrentContext();

                    //todo: dismiss contexts here! Always dismiss them at the place they are added,
                    //todo: in order not to lose the control.
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

        private void addControlNode(Node graphControlNode, Node fsmNode) throws Exception {
            /* Create new Control Node */
            Node newControlNode = new Node.Builder(graphControlNode.getDependentVariable())
                    .partedIndices(graphControlNode.getPartedIndices())
                    .successorsCount(graphControlNode.getSuccessors().length).build();

            FsmGraphCreator.FSMGraphMerger.FsmContext.Context currentContext = fsmContext.getCurrentContext();
            if (currentContext.isRootContext()) {
                fsmContext.dismissCurrentContext();/* Dismiss this fsmNode */
                /* Set new FSM current context (add new Control Node to fsmContext, since it's being processed from this point forward) */
                fsmContext.addRootContext(newControlNode);
            } else {
                /* In the parent of this fsmNode, replace fsmNode with new Control Node */
                int fsmParentCondition = currentContext.nodeSourceCondition;
                fsmContext.dismissCurrentContext();/* Dismiss this fsmNode */
                Node fsmParent = fsmContext.getCurrentContext().fsmNode;
                fsmParent.setSuccessor(fsmParentCondition, newControlNode);
                /* Replace FSM current context (add new Control Node to fsmContext, since it's being processed from this point forward) */
                fsmContext.addContext(newControlNode, fsmParentCondition);
            }
            /* Fill new Control Node with clones of fsmNode */
            for (int index = 0; index < graphControlNode.getSuccessors().length; index++) {
                /* Create a clone of fsmNode */
                Node fsmTerminalClone = Utility.clone(fsmNode); //todo: why naming fsmTerminalClone???
                /* Fill new Control Node with the clone */
                newControlNode.setSuccessor(index, fsmTerminalClone);
                /* Traverse the new successor of new Control Node */
                fsmContext.addContext(fsmTerminalClone, index);
                resetTracker.trackCondition(graphControlNode.getDependentVariable(), index);
                graphControlNode.getSuccessors()[index].traverse(this);
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
            public void addContext(Node fsmNode, int nodeSourceCondition) {
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
                private final static int ROOT_CONTEXT_CONDITION = -1;
                private final Node fsmNode;
                private final int nodeSourceCondition;

                public Context(Node fsmNode, int nodeSourceCondition) {
                    this.fsmNode = fsmNode;
                    this.nodeSourceCondition = nodeSourceCondition;
                }

                public Context(Node fsmNode) {
                    this(fsmNode, ROOT_CONTEXT_CONDITION);
                }

                public boolean isRootContext() {
                    return nodeSourceCondition == ROOT_CONTEXT_CONDITION;
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

            public void trackCondition(AbstractVariable parentConditionVar, int parentCondition) {
                contextStack.push(new Context(parentConditionVar, parentCondition));
            }

            public boolean isResetingTerminalNode() {
                if (contextStack.isEmpty()) return false;
                Context currentContext = contextStack.peek();
                return currentContext.parentConditionVariable.isReset() && currentContext.parentCondition == 1;
            }

            public void dismissCurrentContext() {
                contextStack.pop();
            }


            private class Context {
                private final AbstractVariable parentConditionVariable;
                private final int parentCondition;

                public Context(AbstractVariable parentConditionVar, int parentCondition) {
                    this.parentConditionVariable = parentConditionVar;
                    this.parentCondition = parentCondition;
                }
            }
        }
    }
}
