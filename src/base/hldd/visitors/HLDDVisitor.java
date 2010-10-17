package base.hldd.visitors;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.GraphVariable;

/**
 * @author Anton Chepurov
 */
public interface HLDDVisitor {
	void visitNode(Node node) throws Exception;

	void visitGraphVariable(GraphVariable graphVariable) throws Exception;
}
