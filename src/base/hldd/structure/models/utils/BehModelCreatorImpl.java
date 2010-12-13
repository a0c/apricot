package base.hldd.structure.models.utils;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.ConstantVariable;
import base.hldd.structure.variables.GraphVariable;
import io.ConsoleWriter;

import java.util.Collection;
import java.util.List;

/**
 * Creates {@link base.hldd.structure.models.BehModel}, where all HLDD structure elements are indexed.
 *
 * @author Anton Chepurov
 */
public class BehModelCreatorImpl extends AbstractModelCreator {
	private BehModel model;

	public BehModelCreatorImpl(Collection<ConstantVariable> constants, Collection<AbstractVariable> variables,
							   ConsoleWriter consoleWriter) {
		super(constants, variables, consoleWriter);
	}


	protected void doIndexGraphs(int varIndex, int graphIndex, int nodeIndex) {
		List<GraphVariable> sortedGraphList = new GraphVariablesSorter().sort(variables);

		/* Index GRAPHS without D flag */
		for (GraphVariable graphVariable : sortedGraphList) {
			if (graphVariable.isOutput()) continue;
			if (!graphVariable.isDelay()) {
				graphVariable.forceSetIndex(varIndex++);
				graphVariable.getGraph().setIndex(graphIndex++);
				graphVariable.getGraph().getRootNode().indexate(nodeIndex);
				nodeIndex += graphVariable.getGraph().getSize();
			}
		}

		/* Index GRAPHS but outputs */
		for (GraphVariable graphVariable : sortedGraphList) {
			if (graphVariable.isOutput()) continue;
			if (graphVariable.isDelay()) {
				graphVariable.forceSetIndex(varIndex++);
				graphVariable.getGraph().setIndex(graphIndex++);
				graphVariable.getGraph().getRootNode().indexate(nodeIndex);
				nodeIndex += graphVariable.getGraph().getSize();
			}
		}

		/* Index OUTPUTS */
		for (GraphVariable graphVariable : sortedGraphList) {
			if (graphVariable.isOutput()) {
				graphVariable.forceSetIndex(varIndex++);
				graphVariable.getGraph().setIndex(graphIndex++);
				graphVariable.getGraph().getRootNode().indexate(nodeIndex);
				nodeIndex += graphVariable.getGraph().getSize();
			}
		}

		/* todo: test result */
//        doCheckSorting(sortedGraphList, true);
	}

	@SuppressWarnings({"UnusedDeclaration"}) // method is/was used for debug
	private void doCheckSorting(List<GraphVariable> sortedGraphList, boolean areGraphsIndexed) {
		if (areGraphsIndexed) {
			System.out.println("FINAL sorting test!");
		}
		System.out.println("Starting test for SORTING...");
		for (GraphVariable graphVariable : sortedGraphList) {
			doCheckNode(graphVariable.getGraph().getRootNode(), -1, graphVariable, sortedGraphList, areGraphsIndexed);
		}
		System.out.println("Sorting test completed.");
	}

	private void doCheckNode(Node node, int graphIndex, GraphVariable graphVariable,
							 List<GraphVariable> sortedGraphList, boolean isIndexed) {
		/* Check Index */
		if (graphIndex == -1) {
			graphIndex = isIndexed ? graphVariable.getIndex() : sortedGraphList.indexOf(graphVariable);
		}
		AbstractVariable dependentVariable = node.getDependentVariable();
		if (dependentVariable instanceof GraphVariable && !dependentVariable.isDelay()) {
			@SuppressWarnings({"SuspiciousMethodCalls"})
			int nodeIndex = isIndexed ? dependentVariable.getIndex() : sortedGraphList.indexOf(dependentVariable);
			if (nodeIndex == -1) {
				System.out.println("Could not obtain node index. Sorted list doesn't contain node dependent variable.");
				System.exit(1);
			}
			if (nodeIndex > graphIndex) {
				System.out.println("Incorrect sorting! Graph index: " + graphIndex + ";  Node index: " + nodeIndex +
						"  (Graph name: " + graphVariable.getName() + ";  Node name: " + dependentVariable.getName() + ")");
			}
		}
		/* Check Children */
		if (node.isControlNode()) {
			for (Node childNode : node.getSuccessors()) {
				doCheckNode(childNode, graphIndex, graphVariable, sortedGraphList, isIndexed);
			}
		}
	}

	protected void doCreateModel() {
		LOGGER.entering(LOGGER.getName(), "doCreateModel(4/4)");
		model = new BehModel(variablesCollection);
		LOGGER.exiting(LOGGER.getName(), "doCreateModel(4/4)");
	}

	public BehModel getModel() {
		if (model == null) {
			create();
		}
		return model;
	}

}
