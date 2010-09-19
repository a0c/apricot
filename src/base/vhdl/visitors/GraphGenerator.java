package base.vhdl.visitors;

import base.HLDDException;
import base.hldd.structure.models.utils.ConditionGraphManager;
import base.hldd.structure.models.utils.ExtraConditionGraphManager;
import base.hldd.structure.nodes.utils.Condition;
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
import parsers.vhdl.OperandLengthSetter;
import ui.ConfigurationHandler;

import java.util.*;
import java.util.logging.Logger;

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

	private final ConfigurationHandler config;
    protected ModelManager modelCollector;
	private ConditionGraphManager conditionGraphManager;
	private ExtraConditionGraphManager extraConditionGraphManager;
    protected AbstractVariable graphVariable;
    protected Node graphVariableRootNode;
    private ContextManager contextManager = new ContextManager();

    /* AUXILIARY fields */
    private final boolean doFlattenConditions;
    private final boolean doCreateGraphsForCS;
    private final boolean doCreateSubGraphs;
    private final boolean isNullATransition;
    /* Set of processed graphVars. Used for skipping processed variables. */
    protected Set<String> processedGraphVars = new HashSet<String>();
    /* For each process, a set of partially set variables for those variables that are parted. */
    protected Map<Process, Map<String, Set<OperandImpl>>> partialSettingsMapByProcess
            = new HashMap<Process, Map<String, Set<OperandImpl>>>();

	public ModelManager getModelCollector() {
        return modelCollector;
    }

    public enum Type {
        BehDD, Beh
    }

    /**
     *
     * @param config optional settings from .config file
     * @param   useSameConstants <code>true</code> if the constant with the <u>same value</u> and <u>adjusted length</u> should be used
     *          for requested value. <code>false</code> if a new constant should be created for the same value, but a different length
     *          than already exists.
     *          <br>todo: <b>NB!</b> <code>true</code> possibility is not implemented yet. When implementing it, either
     *          modify getModelCollector() method to perform adjustment of partedIndices of terminal nodes in GraphVariables,
     *          or make the TerminalNode set its partedIndices itself.
	 * @param doFlattenConditions whether to inline Composite conditions into a set of Control Nodes (true value), or
 *        create a single node function (false value).
	 * @param doCreateGraphsForCS whether to create Conditional Graphs or not
	 * @param doCreateSubGraphs whether to create Extra Graphs or not
	 * @param generatorType generator's type. Used for initialization of isNullTransition only.
     */
    protected GraphGenerator(ConfigurationHandler config, boolean useSameConstants, boolean doFlattenConditions, boolean doCreateGraphsForCS, boolean doCreateSubGraphs, Type generatorType) {
		this.config = config;
		modelCollector = new ModelManager(useSameConstants);
        this.doFlattenConditions = doFlattenConditions;
        this.doCreateGraphsForCS = doCreateGraphsForCS;
		if (this.doCreateGraphsForCS) {
			conditionGraphManager = new ConditionGraphManager(modelCollector);
		}
		this.doCreateSubGraphs = doCreateSubGraphs;
		if (this.doCreateSubGraphs) {
			extraConditionGraphManager = new ExtraConditionGraphManager(modelCollector);
		}
		if (this.doCreateGraphsForCS && this.doCreateSubGraphs) { //todo: move validation to BusinessLogic. Don't pass all these booleans, use Properties instead.
			throw new RuntimeException("Illegal combination: Extended Conditions are mixed with Extra Extended Conditions. See GG.visitIfNode()...");
		}
		if (this.doFlattenConditions && this.doCreateSubGraphs) {
			throw new RuntimeException("Illegal combination: Expanded (!) Conditions are mixed with Extra Extended Conditions. See GG.visitIfNode()...");
		}
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
            if (config.isStateName(signal.getName())) flags.setState(true).setDelay(true);
            base.hldd.structure.variables.Variable signalVariable = new base.hldd.structure.variables.Variable(signal.getName(), signal.getType(), flags);
            modelCollector.addVariable(signalVariable);
        }

        /* Collect VARIABLES */
        for (Process process : architecture.getProcesses()) {
            Set<Variable> variables = process.getVariables();
            for (Variable variable : variables) {
                Flags flags = new Flags();
                if (config.isStateName(variable.getName())) flags.setState(true).setDelay(true);
                base.hldd.structure.variables.Variable newVariable = new base.hldd.structure.variables.Variable(variable.getName(), variable.getType(), flags);
                modelCollector.addVariable(newVariable);
            }
        }

		/* Adjust CONSTANT LENGTHS to the lengths of the variables that use these constants directly */
		adjustConstantAndOperandLengths(architecture);

        for (Process process : architecture.getProcesses()) {
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
     *                      {@link base.hldd.structure.models.utils.ModelManager#getConditionValuesCount(AbstractVariable)  cause 2 }
     */
    public void visitIfNode(IfNode ifNode) throws Exception {

        /*#################################################
        *       P R O C E S S     E X P R E S S I O N
        * #################################################*/
        Expression expression = ifNode.getConditionExpression();
        /* Extract dependentVariable */
        PartedVariableHolder depVariableHolder = doCreateGraphsForCS
				? conditionGraphManager.convertConditionToBooleanGraph(ifNode)
				: modelCollector.convertConditionalStmt(expression, doFlattenConditions);
		if (doCreateSubGraphs) {
			extraConditionGraphManager.generateExtraGraph(ifNode);
		}
        AbstractVariable dependentVariable = depVariableHolder.getVariable();
        Indices partedIndices = depVariableHolder.getPartedIndices();
        int conditionValueInt = depVariableHolder.getTrueValue();

        /* Create ControlNode */
        Node controlNode = dependentVariable instanceof CompositeFunctionVariable
                ? new CompositeNode((CompositeFunctionVariable) dependentVariable)
                : new Node.Builder(dependentVariable).partedIndices(partedIndices).createSuccessors(2).build();
        /* Add VHDL lines the node's been created from */
        controlNode.setSource(ifNode.getSource());

        /*#################################################
        *       P R O C E S S     T R U E  P A R T
        * #################################################*/
        /* Extract condition of dependentVariable: already DONE above using depVariableHolder */
        /* Create new Current Context and push it to Context Stack */
        contextManager.addContext(new Context(controlNode, Condition.createCondition(conditionValueInt)));
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
            contextManager.addContext(new Context(controlNode, Condition.createCondition(conditionValueInt) ));
            /* Process FALSE PART */
            ifNode.getFalsePart().traverse(this);
            /* Remove Current Context from stack */
            contextManager.removeContext();
        }

        /*#################################################
        *       F I N A L I Z E    C O N T R O L  N O D E
        * #################################################*/
		finalizeControlNode(controlNode);
        contextManager.fillCurrentContextWith(controlNode);


    }

	/**
	 * Adds all missing conditions so that all the control values become present in the node.
	 * Missing successors can be safely filled after this method has been invoked.
	 * @param controlNode node to finalize
	 * @throws HLDDException if 'others' could not be obtained
	 */
	private void finalizeControlNode(Node controlNode) throws HLDDException {
		if (controlNode.isTerminalNode() || controlNode.isEmptyControlNode()) return;

		Condition others = controlNode.getOthers();
		if (others == null) return;

		controlNode.setSuccessor(others, null);

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
				Indices graphPartedIndices = ((PartedVariable) graphVariable).getPartedIndices();
				/* Adjust partedIndices, if only a part of the valueOperand (including its partedIndices) is used:
				* in2(8) := '0';
				* in2 := "000000001";			// graphVariable is parted, e.g. in(3)
				* in2 := d_in;					// graphVariable is parted, e.g. in(3)
				* in2 := d_in3(9 downto 2) ;	// graphVariable is parted, e.g. in(3)
				* in2(3) := d_in3(5) ;
				* in2(3 downto 0) := d_in3(5 downto 2) ;
				* */
				if (isDirectPartialAssignment(graphPartedIndices, transitionNode.getTargetOperand().getPartedIndices())) {
					// in2(8) := '0';
					// in2(3 downto 0) := d_in3(5 downto 2) ;  // graphVariable is "in2(3 downto 0)"
					// Actually, absoluteFor() below covers this also, however constant's extractSubConstant() does not.
				} else {
					if (dependentVariable instanceof ConstantVariable) {
						/* v_out <= "0000"; // and looking for 'v_out(1)' */
						dependentVariable = modelCollector.extractSubConstant((ConstantVariable) dependentVariable, graphPartedIndices);
						partedIndices = null;
					} else {
						/* Subtract indices (get Absolute indices out of relative) */
						partedIndices = graphPartedIndices.absoluteFor(
										transitionNode.getTargetOperand().getPartedIndices(),
										partedIndices);
					}
				}
			}
            /* Create TerminalNode */
            Node terminalNode = new Node.Builder(dependentVariable).partedIndices(partedIndices).build();
            /* Add VHDL lines the node's been created from */
            terminalNode.setSource(transitionNode.getSource());
            /* Add TerminalNode to Current Context and remove Current Context from stack, if the stack is NOT empty.
             * If the stack is empty, initiate the root node */
            contextManager.fillCurrentContextWith(terminalNode);
        }
    }

	private boolean isDirectPartialAssignment(Indices partedGraph, Indices target) {
		return partedGraph.equals(target);
	}

    private boolean isGraphVariableSetIn(TransitionNode transitionNode) throws Exception {
        if (graphVariable == null) {
            if (transitionNode.isNull()) return false;
            /* GraphVariable to process has not been set yet. */
            /* Check whether the transition variable has been processed already.
            *  If not, then set graphVariable to this transition variable. */
            String transitionVarName = transitionNode.getTargetOperand().toString();
            if (!processedGraphVars.contains(transitionVarName)) {
				setGraphVariable(modelCollector.getVariable(transitionVarName));
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
        AbstractVariable dependentVariable = doCreateGraphsForCS
				? conditionGraphManager.convertConditionToGraph(caseNode)
				: modelCollector.convertOperandToVariable(variableOperand, null, false);
		if (doCreateSubGraphs) {
			extraConditionGraphManager.generateExtraGraph(caseNode);
		}
        Indices partedIndices = variableOperand.getPartedIndices();
        /* Count possible conditions of dependentVariable */
        //todo: suspicious action: dependentVariable.isState() ? caseNode.getConditions().size(). May be "when => others", may be "when A | B | C =>" ... consider these...
//        int conditionValuesCount = dependentVariable.isState() ? caseNode.getConditions().size() : modelCollector.getConditionValuesCount(dependentVariable);
        int conditionValuesCount = modelCollector.getConditionValuesCount(dependentVariable);
        /* Create Control Node */
        Node controlNode = new Node.Builder(dependentVariable).partedIndices(partedIndices).createSuccessors(conditionValuesCount).build();
        /* Add VHDL lines the node's been created from */
        controlNode.setSource(caseNode.getSource()); //todo: inline with the creation process above...

        /*#################################################
        *       P R O C E S S     C O N D I T I O N S
        * #################################################*/
        /* Create new Current Context (without awaitedCondition) and add it to Context Stack */
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
		finalizeControlNode(controlNode);
        contextManager.fillCurrentContextWith(controlNode);

    }

    public void visitWhenNode(WhenNode whenNode) throws Exception {
        /*#################################################
        *       P R O C E S S     C O N D I T I O N
        * #################################################*/
        doCheckWhenTransitions(whenNode);

        if (!whenNode.isOthers()) {
            /* Extract whenConditions */
            Condition whenCondition = modelCollector.convertOperandsToCondition(whenNode.getConditionOperands());
			/* Get controlNode from Current Context.
             * Create new Context and add it to Context Stack */
			Node controlNode = contextManager.getCurrentContext().getControlNode();
			Condition awaitedCondition = !doCreateGraphsForCS ? whenCondition
					: conditionGraphManager.mapDirect((GraphVariable) controlNode.getDependentVariable(), whenCondition);
			contextManager.addContext(new Context(controlNode, awaitedCondition));

            /*#################################################
            *       P R O C E S S     T R A N S I T I O N S
            * #################################################*/
            whenNode.getTransitions().traverse(this);
            /* Remove Current Context from stack */
            contextManager.removeContext();
        } else {
            /* For OTHERS, substitute OTHERS in whenNode with ALL UNPROCESSED conditions and process the node */
			String[] conditionsAsString = contextManager.getCurrentContext().getOthersConditions();
			if (conditionsAsString == null) {
				return;
			}
			whenNode.setConditions(conditionsAsString);
			visitWhenNode(whenNode);
            /* Restore condition */
            whenNode.setConditions("OTHERS");
        }
    }

    @Deprecated
    /* Beh DD tree can have multiple nodes (partial setting variables).
    Though, it still cannot set multiple variable in one process tree. */
    protected abstract void doCheckWhenTransitions(WhenNode whenNode) throws Exception;

    private void adjustConstantAndOperandLengths(Architecture architecture) throws Exception {
        /* Adjust CONSTANT LENGTHS to the lengths of the variables that use these constants directly */
        architecture.traverse(new ConstantLengthAdjuster());
		/* Adjust OPERAND LENGTHS */
		architecture.traverse(new OperandLengthAdjuster());
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
		setGraphVariable(initGraphVariable);
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

	private void setGraphVariable(AbstractVariable newGraphVariable) {
		graphVariable = newGraphVariable;
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
				setGraphVariable(new PartedVariable(partSetOperand.getName(), wholeVariable.getType(), partSetOperand.getPartedIndices()));
				graphVariableRootNode = null;
				contextManager.clear();
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
            catExpession.addOperand(new OperandImpl(baseVariable.getPureName(), baseVariable.getPartedIndices(), false));
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
			/* Keep track of condition, even if it's empty (!wasFilled()), to preserve the original VHDL structure.
			* For branches like 'WHEN PREFIX_Y | PREFIX_D | PREFIX_D_Y =>'), that don't have a transition in them,
			* missing successor will be further filled once for the whole Condition,
			* not for every separate value. Keep track by initializing to null (Condition will be stored). */
			Context currentContext = getCurrentContext();
			if (currentContext != null && !currentContext.isCaseContext() && !currentContext.wasFilled()) {
				currentContext.fill(null);
			}
            /* Remove Current Context */
            contextStack.pop();
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
            if (fillingNode.isControlNode()) { // todo: obsolete check
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
                    fillEmptySuccessorsFor(getCurrentContext().getAwaitedNode());
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
         * @param nodeToFill where to fill empty successors
         * @throws Exception {@link base.hldd.structure.nodes.Node#isEmptyControlNode()},
         *                   {@link Node#fillEmptySuccessorsWith(base.hldd.structure.nodes.Node)}
         */
        private void fillEmptySuccessorsFor(Node nodeToFill) throws Exception {
                /* For non-empty ControlNodes fill the missing successors with the latest defaultValue */
                if (nodeToFill != null &&
                        nodeToFill.isControlNode() && !nodeToFill.isEmptyControlNode()) { // todo: consider removing 2nd and 3rd condition from here, if previous checks filter "bad" nodes out 
                    nodeToFill.fillEmptySuccessorsWith(getDefaultValueNode());
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
                return new Node.Builder(graphVariable).build();
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
        private final Condition awaitedCondition;

		public Context(Node controlNode, Condition awaitedCondition) {
            this.controlNode = controlNode;
            this.awaitedCondition = awaitedCondition;
        }

        /**
         * Constructor to be used by {@link CaseNode}
         * @param controlNode conditional node
         */
        public Context(Node controlNode) {
            this.controlNode = controlNode;
            awaitedCondition = null;
        }

        public Node getControlNode() {
            return controlNode;
        }

        /**
         * @return node that is being filled
         */
        public Node getAwaitedNode() {
			return controlNode.getSuccessor(awaitedCondition);
		}

        /**
         *
         * @param fillingNode node to fill the current context with
         * @throws Exception cause {@link base.hldd.structure.nodes.Node#setSuccessor(Condition, base.hldd.structure.nodes.Node) cause }
         */
        public void fill(Node fillingNode) throws Exception {
			/* Override the current node or fill the empty place */
			controlNode.setSuccessor(awaitedCondition, (fillingNode != null && fillingNode instanceof CompositeNode) ?
					((CompositeNode) fillingNode).getRootNode() : fillingNode);
		}

		public boolean wasFilled() throws HLDDException {
			return !controlNode.isEmptyControlNode() && getAwaitedNode() != null; // check for emptiness in order to avoid useless LOGGING in Node.getSuccessor()  
		}

		public boolean isCaseContext() {
			return awaitedCondition == null;
		}

		public String[] getOthersConditions() throws HLDDException {
			Condition others = controlNode.getOthers();
			if (doCreateGraphsForCS) {
				others = conditionGraphManager.mapReverse(((GraphVariable) controlNode.getDependentVariable()), others);
			}
			return others == null ? null : others.toStringArray();
		}
	}

    /**
     * Adjusts CONSTANT LENGTHS to the lengths of the variables that use these constants directly.<br>
     * NB! Only adjusts named constrants!
     */
    private class ConstantLengthAdjuster extends AbstractVisitor {
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

            AbstractOperand valueOperand = transitionNode.getValueOperand();
            String targetName = transitionNode.getTargetOperand().getName();

            adjustOperands(new OperandImpl(targetName), valueOperand);
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
            
            String rightVarName = ((OperandImpl) rightOperand).getName();
			String leftVarName = ((OperandImpl) leftOperand).getName();
			ConstantVariable constantVariable = modelCollector.getConstant(rightVarName);
			if (constantVariable != null) {
				AbstractVariable leftVariable = modelCollector.getVariable(leftVarName);
				if (leftVariable == null) {
					throw new Exception("Cannot adjust the length of the following constant: " + constantVariable.getName() + "." +
							"\nVariable collector doesn't contain the following adjusting variable: " + leftVarName);
				}
				
				/* Adjust Constant Length */
				Indices length = leftOperand.isParted() ? leftOperand.getPartedIndices() : leftVariable.getLength();
				constantVariable.setLength(length);
			}
		}
    }
	
	private class OperandLengthAdjuster extends AbstractVisitor {
		
		@Override
		public void visitEntity(Entity entity) throws Exception {}
		@Override
		public void visitArchitecture(Architecture architecture) throws Exception {
			architecture.getTransitions().traverse(this);
		}
		@Override
		public void visitProcess(Process process) throws Exception {
			process.getRootNode().traverse(this);
		}
		@Override
		public void visitIfNode(IfNode ifNode) throws Exception {
			Expression conditionExpression = ifNode.getConditionExpression();
			doSetLengthFor(conditionExpression);
			
			ifNode.getTruePart().traverse(this);
			
			if (ifNode.getFalsePart() != null) {
				ifNode.getFalsePart().traverse(this);
			}
		}
		@Override
		public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
			if (transitionNode.isNull()) {
				return;
			}
			doSetLengthFor(transitionNode.getValueOperand());
		}
		@Override
		public void visitCaseNode(CaseNode caseNode) throws Exception {
			for (WhenNode whenNode : caseNode.getConditions()) {
				whenNode.traverse(this);
			}
		}
		@Override
		public void visitWhenNode(WhenNode whenNode) throws Exception {
			whenNode.getTransitions().traverse(this);
		}
		
		private void doSetLengthFor(AbstractOperand operand) throws Exception {
			if (operand.getLength() == null) {
				new OperandLengthSetter(modelCollector, operand); //todo: consider: v_out <= "0000"; // v_out may be 3:0 and may be 0:3
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
