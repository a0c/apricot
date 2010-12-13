package parsers;

import base.Range;
import base.Type;
import base.hldd.structure.Flags;
import base.hldd.structure.Graph;
import base.hldd.structure.models.BehModel;
import base.hldd.structure.models.Model;
import base.hldd.structure.models.utils.ModelCreator;
import base.hldd.structure.models.utils.ModelManager;
import base.hldd.structure.models.utils.RTLModelCreatorImpl;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.fsm.Transitions;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.*;
import base.hldd.visitors.FsmGraphCreator;
import base.hldd.visitors.ResetInspector;
import base.hldd.visitors.TerminalNodeCollector;
import io.ConsoleWriter;
import ui.ConfigurationHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Anton Chepurov
 */
public class Beh2RtlTransformer {

	private static final Logger LOGGER = Logger.getLogger(Beh2RtlTransformer.class.getName());

	private static final String FSM_VARIABLE_NAME = "CONTROL";

	private ModelManager modelCollector;
	private BehModel behModel;
	private final ConsoleWriter consoleWriter;
	private Model rtlModel;

	public Beh2RtlTransformer(BehModel behModel, ConsoleWriter consoleWriter) {
		this.behModel = behModel;
		this.consoleWriter = consoleWriter;
	}

	public void transform() throws Exception {

		if (modelCollector == null) {
			modelCollector = new ModelManager();
		}

		/* Minimize */
		behModel.minimize();

		/* Copy CONSTANTS */
		for (ConstantVariable constantVariable : behModel.getConstants()) {
			modelCollector.addVariable(constantVariable);
		}
		/* Copy INPUTS, FUNCTIONS and STATE variable */
		for (AbstractVariable absVariable : behModel.getVariables()) {
			if (absVariable.isInput()) modelCollector.addVariable(absVariable);
			else if (absVariable instanceof FunctionVariable) modelCollector.addVariable(absVariable);
		}

		/* Map GraphVariables with their resetting variables */
		ResetInspector resetInspector = createResetMapping();

		/* Map values of MUX_ADDR variables with their successors */
		TerminalNodeCollector termNodeCollector = createMuxAddrValuesMapping();
		/*todo: If there's only 1 terminal node, then substitute all usages of this variable with the dependent
		  todo: variable of this terminal node and remove the variable itself. Usages in Functions and Graphs. */

		/* Create control part outputs */
		ControlPartManager controlPartManager = new ControlPartManager(termNodeCollector);
		createControlPartOutputs(resetInspector, controlPartManager);

		/* Create FSM Graph */
		createFsmGraph(controlPartManager);

		/* Create Graphs */
		createGraphs(resetInspector, controlPartManager);

		/* Create new MODEL */
		ModelCreator modelCreator = new RTLModelCreatorImpl(modelCollector.getConstants(), modelCollector.getVariables(), controlPartManager, consoleWriter);
		rtlModel = (Model) modelCreator.getModel();

	}


	private void createGraphs(ResetInspector resetInspector, ControlPartManager controlPartManager) throws Exception {

		for (AbstractVariable absVariable : behModel.getVariables()) {
			if (absVariable instanceof GraphVariable) {
				GraphVariable graphVariable = (GraphVariable) absVariable;
				if (graphVariable.isState()) continue;
				Node rootNode = null;

				/* Create RESET if needed */
				if (resetInspector.isResettableVariable(graphVariable)) {
					/* Create var_RESET Control Node */
					rootNode = new Node.Builder(modelCollector.getVariable(graphVariable.getName() + ControlPartManager.SUFFIX_RESET)).createSuccessors(2).build();
					/* Fill Control Node with resetting successor */
					rootNode.setSuccessor(Condition.TRUE, new Node.Builder(resetInspector.getResettingVariable(graphVariable)).build());
				}

				/* Create ENABLE and MUX_ADDR if needed:
				* (1) 1 Terminal Node / 1 Retaining   ------>
				* (2) 1 Terminal Node + 1 Retaining  ------>  ENABLE
				* (3) 2 Terminal Nodes + 1 Retaining ------>  ENABLE + MUX_ADDR
				* (4) 2 Terminal Nodes               ------>  MUX_ADDR
				* */
				Node nodeForRoot;
				int terminalNodesCount = controlPartManager.getTerminalNodesCount(graphVariable);
				int valueRetainingCount = controlPartManager.termNodeCollector.isValueRetainingUsed(graphVariable) ? 1 : 0;
				if (terminalNodesCount + valueRetainingCount > 1) {
					/* (2) (3) (4) */
					/* Create ENABLE if needed */
					Node enableNode = null;
					if (valueRetainingCount == 1) {
						/* (2) (3) */
						enableNode = new Node.Builder(modelCollector.getVariable(graphVariable.getName() + ControlPartManager.SUFFIX_ENABLE)).createSuccessors(2).build();
						/* Fill ENABLE with VALUE RETAINING node */
						enableNode.setSuccessor(Condition.FALSE, new Node.Builder(graphVariable).build());
					}

					/* Create MUX_ADDR if needed */
					Node muxAddrNode = null;
					if (terminalNodesCount > 1) {
						/* (3) (4) */
						muxAddrNode = new Node.Builder(modelCollector.getVariable(graphVariable.getName() + ControlPartManager.SUFFIX_MUX_ADDR)).createSuccessors(terminalNodesCount).build();
						/* Fill Mux_Addr with Terminal Nodes */
						controlPartManager.fillMuxAddrNode(graphVariable, muxAddrNode);
					}

					/* Here at least 1 ControlNode out of 2 is not null */
					if (enableNode == null) {
						/* MUX_ADDR */
						nodeForRoot = muxAddrNode;
					} else {
						nodeForRoot = enableNode;
						if (muxAddrNode == null) {
							/* ENABLE */
							/* Fill ENABLE with the only Terminal Node */
							Node theOnlyTerminalNode = new Node.Builder(controlPartManager.termNodeCollector.getTerminalNodes(graphVariable).get(0).getDependentVariable()).build();
							enableNode.setSuccessor(Condition.TRUE, theOnlyTerminalNode);

						} else {
							/* ENABLE + MUX_ADDR */
							/* Fill ENABLE with MUX_ADDR node */
							enableNode.setSuccessor(Condition.TRUE, muxAddrNode);

						}
					}

				} else {
					/* (1) */
					/* Neither ENABLE nor MUX_ADDR is needed. Add the only Terminal Node either to RESET node or as rootNode */
					/* The only Terminal Node can be either a Retaining or a trivial one */
					if (valueRetainingCount == 1) {
						nodeForRoot = new Node.Builder(graphVariable).build();
					} else {
						nodeForRoot = new Node.Builder(controlPartManager.termNodeCollector.getTerminalNodes(graphVariable).get(0).getDependentVariable()).build();
					}
				}
				/* Set the 0-successor for rootNode or rootNode itself if it's missing */
				if (rootNode == null) {
					rootNode = nodeForRoot;
				} else {
					rootNode.setSuccessor(Condition.FALSE, nodeForRoot);
				}


				/* Create new GraphVariable */
				graphVariable.setGraph(new Graph(rootNode));

				modelCollector.addVariable(graphVariable);
			}
		}
	}

	private void createFsmGraph(ControlPartManager controlPartOrdering) throws Exception {
		LOGGER.fine("Start creating FSM graph...");

		Variable fsmBaseVariable = new Variable(FSM_VARIABLE_NAME, null, new Flags().setFSM(true));
		FsmGraphCreator graphCreator = new FsmGraphCreator(controlPartOrdering);

		/* Firstly traverse STATE graph */
		LOGGER.fine("Processing STATE graph...");
		boolean hasStateVar = false;
		for (AbstractVariable absVariable : behModel.getVariables()) {
			if (absVariable instanceof GraphVariable) {
				GraphVariable graphVariable = (GraphVariable) absVariable;
				if (graphVariable.isState()) {
					graphVariable.traverse(graphCreator);
					hasStateVar = true;
					break;
				}
			}
		}
		LOGGER.fine("Processing STATE graph DONE.");
		if (!hasStateVar) {
			LOGGER.info("No state variable found");
			throw new Exception("STATE variable is missing for the Control Part of the design." +
					"\nTo transform Beh-HLDD to RTL-HLDD, exactly 1 state variable is needed." +
					"\n\nYou can specify its name directly in <design_name>.config file" +
					"\nas a key-value pair with key \"" + ConfigurationHandler.STATE_NAME + "\", and re-run both" +
					"\n\"VHDL Beh => HLDD Beh\" and \"HLDD Beh => HLDD RTL\" converters.");
		}
		/* Traverse all graphs */
		int i = 1, total = behModel.getGraphCount() - 1; // -1 ---> STATE graph
		for (AbstractVariable absVariable : behModel.getVariables()) {
			if (absVariable instanceof GraphVariable) {
				GraphVariable graphVariable = (GraphVariable) absVariable;
				if (graphVariable.isState()) continue;
				LOGGER.fine("Processing graph #" + i + " ( " + (total - i) + " in queue ) =========== " + graphVariable.getBaseVariable());
				graphVariable.traverse(graphCreator);
				i++;
			}
		}

		GraphVariable graphVariable = new GraphVariable(fsmBaseVariable, graphCreator.getRootNode());

		modelCollector.addVariable(graphVariable);
		LOGGER.fine("FSM graph created successfully!");
	}

	private void createControlPartOutputs(ResetInspector resetInspector, ControlPartManager controlPartManager) throws Exception {

		/* Firstly, search for STATE variable,
		* in order to make it have the leading position amongst Control Part Variables */
		for (AbstractVariable absVar : behModel.getVariables()) {
			if (absVar.isState()) {
				modelCollector.addVariable(((GraphVariable) absVar).getBaseVariable());
				controlPartManager.addControlPartVariable(absVar);
				break;
			}
		}

		/* Create Control Part Outputs for every GraphVariable  */
		TerminalNodeCollector termNodeCollector = controlPartManager.termNodeCollector;
		for (AbstractVariable absVar : behModel.getVariables()) {
			if (absVar.isState()) continue;
			if (absVar instanceof GraphVariable) {
				GraphVariable graphVariable = (GraphVariable) absVar;
				int terminalNodesCount = termNodeCollector.getTerminalNodesCount(graphVariable);
				boolean isValueRetainingUsed = termNodeCollector.isValueRetainingUsed(graphVariable);

				/* Create ENABLE */
				Variable newVariable;
				if (isValueRetainingUsed) {
					newVariable = new Variable(graphVariable.getName() + ControlPartManager.SUFFIX_ENABLE, Type.BIT_TYPE, new Flags().setCout(true));
					modelCollector.addVariable(newVariable);
					controlPartManager.addControlPartVariable(newVariable);
				}

				/* Create RESET */
				if (resetInspector.isResettableVariable(graphVariable)) {
					newVariable = new Variable(graphVariable.getName() + ControlPartManager.SUFFIX_RESET, Type.BIT_TYPE, new Flags().setCout(true));
					modelCollector.addVariable(newVariable);
					controlPartManager.addControlPartVariable(newVariable);
				}

				/* Create MUX_ADDR */
				if (terminalNodesCount > 1) {
					newVariable = new Variable(graphVariable.getName() + ControlPartManager.SUFFIX_MUX_ADDR,
							new Type(new Range(termNodeCollector.getMuxAddrLength(graphVariable) - 1, 0)), new Flags().setCout(true));
					modelCollector.addVariable(newVariable);
					controlPartManager.addControlPartVariable(newVariable);
				}
			}
		}
	}

	private TerminalNodeCollector createMuxAddrValuesMapping() throws Exception {

		TerminalNodeCollector termNodeCollector = new TerminalNodeCollector();
		for (AbstractVariable absVar : behModel.getVariables()) {
			if (absVar.isState()) continue;
			if (absVar instanceof GraphVariable) {
				GraphVariable graphVariable = (GraphVariable) absVar;
				graphVariable.traverse(termNodeCollector);
			}
		}
		return termNodeCollector;
	}

	private ResetInspector createResetMapping() throws Exception {

		ResetInspector inspector = new ResetInspector();
		for (AbstractVariable absVar : behModel.getVariables()) {
			if (absVar instanceof GraphVariable) {
				GraphVariable graphVariable = (GraphVariable) absVar;
				graphVariable.traverse(inspector);
			}
		}
		return inspector;
	}

	public Model getRtlModel() throws Exception {
		if (rtlModel == null) {
			transform();
		}
		return rtlModel;
	}

	public class ControlPartManager {
		private static final String SUFFIX_ENABLE = "_EN";
		private static final String SUFFIX_RESET = "_RESET";
		private static final String SUFFIX_MUX_ADDR = "_MUX_ADDR";

		/* key - Control Part Output variable, value - index of this variable in FSM Transition */
		private Map<AbstractVariable, Integer> varIndexHash;
		private Integer nextIndex;

		private final TerminalNodeCollector termNodeCollector;

		public ControlPartManager(TerminalNodeCollector termNodeCollector) {
			this.termNodeCollector = termNodeCollector;
			varIndexHash = new HashMap<AbstractVariable, Integer>();
			nextIndex = 0;
		}

		public void addControlPartVariable(AbstractVariable controlPartVariable) {
			varIndexHash.put(controlPartVariable, nextIndex++);
		}

		public int getTerminalNodesCount(GraphVariable graphVariable) {
			return termNodeCollector.getTerminalNodesCount(graphVariable);
		}

		public int getCoutIndex(AbstractVariable controlPartOutputVar) {
			return varIndexHash.get(controlPartOutputVar);
		}

		public int getCoutCount() {
			return varIndexHash.size();
		}

		public Transitions createTransition() {
			return new Transitions(getCoutCount());
		}

		/**
		 * @param transitions		 destination transitions (WHERE to insert TO)
		 * @param controlPartVariable transition variable (AT WHAT PLACE in the destination to insert to)
		 * @param value			   value of the transition (WHAT to insert)
		 * @throws Exception if specified Control Part Variable doesn't exist.
		 *                   <br>{@link base.hldd.structure.nodes.fsm.Transitions#insertTransition(Integer, int) cause2 }
		 */
		public void insertTransition(Transitions transitions, AbstractVariable controlPartVariable, int value) throws Exception {
			if (!varIndexHash.containsKey(controlPartVariable)) {
				throw new Exception("Specified Control Part Variable (" + controlPartVariable + ") does not exist");
			}
			transitions.insertTransition(varIndexHash.get(controlPartVariable), value);
		}

		public void insertTransition(Transitions transitions, Node terminalNode, GraphVariable graphVariable, boolean isResettingTerminal) throws Exception {
			AbstractVariable controlPartVariable;

			if (isResettingTerminal) {
				// Register value must be RESET:
				// 1) DISABLE register (set ENABLE to '0')
				// 2) set MUX_ADDR to 'X' ( this is already done during initialization of FSM transitions with NULLs )
				// 3) set RESET to '1'
				/* 1 */
				controlPartVariable = modelCollector.getVariable(graphVariable.getName() + SUFFIX_ENABLE);
				if (controlPartVariable != null) insertTransition(transitions, controlPartVariable, 0);
				/* 3 */
				controlPartVariable = modelCollector.getVariable(graphVariable.getName() + SUFFIX_RESET);
				insertTransition(transitions, controlPartVariable, 1);

			} else if (terminalNode.getDependentVariable() == graphVariable) {
				// Register value must be RETAINED:
				// 1) DISABLE register (set ENABLE to '0')
				// 2) set MUX_ADDR to 'X' ( this is already done during initialization of FSM transitions with NULLs )
				// 3) set RESET to '0'
				/* 1 */
				controlPartVariable = modelCollector.getVariable(graphVariable.getName() + SUFFIX_ENABLE);
				if (controlPartVariable != null) insertTransition(transitions, controlPartVariable, 0);
				/* 3 */
				controlPartVariable = modelCollector.getVariable(graphVariable.getName() + SUFFIX_RESET);
				if (controlPartVariable != null) insertTransition(transitions, controlPartVariable, 0);

			} else {
				// Corresponding MUX ADDRESS must be activated:
				// 1) ENABLE register (set ENABLE to '1')
				// 2) set MUX_ADDR to corresponding value, taken from hash
				// 3) set RESET to '0'
				/* 1 */
				controlPartVariable = modelCollector.getVariable(graphVariable.getName() + SUFFIX_ENABLE);
				if (controlPartVariable != null) insertTransition(transitions, controlPartVariable, 1);
				/* 2 */
				controlPartVariable = modelCollector.getVariable(graphVariable.getName() + SUFFIX_MUX_ADDR);
				if (controlPartVariable != null) {
					int transitionValue = termNodeCollector.getTransitionValue(graphVariable, terminalNode);
					insertTransition(transitions, controlPartVariable, transitionValue);
				}
				/* 3 */
				controlPartVariable = modelCollector.getVariable(graphVariable.getName() + SUFFIX_RESET);
				if (controlPartVariable != null) insertTransition(transitions, controlPartVariable, 0);

			}
		}

		public void fillMuxAddrNode(GraphVariable graphVariable, Node muxAddrNode) throws Exception {
			termNodeCollector.fillMuxAddrNode(graphVariable, muxAddrNode);
		}
	}
}
