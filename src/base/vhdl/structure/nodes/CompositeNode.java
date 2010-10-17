package base.vhdl.structure.nodes;

import base.vhdl.visitors.AbstractVisitor;
import base.vhdl.processors.AbstractProcessor;

import java.util.List;
import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class CompositeNode extends AbstractNode {

	private List<AbstractNode> children;

	public CompositeNode() {
		children = new LinkedList<AbstractNode>();
	}

	public void addNode(AbstractNode newNode) {
		newNode.setParent(parentNode);
		children.add(newNode);
	}

	public List<AbstractNode> getChildren() {
		return children;
	}

	public void setChildren(List<AbstractNode> children) {
		this.children = children;
	}

	public void traverse(AbstractVisitor visitor) throws Exception {
		for (AbstractNode child : children) {
			child.traverse(visitor);
		}
	}

	public void process(AbstractProcessor processor) {
		processor.processCompositeNode(this);
	}

	public boolean isIdenticalTo(AbstractNode comparedNode) {
		/* Compare TYPES */
		if (!(comparedNode instanceof CompositeNode)) return false;

		/* Get CHILDREN */
		CompositeNode comparedCompNode = (CompositeNode) comparedNode;
		AbstractNode[] children = this.children.toArray(new AbstractNode[this.children.size()]);
		AbstractNode[] comparedChildren = comparedCompNode.children.toArray(new AbstractNode[comparedCompNode.children.size()]);
		/* Compare NUMBER of CHILDREN */
		if (children.length != comparedChildren.length) return false;
		/* Compare CHILDREN */
		for (int i = 0; i < comparedChildren.length; i++) {
			if (!children[i].isIdenticalTo(comparedChildren[i])) return false;
		}

		/* All tests passed. */
		return true;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (AbstractNode abstractNode : children) {
			sb.append(abstractNode.toString()).append("\n");
		}
		return sb.toString();
	}

	public void setParent(AbstractNode parentNode) {
		super.setParent(parentNode);
		/* Set new parent for all children */
		for (AbstractNode child : children) {
			child.setParent(parentNode);
		}
	}

}
