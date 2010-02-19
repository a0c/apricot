package base.vhdl.visitors;

import base.vhdl.structure.nodes.*;
import base.vhdl.structure.Process;
import base.vhdl.structure.*;
import base.vhdl.structure.Variable;
import base.hldd.structure.models.utils.ModelManager;
import base.hldd.structure.models.utils.PartedVariableHolder;
import base.hldd.structure.models.utils.ModelManager.CompositeFunctionVariable;
import base.hldd.structure.variables.*;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.CompositeNode;
import base.hldd.structure.Flags;
import base.hldd.visitors.ObsoleteResetRemoverImpl;
import base.Indices;
import base.VHDL2HLDDMapping;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import parsers.vhdl.VHDLStructureParser;

/**
 *
 * Generates graphs out of VHDL Structure (Entity)
 * <p>
 * IfNode and CaseNode policy:<br>
 * If the condition has a missing successor, then it is filled 
 * with a terminal node based on a concrete dependent Variable
 * ( usually to make the graph retain its value ).<br>
 *  Possibility is implemented as method fillEmptySuccessorsWith() of class Node.
 * <p>
 *  <b>NB!</b>  Trimming of redundant nodes is performed on the VHDL 
 *              Structure (Entity) using visitors (IfNodeSimplifier).
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 22:55:15
 */
public abstract class GraphGenerator extends AbstractVisitor {

	protected static final Logger LOG = Logger.getLogger(GraphGenerator.class.getName());

    protected ModelManager modelCollector;
    protected AbstractVariable graphVariable;
    protected Node graphVariableRootNode;
    private ContextManager contextManager = new ContextManager();

    /* AUXILIARY fields */
    private final boolean doExpandConditions;
    private final boolean isNullATransition;
    /* Set of processed graphVars. Used for skipping processed variables. */
    protected Set<String> processedGraphVars = new HashSet<String>();
    /* For each process, a set of partially set variables for those variables that are parted. */
    protected Map<Process, Map<String, Set<OperandImpl>>> partialSettingsMapByProcess
            = new HashMap<Process, Map<String, Set<OperandImpl>>>();
    /**
     * Maps VHDL to HLDD.
     */
    private final VHDL2HLDDMapping vhdl2hlddMapping = VHDL2HLDDMapping.getInstance();

	public ModelManager getModelCollector() {
        return modelCollector;
    }

    public enum Type {
        BehDD, Beh
    }

    /**
     *
     * @param   useSameConstants <code>true</code> if the constant with the <u>same value</u> and <u>adjusted length</u> should be used
     *          for requested value. <code>false</code> if a new constant should be created for the same value, but a different length
     *          than already exists.
     *          <br>todo: <b>NB!</b> <code>true</code> possibility is not implemented yet. When implementing it, either
     *          modify getModelCollector() method to perform adjustment of partedIndices of terminal nodes in GraphVariables,
     *          or make the TerminalNode set its partedIndices itself.
     * @param doExpandConditions whether to expand Composite conditions into a set of Control Nodes (true value), or
     *        create a single node function (false value).
     * @param generatorType generator's type. Used for initialization of isNullTransition only.  
     */
    protected GraphGenerator(boolean useSameConstants, boolean doExpandConditions, Type generatorType) {
        this.doExpandConditions = doExpandConditions;
        modelCollector = new ModelManager(useSameConstants);
        isNullATransition = generatorType == Type.BehDD;
    }

    /* Here define a common processing of all ENTITY parts but AbstractNodes(ParseTree) */

    public void visitEntity(Entity entity) throws Exception {

        /* Collect CONSTANTS */
        collectConstants(entity.getConstants());

        /* Process PORTS */
        Set<Port> ports = entity.getPorts();
        for (Port port : ports) {
            /* Skip CLOCKING port */
            if (isClockName(port.getName())) continue;
            Flags flags = new Flags().setInput(port.isInput()).setOutput(!port.isInput()).setReset(isResetName(port.getName()));
            base.hldd.structure.variables.Variable portVariable = new base.hldd.structure.variables.Variable(port.getName(), port.getType(), flags);
            modelCollector.addVariable(portVariable);
        }

    }

    public void visitArchitecture(Architecture architecture) throws Exception {

        /* Collect CONSTANTS */
        collectConstants(architecture.getConstants());
        for (Process process : architecture.getProcesses()){
            collectConstants(process.getConstants());
        }

        /* Collect SIGNALS */
        Set<Signal> signals = architecture.getSignals();
        for (Signal signal : signals) {
            Flags flags = new Flags();
            if (isStateName(signal.getName())) flags.setState(true).setDelay(true);
            base.hldd.structure.variables.Variable signalVariable = new base.hldd.structure.variables.Variable(signal.getName(), signal.getType(), flags);
            modelCollector.addVariable(signalVariable);
        }

        /* Collect VARIABLES */
        for (Process process : architecture.getProcesses()) {
            Set<Variable> variables = process.getVariables();
            for (Variable variable : variables) {
                Flags flags = new Flags();
                if (isStateName(variable.getName())) flags.setState(true).setDelay(true);
                base.hldd.structure.variables.Variable newVariable = new base.hldd.structure.variables.Variable(variable.getName(), variable.getType(), flags);
                modelCollector.addVariable(newVariable);
            }
        }

        for (Process process : architecture.getProcesses()) {
            /* Adjust CONSTANT LENGTHS to the lengths of the variables that use these constants directly */
            adjustConstantLengths(process);
            /* Collect partialSet variables */
            collectPartialSettings(process);
        }

        /* Process process-external transitions of the Architecture */
        for (AbstractNode archTransNode : architecture.getTransitions().getChildren()) {
            couldProcessNextGraphVariable(null, archTransNode);
        }
    }

    private void collectConstants(Set<Constant> constants) throws Exception {
        for (Constant constant : constants) {
            ConstantVariable constantVariable = new ConstantVariable(constant.getName(), constant.getValue());
            modelCollector.addVariable(constantVariable);
        }
    }

    /* Here only request processing of AbstractNodes(ParseTree) */

    /**
     *
     * @param ifNode        IfNode being visited
     * @throws Exception    {@link base.hldd.structure.nodes.Node#isEmptyControlNode() cause1 }
     *                      {@link base.hldd.structure.models.utils.ModelManager#getConditionsCount(AbstractVariable)  cause 2 }
     */
    public void visitIfNode(IfNode ifNode) throws Exception {

        /*#################################################
        *       P R O C E S S     E X P R E S S I O N
        * #################################################*/
        Expression expression = ifNode.getConditionExpression();
        /* Extract dependentVariable */
        PartedVariableHolder depVariableHolder = modelCollector.getBooleanDependentVariable(expression, doExpandConditions);
        AbstractVariable dependentVariable = depVariableHolder.getVariable();
        Indices partedIndices = depVariableHolder.getPartedIndices();
        int conditionValueInt = depVariableHolder.getTrueValue(); /*modelCollector.getConditionValue(dependentVariable, expression);*/
        /* Count possible conditions of dependentVariable */
        int conditionsCount = 2 /*modelCollector.getConditionsCount(dependentVariable)*/;

        /* Create ControlNode */
        Node controlNode = dependentVariable instanceof CompositeFunctionVariable
                ? new CompositeNode((CompositeFunctionVariable) dependentVariable)
                : new Node.Builder(dependentVariable).partedIndices(partedIndices).successorsCount(conditionsCount).build();
        /* Add VHDL lines the node's been created from */
        controlNode.setVhdlLines(vhdl2hlddMapping.getLinesForNode(ifNode));

        /*#################################################
        *       P R O C E S S     T R U E  P A R T
        * #################################################*/
        /* Extract condition of dependentVariable: already DONE above using depVariableHolder */
        /* Create new Current Context and push it to Context Stack */
        contextManager.addContext(new Context(controlNode, new int[]{conditionValueInt} ));
        /* Process TRUE PART */
        doCheckTruePart(ifNode);
        ifNode.getTruePart().traverse(this);
        /* Remove Current Context from stack */
        contextManager.removeContext();

        /*#################################################
        *       P R O C E S S     F A L S E  P A R T
        * #################################################*/
        if (ifNode.getFalsePart() != null) {
            doCheckFalsePart(ifNode);
            /* Get false condition of dependentVariable */
            conditionValueInt = ModelManager.invertBit(conditionValueInt);
            /* Create new Current Context and push it to Context Stack */
            contextManager.addContext(new Context(controlNode, new int[]{conditionValueInt} ));
            /* Process FALSE PART */
            ifNode.getFalsePart().traverse(this);
            /* Remove Current Context from stack */
            contextManager.removeContext();
        }

        /*#################################################
        *       F I N A L I Z E    C O N T R O L  N O D E
        * #################################################*/
        contextManager.fillCurrentContextWith(controlNode);


    }

    @Deprecated
    /* Beh DD tree can have multiple nodes (partial setting variables).
    Though, it still cannot set multiple variable in one process tree. */
    protected abstract void doCheckTruePart(IfNode ifNode) throws Exception;
    @Deprecated
    /* Beh DD tree can have multiple nodes (partial setting variables).
    Though, it still cannot set multiple variable in one process tree. */
    protected abstract void doCheckFalsePart(IfNode ifNode) throws Exception;

    /**
     *
     * @param transitionNode    Transition Node being visited
     * @throws Exception {@link GraphGenerator.Context#fill(base.hldd.structure.nodes.Node) cause }
     */
    public void visitTransitionNode(TransitionNode transitionNode) throws Exception {

        /* Only process TerminalNodes that set the graphVariable */
        if (isGraphVariableSetIn(transitionNode)) {
            /* ######### Create TERMINAL NODE ############*/

            /* Extract dependentVariable and partedIndices */
            AbstractVariable dependentVariable = transitionNode.isNull()
                    ? graphVariable /*modelCollector.getVariable(graphVariable.getName())*/ // Retain value
                    : modelCollector.convertOperandToVariable(transitionNode.getValueOperand(), graphVariable.getLength(), true);
            /* branch <= not CCR(CBIT); =====> don't take partedIndices into account, they were already used during Function creation */
            Indices partedIndices = dependentVariable instanceof FunctionVariable && ((FunctionVariable) dependentVariable).getOperator() == Operator.INV
                    ? null : transitionNode.getValueOperandPartedIndices();
            if (graphVariable instanceof PartedVariable && !transitionNode.isNull()) {
                /* Subtract indices (get Absolute indices out of relative) */
                partedIndices = ((PartedVariable) graphVariable).getPartedIndices().absoluteFor(
                                transitionNode.getTargetOperand().getPartedIndices(),
                                partedIndices);
//                partedIndices = Indices.extractAbsIndicesFor(partedIndices,
//                        ((PartedVariable) graphVariable).getPartedIndices(),
//                        transitionNode.getTargetOperand().getPartedIndices());
            }
            /* Create TerminalNode */
            Node terminalNode = new Node.Builder(dependentVariable).partedIndices(partedIndices).build();
            /* Add VHDL lines the node's been created from */
            terminalNode.setVhdlLines(vhdl2hlddMapping.getLinesForNode(transitionNode));
            /* Add TerminalNode to Current Context and remove Current Context from stack, if the stack is NOT empty.
             * If the stack is empty, initiate the root node */
            contextManager.fillCurrentContextWith(terminalNode);
        }
    }

    private boolean isGraphVariableSetIn(TransitionNode transitionNode) throws Exception {
        if (graphVariable == null) {
            if (transitionNode.isNull()) return false;
            /* GraphVariable to process has not been set yet. */
            /* Check whether the transition variable has been processed already.
            *  If not, then set graphVariable to this transition variable. */
            String transitionVarName = transitionNode.getTargetOperand().toString();
            if (!processedGraphVars.contains(transitionVarName)) {
                graphVariable = modelCollector.getVariable(transitionVarName);
                if (graphVariable == null) {
                    return false;//todo: commented for DEMO. (Informs that the initial variable of partedSetting variables is not found or the like... In any way, has smth to do with partedSetting variables)  
//                    Exception exception = new Exception("GraphVariable to process could not be set:" +
//                            "\nModel collector does not contain the requested variable: " + transitionVarName +
//                            "\nVariable " + transitionVarName + " is not declared.");
//                    Object solution = ExceptionSolver.getInstance().findSolution(exception.getMessage(), ExceptionSolver.SolutionOptions.IGNORE);
//                    if (solution instanceof Boolean) {
//                        /* Skip this transition. */
//
//                        return false;
//                    } else throw exception;
                }
                return true;/* A new graphVariable is set. Start processing its TransitionNodes. */
            }
            return false; /* This transition variable has already been processed. */

        } else return transitionNode.isTransitionOf(graphVariable, isNullATransition);
    }

    public void visitCaseNode(CaseNode caseNode) throws Exception {
        /*#################################################
        *       P R O C E S S     C O N D I T I O N
        * #################################################*/
        /* Extract dependentVariable */
        AbstractOperand variableOperand = caseNode.getVariableOperand();
        AbstractVariable dependentVariable = modelCollector.convertOperandToVariable(variableOperand, null, false);
        Indices partedIndices = variableOperand.getPartedIndices();
        /* Count possible conditions of dependentVariable */
        //todo: suspicious action: dependentVariable.isState() ? caseNode.getConditions().size(). May be "when => others", may be "when A | B | C =>" ... consider these...
//        int conditionsCount = dependentVariable.isState() ? caseNode.getConditions().size() : modelCollector.getConditionsCount(dependentVariable);
        int conditionsCount = modelCollector.getConditionsCount(dependentVariable);
        /* Create Control Node */
        Node controlNode = new Node.Builder(dependentVariable).partedIndices(partedIndices).successorsCount(conditionsCount).build();
        /* Add VHDL lines the node's been created from */
        controlNode.setVhdlLines(vhdl2hlddMapping.getLinesForNode(caseNode));

        /*#################################################
        *       P R O C E S S     C O N D I T I O N S
        * #################################################*/
        /* Create new Current Context (without awaitedConditions) and add it to Context Stack */
        contextManager.addContext(new Context(controlNode));
        for (WhenNode whenNode : caseNode.getConditions()) {
            /* Traverse When Condition */
            whenNode.traverse(this);
        }
        /* Remove Case Context from Context Stack */
        contextManager.removeContext();


        /*#################################################
        *       F I N A L I Z E    C O N T R O L  N O D E
        * #################################################*/
        contextManager.fillCurrentContextWith(controlNode);

    }

    public void visitWhenNode(WhenNode whenNode) throws Exception {
        /*#################################################
        *       P R O C E S S     C O N D I T I O N
        * #################################################*/
        doCheckWhenTransitions(whenNode);

        if (!whenNode.isOthers()) {
            /* Extract whenConditions */
            int[] conditionValuesInt = modelCollector.getConditionValues(whenNode.getConditionOperands() /*conditionString*/);

            /* Register processed condition values in the caseContext */
            contextManager.registerProcessedConditions(conditionValuesInt);
            /* Get controlNode from Current Context.
             * Create new Context and add it to Context Stack */
            contextManager.addContext(new Context(contextManager.getCurrentContext().getControlNode(), conditionValuesInt));

            /*#################################################
            *       P R O C E S S     T R A N S I T I O N S
            * #################################################*/
            whenNode.getTransitions().traverse(this);
            /* Remove Current Context from stack */
            contextManager.removeContext();
        } else {
            /* For OTHERS, substitute OTHERS in whenNode with ALL UNPROCESSED conditions and process the node */
            boolean[] processedConditions = contextManager.getCurrentContext().getProcessedConditions();
            for (int i = 0; i < processedConditions.length; i++) {
                if (!processedConditions[i]) {
                    /* Temporarily replace the condition with a numeric value */
                    whenNode.setConditions(String.valueOf(i));
                    visitWhenNode(whenNode);
                }
            }
            /* Restore condition */
            whenNode.setConditions("OTHERS");
        }
    }

    @Deprecated
    /* Beh DD tree can have multiple nodes (partial setting variables).
    Though, it still cannot set multiple variable in one process tree. */
    protected abstract void doCheckWhenTransitions(WhenNode whenNode) throws Exception;

    private void adjustConstantLengths(Process process) throws Exception {
        /* Adjust CONSTANT LENGTHS to the lengths of the variables that use these constants directly */
        process.traverse(new ConstantLengthAdjuster());
    }

    private void collectPartialSettings(Process process) throws Exception {
        PartialSetVariableCollector collector = new PartialSetVariableCollector(process.getVariables());
        process.traverse(collector);
        Map<String, Set<OperandImpl>> partialSettingsMap = collector.getPartialSettingsMap();
        if (!partialSettingsMap.isEmpty()) {
            partialSettingsMapByProcess.put(process, partialSettingsMap);
        }
    }

	/**
     * @param oldGraphVariable old variable on the base of which to build the
     *        new one and what to replace with the new variable 
     * @param graphVarRootNode root node of the new variable
     * @param isDelay Delay-flag to set to the new GraphVariable
     * @return the newly created GraphVariable
     * @throws Exception If {@link base.hldd.structure.models.utils.ModelManager#replace(AbstractVariable, AbstractVariable)}
     *         throws an exception
     */
    protected GraphVariable createAndReplaceNewGraph(AbstractVariable oldGraphVariable, Node graphVarRootNode, boolean isDelay) throws Exception {
        GraphVariable newGraphVariable = new GraphVariable(oldGraphVariable, graphVarRootNode);
        newGraphVariable.setDelay(isDelay);
        modelCollector.replace(oldGraphVariable, newGraphVariable);
        return newGraphVariable;
    }

    /**
     * @param initGraphVariable variable to initialize {@link #graphVariable} with
     * @param rootNode where to start processing traversal from
     * @return <code>true</code> if next graph variable was processed.
     *         <code>false</code> if no graph variables could be found to
     *         process during traversal of the specified rootNode
     * @throws Exception if {@link AbstractNode#traverse(AbstractVisitor)} or
     *         {@link #createAndReplaceNewGraph(base.hldd.structure.variables.AbstractVariable,
               base.hldd.structure.nodes.Node, boolean)} throw an Exception
     */
    protected boolean couldProcessNextGraphVariable(AbstractVariable initGraphVariable, AbstractNode rootNode) throws Exception {
        graphVariable = initGraphVariable;
        graphVariableRootNode = null;
        contextManager.clear();
        rootNode.traverse(this);
        if (graphVariableRootNode != null) {
            /* Create new GraphVariable and replace old one */
            GraphVariable newGraphVariable = createAndReplaceNewGraph(graphVariable, graphVariableRootNode, isDelay(graphVariable.getName()));

            /* Remove redundant resets */
            graphVariableRootNode.traverse(new ObsoleteResetRemoverImpl(newGraphVariable));

            return true;
        } else return false;
    }

    protected abstract boolean isDelay(String variableName);

    protected void processPartialSettings(Process process) throws Exception {
        /* For each partially set variable in process, traverse the tree once for each partial setting variable */
        Map<String,Set<OperandImpl>> partialSettingsMap = partialSettingsMapByProcess.get(process);
        for (String varName : partialSettingsMap.keySet()) {
            Set<OperandImpl> partSetOperandsSet = partialSettingsMap.get(varName);
            AbstractVariable wholeVariable = modelCollector.getVariable(varName); // base variable of partial setting variables
            /* Check for missing CAT inputs and for intersections (throw Exception).*/
            doCheckPartialSetForCompleteness(varName, wholeVariable.getLength(), partSetOperandsSet);
            /* For each partial setting variable, traverse the tree */
            List<GraphVariable> partialSetVarsList = new LinkedList<GraphVariable>();
            for (OperandImpl partSetOperand : partSetOperandsSet) {
                /* Create new variable */ //todo: may be substitute with couldProcessNextGraphVariable(). Check ModelCollector.replace() to act equally to modelCollector.addVariable() met below:
                graphVariable = new PartedVariable(partSetOperand.getName(), wholeVariable.getType(), partSetOperand.getPartedIndices());
                graphVariableRootNode = null;
                process.getRootNode().traverse(this);
                if (graphVariableRootNode != null) {
                    GraphVariable newGraphVariable = new GraphVariable(graphVariable, graphVariableRootNode);
                    modelCollector.addVariable(partSetOperand.toString(), newGraphVariable);
                    //todo: Remove redundant resets?
                    partialSetVarsList.add(newGraphVariable);
                    processedGraphVars.add(partSetOperand.toString());
                } else throw new Exception("Partial setting variable " + graphVariable + " is never set."); //todo: assert could also be used 
            }
            /* Create CAT function */
            AbstractVariable catFunction = createCatFunction(partialSetVarsList);
            /* Make base variable tree contain 1 Terminal Node only: the CAT node */
            createAndReplaceNewGraph(wholeVariable, new Node.Builder(catFunction).build(), isDelay(varName)); // isDelay(varName) was at first "true".
            processedGraphVars.add(varName);
        }
    }

    private AbstractVariable createCatFunction(List<GraphVariable> partialSetVarsList) throws Exception {
        /* Sort by index */
        Collections.sort(partialSetVarsList, new GraphVariableComparator());
        /* Create CAT Expression */
        Expression catExpession = new Expression(Operator.CAT, false);
        /* Add all inputs to the expression */
        for (GraphVariable partialGraphVariable : partialSetVarsList) {
            PartedVariable baseVariable = ((PartedVariable) partialGraphVariable.getBaseVariable());
            catExpession.addOperand(new OperandImpl(baseVariable.getName(), baseVariable.getPartedIndices(), false));
        }
        /* Add the Expression to ModelCollector */
        return modelCollector.convertOperandToVariable(catExpession, null, true);
    }

    static Indices[] extractUnsetIndices(boolean[] setBits) {
        List<Indices> indicesList = new LinkedList<Indices>();
        int lowest = -1, highest = -1;
        for (int i = 0; i < setBits.length; i++) {
            if (!setBits[i]) {
                if (lowest == -1) {
                    lowest = i;
                }
                highest = i;
            } else {
                if (lowest != -1) {
                    indicesList.add(new Indices(highest, lowest));
                    lowest = -1; highest = -1;
                }
            }
        }
        if (lowest != -1) {
            indicesList.add(new Indices(highest, lowest));
        }
        return indicesList.toArray(new Indices[indicesList.size()]);
    }

    private void doCheckPartialSetForCompleteness(String varName, Indices length, Set<OperandImpl> partSetOperandsSet) throws Exception {
        boolean[] bits = new boolean[length.length()];
        /* Check intersections: if any, inform about them */
        for (OperandImpl partSetOperand : partSetOperandsSet) {
            if (!partSetOperand.isParted()) throw new Exception("Partial setting operand doesn't contain parted indices: " + partSetOperand);
            Indices partedIndices = partSetOperand.getPartedIndices();
            for (int index = partedIndices.getLowest(); index <= partedIndices.getHighest(); index++) {
                /* If this bit has already been set, inform about intersection */
                if (bits[index]) throw new Exception("Intersection of partial setting operands:" +
                        "\nBit " + index + " has already been set for operand " + partSetOperand.getName());
                bits[index] = true;
            }
        }
        /* Check missing partial setting variables: if any, fill the set with missing variables */
        Indices[] unsetIndicesArray = extractUnsetIndices(bits);
        for (Indices unsetIndices : unsetIndicesArray) { //todo: It may occur, that the whole variable is unset. Consider this.
            partSetOperandsSet.add(new OperandImpl(varName, unsetIndices,  false));
        }
    }

    /* AUXILIARY methods and classes */

    /**
     * Class deals with contexts of tree traversal.
     */
    private class ContextManager {
        /**
         * Stack of <b>contexts</b>.<br>
         * Stack is managed <i>manually</i>, i.e. by the clients that use this class. Management is performed by means
         * of {@link base.vhdl.visitors.GraphGenerator.ContextManager#addContext(base.vhdl.visitors.GraphGenerator.Context)} and
         * {@link base.vhdl.visitors.GraphGenerator.ContextManager#removeContext()} methods.<p>
         * Generally, context is <i>added</i> to the stack prior to traversal of {@link IfNode}'s true- and falseParts
         * (transitions of the parts), {@link WhenNode}'s transitions and {@link CaseNode} conditions.<br>
         * Generally, context is <i>removed</i> from the stack DIRECTLY AFTER the above mentioned traversals.<br>
         * So the management of this stack conforms to the rule <b>"He who adds the context removes it"</b>.
         */
        private Stack<Context> contextStack = new Stack<Context>();
        /**
         * Stack of <b>default value nodes (= "default values")</b>.<br>
         * Stack is managed <i>automatically</i>, i.e. internally from within of
         * {@link base.vhdl.visitors.GraphGenerator.ContextManager}.<p>
         * Default values are <i>added</i> to the stack whenever the Current Context gets filled with a Terminal Node.<br>
         * Default values are <i>removed</i> from the stack whenever the Context they were set in gets removed
         * from the stack.
         */
        private Stack<NodeAndContextHolder> defaultValueStack = new Stack<NodeAndContextHolder>();

        public void clear() {
            contextStack = new Stack<Context>();
            defaultValueStack = new Stack<NodeAndContextHolder>();
        }

        public void addContext(Context newContext) {
            contextStack.push(newContext);
        }

        public void removeContext() throws Exception {
            /* Remove Default Value, if its Context is getting removed */
            removeDefaultValue();
            /* Remove Current Context */
            contextStack.pop();
        }

        public void registerProcessedConditions(int... processedCondition) {
            contextStack.peek().registerProcessedConditions(processedCondition);
        }

        public Context getCurrentContext() {
            return contextStack.isEmpty() ? null : contextStack.peek();
        }

        public void fillCurrentContextWith(Node fillingNode) throws Exception {
            /* Add Control Node to Current Context only if the former is not empty */
            if (fillingNode.isControlNode() && fillingNode.isEmptyControlNode()) return;

            /* Update Default Value */
            updateDefaultValue(fillingNode);

            /* Add Node to Current Context if the stack is NOT empty.
             * If the stack is empty, initiate the root node. */
            if (!contextStack.isEmpty()) {
                getCurrentContext().fill(fillingNode);
            } else {
                if (graphVariableRootNode != null && defaultValueStack.peek().defaultValueContext != null) {
					LOG.warning("Rewriting graphVariableRootNode for " + graphVariable.getName() + " and the defaultValueStack is not empty.");
				}
//                fillEmptySuccessorsFor(fillingNode); //todo: remove this invocation. This must've already been done by previous updateDefaultValue()
                graphVariableRootNode = fillingNode instanceof CompositeNode
                        ? ((CompositeNode) fillingNode).getRootNode()
                        : fillingNode;
            }
        }

        private void replaceDefaultValue(Node newDefaultNode) {
            /* Only replace the default value if there is anything to be replaced at all */
            if (!defaultValueStack.isEmpty()) {
                /* Remove previous default value */
                defaultValueStack.pop();
                /* Add new default value */
                addDefaultValue(newDefaultNode);
            }
        }

        private void addDefaultValue(Node defaultValueNode) {
            defaultValueStack.push(new NodeAndContextHolder(defaultValueNode, getCurrentContext()));
        }

        private void updateDefaultValue(Node fillingNode) throws Exception {
            /* For terminal node, replace the previous Default Value completely. */
            /* For control node, fill empty successors with previous Default Value
             * and only then replace the Default Value with the new one. */
            if (fillingNode.isControlNode()) {
                fillEmptySuccessorsFor(fillingNode);
            }
            /* Replace the Default Value */

            /* If the Current Default Value has the same Context, then only override the Default Value.
            * For this, at first remove the Current Default Value and then add the new one, with the
            * common Current Context. */
            //todo: consider using replaceDefaultValue() here (and addDefaultValue() also)
            if (!defaultValueStack.isEmpty() && getDefaultValueContext() == getCurrentContext()) {//todo: consider using getDefaultValueContext only, without checking the defaultValueStack for emptyness
                defaultValueStack.pop();
            }
            defaultValueStack.push(new NodeAndContextHolder(fillingNode, getCurrentContext()));

        }

        private void removeDefaultValue() throws Exception {
            /* Only remove Current Default Value, if the Context being removed is its own one */
            if (!defaultValueStack.isEmpty()
                    && getCurrentContext() == getDefaultValueContext()){
                /* When leaving the context and discarding Default Value, before leaving fill missing
                * successors of Current Context Awaited Nodes with the Default Value being removed.*/
                if (!contextStack.isEmpty()) {
                    fillEmptySuccessorsFor(getCurrentContext().getAwaitedNodes());
                }
                /* Remove Default Value */
                defaultValueStack.pop();
            }
        }

        /**
         * Filling of missing successors is performed <b>only</b> in the following 2 cases:<br>
         * 1) when leaving context (namely, when Default Value gets removed from stack) and<br>
         * 2) when initializing the root node (namely, when filling an <u>empty</u> Current Context stack with a node).<br>
         * TODO: number of usages can be reduced to 1, when used only upon leaving context.
         * TODO: For this, create a special empty context for graphVarRootNode when entering !process! 
         * @param nodesToFill where to fill empty successors
         * @throws Exception {@link base.hldd.structure.nodes.Node#isEmptyControlNode()},
         *                   {@link Node#fillEmptySuccessorsWith(base.hldd.structure.nodes.Node)}
         */
        private void fillEmptySuccessorsFor(Node... nodesToFill) throws Exception {
            for (Node nodeToFill : nodesToFill) {
                /* For non-empty ControlNodes fill the missing successors with the latest defaultValue */
                if (nodeToFill != null &&
                        nodeToFill.isControlNode() && !nodeToFill.isEmptyControlNode()) { // todo: consider removing 2nd and 3rd condition from here, if previous checks filter "bad" nodes out 
                    nodeToFill.fillEmptySuccessorsWith(getDefaultValueNode());
                }
            }
        }

        private GraphGenerator.Context getDefaultValueContext() {
            return defaultValueStack.isEmpty() ? null : defaultValueStack.peek().defaultValueContext;
        }

        /**
         * @return current Default Value Node from the stack or value
         *         retaining node, if the stack of Default Values is empty
         */
        private Node getDefaultValueNode() {
            if (defaultValueStack.isEmpty()) {
                Set<Integer> vhdlLines = getCurrentContext() == null
                        ? new TreeSet<Integer>() : getCurrentContext().getControlNode().getVhdlLines();
                return new Node.Builder(graphVariable).vhdlLines(vhdlLines).build();
            } else {
                Node defaultValueNode = defaultValueStack.peek().defaultValueNode;
                return Node.clone(defaultValueNode);
            }
        }

        private class NodeAndContextHolder {
            private final Node defaultValueNode;
            private final Context defaultValueContext;
            public NodeAndContextHolder(Node defaultValueNode, Context defaultValueContext) {
                this.defaultValueNode = defaultValueNode;
                this.defaultValueContext = defaultValueContext;
            }
        }

    }

    protected class Context {
        private final Node controlNode;
        private final int[] awaitedConditions;
        private boolean[] processedConditions;

        public Context(Node controlNode, int[] awaitedConditions) {
            this.controlNode = controlNode;
            this.awaitedConditions = awaitedConditions;
        }

        /**
         * Constructor to be used by {@link CaseNode}
         * @param controlNode conditional node
         */
        public Context(Node controlNode) {
            this.controlNode = controlNode;
            awaitedConditions = null;
        }

        public Node getControlNode() {
            return controlNode;
        }

        public boolean[] getProcessedConditions() {
            return processedConditions;
        }

        /**
         * @return nodes that are being filled
         */
        public Node[] getAwaitedNodes() {
            Node[] nodes = new Node[awaitedConditions.length];
            for (int i = 0; i < awaitedConditions.length; i++) {
                nodes[i] = controlNode.getSuccessors()[awaitedConditions[i]];
            }
            return nodes;
        }

        /**
         *
         * @param fillingNode node to fill the current context with
         * @throws Exception cause {@link base.hldd.structure.nodes.Node#setSuccessor(int, base.hldd.structure.nodes.Node) cause }
         */
        public void fill(Node fillingNode) throws Exception {

            /* Filling node is added differently depending on the
           * 1) type of the node being filled and
           * 2) type of the node currently at the awaited position.
           *
           * If filling node is a TERMINAL NODE, then it
           *    overrides everything at the awaited position that has been set so far.
           * If filling node is a CONTROL NODE, then it
           *    overrides everything at the awaited position but a CONTROL NODE:
           *      a) If node at the awaited position is CONTROL NODE, then:
           *            x) a copy of the filling node both gets placed instead of every successor of CONTROL NODE
           *               and gets filled with the successor, if it's available.
           *
           * todo: When FILLING, consider the 3rd subsequent condition!!!
           *
           * todo: When changing the algorithm of merging sequential conditional statements (2 or more parallel If-s),
           * todo: change this method and updateDefaultValue() ==> if (n.isControlNode())
           * */

            for (int awaitedCondition : awaitedConditions) {
                Node currentAwaitedNode = controlNode.getSuccessors()[awaitedCondition];
                if (fillingNode.isTerminalNode() || currentAwaitedNode == null || currentAwaitedNode.isTerminalNode()) {
                    /* Override the current node or fill the empty place */
                    controlNode.setSuccessor(awaitedCondition, fillingNode instanceof CompositeNode ?
                            ((CompositeNode) fillingNode).getRootNode() : fillingNode);
                } else {
                    /* todo:        */
                    Node[] curAwaitedSuccessors = currentAwaitedNode.getSuccessors();
                    for (int i = 0; i < curAwaitedSuccessors.length; i++) {
                        Node curAwaitedSuccessor = curAwaitedSuccessors[i];
                        /* Clone filling node */
                        Node fillingNodeCopy = Node.clone(fillingNode);
                        /* Fill missing successors of fillingNode with the successor of current Awaited Node */
                        if (curAwaitedSuccessor != null) {
                            fillingNodeCopy.fillEmptySuccessorsWith(curAwaitedSuccessor);//todo: make this method recursive!!! Consider 3 subsequent conditions
                        }
                        /* Replace the current successor with a copy of fillingNode */
                        currentAwaitedNode.setSuccessor(i, fillingNodeCopy);
                    }

                }
            }

        }

        public void registerProcessedConditions(int... processedConditions) {
            if (this.processedConditions == null) {
                this.processedConditions = new boolean[controlNode.getSuccessors().length];
                /* Mark all conditions as UNPROCESSED */
                Arrays.fill(this.processedConditions, false);
            }
            for (int processedCondition : processedConditions) {
                this.processedConditions[processedCondition] = true;
            }
        }

    }

    /**
     * Adjusts CONSTANT LENGTHS to the lengths of the variables that use these constants directly.<br>
     * NB! Only adjusts named constrants!
     */
    protected class ConstantLengthAdjuster extends AbstractVisitor {
        /* Here only request processing of AbstractNodes(ParseTree) */
        public void visitEntity(Entity entity) throws Exception {}

        public void visitArchitecture(Architecture architecture) throws Exception {}

        public void visitProcess(Process process) throws Exception {
            process.getRootNode().traverse(this);
        }

        public void visitIfNode(IfNode ifNode) throws Exception {
            /* Check condition */
            adjustIfCondition(ifNode.getConditionExpression());

            /* Traverse TRUE part */
            ifNode.getTruePart().traverse(this);
            /* Traverse FALSE part */
            if (ifNode.getFalsePart() != null) {
                ifNode.getFalsePart().traverse(this);
            }
        }

        public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
            if (transitionNode.isNull()) return;

            AbstractOperand operand = transitionNode.getValueOperand();
            String absGraphVariableName = transitionNode.getVariableName();

            adjustOperands(new OperandImpl(absGraphVariableName), operand);
        }

        public void visitCaseNode(CaseNode caseNode) throws Exception {
            for (WhenNode whenNode : caseNode.getConditions()) {
                whenNode.traverse(this);
            }
        }

        public void visitWhenNode(WhenNode whenNode) throws Exception {
            whenNode.getTransitions().traverse(this);
        }

        private void adjustIfCondition(Expression conditionExpression) throws Exception {
            if (conditionExpression.isCompositeCondition()) {
                for (AbstractOperand operand : conditionExpression.getOperands()) {
                    adjustIfCondition((Expression) operand);
                }
            } else {
                /* Adjust CONDITIONAL OPERATORS only */
                if (conditionExpression.getOperator().isCondition()) {
                    List<AbstractOperand> operands = conditionExpression.getOperands();
                    if (operands.size() == 2) {
                        adjustOperands(operands.get(0), operands.get(1));
                    }
                }
            }
        }

        private void adjustOperands(AbstractOperand leftOperand, AbstractOperand rightOperand) throws Exception {
            /* Adjust Operands only if both are instances of OperandImpl */
            if (!(leftOperand instanceof OperandImpl && rightOperand instanceof OperandImpl)) return;
            
            String rightVariableName = ((OperandImpl) rightOperand).getName();
            if (!VHDLStructureParser.isConstant(rightVariableName)) {
                ConstantVariable constantVariable = modelCollector.getConstant(rightVariableName);
                if (constantVariable != null) {
                    AbstractVariable absGraphVariable = modelCollector.getVariable(((OperandImpl) leftOperand).getName());
                    if (absGraphVariable == null) {           
                        throw new Exception("Cannot adjust the length of the following constant: " + constantVariable.getName() + "." +
                                "\nVariable collector doesn't contain the following adjusting variable: " + ((OperandImpl) leftOperand).getName());
                    }

                    /* Adjust Constant Length */
                    Indices length = leftOperand.isParted()
                            ? leftOperand.getPartedIndices()
                            : absGraphVariable.getLength();
                    constantVariable.setLength(length);
                }
            }

        }
    }

    private class PartialSetVariableCollector extends AbstractVisitor {
        private final Set<Variable> processVariables;
        private Map<String, Set<OperandImpl>> partialSettingsMap = new HashMap<String, Set<OperandImpl>>();

        public PartialSetVariableCollector(Set<Variable> processVariables) {
            this.processVariables = processVariables;
        }

        /* Here only request processing of AbstractNodes(ParseTree) */
        public void visitEntity(Entity entity) throws Exception {}

        public void visitArchitecture(Architecture architecture) throws Exception {}

        public void visitProcess(Process process) throws Exception {
            process.getRootNode().traverse(this);
        }

        public void visitIfNode(IfNode ifNode) throws Exception {
            ifNode.getTruePart().traverse(this);
            if (ifNode.getFalsePart() != null) {
                ifNode.getFalsePart().traverse(this);
            }
        }

        public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
            if (transitionNode.isNull()) return;
            OperandImpl varOperand = transitionNode.getTargetOperand();
            if (varOperand.isParted()) {
                /* Get the set of parted variables */
                Set<OperandImpl> partedVarSet;
                /* Check that variable is already mapped and map it otherwise */
                String varOperandName = varOperand.getName();
                if (partialSettingsMap.containsKey(varOperandName)) {
                    partedVarSet = partialSettingsMap.get(varOperandName);
                } else {
                    partedVarSet = new HashSet<OperandImpl>();
                    partialSettingsMap.put(varOperandName, partedVarSet);
                }
                /* Add operand to set */
                partedVarSet.add(varOperand);
            }
        }

        public void visitCaseNode(CaseNode caseNode) throws Exception {
            for (WhenNode whenNode : caseNode.getConditions()) {
                whenNode.traverse(this);
            }
        }

        public void visitWhenNode(WhenNode whenNode) throws Exception {
            whenNode.getTransitions().traverse(this);
        }

        public Map<String, Set<OperandImpl>> getPartialSettingsMap() {
            doDisinterlapPartialSettings();
            return partialSettingsMap;
        }

        /**
         * Splits partial settings into non-interlaping regions
         */
        private void doDisinterlapPartialSettings() {

            /* For each parted variable... */
            for (String varName : partialSettingsMap.keySet()) {
                /* create a new set of parted variable operands... */
                HashSet<OperandImpl> newPartedVarSet = new HashSet<OperandImpl>();
                /* and fill it with non-intersecting parted variable operands. */

                /* Use 2 SortedSets: a separate one for START and END markers.
                 * 1) Place START and END markers into 2 SortedSets according to the following:
                 *      a) Starting index -> "i" for START and "i - 1" for END
                 *      b) Ending index   -> "i" for END and "i + 1" for START
                 * 2) Create intervals: take next marker from either SortedSet. Number of markers in both sets is equal.
                 * */
                SortedSet<Integer> startsSet = new TreeSet<Integer>();
                SortedSet<Integer> endsSet = new TreeSet<Integer>();
                int lowerBound = -1;
                int upperBound = modelCollector.getVariable(varName).getLength().length() /*getHighestSB() + 1*/;
                /* Fill SortedSets */
                for (OperandImpl partialSetOperand : partialSettingsMap.get(varName)) {
                    Indices partedIndices = partialSetOperand.getPartedIndices();
                    /* Starting index */
                    startsSet.add(partedIndices.getLowest());
                    if (partedIndices.getLowest() - 1 > lowerBound) {
                        endsSet.add(partedIndices.getLowest() - 1);
                    }
                    /* Ending index */
                    endsSet.add(partedIndices.getHighest());
                    if (partedIndices.getHighest() + 1 < upperBound) {
                        startsSet.add(partedIndices.getHighest() + 1);
                    }
                }
                /* Check number of markers */
                if (startsSet.size() != endsSet.size()) throw new RuntimeException("Unexpected bug occured while " +
                        "extracting non-intersecting regions for partial setting variables:" +
                        "\n Amounts of START and END markers are different.");
                /* Create intervals */
                Integer[] startsArray = new Integer[startsSet.size()];
                startsSet.toArray(startsArray);
                Integer[] endsArray = new Integer[endsSet.size()];
                endsSet.toArray(endsArray);
                for (int i = 0; i < startsArray.length; i++) {
                    newPartedVarSet.add(new OperandImpl(varName, new Indices(endsArray[i], startsArray[i]), false));
                }

                /* Replace the variable set in partigalSettingsMap with the new one */
                partialSettingsMap.put(varName, newPartedVarSet);
            }

        }
    }

    private class GraphVariableComparator implements Comparator<GraphVariable> {
        public int compare(GraphVariable o1, GraphVariable o2) {
            if (o1.getBaseVariable().getClass() != PartedVariable.class
                    || o2.getBaseVariable().getClass() != PartedVariable.class)
                throw new RuntimeException("GraphVariables are compered whose base variables are not instances of "
                        + PartedVariable.class.getSimpleName());
            /* Compare GraphVariables by the parted indices of their base variables */
            /* NB! Larger indices should be catted earlier, so swap the compared indices */
            return ((PartedVariable) o2.getBaseVariable()).getPartedIndices().compareTo(((PartedVariable) o1.getBaseVariable()).getPartedIndices());
        }
    }



//    public AbstractVariable getReplacingVariable(AbstractVariable initialVariable) {
//        //todo: if replacingChildren is Empty...
//        for (AbstractNode replacingNode : replacingChildren) {
//            TransitionNode replacingTransitionNode = (TransitionNode) replacingNode;
//            if (((OperandImpl) replacingTransitionNode.getValueOperand()).getName().equals(initialVariable.getName())) {
//                return modelCollector.getVariable(replacingTransitionNode.getVariableName());
//            }
//        }
//        return null;
//    }

}
