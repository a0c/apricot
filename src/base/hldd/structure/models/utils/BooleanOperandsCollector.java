package base.hldd.structure.models.utils;

import base.HLDDException;
import base.hldd.structure.variables.*;
import base.vhdl.structure.Operator;

import java.util.LinkedList;

/**
* <br><br>User: Anton Chepurov
* <br>Date: 24.05.2010
* <br>Time: 12:43:22
*/
class BooleanOperandsCollector {
	private PartedVariableHolder rootVarHolder;
	private LinkedList<PartedVariableHolder> list = new LinkedList<PartedVariableHolder>();

	public BooleanOperandsCollector(PartedVariableHolder partedVariableHolder) {
		rootVarHolder = partedVariableHolder;
	}

	public LinkedList<PartedVariableHolder> collect() throws HLDDException {

		collect(rootVarHolder);

		return list;
	}

	private void collect(PartedVariableHolder partedVariableHolder) throws HLDDException {

		AbstractVariable abstractVariable = partedVariableHolder.getVariable();
		int length = partedVariableHolder.isParted() ? partedVariableHolder.getPartedIndices().length()
				: abstractVariable.getLength().length();

		if (abstractVariable.getClass() == Variable.class || abstractVariable instanceof GraphVariable) {

			if (length > 1) { // accept (collect) only boolean operands (length == 1)
				return;
			}

			addToList(partedVariableHolder);

		} else if (abstractVariable instanceof FunctionVariable) {

			FunctionVariable functionVariable = (FunctionVariable) abstractVariable;

			Operator operator = functionVariable.getOperator();

			if (operator.isLogical(length)) { // go inside logical functions only. Note: conditions like filt1fly='1' are represented by Variables, not FunctionVariables
				for (PartedVariableHolder operand : functionVariable.getOperands()) {
					collect(operand);
				}
			} else {
				addToList(partedVariableHolder);
			}


		} else if (!(abstractVariable instanceof ConstantVariable)) {
			throw new HLDDException("ConditionGraphManager: don't know how to process "
					+ abstractVariable.getClass().getSimpleName() + " when collecting Boolean Operands");
		}
	}

	private void addToList(PartedVariableHolder partedVariableHolder) {
		if (!list.contains(partedVariableHolder)) {
			list.add(partedVariableHolder);
		}
	}
}
