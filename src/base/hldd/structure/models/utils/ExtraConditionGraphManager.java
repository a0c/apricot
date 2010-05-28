package base.hldd.structure.models.utils;

import base.Indices;
import base.Type;
import base.hldd.structure.Flags;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.ConstantVariable;
import base.hldd.structure.variables.GraphVariable;
import base.hldd.structure.variables.Variable;
import base.vhdl.structure.Expression;
import base.vhdl.structure.nodes.AbstractNode;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.IfNode;

import java.util.LinkedList;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 24.05.2010
 * <br>Time: 12:53:57
 */
public class ExtraConditionGraphManager {
	private final ModelManager modelCollector;

	public ExtraConditionGraphManager(ModelManager modelCollector) {
		this.modelCollector = modelCollector;
	}

	public void generateExtraGraph(AbstractNode node) throws Exception {

		if (node instanceof CaseNode) {

			new ConditionGraphManager(modelCollector).convertConditionToGraph(node);

		} else if (node instanceof IfNode) {

			Expression expression = ((IfNode) node).getConditionExpression();

			PartedVariableHolder conditionVarHolder = modelCollector.convertConditionalStmt(expression, false);
			LinkedList<PartedVariableHolder> boolOperandsList = new BooleanOperandsCollector(conditionVarHolder).collect();

			String baseName = ConditionGraphManager.createName(node);
			ConstantVariable constant0 = modelCollector.getConstant0();
			ConstantVariable constant1 = modelCollector.getConstant1();
			for (PartedVariableHolder boolOperand : boolOperandsList) {

				AbstractVariable operandVar = boolOperand.getVariable();
				Indices partedIndices = boolOperand.getPartedIndices();
				boolean isInversed = boolOperand.isInversed();

				String name = baseName + "__" + operandVar.getName() + Indices.toString(partedIndices);

				Node controlNode = new Node.Builder(operandVar).partedIndices(partedIndices).createSuccessors(2).build();

				controlNode.setSuccessor(Condition.TRUE, new Node.Builder(isInversed ? constant0 : constant1).build());
				controlNode.setSuccessor(Condition.FALSE, new Node.Builder(isInversed ? constant1 : constant0).build());

				Variable baseVariable = new Variable(name, Type.BOOL_TYPE, new Flags().setExpansion(true));

				GraphVariable graphVariable = new GraphVariable(baseVariable, controlNode);
				
				modelCollector.getIdenticalVariable(graphVariable);

			}
		}
	}

}
