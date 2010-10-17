package base.vhdl.structure.nodes;

import base.vhdl.structure.Expression;
import base.vhdl.processors.AbstractProcessor;
import base.vhdl.visitors.AbstractVisitor;

/**
 * @author Anton Chepurov
 */
public class IfNode extends AbstractNode {

	private Expression conditionExpression;

	private CompositeNode truePart;

	private CompositeNode falsePart;


	public IfNode(Expression conditionExpression) {
		this.conditionExpression = conditionExpression;
		truePart = new CompositeNode(); //todo: don't create the object inside it!!! create using Creator, otherwise if IfNode doesn't have the true part, it will still have the true CompositeNode.
		truePart.setParent(this);
		falsePart = null;
	}

	/**
	 * Adds a new node either to <code>truePart</code> or <code>falsePart</code> according to the following strategy:
	 * <br>- While <code>falsePart</code> is not initiated, add nodes to <code>truePart</code>.
	 * <br>- As soon as <code>falsePart</code> is initiated, add nodes to <code>falsePart</code>.
	 * <br><br><b>NB!</b> <code>falsePart</code> is initiated by VHDLStructureBuilder during the parsing process,
	 * when an ELSIF or ELSE part is met.
	 *
	 * @param newNode node to add to true/false parts
	 */
	public void addTransition(AbstractNode newNode) {
		if (falsePart == null) {
			/* While falsePart is not created
			* add newNode to TRUE_PART */
			truePart.addNode(newNode);
		} else {
			/* IF falsePart is already created,
			* add newNode to FALSE_PART */
			falsePart.addNode(newNode);
		}
	}

	public void markFalsePart() {
		if (falsePart == null) {
			falsePart = new CompositeNode();
			falsePart.setParent(this);
		}
	}

	public void traverse(AbstractVisitor visitor) throws Exception {
		visitor.visitIfNode(this);
	}

	public void process(AbstractProcessor processor) {
		processor.processIfNode(this);
	}

	/* GETTERS and SETTERS */

	public Expression getConditionExpression() {
		return conditionExpression;
	}

	public CompositeNode getTruePart() {
		return truePart;
	}

	public CompositeNode getFalsePart() {
		return falsePart; //todo: check if falsePart == null, and init it, so that clients wouldn't need to check if falsePart == null themselves
	}

	public void setFalsePart(CompositeNode falsePart, IfNode parentNode) {
		this.falsePart = falsePart;
		this.falsePart.setParent(parentNode);
	}

	public boolean hasFalseChild(AbstractNode abstractNodeToCheck) {
		if (falsePart == null) return false;
		for (AbstractNode child : falsePart.getChildren()) {
			if (child == abstractNodeToCheck) {
				return true;
			}
		}
		return false;
	}

	public boolean isIdenticalTo(AbstractNode comparedNode) {
		if (!(comparedNode instanceof IfNode)) return false;
		IfNode comparedIfNode = (IfNode) comparedNode;

		/* Compare EXPRESSIONS */
		if (!conditionExpression.isIdenticalTo(comparedIfNode.conditionExpression)) return false;
		/* Compare TRUE part */
		if (!truePart.isIdenticalTo(comparedIfNode.getTruePart())) return false;
		/* Compare FALSE part */
		if (falsePart == null ^ comparedIfNode.getFalsePart() == null) return false;
		if (falsePart != null && !falsePart.isIdenticalTo(comparedIfNode.getFalsePart())) return false;

		/* All tests passed */
		return true;
	}

	public String toString() {
		return conditionExpression.toString();
	}
}


