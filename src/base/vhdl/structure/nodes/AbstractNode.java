package base.vhdl.structure.nodes;

import base.SourceLocation;
import base.vhdl.visitors.Visitable;
import base.vhdl.processors.Processable;
import base.vhdl.processors.NodeReplacerImpl;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractNode implements Visitable, Processable {

	protected AbstractNode parentNode;
	/* Line numbers in VHDL file this Node was created from */
	private SourceLocation source;

	public abstract boolean isIdenticalTo(AbstractNode comparedNode);

	/**
	 * Replaces the node with the specified replacingNode.
	 * May say whether any replacement took place or not
	 * (use {@link base.vhdl.processors.NodeReplacerImpl#isReplaced}
	 * for this).
	 *
	 * @param replacingNode node to replace with
	 * @param sourceProcess process which has this node in its tree. Required in case
	 *                      the node is the root node of the process (i.e. has no parent node).
	 * @throws Exception if TerminalNode is being replaced
	 */
	public void replaceWith(AbstractNode replacingNode, base.vhdl.structure.Process sourceProcess) throws Exception {
		NodeReplacerImpl nodeReplacer = new NodeReplacerImpl(this, replacingNode);
		if (parentNode == null) { // this node is the root node of the process
			sourceProcess.getRootNode().process(nodeReplacer);
		} else {
			parentNode.process(nodeReplacer);
		}
	}

	public void setParent(AbstractNode parentNode) {
		this.parentNode = parentNode;
	}

	public AbstractNode getParentNode() {
		return parentNode;
	}

	public void setSource(SourceLocation source) {
		this.source = source;
	}

	public SourceLocation getSource() {
		return source;
	}
}
