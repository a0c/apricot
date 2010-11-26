package base.hldd.structure.models.utils;

import base.HLDDException;
import base.hldd.structure.variables.*;
import base.vhdl.structure.Operator;

import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
class BooleanOperandsCollector {
	private RangeVariableHolder rootVarHolder;
	private LinkedList<RangeVariableHolder> list = new LinkedList<RangeVariableHolder>();

	public BooleanOperandsCollector(RangeVariableHolder rangeVariableHolder) {
		rootVarHolder = rangeVariableHolder;
	}

	public LinkedList<RangeVariableHolder> collect() throws HLDDException {

		collect(rootVarHolder);

		return list;
	}

	private void collect(RangeVariableHolder rangeVariableHolder) throws HLDDException {

		AbstractVariable abstractVariable = rangeVariableHolder.getVariable();
		int length = rangeVariableHolder.isRange() ? rangeVariableHolder.getRange().length()
				: abstractVariable.getLength().length();

		if (abstractVariable.getClass() == Variable.class || abstractVariable instanceof GraphVariable) {

			if (length > 1) { // accept (collect) only boolean operands (length == 1)
				return;
			}

			addToList(rangeVariableHolder);

		} else if (abstractVariable instanceof FunctionVariable) {

			FunctionVariable functionVariable = (FunctionVariable) abstractVariable;

			Operator operator = functionVariable.getOperator();

			if (operator.isLogical(length)) { // go inside logical functions only. Note: conditions like fly='1' are represented by Variables, not FunctionVariables
				for (RangeVariableHolder operand : functionVariable.getOperands()) {
					collect(operand);
				}
			} else {
				addToList(rangeVariableHolder);
			}


		} else if (!(abstractVariable instanceof ConstantVariable)) {
			throw new HLDDException("ConditionGraphManager: don't know how to process "
					+ abstractVariable.getClass().getSimpleName() + " when collecting Boolean Operands");
		}
	}

	private void addToList(RangeVariableHolder rangeVariableHolder) {
		if (!list.contains(rangeVariableHolder)) {
			list.add(rangeVariableHolder);
		}
	}
}
