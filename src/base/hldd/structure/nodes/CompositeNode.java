package base.hldd.structure.nodes;

import base.HLDDException;
import base.SourceLocation;
import base.hldd.structure.models.utils.ModelManager.CompositeFunctionVariable;
import base.hldd.structure.models.utils.RangeVariableHolder;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.AbstractVariable;
import base.vhdl.structure.Operator;
import base.Range;

import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Represents conditions like <tt>if tre = '0' or dsr = '0' then</tt>, i.e. composite conditions made of OR-s or AND-s.
 * <p/>
 * Objects of this class exist only temporarily, during creation of a complex
 * ControlNode.
 * That's why it doesn't correspond to the whole Node's interface,
 * but simply stores a <code>rootNode</code> of the ComplexNode.
 *
 * @author Anton Chepurov
 */
public class CompositeNode extends Node {
	private Node rootNode;
	private Node lastNode;
	/* Auxiliary fields */
	private Map<Node, Condition> trueValuesByNodes;

	public CompositeNode(CompositeFunctionVariable depCompositeVariable) throws HLDDException {
		trueValuesByNodes = new LinkedHashMap<Node, Condition>(depCompositeVariable.getFunctionVariables().length);
		rootNode = getNode(depCompositeVariable);
		lastNode = getLastControlNode(rootNode);
	}

	private Node getNode(CompositeFunctionVariable compositeFunctionVar) throws HLDDException {
		Node rootNode = null;
		Node previousNode = null;
		Operator compositeOperator = compositeFunctionVar.getCompositeOperator();
		for (RangeVariableHolder funcVarHolder : compositeFunctionVar.getFunctionVariables()) {
			AbstractVariable funcVar = funcVarHolder.getVariable();
			Range range = funcVarHolder.getRange();
			int trueValue = funcVarHolder.getTrueValue();
			/* Create newControlNode */
			Node controlNode = funcVar instanceof CompositeFunctionVariable
					? getNode((CompositeFunctionVariable) funcVar)
					: new Builder(funcVar).range(range).createSuccessors(2).build(); /* Condition Function has 2 values/successors only */
			controlNode.setSuccessor(Condition.TRUE, null); // init all conditions (TRUE and FALSE), so that in setSuccessor(Condition, Node)
			controlNode.setSuccessor(Condition.FALSE, null);// we could simply iterate conditions, w/o fine-tuning (getSuccessorInternal()).
			trueValuesByNodes.put(controlNode, Condition.createCondition(trueValue));/* Actually, can check, if doesn't contain... */ //todo: make funcVarHolder.getTrueValue() return Condition instead of int

			/* If rootNode is null, then controlNode is the first controlNode in CompositeNode */
			if (rootNode == null) {
				/* Set rootNode only once */
				rootNode = controlNode;
				/* Set as previousNode */
				previousNode = controlNode;
			} else {
				/* controlNode is not the first one.
				* Add it as a successor to the previousNode */
				Condition controlCondition = compositeOperator == Operator.AND
						? trueValuesByNodes.get(previousNode)			   // previously was 1
						: trueValuesByNodes.get(previousNode).invert();   // previously was 0
				try {
					previousNode.setSuccessor(controlCondition, controlNode);
					/* Set new previousNode */
					previousNode = getLastControlNode(controlNode);
				} catch (Exception e) {
					throw new RuntimeException(e);/* Do nothing. Exception is guaranteed not to be thrown here. */
				}
			}
		}
		return rootNode;
	}

	private static Node getLastControlNode(Node controlNode) {
		Node lastNode = controlNode;
		for (Node successor : controlNode.getSuccessors()) {
			if (successor != null && successor.isControlNode()) {
				lastNode = getLastControlNode(successor);
			}
		}
		return lastNode;
	}

	public boolean isEmptyControlNode() throws HLDDException {
		return lastNode.isEmptyControlNode();
	}

	public void fillEmptySuccessorsWith(Node fillingNode, boolean isF4RTL) {
		if (isF4RTL && fillingNode == null) return;
		fillEmptySuccessorsWith(rootNode, fillingNode, isF4RTL);
	}

	private static void fillEmptySuccessorsWith(Node node, Node fillingNode, boolean isF4RTL) {
		if (node.isControlNode()) {
			node.fillEmptySuccessorsWith(fillingNode, isF4RTL);
			for (Node successor : node.getSuccessors()) {
				fillEmptySuccessorsWith(successor, fillingNode, isF4RTL);
			}
		}
	}

	public void setSuccessor(Condition condition, Node successor) throws HLDDException {
		propagateSuccessor(rootNode, condition, successor);
	}

	private void propagateSuccessor(Node node, Condition controlCondition, Node newSuccessor) throws HLDDException {
		/* Set successors for ControlNodes only and only for nodes that are
				 * part of CompositeCondition, i.e. skip control nodes that are
				 * "terminal" nodes for CompositeCondition. */
		if (node.isControlNode() && trueValuesByNodes.containsKey(node)) {
			Condition adjustedCondition = adjustCondition(node, controlCondition);
			int conditionsCount = node.getConditionsCount();
			for (int idx = 0; idx < conditionsCount; idx++) {
				Condition condition = node.getCondition(idx);
				Node successor = node.getSuccessor(condition);
				/* If condition successor is missing, then:
								 * if the successor's condition complies with (adjusted) controlCondition,
								 *    then fill it with newSuccessor;
								 * */
				if (successor == null) {
					if (condition.equals(adjustedCondition)) { // previously was controlCondition
						try {
							node.setSuccessor(condition, newSuccessor);
						} catch (Exception e) {
							throw new RuntimeException(e);/* Do nothing. Exception is guaranteed not to be thrown here. */
						}
					}
				} else {
					propagateSuccessor(successor, controlCondition, Node.clone(newSuccessor));
				}
			}
		}
	}

	private Condition adjustCondition(Node node, Condition controlCondition) {
		return controlCondition.equals(trueValuesByNodes.get(node)) ? Condition.TRUE : Condition.FALSE;
	}

	public Node clone() {
		return rootNode.clone();
	}

	public Node getRootNode() {
		return rootNode;
	}

	public boolean isIdenticalTo(Node comparedNode) {
		return comparedNode instanceof CompositeNode && rootNode.isIdenticalTo(((CompositeNode) comparedNode).getRootNode());
	}

	public boolean isTerminalNode() {
		return false;
	}

	public Collection<Node> getSuccessors() {
		return lastNode.getSuccessors();
	}

	@Override
	public Node getSuccessor(Condition condition) {
		return lastNode.getSuccessor(condition);
	}

	@Override
	public Condition getOthers() throws HLDDException {
		return lastNode.getOthers();
	}

	@Override
	public void setSource(SourceLocation source) {
		for (Node node : trueValuesByNodes.keySet()) {
			if (node.isControlNode()) {
				node.setSource(source);
			}
		}
	}
}
