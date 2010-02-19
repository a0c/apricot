package base.hldd.structure.models.utils;

import base.hldd.structure.variables.*;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Utility;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 20.02.2008
 * <br>Time: 0:31:24
 */
public abstract class AbstractModelCreator implements ModelCreator {
    protected static final Logger LOG = Logger.getLogger(AbstractModelCreator.class.getName());
    protected ConstantVariable[] constants;
    protected AbstractVariable[] variables;
    protected HashMap<Integer, AbstractVariable> indexVariableHash;

    public AbstractModelCreator(ConstantVariable[] constants, AbstractVariable[] variables) {
        this.constants = constants;
        this.variables = variables;
        indexVariableHash = new HashMap<Integer, AbstractVariable>();
    }

    public void create() {

        /* Remove those CONSTANTS that are not used neither in FUNCTION VARIABLES, nor in GRAPH VARIABLES */
        removeObsoleteConstants();

        /* Perform INDEXATION */ //todo: ModelIndexator -> modelIndexator.indexate(constants, variables);
        performIndexation();

        /* Hash indices and variables */
        hashIndices();

        /* Create MODEL */
        doCreateModel();

    }

    private void performIndexation() {
        LOG.entering(LOG.getName(), "performIndexation(2/4)");
        int varIndex = 0, graphIndex = 0, nodeIndex = 0;

//        /* Strip indices from all variables */
//        for (AbstractVariable variable : variables) {
//            variable.forceSetIndex(-1);
//        }

        /* Index INPUTS */
        for (AbstractVariable variable : variables) {
            if (variable instanceof Variable) {
                Variable variable1 = (Variable) variable;
                if (variable1.isInput()) {
                    variable1.forceSetIndex(varIndex++);
                }
            }
        }
        /* Index CONSTANTS */
        for (ConstantVariable constant : constants) {
            constant.forceSetIndex(varIndex++);
        }
        /* Index FUNCTIONS */
        for (AbstractVariable variable : variables) {
            if (variable instanceof FunctionVariable) {
                variable.forceSetIndex(varIndex++);
            }
        }
        /* Index GRAPHS */
        doIndexGraphs(varIndex, graphIndex, nodeIndex);

        LOG.exiting(LOG.getName(), "performIndexation(2/4)");
    }

    protected abstract void doIndexGraphs(int varIndex, int graphIndex, int nodeIndex);

    private void hashIndices() {
        LOG.entering(LOG.getName(), "hashIndices(3/4)");

        for (ConstantVariable constant : constants) {
            if (constant.getIndex() == -1) {
                String msg = "Unindexed constant: " + constant.getName();
                LOG.warning(msg);
                System.out.println(msg);
                continue;
            }
            indexVariableHash.put(constant.getIndex(), constant);
        }
        for (AbstractVariable variable : variables) {
            if (variable.getIndex() == -1) {
                String msg = "Unindexed variable: " + variable.getName();
                LOG.warning(msg);
                System.out.println(msg);
                continue;
            }
            indexVariableHash.put(variable.getIndex(), variable);
        }

        LOG.exiting(LOG.getName(), "hashIndices(3/4)");
    }

    protected abstract void doCreateModel();

    private void removeObsoleteConstants() {
        LOG.entering(LOG.getName(), "removeObsoleteConstants(1/4)");
        ArrayList<ConstantVariable> usedConstants = new ArrayList<ConstantVariable>();

        for (ConstantVariable constant : constants) {
            boolean isUsed = false;

            for (AbstractVariable variable : variables) {
                if (variable instanceof FunctionVariable) {

                    FunctionVariable functionVariable = (FunctionVariable) variable;

                    for (PartedVariableHolder
                            operandHolder : functionVariable.getOperands()) {
                        if (operandHolder.getVariable() == constant) {
                            isUsed = true;
                            break;
                        }
                    }
                    
                } else if (variable instanceof GraphVariable) {

                    GraphVariable graphVariable = (GraphVariable) variable;
                    Node rootNode = graphVariable.getGraph().getRootNode();
                    if (Utility.isVariableUsedAsTerminal(rootNode, constant)) {
                        isUsed = true;
                        break;
                    }

                }

            }

            if (isUsed) {
                usedConstants.add(constant);
            }
        }

        constants = usedConstants.toArray(new ConstantVariable[usedConstants.size()]);
        LOG.exiting(LOG.getName(), "removeObsoleteConstants(1/4)");
    }

}
