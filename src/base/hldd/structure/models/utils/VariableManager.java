package base.hldd.structure.models.utils;

import base.Indices;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.ConstantVariable;
import base.hldd.structure.variables.FunctionVariable;
import base.hldd.structure.variables.UserDefinedFunctionVariable;
import base.vhdl.structure.Operator;

import java.math.BigInteger;
import java.util.TreeMap;
import java.util.Collection;
import java.util.TreeSet;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.11.2008
 * <br>Time: 14:48:58
 */
public class VariableManager {
    private TreeMap<String, AbstractVariable> vars = new TreeMap<String, AbstractVariable>();
    private TreeMap<String, ConstantVariable> consts = new TreeMap<String, ConstantVariable>();


    /**
     *
     * @param newVariable variable to be added
     */
    public void addVariable(AbstractVariable newVariable) {
        addVariable(newVariable.getName(), newVariable);
    }

    /**
     *
     * @param varName name of the variable to use as key in maps
     * @param newVariable variable to be added
     */
    public void addVariable(String varName, AbstractVariable newVariable) {
        if (newVariable instanceof ConstantVariable) {
            if (!consts.containsKey(varName)) {
                consts.put(varName, (ConstantVariable) newVariable);
            }
        } else {
            vars.put(varName, newVariable);
        }
    }

    public void removeVariable(AbstractVariable variableToRemove) {
        if (variableToRemove instanceof ConstantVariable) {
            consts.remove(variableToRemove.getName());
        } else {
            vars.remove(variableToRemove.getName());
        }
    }

    public Collection<AbstractVariable> getVariables() {
        return vars.values();
    }

	public Collection<FunctionVariable> getFunctions(Operator operator) {
		return getFunctions(vars.values(), operator);
	}

	public Collection<FunctionVariable> getUDFunctions(String userDefinedOperator) {
		return getUDFunctions(vars.values(), userDefinedOperator);
	}

	public static Collection<FunctionVariable> getFunctions(Collection<AbstractVariable> vars, Operator operator) {
		TreeSet<FunctionVariable> funcSet = new TreeSet<FunctionVariable>(FunctionVariable.getComparator());

		if (operator == null) {
			for (AbstractVariable variable : vars) {
				if (variable.getClass() == FunctionVariable.class) {
					FunctionVariable functionVariable = (FunctionVariable) variable;

					// skip functions from deeper levels of hierarchy
					if (!functionVariable.isTopLevel()) {
						continue;
					}

					funcSet.add(functionVariable);
				}
			}
		} else {
			for (AbstractVariable variable : vars) {
				if (variable.getClass() == FunctionVariable.class) {
					FunctionVariable functionVariable = (FunctionVariable) variable;

					// skip functions from deeper levels of hierarchy
					if (!functionVariable.isTopLevel()) {
						continue;
					}

					if (functionVariable.getOperator() == operator) {
						funcSet.add(functionVariable);
					}
				}
			}
		}
		return funcSet;
	}

	public static Collection<FunctionVariable> getUDFunctions(Collection<AbstractVariable> vars, String userDefinedOperator) {
		TreeSet<FunctionVariable> funcSet = new TreeSet<FunctionVariable>(FunctionVariable.getComparator());

		if (userDefinedOperator == null) {
			for (AbstractVariable variable : vars) {
				if (variable.getClass() == UserDefinedFunctionVariable.class) {
					UserDefinedFunctionVariable functionVariable = (UserDefinedFunctionVariable) variable;

					funcSet.add(functionVariable);
				}
			}
		} else {
			for (AbstractVariable variable : vars) {
				if (variable.getClass() == UserDefinedFunctionVariable.class) {
					UserDefinedFunctionVariable functionVariable = (UserDefinedFunctionVariable) variable;

					if (functionVariable.getUserDefinedOperator().equals(userDefinedOperator)) {
						funcSet.add(functionVariable);
					}
				}
			}
		}
		return funcSet;
	}

    public Collection<ConstantVariable> getConstants() {
        return consts.values();
    }

    public AbstractVariable getVariableByName(String variableName) {
        return vars.get(variableName);
    }

    public ConstantVariable getConstantByName(String constantName) {
        return consts.get(constantName);
    }

	/**
	 * Looks for a constant with the desired value in stored constants.
	 * If no such constant is found, a new one is created and stored.
	 * The new constant is also returned.
	 * @param value desired value of the constant
	 * @param targetLength requested length or <code>null</code> if doesn't matter
	 * @return an instance of ConstantVariable with the value of <code>constValueToFind</code>
	 */
	public ConstantVariable getConstantByValue(BigInteger value, Indices targetLength) {

		// Prefer constants with meaningful names (look for direct constant)
		ConstantVariable constWithSmartName = ConstantVariable.createNamedConstant(value, null, targetLength);
		ConstantVariable existingConstant = getConstantByName(constWithSmartName.getName());
		if (existingConstant != null) {
			if (targetLength == null || existingConstant.getLength().equals(targetLength)) {
				return existingConstant;
			}
		}

		// Search for EXISTENT constants
		for (String constName : consts.keySet()) {
			ConstantVariable constantVariable = consts.get(constName);
			if (constantVariable.getValue().equals(value)) {
				/* Constan with the SAME VALUE found. */
				/* For EVERY variable LENGTH there must be a SEPARATE constant */
				if (targetLength == null) {
					/* If length doesn't matter, return the first met constant with the required value */
					return constantVariable;
				} else {
					if (targetLength.equals(constantVariable.getLength())) {
						return constantVariable;
					}
				}
			}
		}
		// EXISTENT constant is not found, so create a new one
		consts.put(constWithSmartName.getName(), constWithSmartName);
		return constWithSmartName;

	}

	public ConstantVariable getConstantByValue(BigInteger value) {
		return getConstantByValue(value, null);
	}

}
