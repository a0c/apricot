package base.vhdl.visitors;

import base.hldd.structure.models.utils.ModelManager;
import base.vhdl.structure.*;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.WhenNode;
import base.Indices;

import java.util.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 27.10.2009
 * <br>Time: 11:11:48
 */
public class PartialSetVariableCollector extends AbstractVisitor {
    private Map<String, Set<OperandImpl>> partialSettingsMap = new HashMap<String, Set<OperandImpl>>();

	private final ModelManager modelManager;

	public PartialSetVariableCollector(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

    /* Here only request processing of AbstractNodes(ParseTree) */
    public void visitEntity(Entity entity) throws Exception {}

    public void visitArchitecture(Architecture architecture) throws Exception {}

    public void visitProcess(base.vhdl.structure.Process process) throws Exception {
        process.getRootNode().traverse(this);
    }

    public void visitIfNode(IfNode ifNode) throws Exception {
        ifNode.getTruePart().traverse(this);
        if (ifNode.getFalsePart() != null) {
            ifNode.getFalsePart().traverse(this);
        }
    }

    public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
        if (transitionNode.isNull()) return;
        OperandImpl varOperand = transitionNode.getTargetOperand();
        if (varOperand.isParted()) {
            /* Get the set of parted variables */
            Set<OperandImpl> partedVarSet;
            /* Check that variable is already mapped and map it otherwise */
            String varOperandName = varOperand.getName();
            if (partialSettingsMap.containsKey(varOperandName)) {
                partedVarSet = partialSettingsMap.get(varOperandName);
            } else {
                partedVarSet = new HashSet<OperandImpl>();
                partialSettingsMap.put(varOperandName, partedVarSet);
            }
            /* Add operand to set */
            partedVarSet.add(varOperand);
        }
    }

    public void visitCaseNode(CaseNode caseNode) throws Exception {
        for (WhenNode whenNode : caseNode.getConditions()) {
            whenNode.traverse(this);
        }
    }

    public void visitWhenNode(WhenNode whenNode) throws Exception {
        whenNode.getTransitions().traverse(this);
    }

    public Map<String, Set<OperandImpl>> getPartialSettingsMap() {
        doDisinterlapPartialSettings();
        return partialSettingsMap;
    }

    /**
     * Splits partial settings into non-interlaping regions
     */
    private void doDisinterlapPartialSettings() {

        /* For each parted variable... */
        for (String varName : partialSettingsMap.keySet()) {
            /* create a new set of parted variable operands... */
            HashSet<OperandImpl> newPartedVarSet = new HashSet<OperandImpl>();
            /* and fill it with non-intersecting parted variable operands. */

            /* Use 2 SortedSets: a separate one for START and END markers.
             * 1) Place START and END markers into 2 SortedSets according to the following:
             *      a) Starting index -> "i" for START and "i - 1" for END
             *      b) Ending index   -> "i" for END and "i + 1" for START
             * 2) Create intervals: take next marker from either SortedSet. Number of markers in both sets is equal.
             * */
            SortedSet<Integer> startsSet = new TreeSet<Integer>();
            SortedSet<Integer> endsSet = new TreeSet<Integer>();
            int lowerBound = -1;
            int upperBound = modelManager.getVariable(varName).getLength().length();
            /* Fill SortedSets */
            for (OperandImpl partialSetOperand : partialSettingsMap.get(varName)) {
                Indices partedIndices = partialSetOperand.getPartedIndices();
                /* Starting index */
                startsSet.add(partedIndices.getLowest());
                if (partedIndices.getLowest() - 1 > lowerBound) {
                    endsSet.add(partedIndices.getLowest() - 1);
                }
                /* Ending index */
                endsSet.add(partedIndices.getHighest());
                if (partedIndices.getHighest() + 1 < upperBound) {
                    startsSet.add(partedIndices.getHighest() + 1);
                }
            }
            /* Check number of markers */
            if (startsSet.size() != endsSet.size()) throw new RuntimeException("Unexpected bug occured while " +
                    "extracting non-intersecting regions for partial setting variables:" +
                    "\n Amounts of START and END markers are different.");
            /* Create intervals */
            Integer[] startsArray = new Integer[startsSet.size()];
            startsSet.toArray(startsArray);
            Integer[] endsArray = new Integer[endsSet.size()];
            endsSet.toArray(endsArray);
            for (int i = 0; i < startsArray.length; i++) {
                newPartedVarSet.add(new OperandImpl(varName, new Indices(endsArray[i], startsArray[i]), false));
            }

            /* Replace the variable set in partigalSettingsMap with the new one */
            partialSettingsMap.put(varName, newPartedVarSet);
        }

    }
}
