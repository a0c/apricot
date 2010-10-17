package base.vhdl.structure;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Anton Chepurov
 */
public class UserDefinedFunction extends AbstractOperand {
	private final String userDefinedFunction;
	private final List<AbstractOperand> operands;

	public UserDefinedFunction(String userDefinedFunction, boolean isInverted, int expectedOperandsSize) {
		super(isInverted);
		this.userDefinedFunction = userDefinedFunction;
		this.operands = new ArrayList<AbstractOperand>(expectedOperandsSize);
	}

	public void addOperand(AbstractOperand operand) {
		operands.add(operand);
	}

	public List<AbstractOperand> getOperands() {
		return operands;
	}

	public String getUserDefinedFunction() {
		return userDefinedFunction;
	}

	public boolean isIdenticalTo(AbstractOperand comparedOperand) {
		/* Compare Class */
		if (getClass() != comparedOperand.getClass()) return false;
		UserDefinedFunction comparedFunction = (UserDefinedFunction) comparedOperand;
		/* Compare Function */
		if (!userDefinedFunction.equalsIgnoreCase(comparedFunction.userDefinedFunction)) return false;
		/* Compare Operands */
		if (operands.size() != comparedFunction.operands.size()) return false;
		for (int i = 0; i < operands.size(); i++) {
			if (!operands.get(i).isIdenticalTo(comparedFunction.operands.get(i))) return false;
		}
		/* All checks done */
		return true;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(userDefinedFunction);
		sb.append(" ( ");
		for (AbstractOperand operand : operands) {
			sb.append(operand).append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(" )");
		return sb.toString();
	}
}
