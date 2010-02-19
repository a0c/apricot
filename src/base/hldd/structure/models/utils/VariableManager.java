package base.hldd.structure.models.utils;

import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.ConstantVariable;

import java.util.TreeMap;
import java.util.Collection;

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
     * @throws Exception if the collector has already got the variable with identical name
     */
    public void addVariable(AbstractVariable newVariable) throws Exception {
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
//                throw new Exception("Set of CONSTANTS does already have the following constant: " + newVariable);
            }
        } else {
            if (vars.put(varName, newVariable) != null) {
//                throw new Exception("Set of VARIABLES does already have the following variable: " + newVariable);
            }
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

    public Collection<ConstantVariable> getConstants() {
        return consts.values();
    }

    public AbstractVariable[] getVariablesAsArray() {
        Collection<AbstractVariable> variablesCollection = getVariables();
        return variablesCollection.toArray(new AbstractVariable[variablesCollection.size()]);
    }

    public ConstantVariable[] getConstantsAsArray() {
        Collection<ConstantVariable> constantsCollection = getConstants();
        return constantsCollection.toArray(new ConstantVariable[constantsCollection.size()]);
    }

    public AbstractVariable getVariableByName(String variableName) {
        return vars.get(variableName);
    }

    public ConstantVariable getConstantByName(String constantName) {
        return consts.get(constantName);
    }

    public TreeMap<String, ConstantVariable> getConsts() {
        return consts;
    }
}
