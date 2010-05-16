package parsers.hldd;

import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.GraphVariable;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.FunctionVariable;
import base.Indices;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.09.2008
 * <br>Time: 21:50:31
 */
public class Collector {
    private int funcOffset, graphOffset;
    private HashMap<Integer, Object> indexVarHash;
    private GraphVariable currentGraphVariable;
    private int currentGraphLength;
    private int currentGraphIndex;
    /**
     * Array of nodes in the order of ascending relative index
     */
    private NodeData[] currentNodes;

    public Collector() {
        indexVarHash = new HashMap<Integer, Object>();
        funcOffset = -1;
        graphOffset = -1;
    }

    HashMap<Integer, AbstractVariable> getIndexVariableHash() {
        HashMap<Integer, AbstractVariable> returnHash = new HashMap<Integer, AbstractVariable>();
        for (int index : indexVarHash.keySet()) {
            returnHash.put(index, (AbstractVariable) indexVarHash.get(index));
        }
        return returnHash;
    }

    public Object getVarObject(int index) {
        return indexVarHash.get(index);
    }

    /**
     * @return number of variables in collector
     */
    public int getSize() {
        return indexVarHash.size();
    }

    void addVariable(AbstractVariable newVariable) {
        indexVarHash.put(newVariable.getIndex(), newVariable);
    }

    void addFunctionData(String functionType, String name, int index, int[] inputIndices, Indices[] inputPartedIndices, Indices length) {
        indexVarHash.put(index, new FunctionData(functionType, name, index, inputIndices, inputPartedIndices, length));
    }

    void addGraphVariableData(GraphVariable newGraphVariable, int graphLength, int graphIndex) {
        currentGraphVariable = newGraphVariable;
        currentGraphLength = graphLength;
        currentGraphIndex = graphIndex;
        currentNodes = new NodeData[graphLength];
    }

    void addNodeData(int relativeNodeIndex, int depVarIndex, Indices depVarPartedIndices, TreeMap<Condition,Integer> successors, String[] windowPlaceholders) {
        currentGraphLength--;
        currentNodes[relativeNodeIndex] = new NodeData(depVarIndex, depVarPartedIndices, successors, windowPlaceholders);
        if (currentGraphLength == 0) {
            indexVarHash.put(currentGraphVariable.getIndex(), new GraphVariableData(currentGraphVariable, currentGraphIndex, currentNodes));
        }
    }

    void addFunctionVariable(FunctionVariable newFunctionVariable) {
        indexVarHash.put(newFunctionVariable.getIndex(), newFunctionVariable);
    }

    public void addGraphVariable(GraphVariable graphVariable) {
        indexVarHash.put(graphVariable.getIndex(), graphVariable);
    }

    /* ####  S E T T E R S  #### */
    public void setFuncOffset(int funcOffset) {
        this.funcOffset = funcOffset;
    }

    public void setGraphOffset(int graphOffset) {
        this.graphOffset = graphOffset;
    }
    
    /* ####  G E T T E R S  #### */
    public int getFuncOffset() throws Exception {
        if (funcOffset == -1) throw new Exception("funcOffset is accessed without being first set");
        return funcOffset;
    }

    public int getGraphOffset() throws Exception {
        if (graphOffset == -1) throw new Exception("graphOffset is accessed without being first set");
        return graphOffset;
    }
    /* ####  H E L P E R   C L A S S E S  #### */
    
    public class FunctionData {
        final String functionType;
        final String name;
        final int index;
        final int[] inputIndices;
        Indices[] inputPartedIndices;
        final Indices length;

        public FunctionData(String functionType, String name, int index, int[] inputIndices, Indices[] inputPartedIndices, Indices length) {
            this.functionType = functionType;
            this.name = name;
            this.index = index;
            this.inputIndices = inputIndices;
            this.inputPartedIndices = inputPartedIndices;
            this.length = length;
        }
    }

    public class GraphVariableData {
        public final GraphVariable graphVariable;
        public final int graphIndex;
        public final NodeData[] nodes;

        public GraphVariableData(GraphVariable graphVariable, int graphIndex, NodeData[] nodes) {
            this.graphVariable = graphVariable;
            this.graphIndex = graphIndex;
            this.nodes = nodes;
        }
    }

    public class NodeData {
        public final int depVarIndex;
        public Indices depVarPartedIndices;
        public final TreeMap<Condition, Integer> successors;
        public final String[] windowPlaceholders;

        public NodeData(int depVarIndex, Indices depVarPartedIndices, TreeMap<Condition, Integer> successors, String[] windowPlaceholders) {
            this.depVarIndex = depVarIndex;
            this.depVarPartedIndices = depVarPartedIndices;
            this.successors = successors;
            this.windowPlaceholders = windowPlaceholders;
        }
    }
}