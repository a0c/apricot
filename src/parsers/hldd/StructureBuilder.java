package parsers.hldd;

import base.Range;
import base.hldd.structure.models.BehModel;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.*;
import base.hldd.structure.variables.utils.GraphVariableCreator;
import base.hldd.structure.Flags;
import base.vhdl.structure.Operator;
import base.Type;

import java.math.BigInteger;
import java.util.TreeMap;

/**
 * @author Anton Chepurov
 */
public class StructureBuilder {
	private int nodeCount, varCount, graphCount, inpCount, outpCount, constCount, funcCount;
	private BehModel model;
	private Collector collector;
	private final GraphVariableCreator graphVariableCreator;

	public StructureBuilder(GraphVariableCreator graphVariableCreator) {
		this.graphVariableCreator = graphVariableCreator;
		collector = new Collector();
	}


	public BehModel getModel() throws Exception {
		/* Check statistics */
		doCheckStat();
		/* Create REAL MODEL out of Collector data */
		createModel();

		return model;
	}

	private void createModel() throws Exception {
		boolean incomplete = true;

		/* Create Functions */
		while (incomplete) {
			incomplete = false;
			FULL_SCAN_OF_VARIABLES:
			for (int index = collector.getFuncOffset(); index < collector.getGraphOffset(); index++) {
				Object o = collector.getVarObject(index);
				if (o instanceof Collector.FunctionData) {
					Collector.FunctionData functionData = (Collector.FunctionData) o;

					/* Create Function */
					FunctionVariable functionVariable;
					try {
						/* Try to create normal function. If failed - create User Defined Function */
						functionVariable = new FunctionVariable(Operator.valueOf(
								functionData.functionType),
								functionData.nameIdx);
						if (functionVariable.isMemory()) {
							functionVariable.setName(functionData.name);
						}
					} catch (IllegalArgumentException e) {
						/* User defined function */
						functionVariable = new UserDefinedFunctionVariable(
								functionData.functionType,
								functionData.nameIdx,
								functionData.inputIndices.length,
								functionData.length);
					}

					/* Add operands to Function */
					for (int inputIndex = 0; inputIndex < functionData.inputIndices.length; inputIndex++) {
						Object inputVarObject = collector.getVarObject(functionData.inputIndices[inputIndex]);
						AbstractVariable inputVariable = null;
						if (inputVarObject instanceof AbstractVariable) {
							inputVariable = (AbstractVariable) inputVarObject;
						} else if (inputVarObject instanceof Collector.GraphVariableData) {
							inputVariable = ((Collector.GraphVariableData) inputVarObject).graphVariable;
						} else if (inputVarObject instanceof Collector.FunctionData) {
							/* If some input of the function is another Function, then skip this run
							* of Function creation (wait for the input function to be created first) */
							incomplete = true;
							continue FULL_SCAN_OF_VARIABLES; //todo: Place for possible optimization: if dependency is analyzed, then right ordering will allow not to skip unknown inputs... However, optimization overhead may be larger than current skipping.
						}

						functionVariable.addOperand(inputVariable, functionData.inputRanges[inputIndex]);
					}

					/* Set index to Function Variable and add variable to collector */
					functionVariable.setIndex(functionData.index); //todo: use forceSetIndex() if doesn't work
					collector.addFunctionVariable(functionVariable);
				}
			}
		}

		/* Create GraphVariables */
		graphVariableCreator.createGraphVariables(collector);

		model = new BehModel(collector.getVariablesCollection());
	}

	private void doCheckStat() throws Exception {
		if (nodeCount != 0 || varCount != 0 || graphCount != 0 || inpCount != 0
				|| outpCount != 0 || constCount != 0 || funcCount != 0) {
			throw new Exception("Some elements haven't been read." +
					"\nNumber of unread elements:" +
					"\nNodes:   " + nodeCount +
					"\nVars:    " + varCount +
					"\nGraphs:  " + graphCount +
					"\nInputs:  " + inpCount +
					"\nOutputs: " + outpCount +
					"\nConsts:  " + constCount +
					"\nFuncs:   " + funcCount
			);
		}
	}

	public void buildStat(String[] statArray) throws Exception {
		if (statArray.length % 2 == 1) {
			throw new Exception("Array of statistics is malformed. It contains an odd number of elements.");
		}

		for (int index = 0; index < statArray.length; index += 2) {
			String statString = statArray[index];
			if (statString.equals("NODS")) {
				nodeCount = Integer.parseInt(statArray[index + 1]);
			} else if (statString.equals("VARS")) {
				varCount = Integer.parseInt(statArray[index + 1]);
			} else if (statString.equals("GRPS")) {
				graphCount = Integer.parseInt(statArray[index + 1]);
			} else if (statString.equals("INPS")) {
				inpCount = Integer.parseInt(statArray[index + 1]);
			} else if (statString.equals("OUTS")) {
				outpCount = Integer.parseInt(statArray[index + 1]);
			} else if (statString.equals("CONS")) {
				constCount = Integer.parseInt(statArray[index + 1]);
			} else if (statString.equals("FUNS")) {
				funcCount = Integer.parseInt(statArray[index + 1]);
			}
		}

		collector.setFuncOffset(inpCount + constCount);
		collector.setGraphOffset(collector.getFuncOffset() + funcCount);
	}

	public void buildConstant(int index, String name, Range length, BigInteger constValue) throws Exception {
		varCount--;
		constCount--;
		/* Create new Constant */
		ConstantVariable newConstantVariable = new ConstantVariable(name, constValue);
		newConstantVariable.setLength(length);
		newConstantVariable.forceSetIndex(index);
		/* Collect Constant */
		collector.addVariable(newConstantVariable);
	}

	public void buildFunction(int index, String name, int nameIdx, String functionType, int[] inputIndices, Range[] inputRanges, Range length) {
		varCount--;
		funcCount--;
		/* Collect FunctionData */
		collector.addFunctionData(functionType, name, nameIdx, index, inputIndices, inputRanges, length);
	}

	public void buildGraph(int index, Flags flags, String name, Range range, Range length, int graphLength, int graphIndex) {
		varCount--;
		graphCount--;
		/* Create base variable for GraphVariable */
		Variable baseVariable = range == null
				? new Variable(name, new Type(length), flags)
				: new RangeVariable(name, new Type(length), range, flags);
		baseVariable.forceSetIndex(index);
		if (baseVariable.isOutput()) outpCount--;
		GraphVariable newGraphVariable = new GraphVariable(baseVariable, null);
		/* Collect GraphVariable */
		collector.addGraphVariableData(newGraphVariable, graphLength, graphIndex);
	}

	public void buildNode(int relativeNodeIndex, int depVarIndex, Range depVarRange,
						  TreeMap<Condition, Integer> successors, String[] windowPlaceholders) {
		nodeCount--;
		/* Collect NodeData */
		collector.addNodeData(relativeNodeIndex, depVarIndex, depVarRange, successors, windowPlaceholders);
	}

	public void buildVariable(int index, Flags flags, String name, Range length) throws Exception {
		varCount--;
		/* Create new Variable */
		Variable newVariable = new Variable(name, new Type(length), flags);
		newVariable.forceSetIndex(index);
		if (newVariable.isInput()) inpCount--;
		/* Collect Variable */
		collector.addVariable(newVariable);
	}
}
