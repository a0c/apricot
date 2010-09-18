package base.hldd.structure.models.utils;

import base.HLDDException;
import base.SourceLocation;
import base.Type;
import base.hldd.structure.Flags;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.*;
import base.vhdl.structure.AbstractOperand;
import base.vhdl.structure.Operator;
import base.vhdl.structure.nodes.AbstractNode;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.WhenNode;

import java.math.BigInteger;
import java.util.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 18.05.2010
 * <br>Time: 19:53:43
 */
public class ConditionGraphManager {
	private ModelManager modelCollector;
	private Map<GraphVariable, Map<Condition, Condition>> portmapByGraph = new HashMap<GraphVariable, Map<Condition, Condition>>();

	public ConditionGraphManager(ModelManager modelCollector) {
		this.modelCollector = modelCollector;
	}

	public GraphVariable convertConditionToGraph(AbstractNode conditionNode) throws Exception { //todo: think over Exception strategy (HLDDException, ExtendedException, etc...)

		GraphVariable graphVariable;

		if (conditionNode instanceof CaseNode) {

			graphVariable = caseToGraph((CaseNode) conditionNode);

		} else if (conditionNode instanceof IfNode) {

			graphVariable = ifToGraph(((IfNode) conditionNode));

		} else {

			throw new HLDDException("Converting " + conditionNode.getClass().getSimpleName() + " into Condition Graph." +
					"\nCaseNode or IfNode expected.");
		}

		return (GraphVariable) modelCollector.getIdenticalVariable(graphVariable);
	}

	public PartedVariableHolder convertConditionToBooleanGraph(AbstractNode conditionNode) throws Exception {

		if (conditionNode instanceof IfNode) {

			GraphVariable graphVariable = convertConditionToGraph(conditionNode);

			return new PartedVariableHolder(graphVariable, null, 1);

		} else {
			throw new HLDDException("Converting " + conditionNode.getClass().getSimpleName() + " into Boolean Condition Graph." +
					"\nIfNode expected.");
		}

	}

	private GraphVariable caseToGraph(CaseNode caseNode) throws Exception {
		String baseVarName = createName(caseNode);

		/* Root Node */
		AbstractOperand caseOperand = caseNode.getVariableOperand();
		AbstractVariable caseVariable = modelCollector.convertOperandToVariable(caseOperand, null, false);
		int conditionValuesCount = modelCollector.getConditionValuesCount(caseVariable);
		Node rootNode = new Node.Builder(caseVariable).createSuccessors(conditionValuesCount).
				partedIndices(caseOperand.getPartedIndices()).source(caseNode.getSource()).build();
		/* Fill Root Node, and map branches (to condition values)*/
		Map<Condition, Condition> conditionMapping = new HashMap<Condition, Condition>();
		Set<ConstantVariable> terminalVariablesSet = new HashSet<ConstantVariable>();
		BigInteger terminalValue = BigInteger.ZERO;
		Node tmpNode = rootNode.clone();
		for (WhenNode whenNode : caseNode.getConditions()) {
			// WhenCondition
			Condition whenCondition;
			if (whenNode.isOthers()) {
				whenCondition = tmpNode.getOthers();
				if (whenCondition == null) {
					continue;
				}
			} else {
				whenCondition = modelCollector.convertOperandsToCondition(whenNode.getConditionOperands());
				tmpNode.setSuccessor(whenCondition, null); // todo: replace OTHERS with Condition with a separate visitor, to avoid checking for isOthers() and keeping track of conditions here (and other places like GG)
			}

			// Terminal node
			String name = baseVarName + "___D_" + terminalValue;
			ConstantVariable terminalVariable = ConstantVariable.createNamedConstant(terminalValue, name, null);
			modelCollector.addVariable(terminalVariable);
			terminalVariablesSet.add(terminalVariable);
			Node terminalNode = new Node.Builder(terminalVariable)/*.source(whenNode.getSource())*/.build(); //todo: fix commented part
			// Fill
			for (Condition condition : whenCondition.asList()) {
				rootNode.setSuccessor(condition, terminalNode.clone());
			}
			// Map
			conditionMapping.put(whenCondition, Condition.createCondition(terminalValue.intValue()));
			terminalValue = terminalValue.add(BigInteger.ONE);
		}
		// now that we know how many terminalValue-s rootNode has, update lengths of terminalVariable-s
		Type type = Type.createFromValues(terminalValue.subtract(BigInteger.ONE).intValue(), 0);
		for (ConstantVariable constantVariable : terminalVariablesSet) {
			constantVariable.setLength(type.getLength());
		}

		/* Base Variable */
		Variable baseVariable = new Variable(baseVarName, type, new Flags().setExpansion(true));

		/* GRAPH */
		GraphVariable graphVariable = new GraphVariable(baseVariable, rootNode);

		portmapByGraph.put(graphVariable, conditionMapping);

		return graphVariable;
	}

	private GraphVariable ifToGraph(IfNode ifNode) throws Exception {

		PartedVariableHolder conditionVarHolder = modelCollector.convertConditionalStmt(ifNode.getConditionExpression(), false);

		Node rootNode = new FullTreeCreator(conditionVarHolder, ifNode).create();

		Variable baseVariable = new Variable(createName(ifNode), Type.BOOL_TYPE, new Flags().setExpansion(true));

		return new GraphVariable(baseVariable, rootNode);

	}

	static String createName(AbstractNode node) throws HLDDException {
		SourceLocation source = node.getSource();
		if (source == null) {
			throw new HLDDException("ConditionGraphManager: failed to create name for " + node.getClass().getSimpleName()
					+ ", because node doesn't have source. Only IfNode and CaseNode are supported.");
		}
		Integer firstSourceLine = source.getFirstLine();
		if (node instanceof CaseNode) {
			return "CASE__" + firstSourceLine;
		} else if (node instanceof IfNode) {
			return "IF__" + firstSourceLine;
		} else {
			throw new HLDDException("ConditionGraphManager: creating name for " + node.getClass().getSimpleName()
					+ ". Only IfNode and CaseNode are supported.");
		}
	}

	public Condition mapDirect(GraphVariable graphVariable, Condition whenCondition) throws HLDDException {
		Map<Condition, Condition> portMap = portmapByGraph.get(graphVariable);
		if (portMap == null) {
			throw new HLDDException("Lacking port map for Graph Variable " + graphVariable.getBaseVariable());
		}

		Condition condition = portMap.get(whenCondition);
		if (condition == null) {
			throw new HLDDException("Port map lacks DIRECT mapping for whenCondition " + whenCondition);
		}
		return condition;
	}

	public Condition mapReverse(GraphVariable graphVariable, Condition condition) throws HLDDException {
		if (condition == null) {
			return null; // excessive OTHERS (not actually needed)
		}
		Map<Condition, Condition> portMap = portmapByGraph.get(graphVariable);
		if (portMap == null) {
			throw new HLDDException("Lacking port map for Graph Variable " + graphVariable.getBaseVariable());
		}

		for (Map.Entry<Condition, Condition> entry : portMap.entrySet()) {
			if (entry.getValue().equals(condition)) {
				return entry.getKey();
			}
		}
		throw new HLDDException("Port map lacks REVERSE mapping for condition " + condition);
	}

	private class FullTreeCreator {
		private PartedVariableHolder sourceCondition;
		private SourceLocation source;
		private Interpreter interpreter;

		public FullTreeCreator(PartedVariableHolder sourceCondition, IfNode ifNode) throws Exception {
			this.sourceCondition = sourceCondition;
			this.source = ifNode.getSource();
			this.interpreter = new Interpreter(this.sourceCondition, modelCollector.getConstant0(), modelCollector.getConstant1(), source.toString());
		}

		public Node create() throws Exception {

			LinkedList<PartedVariableHolder> boolOperandsList = new BooleanOperandsCollector(sourceCondition).collect();

//			new UnusedFunctionsRemover(sourceCondition, boolOperandsList).remove();

			return createFrom(boolOperandsList, null);
		}

		private Node createFrom(LinkedList<PartedVariableHolder> boolOperandsList, Node terminalNode) throws HLDDException {

			if (!boolOperandsList.isEmpty()) {

				PartedVariableHolder thisVarHolder = boolOperandsList.removeFirst(); // TAKE

				Node thisNode = new Node.Builder(thisVarHolder.getVariable()).partedIndices(thisVarHolder.getPartedIndices()).
						source(source).createSuccessors(2).build();

				/* TRUE branch */
				interpreter.assign(thisVarHolder, true);
				thisNode.setSuccessor(Condition.TRUE, createFrom(boolOperandsList, selectTerminal(terminalNode, true)));
				/* FALSE branch */
				interpreter.assign(thisVarHolder, false);
				thisNode.setSuccessor(Condition.FALSE, createFrom(boolOperandsList, selectTerminal(terminalNode, true)));

				interpreter.free(thisVarHolder);
				boolOperandsList.addFirst(thisVarHolder); // PUT BACK

				return thisNode;

			} else {

				return selectTerminal(terminalNode, false);

			}

		}

		private Node selectTerminal(Node terminalNode, boolean ignoreCalcError) throws HLDDException {
			return terminalNode == null ? interpreter.calculateTerminalNode(ignoreCalcError) : terminalNode.clone();
		}

	}

	private class Interpreter {
		private final PartedVariableHolder evaluatedCondition;
		private final ConstantVariable constant0;
		private final ConstantVariable constant1;
		private final String sourceAsString;

		private final HashMap<PartedVariableHolder, Boolean> valueByVariable = new HashMap<PartedVariableHolder, Boolean>();

		public Interpreter(PartedVariableHolder evaluatedCondition, ConstantVariable constant0, ConstantVariable constant1, String sourceAsString) {
			this.evaluatedCondition = evaluatedCondition;
			this.constant0 = constant0;
			this.constant1 = constant1;
			this.sourceAsString = sourceAsString;
		}

		public void assign(PartedVariableHolder variable, boolean value) {
			valueByVariable.put(variable, value);
		}

		public void free(PartedVariableHolder variable) {
			valueByVariable.remove(variable);
		}

		public Node calculateTerminalNode(boolean ignoreCalcError) throws HLDDException {
			try {

				boolean value = evaluate(evaluatedCondition);

				return new Node.Builder(value ? constant1 : constant0).build();

			} catch (EvaluationImpossibleException e) {
				if (ignoreCalcError) {
					return null;
				} else {
					throw new HLDDException(e.getMessage());
				}
			}
		}

		@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
		private boolean evaluate(PartedVariableHolder variable) throws HLDDException, EvaluationImpossibleException {

			Boolean result = null;
			boolean isInversed = variable.getTrueValue() == 0; //todo: store TrueValue as boolean.

			if (valueByVariable.containsKey(variable)) { // look for direct value (terminal point)

				result = valueByVariable.get(variable);

			} else {

				AbstractVariable abstractVariable = variable.getVariable();

				if (abstractVariable instanceof FunctionVariable) {

					FunctionVariable functionVariable = (FunctionVariable) abstractVariable;
					Operator operator = functionVariable.getOperator();
					List<PartedVariableHolder> operands = functionVariable.getOperands();

					switch (operator) {
						case OR:
							for (PartedVariableHolder operand : operands) {
								if (result == null) {
									result = evaluate(operand);
								} else {
									result = result || evaluate(operand);
								}
							}
							break;
						case AND:
							for (PartedVariableHolder operand : operands) {
								if (result == null) {
									result = evaluate(operand);
								} else {
									result = result && evaluate(operand);
								}
							}
							break;
						case XOR:
							for (PartedVariableHolder operand : operands) {
								if (result == null) {
									result = evaluate(operand);
								} else {
									result = result ^ evaluate(operand);
								}
							}
							break;
						case EQ:
							boolean opA = evaluate(operands.get(0));
							boolean opB = evaluate(operands.get(1));
							result = opA == opB;
							break;
						case INV:
							opA = evaluate(operands.get(0));
							result = !opA;
							break;
						default:
							throw new HLDDException("ConditionGraphManager: don't know how to evaluate " + operator + " (lines " + sourceAsString + ")");

					}

				} else {
					throw new EvaluationImpossibleException("ConditionGraphManager: cannot evaluate " + variable.getVariable().getName());
				}
			}

			return isInversed ? !result : result;

		}

		private class EvaluationImpossibleException extends Exception {
			public EvaluationImpossibleException(String message) {
				super(message);
			}
		}
	}

/*
	private class UnusedFunctionsRemover {
		private final PartedVariableHolder sourceCondition;
		private final LinkedList<PartedVariableHolder> boolOperandsList;

		public UnusedFunctionsRemover(PartedVariableHolder sourceCondition, LinkedList<PartedVariableHolder> boolOperandsList) {
			this.sourceCondition = sourceCondition;
			this.boolOperandsList = boolOperandsList;
		}

		public void remove() {
			remove(sourceCondition);
		}

		private void remove(PartedVariableHolder partedVariableHolder) {

			AbstractVariable variable = partedVariableHolder.getVariable();

			if (variable instanceof FunctionVariable) {

				if (!boolOperandsList.contains(partedVariableHolder)) {
					modelCollector.removeVariable(variable);
				}
				
				FunctionVariable functionVariable = (FunctionVariable) variable;
				for (PartedVariableHolder operand : functionVariable.getOperands()) {
					remove(operand);
				}
			}
		}
	}
*/

}
