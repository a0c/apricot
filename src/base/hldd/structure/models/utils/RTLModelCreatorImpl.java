package base.hldd.structure.models.utils;

import base.hldd.structure.variables.ConstantVariable;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.GraphVariable;
import base.hldd.structure.models.Model;
import base.hldd.structure.models.BehModel;
import parsers.Beh2RtlTransformer.ControlPartManager;

import java.util.Collection;
import java.util.List;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 20.02.2008
 * <br>Time: 0:36:46
 */
public class RTLModelCreatorImpl extends AbstractModelCreator {
    private Model rtlModel;
    private final ControlPartManager controlPartManager;

    public RTLModelCreatorImpl(Collection<ConstantVariable> constants, Collection<AbstractVariable> variables, ControlPartManager controlPartManager) {
        super(constants, variables);
        this.controlPartManager = controlPartManager;
    }

    protected void doIndexGraphs(int varIndex, int graphIndex, int nodeIndex) {
		List<GraphVariable> sortedGraphList = new GraphVariablesSorter().sort(variables);

        /* Index STATE variable */
        for (AbstractVariable absVariable : variables) {
            if (absVariable.isState()) {
                absVariable.forceSetIndex(varIndex++);
                break;
            }
        }

        /* Index CONTROL PART OUTPUTS
        * NB! Their order must be preserved! */
        for (AbstractVariable absVariable : variables) {
            if (absVariable.isCout() && !absVariable.isState()) {
                absVariable.forceSetIndex(varIndex + controlPartManager.getCoutIndex(absVariable) - 1);
            }
        }
        varIndex += controlPartManager.getCoutCount() - 1;

        /* Index FSM GRAPH */
        for (AbstractVariable absVariable : variables) {
            if (absVariable instanceof GraphVariable) {
                GraphVariable graphVariable = (GraphVariable) absVariable;
                if (graphVariable.isFSM()) {
                    graphVariable.forceSetIndex(varIndex++);
                    graphVariable.getGraph().setIndex(graphIndex++);
                    graphVariable.getGraph().getRootNode().indexate(nodeIndex);
                    nodeIndex += graphVariable.getGraph().getSize();
                }
            }
        }

        /* Index GRAPHS but outputs */
        for (AbstractVariable absVariable : sortedGraphList) {
            if (absVariable instanceof GraphVariable) {
                GraphVariable graphVariable = (GraphVariable) absVariable;
                if (graphVariable.isOutput()) continue;
                graphVariable.forceSetIndex(varIndex++);
                graphVariable.getGraph().setIndex(graphIndex++);
                graphVariable.getGraph().getRootNode().indexate(nodeIndex);
                nodeIndex += graphVariable.getGraph().getSize();
            }
        }

        /* Index OUTPUT GRAPHS */
        for (AbstractVariable absVariable : sortedGraphList) {
            if (absVariable instanceof GraphVariable) {
                GraphVariable graphVariable = (GraphVariable) absVariable;
                if (graphVariable.isOutput()) {
                    graphVariable.forceSetIndex(varIndex++);
                    graphVariable.getGraph().setIndex(graphIndex++);
                    graphVariable.getGraph().getRootNode().indexate(nodeIndex);
                    nodeIndex += graphVariable.getGraph().getSize();
                }
            }
        }

    }

    protected void doCreateModel() {
        rtlModel = new Model(variablesCollection);
    }

    public BehModel getModel() {
        if (rtlModel == null) {
            create();
        }
        return rtlModel;
    }
}
