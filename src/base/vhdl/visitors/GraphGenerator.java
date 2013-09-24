package base.vhdl.visitors;

import base.*;
import base.hldd.structure.Flags;
import base.hldd.structure.models.BehModel;
import base.hldd.structure.models.utils.*;
import base.hldd.structure.models.utils.ModelManager.CompositeFunctionVariable;
import base.hldd.structure.nodes.CompositeNode;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.*;
import base.hldd.visitors.ObsoleteResetRemoverImpl;
import base.vhdl.structure.*;
import base.vhdl.structure.Process;
import base.vhdl.structure.Variable;
import base.vhdl.structure.nodes.*;
import base.vhdl.structure.utils.OperandStorage;
import parsers.vhdl.OperandLengthSetter;
import ui.ConfigurationHandler;
import ui.ConverterSettings;
import ui.ExtendedException;

import java.util.*;
import java.util.logging.Logger;

/**
 * Generates graphs out of VHDL Structure (Entity)
 * <p/>
 * IfNode and CaseNode policy:<br>
 * If the condition has a missing successor, then it is filled
 * with a terminal node based on a concrete dependent Variable
 * ( usually to make the graph retain its value ).<br>
 * Possibility is implemented as method fillEmptySuccessorsWith() of class Node.
 * <p/>
 * <b>NB!</b>  Trimming of redundant nodes is performed on the VHDL
 * Structure (Entity) using visitors (IfNodeSimplifier).
 *
 * @author Anton Chepurov
 */
public abstract class GraphGenerator extends AbstractVisitor {

	protected static final Logger LOGGER = Logger.getLogger(GraphGenerator.class.getName());

	public static final int MAX_DYNAMIC_RANGE_ALLOWED = 64;

	private final ConfigurationHandler config;
	private final ConverterSettings settings;
	private final Collection<Constant> generics;
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

	public ModelManager getModelCollector() {
		return modelCollector;
	}

	public enum GeneratorType {
		BehaviouralDD, Behavioural
	}

	/**
	 * @param config		optional settings from .config file
	 * @param settings	  base settings for conversion
	 * @param generics	  from component instantiation
	 * @param generatorType generator's type. Used for initialization of isNullTransition only.
	 */
	protected GraphGenerator(ConfigurationHandler config, ConverterSettings settings, Collection<Constant> generics, GeneratorType generatorType) {
		this.config = config;
		this.settings = settings;
		this.generics = generics != null ? generics : Collections.<Constant>emptyList();
		modelCollector = new ModelManager();
		this.doFlattenConditions = settings.isDoFlattenConditions();
		this.doCreateGraphsForCS = settings.isDoCreateCSGraphs(); // todo: should be represented as Enum (CSMode or ConditionalStatementMode)
		if (this.doCreateGraphsForCS) {
			conditionGraphManager = new ConditionGraphManager(modelCollector);
		}
		this.doCreateSubGraphs = settings.isDoCreateExtraCSGraphs();
		if (this.doCreateSubGraphs) {
			extraConditionGraphManager = new ExtraConditionGraphManager(modelCollector);
		}
		if (this.doCreateGraphsForCS && this.doCreateSubGraphs) { //todo: move validation to BusinessLogic. Don't pass all these boolean-s, use Properties instead.
			throw new RuntimeException("Illegal combination: Extended Conditions are mixed with Extra Extended Conditions. See GG.visitIfNode()...");
		}
		if (this.doFlattenConditions && this.doCreateSubGraphs) {
			throw new RuntimeException("Illegal combination: Expanded (!) Conditions are mixed with Extra Extended Conditions. See GG.visitIfNode()...");
		}
		isNullATransition = generatorType == GeneratorType.BehaviouralDD;
	}

	public void visitEntity(Entity entity) throws Exception {

		/* Collect CONSTANTS */
		collectConstants(entity.getConstants());
		collectConstants(mergeGenerics(entity.getGenericConstants(), generics)); // override default generics with instantiated generics

		/* Process PORTS */
		Set<Port> ports = entity.getPorts();
		for (Port port : ports) {
			/* Skip CLOCKING port */
			if (config.isClockName(port.getName())) continue;
			Flags flags = new Flags().setInput(port.isInput()).setOutput(!port.isInput()).setReset(config.isResetName(port.getName()));
			base.hldd.structure.variables.Variable portVariable = new base.hldd.structure.variables.Variable(port.getName(), port.getType(), flags);
			modelCollector.addVariable(portVariable);
		}

	}

	private Collection<Constant> mergeGenerics(Set<Constant> genericConstants, Collection<Constant> instanceGenerics) {

		Map<String, Constant> mergedGenerics = new HashMap<String, Constant>(genericConstants.size() + instanceGenerics.size());

		for (Constant instGeneric : instanceGenerics) {
			mergedGenerics.put(instGeneric.getName(), instGeneric);
		}

		for (Constant generic : genericConstants) {
			String name = generic.getName();
			if (!mergedGenerics.containsKey(name)) {
				mergedGenerics.put(name, generic);
			}
		}
		return mergedGenerics.values();
	}

	public void visitArchitecture(Architecture architecture) throws Exception {

		/* Collect CONSTANTS */
		collectConstants(architecture.getConstants());
		for (Process process : architecture.getProcesses()) {
			collectConstants(process.getConstants());
		}

		/* Collect SIGNALS */
		Set<Signal> signals = architecture.getSignals();
		for (Signal signal : signals) {
			Flags flags = new Flags();
			if (config.isStateName(signal.getName())) flags.setState(true).setDelay(true);
			base.hldd.structure.variables.Variable signalVariable = new base.hldd.structure.variables.Variable(signal.getName(), signal.getType(), flags);
			setDefaultValue(signalVariable, signal.getDefaultValue(), signal.getName());
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

		createDynamicRangeReadGraphs(architecture);

		List<base.vhdl.structure.nodes.CompositeNode> rootNodes = new LinkedList<base.vhdl.structure.nodes.CompositeNode>();
		rootNodes.add(architecture.getTransitions());
		for (Process process : architecture.getProcesses()) {
			rootNodes.add(process.getRootNode());
		}
		processRangeAssignments(collectRangeAssignments(architecture), rootNodes);
		/* Process process-external transitions of the Architecture */
		for (AbstractNode archTransNode : architecture.getTransitions().getChildren()) {
			couldProcessNextGraphVariable(null, archTransNode);
		}

		/* Process COMPONENTS */
		for (ComponentInstantiation component : architecture.getComponents()) {
			loadComponent(component);
		}
	}

	private void setDefaultValue(base.hldd.structure.variables.Variable signalVariable, OperandImpl defaultValueOperand, String signalName) throws Exception {

		if (defaultValueOperand == null) {
			return;
		}

		AbstractVariable defaultValue = modelCollector.convertOperandToVariable(defaultValueOperand, signalVariable.getType(), true, null);

		if (!(defaultValue instanceof ConstantVariable)) {
			throw new ExtendedException("Non-constant DEFAULT VALUE variable created for signal " + signalName +
					"\nExpected: ConstantVariable. Actual: " + defaultValue.getClass().getSimpleName(),
					ExtendedException.ERROR_TEXT);
		}

		signalVariable.setDefaultValue((ConstantVariable) defaultValue);
	}

	private void loadComponent(ComponentInstantiation component) throws ExtendedException {

		BehModel model = ComponentLoader.loadModel(component, settings);

		new ComponentMerger(component, model).mergeTo(modelCollector);
	}

	private void createDynamicRangeReadGraphs(Architecture architecture) throws Exception {

		DynamicRangeReadsCollector dynamicRangeReadsCollector = new DynamicRangeReadsCollector();

		architecture.traverse(dynamicRangeReadsCollector);

		for (DynamicRangeReadsCollector.Read dynamicRead : dynamicRangeReadsCollector.getReads()) {

			OperandImpl dynamicRangeRead = dynamicRead.operand;
			SourceLocation source = dynamicRead.source;

			AbstractVariable wholeVariable = modelCollector.getVariable(dynamicRangeRead.getName());
			Type type = wholeVariable.getType().deriveRangeType(Range.BIT_RANGE);
			String dynRangeReadName = OperandImpl.generateNameForDynamicRangeRead(dynamicRangeRead);
			AbstractVariable baseVariable = new base.hldd.structure.variables.Variable(dynRangeReadName, type);

			if (isMemoryVariableRequired(wholeVariable)) {
				modelCollector.createMemory(dynamicRangeRead, source, dynRangeReadName);
				continue;
			}

			Node rootNode = createDynamicNode(dynamicRangeRead, source);
			for (int i = 0; i < rootNode.getConditionValuesCount(); i++) {
				Node successor = new Node.Builder(wholeVariable).range(new Range(i, i)).build();
				rootNode.setSuccessor(Condition.createCondition(i), successor);
			}
			modelCollector.createAndReplaceNewGraph(baseVariable, rootNode, false);
		}
	}

	private boolean isMemoryVariableRequired(AbstractVariable variable) {
		return variable.getType().getLength().length() > MAX_DYNAMIC_RANGE_ALLOWED;
	}

	private OperandStorage collectRangeAssignments(Architecture architecture) throws Exception {

		RangeAssignmentCollector rangeAssignmentCollector = new RangeAssignmentCollector(modelCollector);

		architecture.traverse(rangeAssignmentCollector);

		return rangeAssignmentCollector.getRangeAssignments();
	}

	private void collectConstants(Collection<Constant> constants) throws Exception {
		for (Constant constant : constants) {
			ConstantVariable constantVariable = new ConstantVariable(constant.getName(), constant.getValue(), constant.getType());
			modelCollector.addVariable(constantVariable);
		}
	}

	/**
	 * @param ifNode IfNode being visited
	 * @throws Exception {@link base.hldd.structure.nodes.Node#isEmptyControlNode() cause1 }
	 */
	public void visitIfNode(IfNode ifNode) throws Exception {

		/*#################################################
		*       P R O C E S S     E X P R E S S I O N
		* #################################################*/
		Expression expression = ifNode.getConditionExpression();
		SourceLocation source = ifNode.getSource();
		/* Extract dependentVariable */
		RangeVariableHolder depVariableHolder = doCreateGraphsForCS
				? conditionGraphManager.convertConditionToBooleanGraph(ifNode, source)
				: modelCollector.convertConditionalStmt(expression, doFlattenConditions, source);
		if (doCreateSubGraphs) {
			extraConditionGraphManager.generateExtraGraph(ifNode, source);
		}
		AbstractVariable dependentVariable = depVariableHolder.getVariable();
		Range range = depVariableHolder.getRange();
		int conditionValueInt = depVariableHolder.getTrueValue();

		/* Create ControlNode */
		Node controlNode = dependentVariable instanceof CompositeFunctionVariable
				? new CompositeNode((CompositeFunctionVariable) dependentVariable)
				: new Node.Builder(dependentVariable).range(range).createSuccessors(2).build();
		/* Add VHDL lines the node's been created from */
		controlNode.setSource(source);

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
			contextManager.addContext(new Context(controlNode, Condition.createCondition(conditionValueInt)));
			/* Process FALSE PART */
			ifNode.getFalsePart().traverse(this);
			/* Remove Current Context from stack */
			contextManager.removeContext();
		}

		/*#################################################
		*       F I N A L I Z E    C O N T R O L  N O D E
		* #################################################*/
		insertControlNode(controlNode);
	}

	private void insertControlNode(Node controlNode) throws Exception {
		finalizeControlNode(controlNode);
		contextManager.fillCurrentContextWith(controlNode);
	}

	/**
	 * Adds all missing conditions so that all the control values become present in the node.
	 * Missing successors can be safely filled after this method has been invoked.
	 *
	 * @param controlNode node to finalize
	 * @throws HLDDException if 'others' could not be obtained
	 */
	private void finalizeControlNode(Node controlNode) throws HLDDException {
		if (controlNode.isTerminalNode() || controlNode.isEmptyControlNode()) return;

		Condition others = controlNode.getOthers();
		if (others == null) return;

		controlNode.setSuccessor(others, null);
	}

	/**
	 * Behavioural DD tree cannot set multiple variables in one process tree.
	 * Though, it still can have multiple nodes (range assignment variables).
	 *
	 * @param ifNode true part of which to be checked
	 * @throws Exception if check fails
	 */
	protected abstract void doCheckTruePart(IfNode ifNode) throws Exception;

	/**
	 * Behavioural DD tree cannot set multiple variables in one process tree.
	 * Though, it still can have multiple nodes (range assignment variables).
	 *
	 * @param ifNode false part of which to be checked
	 * @throws Exception if check fails
	 */
	protected abstract void doCheckFalsePart(IfNode ifNode) throws Exception;

	/**
	 * @param transitionNode Transition Node being visited
	 * @throws Exception {@link GraphGenerator.Context#fill(base.hldd.structure.nodes.Node) cause }
	 */
	public void visitTransitionNode(TransitionNode transitionNode) throws Exception {

		/* Only process TerminalNodes that set the graphVariable */
		if (isGraphVariableSetIn(transitionNode)) {

			SourceLocation source = transitionNode.getSource();
			/* ######### Create TERMINAL NODE ############*/

			/* Extract dependentVariable and range */
			AbstractVariable dependentVariable = transitionNode.isNull()
					? graphVariable // Retain value
					: modelCollector.convertOperandToVariable(transitionNode.getValueOperand(), graphVariable.getType(), true, source);
			/* branch <= not CCR(CBIT); =====> don't take range into account, they were already used during Function creation */
			Range range = dependentVariable instanceof FunctionVariable && ((FunctionVariable) dependentVariable).getOperator() == Operator.INV
					? null : transitionNode.getValueOperandRange();
			boolean isDynamicRange = !transitionNode.isNull() && transitionNode.getTargetOperand().isDynamicRange();
			if (graphVariable instanceof RangeVariable && !transitionNode.isNull() && !isDynamicRange) {
				Range graphRange = ((RangeVariable) graphVariable).getRange();
				/* Adjust range, if only a part of the valueOperand (including its range) is used:
				* in2(8) := '0';
				* in2 := "000000001";			// graphVariable is a range, e.g. in(3)
				* in2 := d_in;					// graphVariable is a range, e.g. in(3)
				* in2 := d_in3(9 downto 2) ;	// graphVariable is a range, e.g. in(3)
				* in2(3) := d_in3(5) ;
				* in2(3 downto 0) := d_in3(5 downto 2) ;
				* */
				if (isDirectRangeAssignment(graphRange, transitionNode.getTargetOperand().getRange())) {
					// in2(8) := '0';
					// in2(3 downto 0) := d_in3(5 downto 2) ;  // graphVariable is "in2(3 downto 0)"
					// Actually, absoluteFor() below covers this also, however constant 's extractSubConstant() does not.
				} else {
					if (dependentVariable instanceof ConstantVariable) {
						/* v_out <= "0000"; // and looking for 'v_out(1)' */
						dependentVariable = modelCollector.extractSubConstant((ConstantVariable) dependentVariable, graphRange);
						range = null;
					} else {
						/* Subtract ranges (get Absolute range out of relative) */
						range = graphRange.absoluteFor(
								transitionNode.getTargetOperand().getRange(),
								range);
					}
				}
			}
			/* Create TerminalNode */
			Node terminalNode = new Node.Builder(dependentVariable).range(range).build();
			/* Add VHDL lines the node's been created from */
			terminalNode.setSource(source);
			if (isDynamicRange) {
				if (isMemoryVariableRequired(graphVariable)) {
					insertIndexNode(transitionNode.getTargetOperand(), terminalNode);
					((base.hldd.structure.variables.Variable) graphVariable).setMemory(true);
				} else {
					insertDynamicNode(transitionNode.getTargetOperand(), terminalNode);
				}
				return;
			}
			/* Add TerminalNode to Current Context and remove Current Context from stack, if the stack is NOT empty.
			* If the stack is empty, initiate the root node */
			contextManager.fillCurrentContextWith(terminalNode);
		}
	}

	private void insertIndexNode(OperandImpl targetOperand, Node terminalNode) throws Exception {
		Node controlNode = createDynamicNode(targetOperand, terminalNode.getSource());
		Condition others = controlNode.getOthers();

		contextManager.addContext(new Context(controlNode, others));
		contextManager.fillCurrentContextWith(terminalNode);
		contextManager.removeContext();

		insertControlNode(controlNode);
	}

	private void insertDynamicNode(OperandImpl targetOperand, Node terminalNode) throws Exception {
		Node controlNode = createDynamicNode(targetOperand, terminalNode.getSource());
		Range range = ((RangeVariable) graphVariable).getRange();
		if (range.length() != 1) {
			throw new Exception("Dynamic node can be inserted for single bit graph only. Actual: " + graphVariable.getName());
		}
		int condition = range.getHighest();

		contextManager.addContext(new Context(controlNode, Condition.createCondition(condition)));
		contextManager.fillCurrentContextWith(terminalNode);
		contextManager.removeContext();

		insertControlNode(controlNode);
	}

	private Node createDynamicNode(OperandImpl dynamicRangeOperand, SourceLocation source) {
		int wholeLength = modelCollector.getVariable(dynamicRangeOperand.getName()).getLength().length();
		OperandImpl dynamicRange = dynamicRangeOperand.getDynamicRange();
		AbstractVariable dynamicVariable = modelCollector.getVariable(dynamicRange.getName());
		Range dynRange = dynamicRange.getRange();
		int conditionsCount = dynamicVariable.getType().countPossibleValues(dynRange, wholeLength);
		return new Node.Builder(dynamicVariable).range(dynRange).
				createSuccessors(conditionsCount).source(source).build();
	}

	private boolean isDirectRangeAssignment(Range graphRange, Range targetRange) {
		return graphRange.equals(targetRange);
	}

	private boolean isGraphVariableSetIn(TransitionNode transitionNode) throws Exception {
		if (graphVariable == null) {
			if (transitionNode.isNull()) return false;
			/* GraphVariable to process has not been set yet. */
			OperandImpl targetOperand = transitionNode.getTargetOperand();
			String transitionVarName = targetOperand.isDynamicRange() ? targetOperand.getName() : targetOperand.toString();
			if (processedGraphVars.contains(transitionVarName)) return false;

			setGraphVariable(modelCollector.getVariable(transitionVarName));
			if (graphVariable == null) {
				return false;//todo: commented for DEMO. (Informs that the initial variable of range assignment variables is not found or the like... In any way, has something to do with range assignment variables)
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

		} else return isVariableSetIn(transitionNode, graphVariable, isNullATransition, modelCollector);
	}

	static boolean isVariableSetIn(TransitionNode transitionNode, AbstractVariable variable, boolean isNullATransition, TypeResolver typeResolver) {

		if (transitionNode.isNull()) {
			return isNullATransition;
		}

		OperandImpl targetOperand = transitionNode.getTargetOperand();

		Range variableRange = variable instanceof RangeVariable ? ((RangeVariable) variable).getRange() : null;
		OperandImpl variableOperand = new OperandImpl(variable.getPureName(), variableRange, false);
		return targetOperand.contains(variableOperand, typeResolver);
	}

	public void visitCaseNode(CaseNode caseNode) throws Exception {
		/*#################################################
		*       P R O C E S S     C O N D I T I O N
		* #################################################*/
		SourceLocation source = caseNode.getSource();
		/* Extract dependentVariable */
		AbstractOperand variableOperand = caseNode.getVariableOperand();
		AbstractVariable dependentVariable = doCreateGraphsForCS
				? conditionGraphManager.convertConditionToGraph(caseNode, source)
				: modelCollector.convertOperandToVariable(variableOperand, null, false, source);
		if (doCreateSubGraphs) {
			extraConditionGraphManager.generateExtraGraph(caseNode, source);
		}
		Range range = variableOperand.getRange();
		/* Count possible conditions of dependentVariable */
		//todo: suspicious action: dependentVariable.isState() ? caseNode.getConditions().size(). May be "when => others", may be "when A | B | C =>" ... consider these...
//		int conditionValuesCount = dependentVariable.isState() ? caseNode.getConditions().size() : modelCollector.countPossibleValues(dependentVariable);
		int conditionValuesCount = dependentVariable.getType().countPossibleValues(range);
		/* Create Control Node */
		Node controlNode = new Node.Builder(dependentVariable).range(range).createSuccessors(conditionValuesCount).build();
		/* Add VHDL lines the node's been created from */
		controlNode.setSource(source); //todo: inline with the creation process above...

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
		insertControlNode(controlNode);
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

	/**
	 * Behavioural DD tree cannot set multiple variables in one process tree.
	 * Though, it still can have multiple nodes (range assignment variables).
	 *
	 * @param whenNode transitions of which to be checked
	 * @throws Exception if check fails
	 */
	protected abstract void doCheckWhenTransitions(WhenNode whenNode) throws Exception;

	private void adjustConstantAndOperandLengths(Architecture architecture) throws Exception {
		/* Adjust CONSTANT LENGTHS to the lengths of the variables that use these constants directly */
		architecture.traverse(new ConstantLengthAdjuster());
		/* Adjust OPERAND LENGTHS */
		architecture.traverse(new OperandLengthAdjuster());
	}

	/**
	 * @param initGraphVariable variable to initialize {@link #graphVariable} with
	 * @param rootNode		  where to start processing traversal from
	 * @return <code>true</code> if next graph variable was processed.
	 *         <code>false</code> if no graph variables could be found to
	 *         process during traversal of the specified rootNode
	 * @throws Exception if {@link AbstractNode#traverse(AbstractVisitor)} throws an Exception
	 */
	protected boolean couldProcessNextGraphVariable(AbstractVariable initGraphVariable, AbstractNode rootNode) throws Exception {

		setGraphVariable(initGraphVariable);
		graphVariableRootNode = null;
		contextManager.clear();
		rootNode.traverse(this);

		if (graphVariableRootNode == null) {
			graphVariableRootNode = getDefaultValueNode();
			if (graphVariableRootNode == null) {
				return false;
			}
		}
		/* Create new GraphVariable and replace old one */
		GraphVariable newGraphVariable = modelCollector.createAndReplaceNewGraph(graphVariable, graphVariableRootNode,
				isDelay(new OperandImpl(graphVariable.getName())));

		/* Remove redundant resets */
		graphVariableRootNode.traverse(new ObsoleteResetRemoverImpl(newGraphVariable));

		return true;
	}

	private void setGraphVariable(AbstractVariable newGraphVariable) {
		graphVariable = newGraphVariable;
	}

	protected abstract boolean isDelay(OperandImpl operand);

	protected void processRangeAssignments(OperandStorage rangeAssignments,
										   Collection<base.vhdl.structure.nodes.CompositeNode> rootNodes) throws Exception {
		/* For each variable that has range assignments in process, traverse the tree once for each range assignment variable */
		for (OperandStorage.Item item : rangeAssignments.getItems()) {
			String varName = item.name;
			Set<OperandImpl> rangeOperands = item.operands;
			AbstractVariable wholeVariable = modelCollector.getVariable(varName); // base variable of range assignment variables
			/* For each range assignment variable, traverse the tree */
			for (OperandImpl rangeOperand : rangeOperands) {
				/* Create new variable */ //todo: may be substitute with couldProcessNextGraphVariable(). Check ModelCollector.replace() to act equally to modelCollector.addVariable() met below:
				setGraphVariable(modelCollector.getVariable(rangeOperand.toString()));
				graphVariable.setDefaultValue(modelCollector.extractSubConstant(wholeVariable.getDefaultValue(), rangeOperand.getRange()));
				graphVariableRootNode = null;
				contextManager.clear();

				for (base.vhdl.structure.nodes.CompositeNode rootNode : rootNodes) {
					rootNode.traverse(this);
					if (graphVariableRootNode != null) {
						break;
					}
				}
				if (graphVariableRootNode == null) {
					graphVariableRootNode = getDefaultValueNode();
					if (graphVariableRootNode == null) {
						/* will be set in component, hopefully */
						continue;
					}
				}
				modelCollector.createAndReplaceNewGraph(graphVariable, graphVariableRootNode, isDelay(rangeOperand));
				//todo: Remove redundant resets?
				processedGraphVars.add(rangeOperand.toString());
			}

			if (wholeVariable.getType().isArray()) {
				modelCollector.flattenVariableToBits(wholeVariable);
			}

			processedGraphVars.add(varName);
		}
	}

	private Node getDefaultValueNode() {

		if (graphVariable == null) {
			return null;
		}

		AbstractVariable defaultValueVariable = graphVariable.getDefaultValue();

		if (defaultValueVariable == null) {
			return null;
		}

		return new Node.Builder(defaultValueVariable).build();
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
					LOGGER.warning("Rewriting graphVariableRootNode for " + graphVariable.getName() + " and the defaultValueStack is not empty.");
				}
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
					&& getCurrentContext() == getDefaultValueContext()) {
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
		 *
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
		 *
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
	 * NB! Only adjusts named constants!
	 */
	private class ConstantLengthAdjuster extends AbstractVisitor {

		public void visitEntity(Entity entity) throws Exception {
		}

		public void visitArchitecture(Architecture architecture) throws Exception {
		}

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
				Range length = leftOperand.isRange() ? leftOperand.getRange() : leftVariable.getLength();
				Range constLength = constantVariable.getLength();
				if (length.contains(constLength)) { // only expand the length, never narrow it
					constantVariable.setLength(length);
				} else {
					leftVariable.getType().setLength(constLength);
				}
			}
		}
	}

	private class OperandLengthAdjuster extends AbstractVisitor {

		@Override
		public void visitEntity(Entity entity) throws Exception {
		}

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
}
