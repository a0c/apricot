package parsers.psl;

import static base.hldd.structure.models.utils.ModelManager.adjustBooleanCondition;

import base.hldd.structure.variables.*;
import base.hldd.structure.models.utils.VariableManager;
import base.hldd.structure.models.utils.PartedVariableHolder;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.TemporalNode;
import base.hldd.structure.Flags;
import base.psl.structure.*;
import base.vhdl.structure.AbstractOperand;
import base.Type;
import base.Indices;

import java.util.*;
import java.util.logging.Logger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.11.2008
 * <br>Time: 13:36:56
 */
public class ModelManager {
    private static final Logger LOG = Logger.getLogger(ModelManager.class.getName());
    private final base.hldd.structure.models.utils.ModelManager hlddModelManager;
    private VariableManager variableManager = new VariableManager();
    private ContextManager contextManager = new ContextManager();
    /* Current property data */
    private String currentPropertyName;
    /* Data common to all properties */
    private StringBuilder commentLine = new StringBuilder();

    public ModelManager(base.hldd.structure.models.utils.ModelManager hlddModelManager) {
        this.hlddModelManager = hlddModelManager;
    }

    public VariableManager getVariableManager() {
        return variableManager;
    }

    public String getComment() {
        return commentLine == null ? "" : commentLine.toString();
    }
    
    public void initNewPropertyGraph() {
        currentPropertyName = null;
        contextManager.initNewPropertyGraph();
    }

    public void finalizePropertyGraph() throws Exception {
        LOG.info("Creating property GRAPHs...");
        Variable baseVariable = new Variable(currentPropertyName + contextManager.globalWindowToString(), new Type(new Indices(1, 0)), new Flags().setOutput(true));
        variableManager.addVariable(new GraphVariable(baseVariable, contextManager.getCurrentContext().getNode(0)));
    }

    public void setCurrentPropertyName(String currentPropertyName) {
        this.currentPropertyName = currentPropertyName;
    }

    public void addComment(String comment) {
        commentLine.append(comment).append("\n");
    }

    public void initNewPPGContext(Range window, String[] windowPlaceholders) {
        contextManager.initNewPPGContext(window, windowPlaceholders);
    }

    public void dismissCurrentPPGContext() {
        contextManager.dismissCurrentContext();
    }

    /**
     * Adds both the specified node to the current context and the node's dependent variable to the variable manager.
     * If the dependent variable is a {@link FunctionVariable}, then all its operands are added recursively as well.  
     * @param relativeIndex
     * @param node
     * @throws Exception {@link VariableManager#addVariable(AbstractVariable)}!!! todo: remove this exception! this no longer holds!!!
     */
    public void addNode(int relativeIndex, Node node) throws Exception {
        AbstractVariable dependentVariable = node.getDependentVariable();
        /* For GraphVariables, set output flag to FALSE and input flag to TRUE,
         * since they must act as INPUTS in TGM files (AbstractModelCreator will index inputs first.). */
        if (dependentVariable instanceof GraphVariable) {
            node.setDependentVariable(((GraphVariable) dependentVariable).getBaseVariable());
        }
        contextManager.addNodeToCurrentContext(relativeIndex, node);
        addVariables(dependentVariable);
    }

    private void addVariables(AbstractVariable dependentVariable) throws Exception {
        /* For GraphVariables, set output flag to FALSE and input flag to TRUE,
         * since they must act as INPUTS in TGM files (AbstractModelCreator will index inputs first.). */
        if (dependentVariable instanceof GraphVariable) {
            dependentVariable = ((GraphVariable) dependentVariable).getBaseVariable();
            ((Variable) dependentVariable).setOutput(false);
            ((Variable) dependentVariable).setInput(true);
//            node.setDependentVariable(dependentVariable);
        }
        if (dependentVariable instanceof FunctionVariable) {
            /* For FunctionVariable, add all its operands and the FunctionVariable itself */
            FunctionVariable functionVariable = (FunctionVariable) dependentVariable;
            for (PartedVariableHolder operandHolder : functionVariable.getOperands()) {
                addVariables(operandHolder.getVariable());
            }
            variableManager.addVariable(dependentVariable);
        } else {
            variableManager.addVariable(dependentVariable);
        }
    }

    public Node replaceOperandNodeWithExpression(Node operandNodeToReplace, AbstractExpression replacingExpression) throws Exception {

        /* Adjust GLOBAL WINDOW */
        contextManager.adjustWindow();

        if (replacingExpression instanceof OperandImpl) {

            /* Create new CONTROL NODE (Checking is removed here, only 2 values are left) */
            AbstractOperand booleanOperand = ((OperandImpl) replacingExpression).getBaseOperand();
            PartedVariableHolder depVarHolder = hlddModelManager.extractBooleanDependentVariable(booleanOperand, true); //todo: false?
            TemporalNode replacingControlNode = new TemporalNode.Builder(depVarHolder.getVariable())
                    .partedIndices(depVarHolder.getPartedIndices())
                    .successorsCount(2)
                    .window(contextManager.getCurrentWindow()).build();

            /* Fill Control Node with successors (MAP nodes): TRUE and FALSE values; CHECKING value is removed. */
            Node[] operandSuccessors = operandNodeToReplace.getSuccessors();
            for (int condition = 0; condition < operandSuccessors.length; condition++) {
                /* If operandNodeToReplace is TOP (3 port node), skip condition 2 (Boolean Expression has no CHECKING value */
                if (operandSuccessors.length == 3 && condition == 2) continue;

                /* Get Successor, from current context */
                int relativeIndex = operandSuccessors[condition].getRelativeIndex();
                Node newSuccessorNode = contextManager.getNodeByIndex(relativeIndex);
                /* Adjust condition, depending on inversion state of replacingExpression */
                int adjustedCondition = adjustBooleanCondition(condition, depVarHolder.isInversed());

                /* Postpone missing successor (met in CYCLIC HLDDs) */
                if (newSuccessorNode == null) {
                    contextManager.postpone(adjustedCondition, relativeIndex, replacingControlNode);
                }

                /* Fill successor */
                replacingControlNode.setSuccessor(adjustedCondition, newSuccessorNode);
            }

            /* Return the created node */
            return replacingControlNode;

        } else if (replacingExpression instanceof ExpressionImpl) {

            ExpressionImpl expression = (ExpressionImpl) replacingExpression;
            PSLOperator operator = expression.getPslOperator();

            /* Init new context */
            initNewPPGContext(expression.getWindow(), operator.getWindowPlaceholders());

            Node[] propertyGraphNodes = operator.getPropertyGraph().getGraph().getRootNode().toArray(null);
            for (int index = propertyGraphNodes.length - 1; index >= 0; index--) {
                Node node = propertyGraphNodes[index];

                if (node.isTerminalNode()) {

                    /* Map PPG port */
                    contextManager.mapPPGPorts(index, node, operandNodeToReplace);

                } else {

                    AbstractExpression operand = expression.getOperandByName(node.getDependentVariable().getName());
                    addNode(index, replaceOperandNodeWithExpression(node, operand));

                }
            }

            /* Remember ROOT NODE to both process it and return it after the current context gets removed from stack */
            Node rootNode = contextManager.getNodeByIndex(0);

            /* Process ROOT NODE: */
            /* If parent had a window, then add this window to the rootNode of the inner PPG:
             * #############
             * ##         ##: In ExpressionImpl, it is the expression itself that stores the window, rather than its
             * ## COMMENT ##: operands (consider "next[2] a"). So the operands must turn to their parent expression for
             * ##         ##: receiving the window.
             * #############: */
            Range parentWindow = contextManager.getParentWindow();
            if (parentWindow != null) { //todo: Chaing merging of windows when "next next"
                /* Add the window via creation of a TemporalNode on the basis of the rootNode */
                Node[] successors = rootNode.getSuccessors();
                TemporalNode temporalRootNode = new TemporalNode.Builder(rootNode.getDependentVariable())
                        .partedIndices(rootNode.getPartedIndices())
                        .successorsCount(successors.length)
                        .window(parentWindow).build();
                for (int condition = 0; condition < successors.length; condition++) {
                    temporalRootNode.setSuccessor(condition, successors[condition]);
                }
                addNode(0, temporalRootNode);
                rootNode = temporalRootNode;
            }

            /* If cycle exists (some successors are null), map(set) the missing successors */
            contextManager.fillMissingSuccessors();            

            /* Remove CURRENT PPG context from stack */
            dismissCurrentPPGContext();

            /* Return ROOT NODE */
            return rootNode;
        } else throw new RuntimeException("Unexpected bug: Operand node is getting replaced neither with an " +
                OperandImpl.class.getSimpleName() + " nor with an " + ExpressionImpl.class.getSimpleName() + ".");
    }

    public void fillMissingSuccessors() throws Exception {
        contextManager.fillMissingSuccessors();
    }


    /* AUXILIARY CLASSES */
    private class ContextManager {
        private Stack<Context> contextStack = new Stack<Context>();

        /* Global window */
        private Range globalWindow = null;
        private boolean isLastEND;

        public void initNewPropertyGraph() {
            globalWindow = null;
            isLastEND = false;
        }

        public void initNewPPGContext(Range window, String[] windowPlaceholders) {
            addContext(new Context(window, windowPlaceholders));
        }

        private void addContext(Context newContext) {
            contextStack.add(newContext);
        }

        public Context dismissCurrentContext() {
            return contextStack.isEmpty() ? null : contextStack.pop();
        }

        public void addNodeToCurrentContext(int relativeIndex, Node node) {
            Context currentContext = getCurrentContext();
            if (currentContext != null) {
                currentContext.addNode(relativeIndex, node);
            } else {
                //todo...
            }
        }

        public Node getNodeByIndex(int relativeIndex) throws Exception {
            Context currentContext = getCurrentContext();
            if (currentContext != null) {
                return currentContext.getNode(relativeIndex);
            } else throw new Exception("Error while getting node by index from current context. Context stack is empty");
        }

        public Range getCurrentWindow() {
            Context currentContext = getCurrentContext();
            return currentContext != null ? currentContext.getWindow() : null;
        }

        public Range getParentWindow() {
            Context currentContext = removeCurrentContext(true);
            Range parentWindow = getCurrentWindow();
            addContext(currentContext);
            return parentWindow;
        }

        public String[] getCurrentWindowPlaceholders() {
            ModelManager.ContextManager.Context currentContext = getCurrentContext();
            return currentContext != null ? currentContext.getWindowPlaceholders() : null;
        }

        private Context getCurrentContext() {
            if (!contextStack.isEmpty()) {
                return contextStack.peek();
            } else return null;
        }

        public void mapPPGPorts(int portIndex, Node portNode, Node operandNodeToReplace) throws Exception {

            /* #########################
            *  ##### Get PORT VALUE ####
            *  ######################### */
            /* Check portNode to be a Terminal Node */
            if (portNode.isControlNode()) throw new RuntimeException("Unexpected bug: " +
                    "CONTROL NODE is being mapped as PPG port. PPG port can only be a TERMINAL NODE." +
                    "\nActual port node:\n" + portNode.toString()
            );

            /* Check portNode to be based on a Constant */
            if (!(portNode.getDependentVariable() instanceof ConstantVariable)) throw new RuntimeException(
                    "Terminal port node in PPG is not based on a constant:" +
                            "\nActual port node:\n" + portNode.toString()
            );

            /* Get PORT VALUE */
            int portValue = ((ConstantVariable) portNode.getDependentVariable()).getValue().intValue();

            /* ###################################
            *  ##### Get PORT SUCCESSOR NODE #####
            *  ################################### */
            /* Check operandNodeToReplace to be a Control Node */
            if (operandNodeToReplace.isTerminalNode()) throw new RuntimeException("Unexpected bug: " +
                    "operandNodeToReplace is a TERMINAL NODE. Cannot extract condition successor from it.");

            /* Get RELATIVE index of port successor node */
            Node[] successors = operandNodeToReplace.getSuccessors();
            if (portValue > successors.length - 1) return; // Skip CHECKING value of BOOLEAN operand
            int relativeIndex = successors[portValue].getRelativeIndex();

            /* Get Port Successor Node by relative index from previous context */
            Context currentContext = removeCurrentContext(true);
            Node portSuccessorNode = getNodeByIndex(relativeIndex);
            addContext(currentContext);

            /* ##################################
            *  ##### Add PORT SUCCESSOR NODE ####
            *  ################################## */
            if (portSuccessorNode != null) {
                addNode(portIndex, portSuccessorNode);
            }

        }

        private Context removeCurrentContext(boolean mustHave) {
            try {
                return contextStack.pop();
            } catch (EmptyStackException e) {
                if (mustHave) {
                    throw new RuntimeException("Cannot return current context. Context stack is empty.");
                } else {
                    return null;
                }
            }
        }

        public void postpone(int adjustedCondition, int relativeIndex, TemporalNode controlNode) {
            Context currentContext = getCurrentContext();
            if (currentContext != null) {
                currentContext.postpone(adjustedCondition, relativeIndex, controlNode);
            }
        }

        public void adjustWindow() throws Exception {
            /* Skip (do nothing for) operators without windows */
            String[] operatorWPlacholders = getCurrentWindowPlaceholders();
            if (operatorWPlacholders == null) return;

            Range parentOperandWindow = getCurrentWindow();
            if (globalWindow == null) {
                Range realWindow = Range.replacePlaceholders(parentOperandWindow, operatorWPlacholders);
                if (realWindow.getEnd() == -1) {
                    isLastEND = true;
                    realWindow.setEnd(0);
                }
                globalWindow = realWindow;
                globalWindow.setEnd(globalWindow.getEnd() - 1); /* decrease by one. May be changed. */
            } else {
                globalWindow.adjustWindow(Range.replacePlaceholders(parentOperandWindow, operatorWPlacholders));
            }
        }

        private String globalWindowToString() {
            if (globalWindow == null) {
                return "";
            }
            StringBuffer retBuffer = new StringBuffer(globalWindow.toString());

            if (isLastEND) retBuffer.insert(retBuffer.toString().indexOf("..") + 2, "END");

            return retBuffer.toString();
        }

        public void fillMissingSuccessors() throws Exception {
            Context currentContext = getCurrentContext();
            if (currentContext != null) {
                currentContext.fillMissingSuccessors();
            }
        }


        private class Context {
            private Map<Integer, Node> nodeByRelativeIndex = new HashMap<Integer, Node>();
            private PostponedHashings postponedHashings = new PostponedHashings();
            private final Range window;
            private final String[] windowPlaceholders;

            public Context(Range window, String[] windowPlaceholders) {
                this.window = window;
                this.windowPlaceholders = windowPlaceholders;
            }

            public void addNode(int relativeIndex, Node node) {
                nodeByRelativeIndex.put(relativeIndex, node);
            }

            public void postpone(int adjustedCondition, int relativeIndex, TemporalNode controlNode) {
                postponedHashings.postpone(adjustedCondition, relativeIndex, controlNode);
            }

            public Node getNode(int relativeIndex) {
                return nodeByRelativeIndex.get(relativeIndex);
            }

            public Range getWindow() {
                return window;
            }

            public String[] getWindowPlaceholders() {
                return windowPlaceholders;
            }

            public void fillMissingSuccessors() throws Exception {
                postponedHashings.fillMissingSuccessors(nodeByRelativeIndex);
            }

            private class PostponedHashings {
                private List<PostponedHashing> postHashsList = new LinkedList<PostponedHashing>();

                public void postpone(int adjustedCondition, int relativeIndex, TemporalNode controlNode) {
                    postHashsList.add(new PostponedHashing(adjustedCondition, relativeIndex, controlNode));
                }

                public void fillMissingSuccessors(Map<Integer, Node> nodeByRelativeIndex) throws Exception {
                    LOG.entering(LOG.getName(), "fillMissingSuccessors");
                    for (PostponedHashing postponedHashing : postHashsList) {
                        postponedHashing.controlNode.setSuccessor(
                                postponedHashing.adjustedCondition,
                                nodeByRelativeIndex.get(postponedHashing.relativeIndex));
                    }
                    LOG.exiting(LOG.getName(), "fillMissingSuccessors");
                }

                private class PostponedHashing {
                    private final int adjustedCondition;
                    private final int relativeIndex;
                    private final TemporalNode controlNode;

                    public PostponedHashing(int adjustedCondition, int relativeIndex, TemporalNode controlNode) {
                        this.adjustedCondition = adjustedCondition;
                        this.relativeIndex = relativeIndex;
                        this.controlNode = controlNode;
                    }
                }
            }
        }

    }

}
