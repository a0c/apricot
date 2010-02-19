package base.hldd.structure.models.utils;

import base.hldd.structure.variables.*;
import base.hldd.structure.models.BehModel;
import base.hldd.structure.nodes.Node;

import java.util.*;

/**
 * Creates {@link base.hldd.structure.models.BehModel}, where all HLDD structure elements are indexed. 
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 11.02.2008
 * <br>Time: 21:11:19
 */
public class BehModelCreatorImpl extends AbstractModelCreator {
    private BehModel model;

    public BehModelCreatorImpl(ConstantVariable[] constants, AbstractVariable[] variables) {
        super(constants, variables);
    }


    protected void doIndexGraphs(int varIndex, int graphIndex, int nodeIndex) {
        List<GraphVariable> sortedGraphList = new GraphVariablesSorter().sort(variables);

        /* Index GRAPHS without D flag */
        for (GraphVariable graphVariable : sortedGraphList) {
            if (graphVariable.isOutput()) continue;
            if (!graphVariable.isDelay()) {
                graphVariable.setIndex(varIndex++);
                graphVariable.getGraph().setIndex(graphIndex++);
                graphVariable.getGraph().getRootNode().indexate(nodeIndex);
                nodeIndex += graphVariable.getGraph().getSize();
            }
        }

        /* Index GRAPHS but outputs */
        for (GraphVariable graphVariable : sortedGraphList) {
            if (graphVariable.isOutput()) continue;
            if (graphVariable.isDelay()) {
                graphVariable.setIndex(varIndex++);
                graphVariable.getGraph().setIndex(graphIndex++);
                graphVariable.getGraph().getRootNode().indexate(nodeIndex);
                nodeIndex += graphVariable.getGraph().getSize();
            }
        }

        /* Index OUTPUTS */
        for (GraphVariable graphVariable : sortedGraphList) {
            if (graphVariable.isOutput()) {
                graphVariable.setIndex(varIndex++);
                graphVariable.getGraph().setIndex(graphIndex++);
                graphVariable.getGraph().getRootNode().indexate(nodeIndex);
                nodeIndex += graphVariable.getGraph().getSize();
            }
        }

        /* todo: test result */
//        doCheckSorting(sortedGraphList, true);
    }

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
        int nodeIndex = isIndexed ? dependentVariable.getIndex() : sortedGraphList.indexOf(dependentVariable);
        if (dependentVariable instanceof GraphVariable && !dependentVariable.isDelay()) {
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
        LOG.entering(LOG.getName(), "doCreateModel(4/4)");
        model = new BehModel(indexVariableHash);
        LOG.exiting(LOG.getName(), "doCreateModel(4/4)");
    }

    public BehModel getModel() {
        if (model == null) {
            create();
        }
        return model;
    }

    /**
     * For Delay graphs, no order constraints exist (since their values get assigned at the end of a cycle).
     *
     * Non-Delay graphs must be ordered in such a way that for every graph those node dependent variables
     * that are Non-Delays must precede the graph (typical dependency ordering).
     */
    private class GraphVariablesSorter {

        private Set<Node> processedNodes = new HashSet<Node>();
        private Map<GraphVariable, Set<GraphVariable>> dependenciesByVar = new HashMap<GraphVariable, Set<GraphVariable>>();
        private GraphVariable currentlyProcessedVar;

        /**
         * For Delay graphs, no order constraints exist (since their values get assigned at the end of a cycle).
         *
         * Non-Delay graphs must be ordered in such a way that for every graph those node dependent variables
         * that are Non-Delays must precede the graph (typical dependency ordering).
         *
         * @param variables array of variables to sort
         * @return ordered list of GraphVariables
         */
        public List<GraphVariable> sort(AbstractVariable[] variables) {
            /* Collect 2 lists of GraphVariables */
            List<GraphVariable> delayGraphList = new LinkedList<GraphVariable>();
            List<GraphVariable> nonDelayGraphList = new LinkedList<GraphVariable>();
            for (AbstractVariable variable : variables) {
                if (variable instanceof GraphVariable) {
                    GraphVariable graphVariable = (GraphVariable) variable;
                    if (variable.isDelay()) {
                        delayGraphList.add(graphVariable);
                    } else {
                        nonDelayGraphList.add(graphVariable);
                        /* Collect dependent variables of nodes and map them to graph */
                        collectDependencies(graphVariable);
                    }
                }
            }

            /* todo: For debugging purposes: */
//            printDependencies(nonDelayGraphList);
            /* todo: For debugging purposes: */

            /* Sort Non-Delay Graphs list */
            nonDelayGraphList = sortWithComparator(nonDelayGraphList);

            /* Add delay graphs to the ordered nonDelayList and return the latter */
            nonDelayGraphList.addAll(delayGraphList);

            return nonDelayGraphList;
        }

        private List<GraphVariable> sortWithComparator(List<GraphVariable> nonDelayGraphList) {
            /*
            *  The task reduces actually to the task of Topological Sort,
            *  or, in other words, Directed Acyclic Graph (DAG) Sort.
            *  So the implementation below is a Topological Sort of a list pre-sorted by variable names.
            * */

            /* Pre-sort list by variable names to achieve both:
            * 1) alphabetical order of mutually independent variables;
            * 2) deterministic behaviour of the sorting procedure for multiple invocations on the same input data.
            * */
            Collections.sort(nonDelayGraphList, new Comparator<GraphVariable>() {
                public int compare(GraphVariable o1, GraphVariable o2) {
                    return o1.getName().compareTo(o2.getName()); // todo: PartedVariable.getUniqueName()
                }
            });

            /* Sort Topologically */
            LinkedList<GraphVariable> sortedList = new LinkedList<GraphVariable>();
            while (!nonDelayGraphList.isEmpty()) {

                /* Get next variable without dependencies */
                GraphVariable noDependenciesVar = null;
                for (GraphVariable variable : nonDelayGraphList) {
                    if (hasNoDependencies(variable)) {
                        noDependenciesVar = variable;
                        dependenciesByVar.remove(noDependenciesVar); /* Remove from map to speedup all requests to dependenciesByVar */
                        break;
                    }
                }
                if (noDependenciesVar == null) {
                    printDependencies(nonDelayGraphList);
                    throw new RuntimeException(getClass().getSimpleName() + ": Cannot order Non-Delay GraphVariables." +
                            "\nReason: Cyclic dependency exists." +
                            "\n(NonDelayGraphList is not empty, but doesn't contain a variable WITHOUT dependencies)");
                }

                /* Add to sorted list */
                sortedList.add(noDependenciesVar);
                /* Remove from input list */
                nonDelayGraphList.remove(noDependenciesVar);
                /* Remove from other variables' dependencies */
                for (Set<GraphVariable> dependencies : dependenciesByVar.values()) {
                    dependencies.remove(noDependenciesVar);
                }
            }

            return sortedList;
        }

        private boolean hasNoDependencies(GraphVariable variable) {
            return dependenciesByVar.get(variable).isEmpty();
        }        

        private void printDependencies(List<GraphVariable> nonDelayGraphList) {
            int i = 0;
            for (GraphVariable graphVariable : dependenciesByVar.keySet()) {
                Set<GraphVariable> set = dependenciesByVar.get(graphVariable);
                if (!set.isEmpty()) {
                    i++;
                    String partedIndices = graphVariable.getBaseVariable() instanceof PartedVariable
                            ? ((PartedVariable) graphVariable.getBaseVariable()).getPartedIndices().toString() : "";
                    System.out.print(graphVariable.getName() + partedIndices + ":   ");
                    for (GraphVariable depVar : set) {
                        partedIndices = depVar.getBaseVariable() instanceof PartedVariable
                            ? ((PartedVariable) depVar.getBaseVariable()).getPartedIndices().toString() : "";
                        System.out.print(depVar.getName() + partedIndices + ", ");
                    }
                    System.out.println("");
                }
            }
            System.out.println("Non-Delay Graph list size: " + nonDelayGraphList.size() + "; Graphs with Non-Delay dependent variables: " + i);
        }

        /**
         * Collect Non-Delay dependent variables of nodes and map them to graph
         * @param graphVariable
         */
        private void collectDependencies(GraphVariable graphVariable) {
            /* Create an empty set of dependent variables and map it to graph */
            Set<GraphVariable> depVarSet = new HashSet<GraphVariable>();
            dependenciesByVar.put(graphVariable, depVarSet);
            currentlyProcessedVar = graphVariable;
            /* Iterate nodes of the graphVariable and add Non-Delay dependent variables to the set */
            collectNonDelayDepVars(depVarSet, graphVariable.getGraph().getRootNode());
            processedNodes = new HashSet<Node>(); /* Free memory and speedup contains() check */
            currentlyProcessedVar = null;
        }

        private void collectNonDelayDepVars(Set<GraphVariable> depVarSet, Node rootNode) {
            /* Collect Non-Delay dependent variable */
            if (processedNodes.contains(rootNode)) return; // Skip processed nodes (used in CYCLIC HLDDs)
            AbstractVariable dependentVariable = rootNode.getDependentVariable();
            addNonDelayGraph(dependentVariable, depVarSet);
            processedNodes.add(rootNode); // Mark node as processed
            /* Process Control Node children */
            if (rootNode.isControlNode()) {
                for (Node successor : rootNode.getSuccessors()) {
                    collectNonDelayDepVars(depVarSet, successor);
                }
            }
        }

        private void addNonDelayGraph(AbstractVariable dependentVariable, Set<GraphVariable> depVarSet) {
            /* Skip value retaining nodes (case when variable depends on itself) */
            if (currentlyProcessedVar == dependentVariable) return;
            /* Skip usages of base whole variables in parted variables */
            if (currentlyProcessedVar.getName().equals(dependentVariable.getName())
                    && currentlyProcessedVar.getBaseVariable() instanceof PartedVariable && dependentVariable instanceof GraphVariable 
                    && !(((GraphVariable) dependentVariable).getBaseVariable() instanceof PartedVariable)) return;
            /* Check for GraphVariables and if failed --- for FunctionVariables */
            if (dependentVariable instanceof GraphVariable && !dependentVariable.isDelay()) {
                depVarSet.add((GraphVariable) dependentVariable);
            } else if (dependentVariable instanceof FunctionVariable) {
                for (PartedVariableHolder operandHolder : ((FunctionVariable) dependentVariable).getOperands()) {
                    /* Check Operands */
                    addNonDelayGraph(operandHolder.getVariable(), depVarSet);
                }
            }
        }

    }
}
