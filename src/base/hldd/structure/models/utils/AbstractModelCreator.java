package base.hldd.structure.models.utils;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Utility;
import base.hldd.structure.variables.*;
import base.hldd.visitors.UsedFunctionsCollectorImpl;
import base.vhdl.structure.Operator;
import io.ConsoleWriter;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractModelCreator implements ModelCreator {
	protected static final Logger LOGGER = Logger.getLogger(AbstractModelCreator.class.getName());
	private final ConsoleWriter consoleWriter;
	protected Collection<ConstantVariable> constants;
	protected Collection<AbstractVariable> variables;
	protected Collection<AbstractVariable> variablesCollection;

	public AbstractModelCreator(Collection<ConstantVariable> constants, Collection<AbstractVariable> variables,
								ConsoleWriter consoleWriter) {
		this.constants = constants;
		this.variables = variables;
		this.consoleWriter = consoleWriter;
		variablesCollection = new LinkedList<AbstractVariable>();
	}

	public void create() {

		/* Remove those CONSTANTS that are not used neither in FUNCTION VARIABLES, nor in GRAPH VARIABLES */
		removeObsoleteConstants();

		removeObsoleteFunctions();
		renameFunctions();

		/* Perform INDEXATION */ //todo: ModelIndexator -> modelIndexator.indexate(constants, variables);
		performIndexation();

		/* Collect variables */
		collectVariables();

		/* Create MODEL */
		doCreateModel();

	}

	private void performIndexation() {
		LOGGER.entering(LOGGER.getName(), "performIndexation(2/4)");
		int varIndex = 0, graphIndex = 0, nodeIndex = 0;

//        /* Strip indices from all variables */
//        for (AbstractVariable variable : variables) {
//            variable.forceSetIndex(-1);
//        }

		/* Index INPUTS */
		for (AbstractVariable variable : variables) {
			if (variable instanceof Variable) {
				Variable variable1 = (Variable) variable;
				if (variable1.isInput()) {
					variable1.forceSetIndex(varIndex++);
				}
			}
		}
		/* Index CONSTANTS */
		for (ConstantVariable constant : constants) {
			constant.forceSetIndex(varIndex++);
		}
		/* Index FUNCTIONS */
		for (AbstractVariable variable : variables) {
			if (variable instanceof FunctionVariable) {
				variable.forceSetIndex(varIndex++);
			}
		}
		/* Index GRAPHS */
		doIndexGraphs(varIndex, graphIndex, nodeIndex);

		LOGGER.exiting(LOGGER.getName(), "performIndexation(2/4)");
	}

	protected abstract void doIndexGraphs(int varIndex, int graphIndex, int nodeIndex);

	private void collectVariables() {
		LOGGER.entering(LOGGER.getName(), "collectVariables(3/4)");

		for (ConstantVariable constant : constants) {
			if (constant.getIndex() == -1) {
				String msg = "Unindexed constant: " + constant.getName();
				LOGGER.warning(msg);
				System.out.println(msg);
				continue;
			}
			variablesCollection.add(constant);
		}
		for (AbstractVariable variable : variables) {
			if (variable.getIndex() == -1) {
				String msg = "Unindexed variable: " + variable.getName();
				LOGGER.warning(msg);
				System.out.println(msg);
				continue;
			}
			variablesCollection.add(variable);
		}

		LOGGER.exiting(LOGGER.getName(), "collectVariables(3/4)");
	}

	protected abstract void doCreateModel();

	private void removeObsoleteConstants() {
		LOGGER.entering(LOGGER.getName(), "removeObsoleteConstants(1/4)");
		LinkedList<ConstantVariable> usedConstants = new LinkedList<ConstantVariable>();

		for (ConstantVariable constant : constants) {
			boolean isUsed = false;

			for (AbstractVariable variable : variables) {
				if (variable instanceof FunctionVariable) {

					FunctionVariable functionVariable = (FunctionVariable) variable;

					for (RangeVariableHolder
							operandHolder : functionVariable.getOperands()) {
						if (operandHolder.getVariable() == constant) {
							isUsed = true;
							break;
						}
					}

				} else if (variable instanceof GraphVariable) {

					GraphVariable graphVariable = (GraphVariable) variable;
					Node rootNode = graphVariable.getGraph().getRootNode();
					if (Utility.isVariableUsedAsTerminal(rootNode, constant)) {
						isUsed = true;
						break;
					}

				}

			}

			if (isUsed) {
				usedConstants.add(constant);
			}
		}

		constants = usedConstants;
		LOGGER.exiting(LOGGER.getName(), "removeObsoleteConstants(1/4)");
	}

	private void removeObsoleteFunctions() {
		LinkedList<AbstractVariable> usedVars = new LinkedList<AbstractVariable>();

		/* Collect function usages */
		UsedFunctionsCollectorImpl functionsCollector = new UsedFunctionsCollectorImpl();
		for (AbstractVariable variable : variables) {
			if (variable instanceof GraphVariable) {
				try {
					((GraphVariable) variable).traverse(functionsCollector);
				} catch (Exception e) {
					throw new RuntimeException("Error while collecting functions used by graphs: " + e.getMessage());
				}
			}
		}

		/* Recollect variables, leaving out unused functions */
		for (AbstractVariable variable : variables) {

			if (variable instanceof FunctionVariable) {

				FunctionVariable functionVariable = (FunctionVariable) variable;
				if (functionsCollector.isUsed(functionVariable)) {
					usedVars.add(variable);
				}

			} else {

				usedVars.add(variable);

			}
		}

		variables = usedVars;
	}

	private void renameFunctions() {
		int idx = 1;
		Operator prevOperator = null;

		Collection<FunctionVariable> functions = VariableManager.getFunctions(variables, null);

		for (FunctionVariable functionVariable : functions) {
			Operator operator = functionVariable.getOperator();

			if (operator != prevOperator) {
				idx = 1;
			}

			functionVariable.setNameIdx(idx++);

			prevOperator = operator;

		}
	}

	/**
	 * For Delay graphs, no order constraints exist (since their values get assigned at the end of a cycle).
	 * <p/>
	 * Non-Delay graphs must be ordered in such a way that for every graph those node dependent variables
	 * that are Non-Delays must precede the graph (typical dependency ordering).
	 */
	protected class GraphVariablesSorter {

		private Set<Node> processedNodes = new HashSet<Node>();
		private Map<GraphVariable, Set<GraphVariable>> dependenciesByVar = new HashMap<GraphVariable, Set<GraphVariable>>();
		private GraphVariable currentlyProcessedVar;
		private Set<String> nonInitialisedVars = new TreeSet<String>();

		/**
		 * For Delay graphs, no order constraints exist (since their values get assigned at the end of a cycle).
		 * <p/>
		 * Non-Delay graphs must be ordered in such a way that for every graph those node dependent variables
		 * that are Non-Delays must precede the graph (typical dependency ordering).
		 *
		 * @param variables collection of variables to sort
		 * @return ordered list of GraphVariables
		 */
		public List<GraphVariable> sort(Collection<AbstractVariable> variables) {
			/* Collect 2 lists of GraphVariables */
			List<GraphVariable> delayGraphList = new LinkedList<GraphVariable>();
			List<GraphVariable> nonDelayGraphList = new LinkedList<GraphVariable>();
			for (AbstractVariable variable : variables) {
				if (variable instanceof GraphVariable) {
					GraphVariable graphVariable = (GraphVariable) variable;
					if (variable.isFSM()) continue;
					if (variable.isDelay()) {
						delayGraphList.add(graphVariable);
					} else {
						nonDelayGraphList.add(graphVariable);
						/* Collect dependent variables of nodes and map them to graph */
						collectDependencies(graphVariable);
					}
				}
			}
			printNonInitialisedVars();
			/* For debugging purposes: */
//            printDependencies(nonDelayGraphList);
			/* For debugging purposes: */

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
			Collections.sort(nonDelayGraphList);

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
					noDependenciesVar = nonDelayGraphList.get(0);
					printDependencies(nonDelayGraphList);
					LOGGER.warning(getClass().getSimpleName() +
							": Cyclic dependency found when ordering Non-Delay GraphVariables." +
							"\n(NonDelayGraphList is not empty, but doesn't contain a variable WITHOUT dependencies)." +
							"\nTaking first var from NonDelayGraphList (sorted alphabetically).");
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
			System.out.println("Cyclic dependency => Taking first var from NonDelayGraphList (sorted alphabetically).");
			int i = 0;
			for (GraphVariable graphVariable : dependenciesByVar.keySet()) {
				Set<GraphVariable> set = dependenciesByVar.get(graphVariable);
				if (!set.isEmpty()) {
					i++;
					System.out.print(graphVariable.getName() + ":   ");
					for (GraphVariable depVar : set) {
						System.out.print(depVar.getName() + ", ");
					}
					System.out.println("");
				}
			}
			System.out.println("Non-Delay Graph list size: " + nonDelayGraphList.size() + "; Graphs with Non-Delay dependent variables: " + i);
		}

		/**
		 * Collect Non-Delay dependent variables of nodes and map them to graph
		 *
		 * @param graphVariable where to look for dependencies
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
			if (currentlyProcessedVar == dependentVariable) {
				nonInitialisedVars.add(currentlyProcessedVar.getName());
				return;
			}
			/* Skip usages of base whole variables in range variables */
			if (isUsageOfBaseVariableInRange(dependentVariable)) return;
			/* Check for GraphVariables and if failed --- for FunctionVariables */
			if (dependentVariable instanceof GraphVariable && !dependentVariable.isDelay()) {
				depVarSet.add((GraphVariable) dependentVariable);
			} else if (dependentVariable instanceof FunctionVariable) {
				for (RangeVariableHolder operandHolder : ((FunctionVariable) dependentVariable).getOperands()) {
					/* Check Operands */
					addNonDelayGraph(operandHolder.getVariable(), depVarSet);
				}
			}
		}

		private boolean isUsageOfBaseVariableInRange(AbstractVariable dependentVariable) {
			if (!(dependentVariable instanceof GraphVariable)) { // Base variable is expected to be a GraphVariable with a CAT-function as the only node
				return false;
			}
			GraphVariable depVar = (GraphVariable) dependentVariable;

			return currentlyProcessedVar.getPureName().equals(depVar.getPureName())
					&& currentlyProcessedVar.getBaseVariable() instanceof RangeVariable
					&& !(depVar.getBaseVariable() instanceof RangeVariable);
		}

		private void printNonInitialisedVars() {
			if (nonInitialisedVars.isEmpty()) {
				return;
			}
			consoleWriter.newLine();
			for (String varName : nonInitialisedVars) {
				consoleWriter.warning("Non-delay variable " + varName + " reads itself (is not initialised).");
			}
		}

	}
}
