package base.hldd.visitors;

import base.HLDDException;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.GraphVariable;

/**
 * @author Anton Chepurov
 */
public class SourceLocationMerger implements HLDDVisitor {
	private Node secondNode;

	public SourceLocationMerger(Node secondNode) {
		this.secondNode = secondNode;
	}

	public void visitNode(Node node) {
		/* Add VHDL lines to node */
		node.addSource(secondNode.getSource());
		/* For ControlNodes, merge the sub-tree as well */
		if (node.isControlNode()) {
			int conditionsCount = node.getConditionsCount();
			for (int idx = 0; idx < conditionsCount; idx++) {
				try {
					Condition condition = node.getCondition(idx);
					Node backupSecond = secondNode; // BACKUP
					/* Update secondNode */
					secondNode = secondNode.getSuccessor(condition);
					/* Traverse successor (secondNode is now updated) */
					visitNode(node.getSuccessor(condition));
					secondNode = backupSecond; // RESTORE
				} catch (HLDDException e) {
					throw new RuntimeException(e); // should not happen
				}
			}
		}
	}

	public void visitGraphVariable(GraphVariable graphVariable) throws Exception {
	}
}
