package base.vhdl.visitors;

import base.vhdl.structure.*;
import base.vhdl.structure.nodes.*;
import base.hldd.structure.variables.ConstantVariable;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * Maps resettable variables with resetting operands.<br>
 * <b>NB!</b> Currently only IfNode-s are searched.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 02.10.2008
 * <br>Time: 11:21:52
 */
public class BehDDResetInspector extends AbstractVisitor {
    private Map<String, AbstractOperand> resetOperandByGraphName = new HashMap<String, AbstractOperand>();

    /* RESET signal may be compared with constant. So keep them available. */
    private Set<Constant> constantSet, processConstantSet;
    private String graphName;
    private boolean doVisit;

    /* Here only request processing of AbstractNodes(ParseTree) */
    public void visitEntity(Entity entity) throws Exception {}

    public void visitArchitecture(Architecture architecture) throws Exception {
        constantSet = architecture.getConstants();
    }

    public void visitProcess(base.vhdl.structure.Process process) throws Exception {
        graphName = BehDDGraphGenerator.extractVariableName(process.getName());
        doVisit = true;
        processConstantSet = process.getConstants();
        process.getRootNode().traverse(this);
    }

    public void visitIfNode(IfNode ifNode) throws Exception {

        /* Stop search when resetting node found */
        if (!doVisit) return;

        TransitionNode resettingNode = detectResettingNode(ifNode);
        if (resettingNode != null) {
            if (!resettingNode.isNull()) {
                /* Check that correct variable (graphName) is reset */
                if (!resettingNode.getVariableName().equalsIgnoreCase(graphName))
                    throw new Exception("Resetting node resets a variable that is different from the graph variable." +
                        "\nExpected: " + graphName + "\nActual: " + resettingNode.getVariableName());
                /* Map resetting */
                resetOperandByGraphName.put(graphName, resettingNode.getValueOperand());
                /* Stop search */
                doVisit = false;
            }
        }
    }

    public void visitTransitionNode(TransitionNode transitionNode) throws Exception {}

    public void visitCaseNode(CaseNode caseNode) throws Exception {}

    public void visitWhenNode(WhenNode whenNode) throws Exception {}

    /**
     * Searches for a resetting node in the specified IfNode.
     * @param ifNode control node where to search for resetting node 
     * @return resetting node or <code>null</code> if it is not found
     * @throws Exception if equality comparing expression doesn't
     *         contain 2 operands exactly   
     */
    private TransitionNode detectResettingNode(IfNode ifNode) throws Exception {
        /* Check the node to be resetting,
        *  Calculate index of comparison reference operand and obtain it */
        AbstractOperand refOperand = findResetReferenceOperand(ifNode);
        if (refOperand == null) return null;

        Integer refOperandValue = parseConstantValue(refOperand);

        if (refOperandValue != null) {
            /* Resetting node is found:
            * 1) if reference operand is 1:
            *       1a) the whole expression is non-inverted:
            *              1aa) the whole expression contains TruePart => etc
            *       1b) the whole expression is inverted:
            *              1bb) the whole expression contains FalsePart => etc
            * 2) if reference operand is 0:
            *       2a) the whole expression is non-inverted:
            *              2aa) the whole expression contains FalsePart => etc
            *       2b) the whole expression is inverted:
            *              2bb) the whole expression contains TruePart => etc
            * 3) if reference operand is a VHDL constant => get its value => goto 1 or 2
            **/
            if (refOperandValue == 1) {
                if (!ifNode.getExpression().isInverted()) {
                    if (ifNode.getTruePart().getChildren().size() > 0) {
                        return returnResettingNode(ifNode.getTruePart());
                    }
                } else {
                    if (ifNode.getFalsePart() != null) {
                        return returnResettingNode(ifNode.getFalsePart());
                    }
                }
            } else if (refOperandValue == 0) {
                if (!ifNode.getExpression().isInverted()) {
                    if (ifNode.getFalsePart() != null) {
                        return returnResettingNode(ifNode.getFalsePart());
                    }
                } else {
                    if (ifNode.getTruePart().getChildren().size() > 0) {
                        return returnResettingNode(ifNode.getTruePart());
                    }
                }
            }
        }

        return null;
    }

    /**
     * Parses a simple operand ({@link base.vhdl.structure.OperandImpl}) to Integer
     * using {@link base.hldd.structure.variables.ConstantVariable#parseConstantValue(String)}.
     * For treating named constant, {@link #constantSet} and {@link #processConstantSet} are searched. 
     * @param refOperand operand to parse
     * @return an Integer value of a constant if the specified operand declares a constant
     *         or <code>null</code> if operand doesn't declare a constant (also if it declares
     *         an expression ({@link base.vhdl.structure.Expression})).
     */
    Integer parseConstantValue(AbstractOperand refOperand) {
        if (refOperand instanceof OperandImpl) {
            String refOperandAsString = ((OperandImpl) refOperand).getOperand();
            Integer refOperandValue = ConstantVariable.parseConstantValue(refOperandAsString);
            if (refOperandValue != null) {
                return refOperandValue;
            } else {
                Constant constant = searchConstant(refOperandAsString);
                if (constant != null) {
                    return constant.getValue();
                }
            }
        } /* Complex condition. Simple conditions like "Reset = 1" or "Reset = 0" are accepted as resetting nodes */ //todo may be it's normal situation?
        return null;
    }

    /**
     * Checks the specified ifNode to be an equality comparison (1) of RESET variable (2),
     * and returns the comparison reference operand.
     * @param ifNode node to check
     * @return comparison reference operand or <code>null</code> if it is not found
     *         (either operation is not an equality check, or RESET is not compared
     *          in this ifNode)
     * @throws Exception if equality comparing expression contains more than 2 operands
     */
    static AbstractOperand findResetReferenceOperand(IfNode ifNode) throws Exception {
        /* To contain resetting node, operator of ifNode must be EQ */
        if (ifNode.getExpression().getOperator() != Operator.EQ) return null;

        /* Get index of operand RESET or detect the abscence of it */
        int resetOperandIndex = -1;
        List<AbstractOperand> operands = ifNode.getExpression().getOperands();
        if (operands.size() != 2) throw new Exception("Unexpected bug: Equality comparing expression doesn't contain 2 operans. Actual number of operands is " + operands.size());
        for (int i = 0; i < operands.size(); i++) {
            AbstractOperand operand = operands.get(i);
            if (operand instanceof OperandImpl) {
                if (GraphGenerator.isResetName(((OperandImpl) operand).getOperand())) {
                    resetOperandIndex = i;
                    break;
                }
            }
        }
        /* To contain resetting node, at least one operand in ifNode must be RESET */
        if (resetOperandIndex == -1) return null;

        /* Calculate index of comparison reference operand and obtain it */
        return operands.get(calcReferenceOperandIndex(resetOperandIndex));
    }

    private Constant searchConstant(String refOperandAsString) {
        if (constantSet != null) {
            for (Constant constant : constantSet) {
                if (constant.getConstantName().equalsIgnoreCase(refOperandAsString)) return constant;
            }
        }
        if (processConstantSet != null) {
            for (Constant constant : processConstantSet) {
                if (constant.getConstantName().equalsIgnoreCase(refOperandAsString)) return constant;
            }
        }
        return null;
    }

    private TransitionNode returnResettingNode(CompositeNode compositeNode) throws Exception {
        List<AbstractNode> children = compositeNode.getChildren();
        if (children.size() > 1)
            throw new Exception("Unexpected bug: Number of resetting nodes is greater than 1." +
                    "\nOnly VHDL Beh DD can be simplified. For other VHDL types implementation is missing.");
        else if (children.size() < 1)
            throw new Exception("Unexpected bug: Number of resetting nodes is 0. Resetting node is missing.");
        else if (!(children.get(0) instanceof TransitionNode))
            throw new Exception("Resetting node is not a " + TransitionNode.class.getSimpleName() + ". Complex resetting conditions are not currently supported.");
        return (TransitionNode) children.get(0);
    }

    static int calcReferenceOperandIndex(int resetOperandIndex) {
        return (resetOperandIndex + 1) % 2;
    }

    boolean isResettable(String graphName) {
        return resetOperandByGraphName.containsKey(graphName);
    }


}
