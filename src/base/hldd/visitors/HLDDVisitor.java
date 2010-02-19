package base.hldd.visitors;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.GraphVariable;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 27.11.2008
 * <br>Time: 12:54:46
 */
public interface HLDDVisitor {
    void visitNode(Node node) throws Exception;

    void visitGraphVariable(GraphVariable graphVariable) throws Exception;
}
