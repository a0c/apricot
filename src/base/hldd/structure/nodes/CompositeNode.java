package base.hldd.structure.nodes;

import base.hldd.structure.models.utils.ModelManager.CompositeFunctionVariable;
import static base.hldd.structure.models.utils.ModelManager.invertBit;
import base.hldd.structure.models.utils.PartedVariableHolder;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.nodes.utils.Utility;
import base.vhdl.structure.Operator;
import base.Indices;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Objects of this class exist only temporarily, during creation of a complex
 * ControlNode.
 * That's why it doesn't correspond to the whole Node's interface,
 * but simply stores a <code>rootNode</code> of the ComplexNode.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.02.2008
 * <br>Time: 15:19:53
 */
public class CompositeNode extends Node {
    private Node rootNode;
    private Node lastNode;
    /* Auxiliary fields */
    private Map<Node, Integer> trueValuesByNodes;

    public CompositeNode(CompositeFunctionVariable depCompositeVariable) {
        trueValuesByNodes = new LinkedHashMap<Node, Integer>(depCompositeVariable.getFunctionVariables().length);
        rootNode = getNode(depCompositeVariable);
        lastNode = getLastControlNode(rootNode);
    }

    private Node getNode(CompositeFunctionVariable compositeFunctionVar) {
        Node rootNode = null;
        Node previousNode = null;
        Operator compositeOperator = compositeFunctionVar.getCompositeOperator();
        for (PartedVariableHolder funcVarHolder : compositeFunctionVar.getFunctionVariables()) {
            AbstractVariable funcVar = funcVarHolder.getVariable();
            Indices partedIndices = funcVarHolder.getPartedIndices();
            int trueValue = funcVarHolder.getTrueValue();
            /* Create newControlNode */
            Node controlNode = funcVar instanceof CompositeFunctionVariable 
                    ? getNode((CompositeFunctionVariable) funcVar)
                    : new Builder(funcVar).partedIndices(partedIndices).successorsCount(2).build(); /* Condition Function has 2 values/successors only */
            trueValuesByNodes.put(controlNode, trueValue);/* Actually, can check, if doesn't contain... */

            /* If rootNode is null, then controlNode is the first controlNode in CompositeNode */
            if (rootNode == null) {
                /* Set rootNode only once */
                rootNode = controlNode;
                /* Set as previousNode */
                previousNode = controlNode;
            } else {
                /* controlNode is not the first one.
                 * Add it as a successor to the previousNode */
                int controlCondition = compositeOperator == Operator.AND
                        ? trueValuesByNodes.get(previousNode)               // previously was 1
                        : invertBit(trueValuesByNodes.get(previousNode));   // previously was 0
                try {
                    previousNode.setSuccessor(controlCondition, controlNode); 
                    /* Set new previousNode */
                    previousNode = getLastControlNode(controlNode);
                } catch (Exception e) { throw new RuntimeException(e);/* Do nothing. Exception is guaranteed not to be thrown here. */ }
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

    public boolean isEmptyControlNode() throws Exception {
        return lastNode.isEmptyControlNode();
    }

    public void fillEmptySuccessorsWith(Node fillingNode/*AbstractVariable dependentVariable, GraphGenerator graphGenerator*/) throws Exception {
        fillEmptySuccessorsWith(rootNode, fillingNode/*dependentVariable, graphGenerator*/);
    }

    private static void fillEmptySuccessorsWith(Node node, Node fillingNode /*AbstractVariable dependentVariable, GraphGenerator graphGenerator*/) {
        if (node.isControlNode()) {
            try {
                node.fillEmptySuccessorsWith(fillingNode /*dependentVariable, graphGenerator*/);
            } catch (Exception e) { throw new RuntimeException(e);/* Do nothing. Exception is guaranteed not to be thrown here. */ }
            for (Node successor : node.getSuccessors()) {
                fillEmptySuccessorsWith(successor, fillingNode/*dependentVariable, graphGenerator*/);
            }
        }
    }

    public void setSuccessor(int controlCondition, Node successor) throws Exception {
        propagateSuccessor(rootNode, controlCondition, successor);
    }

    private void propagateSuccessor(Node node, int controlCondition, Node successor) {
        /* Set successors for ControlNodes only and only for nodes that are
         * part of CompositeCondition, i.e. skip control nodes that are
         * "terminal" nodes for CompositeCondition. */
        if (node.isControlNode() && trueValuesByNodes.containsKey(node)) {
            int adjustedCondition = adjustCondition(node, controlCondition);
            Node[] successors = node.getSuccessors();
            for (int condition = 0; condition < successors.length; condition++) {
                /* If condition successor is missing, then:
                 * if the successor's condition complies with (adjusted) controlCondition,
                 *    then fill it with successor;
                 * */
                if (successors[condition] == null) {
                    if (condition == adjustedCondition) { // previously was controlCondition
                        try {
                            node.setSuccessor(condition, successor);
                        } catch (Exception e) { throw new RuntimeException(e);/* Do nothing. Exception is guaranteed not to be thrown here. */ }
                    }
                } else {
                    propagateSuccessor(successors[condition], controlCondition, successor.isTerminalNode() ? successor : Utility.clone(successor));
                }
            }
        }
    }

    private int adjustCondition(Node node, int controlCondition) {
        return controlCondition == trueValuesByNodes.get(node) ? 1 : 0;
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

    public Node[] getSuccessors() {
        return lastNode.getSuccessors();
    }
}
