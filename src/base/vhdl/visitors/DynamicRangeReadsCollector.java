package base.vhdl.visitors;

import base.SourceLocation;
import base.vhdl.structure.AbstractOperand;
import base.vhdl.structure.Architecture;
import base.vhdl.structure.Entity;
import base.vhdl.structure.OperandImpl;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.WhenNode;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class DynamicRangeReadsCollector extends AbstractVisitor {

	private Collection<Read> reads = new LinkedList<Read>();

	public Iterable<Read> getReads() {
		return reads;
	}

	@Override
	public void visitEntity(Entity entity) throws Exception {
	}

	@Override
	public void visitArchitecture(Architecture architecture) throws Exception {
		architecture.getTransitions().traverse(this);
	}

	@Override
	public void visitProcess(base.vhdl.structure.Process process) throws Exception {
		process.getRootNode().traverse(this);
	}

	@Override
	public void visitIfNode(IfNode ifNode) throws Exception {
		collect(ifNode.getConditionExpression(), ifNode.getSource());
		ifNode.getTruePart().traverse(this);
		if (ifNode.getFalsePart() != null) {
			ifNode.getFalsePart().traverse(this);
		}
	}

	@Override
	public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
		collect(transitionNode.getValueOperand(), transitionNode.getSource());
	}

	@Override
	public void visitCaseNode(CaseNode caseNode) throws Exception {
		collect(caseNode.getVariableOperand(), caseNode.getSource());
		for (WhenNode whenNode : caseNode.getConditions()) {
			whenNode.traverse(this);
		}
	}

	@Override
	public void visitWhenNode(WhenNode whenNode) throws Exception {
		whenNode.getTransitions().traverse(this); //todo: move this stuff to AbstractVisitor and only override where needed. reason: this very line repeats way too often.
	}

	private void collect(AbstractOperand abstractOperand, SourceLocation source) {
		if (abstractOperand instanceof OperandImpl) {
			OperandImpl operand = (OperandImpl) abstractOperand;
			if (operand.isDynamicRange()) {
				reads.add(new Read(operand, source));
			}
		}
	}

	public class Read {
		final OperandImpl operand;
		final SourceLocation source;

		public Read(OperandImpl operand, SourceLocation source) {
			this.operand = operand;
			this.source = source;
		}
	}
}
