package base.vhdl.visitors;

import base.vhdl.structure.*;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.*;

import java.util.Set;
import java.util.HashSet;

/**
 * Traverses a VHDL Beh DD tree and simplifies redundant IfNodes.
 * IfNode is redundant if its TRUE part and FALSE part are identical.
 * Simplification implies replacing the IfNode with its TRUE part.
 * <p/>
 * Simplification is performed in BOTTOM-UP way, starting from the first TransitionNode encountered.
 * For this TransitionNode:
 * 1) node's parent is determined.
 * 2) if the parent is an IfNode, then it's FALSE part is traversed and simplified (method traverseFalsePartOfNodsParent()).
 * 3) when the parent's FALSE part has been traversed, the parent's simplification starts (method simplifyIfNode()).
 * 4) during simplification, if the parent's TRUE and FALSE parts are identical,
 * then the parent IfNode is replaced with its own TRUE part.
 * NB! todo
 * ###########################################################
 * <p/>
 * CaseNodes are simplified as well, if the alsoSimplifyCase flag is set to true.
 * If CaseNode is redundant (all its WhenNodes are identical), then this CaseNode is
 * replaced with the transitions of its first WhenNode. todo: !!!
 * <b>NB!</b> This flag should be set to <code>true</code> only along with the trimMissingFalsePart set to <code>true</code>.
 * Otherwise the possibility of ending up with <u>unsimplified</u> Case condition is very high. <--- wrong! See javadoc of constructor.
 * ###########################################################
 * <p/>
 * IfNodes with missing FALSE parts are simplified as well, if the trimMissingFalsePart flag is set to true.
 * Then the IfNode with a missing FALSE part is replaced with its TRUE part.
 *
 * @author Anton Chepurov
 */
public class IfNodeSimplifier extends AbstractVisitor {

	private TraversalTracker tracker;
	private boolean alsoSimplifyCase;
	private boolean trimMissingFalsePart;

	private Process currentProcess;

	/**
	 * <b>NB!</b> Set both parameters to <code>false</code> to receive
	 * the output most close to the original VHDL file.
	 *
	 * @param alsoSimplifyCase	 if the Case conditions with identical When condition transitions
	 * 							 should be simplified.
	 * 							 <b>NB!</b> Setting this flag to <code>true</code> may still remain inefficient
	 * 							 if the <code>trimMissingFalsePart</code> is set to <code>false</code>, since the possibility
	 * 							 of ending up with identical When conditions of the CaseNode reduces greatly.
	 * @param trimMissingFalsePart if the control IfNodes with missing FALSE parts should be simplified.
	 * 							   <b>NB!</b> It's a hack for the buggy HIF2HDL, which produces
	 * 							   obsolete conditions. In fact, this <b>flag shouldn't be used</b>,
	 * 							   since we don't know when HIF2HDL works correctly and when not. (Works
	 * 							   well for b04).
	 * @throws Exception if simplification of a non VHDL Beh DD structure is being performed
	 */
	public IfNodeSimplifier(boolean alsoSimplifyCase, boolean trimMissingFalsePart) throws Exception {
		this.alsoSimplifyCase = alsoSimplifyCase;
		this.trimMissingFalsePart = trimMissingFalsePart;
	}

	public void visitEntity(Entity entity) throws Exception {
		/* Remove redundant ELSIFs */
		entity.traverse(new RedundantElsifRemover());   // Before: IF RES = 1 .... ELSIF NOT(RES == 1); After: IF RES = 1 ... ELSE;
	}

	public void visitArchitecture(Architecture architecture) throws Exception {
	}

	public void visitProcess(base.vhdl.structure.Process process) throws Exception {
		tracker = new TraversalTracker();
		currentProcess = process;
		process.getRootNode().traverse(this);
	}

	public void visitIfNode(IfNode ifNode) throws Exception {

		/* Simplify TRUE part */
		ifNode.getTruePart().traverse(this);

		/* Simplify FALSE part */
		if (ifNode.getFalsePart() != null) {

			/* Simplify FALSE part if it hasn't been traversed so far */
			if (!tracker.isTraversed(ifNode)) {
				tracker.markTraversed(ifNode);
				ifNode.getFalsePart().traverse(this);
			}

			/* Substitute IDENTICAL parts with a single transition */
			simplifyIfNode(ifNode);
		} else {
			if (trimMissingFalsePart) {
				/*todo: experimental: mark as traversed and simplify  */
				tracker.markTraversed(ifNode);
				/* Substitute IDENTICAL parts with a single transition */
				simplifyIfNode(ifNode);
			}
		}
	}


	public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
		traverseFalsePartOfNodsParent(transitionNode);
	}

	public void visitCaseNode(CaseNode caseNode) throws Exception {
		for (WhenNode whenNode : caseNode.getConditions()) {
			whenNode.traverse(this);
		}

		/*todo: Experimental: Check for Redundant CaseNode */
		if (alsoSimplifyCase) {
			if (caseNode.isRedundantNode()) {
				/* Link its children to its parent */
				caseNode.replaceWith(caseNode.getConditions().get(0).getTransitions(), currentProcess);
			}
		}
	}

	public void visitWhenNode(WhenNode whenNode) throws Exception {
		whenNode.getTransitions().traverse(this);
	}

	private void traverseFalsePartOfNodsParent(TransitionNode transitionNode) throws Exception {
		AbstractNode absParentNode = transitionNode.getParentNode();
		/* Optimizable are only children of IfNodes */
		if (!(absParentNode instanceof IfNode)) return;

		IfNode parentNode = (IfNode) absParentNode;
		CompositeNode falsePart = parentNode.getFalsePart();
		/* Traverse FALSE part */
		if (falsePart != null) {
			/* If transitionNode resides exactly in FALSE part,
			* then terminate the recursion (to both avoid infinite recursion and return back to visitIfNode) */
			if (parentNode.hasFalseChild(transitionNode)) {
				/* Optimizable are only children of IfNodes
				* that reside in TRUE part */
				/* Transition node resides in the FALSE part of the parent IfNode, so exit */
				return;
			}

			/* Traverse (and try to optimize) the FALSE PART */
			tracker.markTraversed(parentNode);
			falsePart.traverse(this);

		} else {
			if (trimMissingFalsePart) {
				/*todo: Experimental: mark the node as traversed and in simplifyIfNode check for empty false part  */
				tracker.markTraversed(parentNode);
			}
		}

	}

	/**
	 * Simplifies specified ifNode in the following cases:    todo: rename "simplify" to "reduce"
	 * 1) if True- and FalseParts are identical;
	 * 2) if <code>trimMissingFalsePart</code> is set to <code>true</code>;
	 *
	 * @param nodeToSimplify node to simplify
	 * @throws Exception if TerminalNode is being simplified
	 */
	private void simplifyIfNode(IfNode nodeToSimplify) throws Exception {

		/* Substitute IDENTICAL parts with a single transition */
		if (/* todo: experimental */trimMissingFalsePart || nodeToSimplify.getTruePart().isIdenticalTo(nodeToSimplify.getFalsePart())) {  /*Before experimental: nodeToSimplify.getFalsePart() == null*/

			nodeToSimplify.replaceWith(nodeToSimplify.getTruePart(), currentProcess);

		}
	}

	/* AUXILIARY CLASSES */

	/**
	 * Keeps track of IfNode's traversed FALSE parts.
	 * Simplification of an IfNode can be performed only if the IfNode's false part
	 * has been traversed and simplified during the traversal.
	 */
	private class TraversalTracker {
		private Set<IfNode> traversedSet;

		private TraversalTracker() {
			traversedSet = new HashSet<IfNode>();
		}

		private void markTraversed(IfNode optimizedNode) {
			traversedSet.add(optimizedNode);
		}

		private boolean isTraversed(IfNode ifNode) {
			return traversedSet.contains(ifNode);
		}
	}

}
