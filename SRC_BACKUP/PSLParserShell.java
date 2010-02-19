package parsers.psl;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.models.utils.*;
import base.hldd.structure.models.utils.ModelManager;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.TemporalNode;
import base.psl.Property;
import base.psl.structure.PPGLibrary;
import base.psl.expression.BooleanExpression;
import base.psl.structure.PPG;
import base.psl.structure.Range;
import base.hldd.structure.variables.*;
import io.PPGLibraryReader;
import io.PSLBufferedReader;
import io.scan.PSLScanner;
import parsers.base.Condition;
import parsers.base.Operand;
import parsers.vhdl.VHDLStructureParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.math.BigInteger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 19.11.2007
 * <br>Time: 20:48:07
 */
public class PSLParserShell {

    /* PPG Library file and PSL file */
    private final File ppgLibraryFile;
    private final File pslFile;
    private final File hlddModelFile;
    /* PPG Library */
    PPGLibrary library;

    private PSLBufferedReader reader;
    /* Collector for PROPERTIES */
    private ArrayList<Property> properties;
    private base.psl.structure.Property[] newProperties;
    private base.hldd.structure.models.utils.ModelManager hlddModelManager;


    /* Final model */
    private BehModel model;
    private String comment;

    public PSLParserShell(File ppgLibraryFile, File pslFile, File hlddModelFile) throws FileNotFoundException {
        this.ppgLibraryFile = ppgLibraryFile;
        this.pslFile = pslFile;
        this.hlddModelFile = hlddModelFile;
        this.reader = new PSLBufferedReader(pslFile);
        this.properties = new ArrayList<Property>();
    }

    public void run() throws Exception {

        /* Read PPG Library */
        readPPGLibrary();

        /* Read (parse) PSL file */
        readPSLFile();

        /* Read base HLDD model */
        readBaseHLDDModel();

        /* Construct Graphs for properties */
        constructPropertyGraphs();

    }

    private void readPPGLibrary() throws Exception {

        library = new PPGLibraryReader(ppgLibraryFile).getPpgLibrary();

    }

    private void readPSLFile() throws Exception {
        PSLStructureBuilder pslStructureBuilder = new PSLStructureBuilder(library);
        new PSLStructureParser(new PSLScanner(pslFile), pslStructureBuilder).parse();
        newProperties = pslStructureBuilder.getProperties();

//        String word;
//
//        /* READ properties */
//        while (true) {
//
//            word = reader.readWordMatchingRegex(Regex.LITERAL_ENDS_WITH_COLON);
//            if (word == null) break;
//
//            if (reader.getLastReadChar() == ':') { // todo: remove this check
//
//                properties.add(new Property(reader, library));
//
//            }
//
//
//        }

    }

    private void readBaseHLDDModel() throws Exception {

        /* Read base HLDD model */
        BehModel hlddModel = BehModel.parseHlddStructure(hlddModelFile);

        /* Fill HLDD Model Manager with HLDD model variables */
        hlddModelManager = new ModelManager(false);
        for (AbstractVariable variable : hlddModel.getVars().values()) {
            if (variable instanceof Variable || variable instanceof GraphVariable) {
                hlddModelManager.addVariable(variable);
            }
        }

    }

    private void constructPropertyGraphs() throws Exception {
        PSLModelCreatorImpl modelCreator = new PSLModelCreatorImpl(newProperties, hlddModelManager);
        model = modelCreator.getModel();
        comment = modelCreator.getComment();
        return;

//        /* CONVERT properties */
//        ModelCollector modelCollector = new ModelCollector();
//
//        for (Property property : properties) {
//
//            modelCollector.initNewProperty();
//
//            /* Recursively convert properties */
//            PPG startingPPG = property.getStartingPPG();
//            Node[] nodes = startingPPG.getPslOperator().getPropertyGraph().getGraph().getNodes();
//            HashMap<Integer, Integer> relToAbsIndices = new HashMap<Integer, Integer>();
//            PostponedHashings postponedHashings = new PostponedHashings();
//            /* Process nodes in reverse order,
//            *  so that when Control Nodes are reached all Terminal Nodes are already hashed
//            *  (so are all control node successors as well ) */
//            for (int i = nodes.length - 1; i >= 0 ; i--) {
//                Node node = nodes[i];
//
//                if (node.isTerminalNodeForPSL()) {
//
//                    /* Hash property OUTPUT PORTS (TERMINAL NODES):
//                     * 1) in MODEL COLLECTOR (final model)
//                     * 2) in relative_to_absolute_indices hash */
//                    Node clonedNode = node.clone();
//                    clonedNode.setAbsoluteIndex(0);
//                    clonedNode.setRelativeIndex(-1);
//                    clonedNode.setDependentVariable(modelCollector.addVariable(clonedNode.getDependentVariable()));
////                    modelCollector.addVariable(clonedNode.getDependentVariable());
//                    int absIndex = modelCollector.hashNode(clonedNode);
//                    relToAbsIndices.put(i, absIndex);
//
//                } else {
//
//                    PPG operandPPG = startingPPG.getOperand(node.getDependentVariableName());
//                    int absIndex = substituteControlNode(node, operandPPG, relToAbsIndices, modelCollector, startingPPG.getWindow(), startingPPG.getPslOperator().getWindowPlaceholders(), postponedHashings);
//                    relToAbsIndices.put(i, absIndex);
//
//                }
//            }
//            /* If cycle exists (some successors are null), map(set) the missing successors */
//            postponedHashings.fillMissingSuccessors(relToAbsIndices);
//
//            modelCollector.constructPropertyGraph(property.getName(), relToAbsIndices.get(0));
//
//        }
//
//        modelCollector.createModel();
//
//        return modelCollector.getModel();
    }

    private int substituteControlNode(Node nodeToSubstitute, PPG ppgToSubstituteWith, HashMap<Integer, Integer> relToAbsIndices,
                                      ModelCollector modelCollector, Range parentOperandWindow, String[] operatorWindowPlaceholders,
                                      PostponedHashings postponedHashings) throws Exception {

        /* Adjust GLOBAL WINDOW in modelCollector */
        modelCollector.adjustWindow(parentOperandWindow, operatorWindowPlaceholders);

        if (ppgToSubstituteWith.isBoolean()) {
            /* 1) Create new CONTROL NODE;
            *  2) Hash it to modelCollector;
            *  3) Return its absolute index. */

            BooleanExpression boolExpr = ppgToSubstituteWith.getBooleanExpression();

            /* Create NEW HashSuccessors */
            TreeMap<Integer, Integer> newHashSuccessors = new TreeMap<Integer, Integer>();
            TreeMap<Integer, Integer> hashSuccessors = nodeToSubstitute.getHashSuccessors();
            Set<Integer> conditions = hashSuccessors.keySet();
            for (Integer condition : conditions) {
                /* If nodeToSubstitute is TOP (3 port node), skip condition 2, since Boolean Expression has no CHECKING state */
                if (conditions.size() == 3 && condition == 2) continue;

                /* Get Absolute Successor index */
                Integer successor = hashSuccessors.get(condition);
                Integer absSuccessor = relToAbsIndices.get(successor);
                /* Calculate NEW CONDITION to be hashed */
                int newCondition = boolExpr.isCondition() && boolExpr.getCondition().isInverted() ? 1 - condition : condition;
                /* If successor hasn't been added yet (cycle), then postpone hashing new condition_successor pair */
                if (absSuccessor == null) {
                    postponedHashings.postpone(newHashSuccessors, newCondition, successor);
                }

                /* Hash new condition_successor pair */
                newHashSuccessors.put(newCondition, absSuccessor);
            }

            /* Add new Variables to modelCollector */
            AbstractVariable dependentVariable = modelCollector.addVariables(boolExpr);
            /* 1) Create new CONTROL NODE */
            Node newControlNode = new TemporalNode(dependentVariable, newHashSuccessors, parentOperandWindow);
            /* 2) Hash it to modelCollector */
            /* 3) Return its absolute index */
            return modelCollector.hashNode(newControlNode);

        } else if (ppgToSubstituteWith.isSERE()) {
            return -1;
        } else {
            /*
            * 1) Create new Control Nodes and map their ports
            * */
            HashMap<Integer, Integer> relToAbsIndicesLocal = new HashMap<Integer, Integer>();
            PostponedHashings postponedHashingsLocal = new PostponedHashings();

            Node[] nodes = ppgToSubstituteWith.getPslOperator().getPropertyGraph().getGraph().getNodes();
            for (int i = nodes.length - 1; i >= 0 ; i--) {
                Node node = nodes[i];
                if (!node.isTerminalNodeForPSL()) {

                    PPG operandPPG = ppgToSubstituteWith.getOperand(node.getDependentVariableName());
                    int absindex = substituteControlNode(node, operandPPG, relToAbsIndicesLocal, modelCollector, ppgToSubstituteWith.getWindow(), ppgToSubstituteWith.getPslOperator().getWindowPlaceholders(), postponedHashingsLocal);
                    relToAbsIndicesLocal.put(i, absindex);

                } else {
                    /* Map PPG ports */
                    /* Restore hashing for TERMINAL NODES in local relToAbs hash */
                    int absIndex = PortMapper.getAbsPortIndex(node, nodeToSubstitute, relToAbsIndices, ppgToSubstituteWith.getPslOperator().getPropertyGraph().countTerminalNodes());
                    if (absIndex != -1) relToAbsIndicesLocal.put(i, absIndex);

                }

            }
            /* If cycle exists (some successors are null), map(set) the missing successors */
            postponedHashingsLocal.fillMissingSuccessors(relToAbsIndicesLocal);

            /* If parent had a window, then add this window to the rootNode of the inner PPG */
            if (parentOperandWindow != null) { //todo: Chaing merging of windows when "next next"
                /* Add the window via creation of a TemporalNode on the basis of the rootNode */
                Node rootNode = modelCollector.hashNodes.get(relToAbsIndicesLocal.get(0));
                modelCollector.hashNodes.put(relToAbsIndicesLocal.get(0), new TemporalNode(rootNode.getDependentVariable(), rootNode.getHashSuccessors(), parentOperandWindow));
            }
            return relToAbsIndicesLocal.get(0);

        }

    }

    public BehModel getModel() {
        return model;
    }

    public String getComment() {
        return comment;
    }

    private enum PSLTerminalNodeConstant {
        INACTIVE,
        FAIL,
        PASS
    }
    

    private class ModelCollector {

        private BehModel model;
//        ArrayList<Node> nodes;
        /* Map of VARIABLES */
        private TreeMap<String, AbstractVariable> vars;
        /* Map of CONSTANTS */
        private TreeMap<String, ConstantVariable> consts;
        /* Map of NODES */
        private HashMap<Integer, Node> hashNodes;
        private int nodeCount;

        // VARS indexation variable
        private int indexVars;
        // NODES indexation variable
        private int indexNodes;
        // GRAPHS indexation variable
        private int indexGraphs;

        private HashMap<Integer, AbstractVariable> hashVars;

        // Global property window (temporal)
        private Range globalWindow;
        private boolean isLastEND;

        ModelCollector() {

            vars = new TreeMap<String, AbstractVariable>();
            consts = new TreeMap<String, ConstantVariable>();
            hashNodes = new HashMap<Integer, Node>();
            hashVars = new HashMap<Integer, AbstractVariable>();
//            this.model = new BehModel();
//            this.nodes = new ArrayList<Node>();

        }

        public void initNewProperty() {
            globalWindow = null;
            isLastEND = false;
        }

        public int hashNode(Node node) {

            //todo: hashVars(node);  ----- already done using addVariable()
            hashNodes.put(nodeCount++, node);
            return nodeCount - 1;

        }

        public AbstractVariable addVariables(BooleanExpression boolExpr) throws Exception {

            if (boolExpr.isCondition()) {
                //todo: ... before adding operands to vars, check their length in VHDL file
                Condition condition = boolExpr.getCondition();
                /* Add variables used in condition to hashes (vars, consts) */
                // Check for constants
                if (VHDLStructureParser.isConstant(condition.getLeftOperand().getVariable())) {
                    ConstantVariable.getConstByValue(new BigInteger(condition.getLeftOperand().getVariable()), -1, consts, true);
                } else {

                    Operand leftOperand = condition.getLeftOperand();
                    String name = leftOperand.getVariable();
                    int highestSB = -1;
                    if (leftOperand.isParted()) highestSB = leftOperand.getIndices()[1];

                    addVariable(new Variable(name, highestSB, "I"));
                }

                if (VHDLStructureParser.isConstant(condition.getRightOperand().getVariable())){
                    ConstantVariable.getConstByValue(new BigInteger(condition.getRightOperand().getVariable()), -1, consts, true);
                } else {
                    Operand rightOperand = condition.getRightOperand();
                    String name = rightOperand.getVariable();
                    int highestSB = -1;
                    if (rightOperand.isParted()) highestSB = rightOperand.getIndices()[1];

                    addVariable(new Variable(name, highestSB, "I"));
                }

                FunctionVariable additionalFunction = FunctionVariable.createAdditionalFunction(condition.toString(), condition, vars, consts);
                return additionalFunction == null ? vars.get(condition.getLeftOperand().getVariable()) : additionalFunction;

            } else {
                Operand operand = boolExpr.getOperand();
                String name = operand.getVariable();
                int highestSB = -1;
                if (operand.isParted()) highestSB = operand.getIndices()[1];

                return addVariable(new Variable(name, highestSB, "I"));
            }

        }

        public void constructPropertyGraph(String propertyName, Integer startingNodeIndex) throws Exception {

            GraphVariable graphVariable = new GraphVariable(new Variable(propertyName + globalWindowToString(), 1, "O"), startingNodeIndex);
            indexNodes += GraphVariable.indexNodes(graphVariable, indexNodes, hashNodes, vars, indexGraphs++);
            vars.put(graphVariable.getName(), graphVariable);

        }

        public void adjustWindow(Range parentOperandWindow, String[] operatorWindowPlaceholders) throws Exception {
            /* Skip (do nothing for) operators without windows */
            if (operatorWindowPlaceholders == null) return;

            if (globalWindow == null) {
                Range realWindow = Range.replacePlaceholders(parentOperandWindow, operatorWindowPlaceholders);
                if (realWindow.getEnd() == -1) {
                    isLastEND = true;
                    realWindow.setEnd(0);
                }
                globalWindow = realWindow;
                globalWindow.setEnd(globalWindow.getEnd() - 1); /* decrease by one. May be changed. */
            } else {
                globalWindow.adjustWindow(Range.replacePlaceholders(parentOperandWindow, operatorWindowPlaceholders));
            }
        }

        private String globalWindowToString() {
            StringBuffer retBuffer = new StringBuffer(globalWindow.toString());

            if (isLastEND) retBuffer.insert(retBuffer.toString().indexOf("..") + 2, "END");

            return retBuffer.toString();
        }

        public AbstractVariable addVariable(AbstractVariable dependentVariable) {

            if (dependentVariable instanceof ConstantVariable) {
                if (consts.containsKey(dependentVariable.getName())) {
                    return consts.get(dependentVariable.getName());
                } else {
                    /* Set variable UNINDEXED */
                    ((Variable) dependentVariable).forceSetIndex(-1);
                    consts.put(dependentVariable.getName(), (ConstantVariable) dependentVariable);
                    return dependentVariable;
                }
            } else {
                if (vars.containsKey(dependentVariable.getName())) {
                    return vars.get(dependentVariable.getName());
                } else {
                    /* Set variable UNINDEXED */
                    ((Variable) dependentVariable).forceSetIndex(-1);
                    vars.put(dependentVariable.getName(), dependentVariable);
                    return dependentVariable;
                }
            }

        }

        public void createModel() {

            /* INDEX everything */
            indexInputs();

            indexConstants();

            indexFunctions();

            indexPropertyGraphs();

            /* HASH everything */
            for (AbstractVariable variable : vars.values()) {
                hashVars.put(variable.getIndex(), variable);
            }
            for (ConstantVariable variable : consts.values()) {
                hashVars.put(variable.getIndex(), variable);
            }

            model = new BehModel(hashVars, "BEHAVIORAL");

        }

        private void indexInputs() {
            for (AbstractVariable variable : vars.values()) {
                if (variable.isInput()) {
                    variable.setIndex(indexVars++);
                }
            }
        }

        private void indexConstants() {
            for (ConstantVariable variable : consts.values()) {
                variable.setIndex(indexVars++);
            }
        }


        private void indexFunctions() {
            for (AbstractVariable variable : vars.values()) {
                if (variable instanceof FunctionVariable) {
                    variable.setIndex(indexVars++);
                }
            }
        }

        private void indexPropertyGraphs() {
            for (AbstractVariable variable : vars.values()) {
                if (variable instanceof GraphVariable) {
                    variable.setIndex(indexVars++);
                }
            }
        }

        /* GETTERS */

        public BehModel getModel() {
            return model;
        }

    }

    private static class PortMapper {


        /**
         *
         * @param terminalPortNode
         * @param nodeToSubstitute
         * @param relToAbsIndices
         * @param operandPortCount
         * @return      absolute port index in hashNodes or -1 if node to substitute hasn't got such a value
         * @throws Exception
         */
        public static int getAbsPortIndex(Node terminalPortNode, Node nodeToSubstitute, HashMap<Integer, Integer> relToAbsIndices, int operandPortCount) throws Exception {

            /* Check terminalPortNode to be a terminal node */
            if (!terminalPortNode.isTerminalNodeForPSL()) throw new Exception("Absolute port index is being searched for a CONTROL node. Control node cannot be a PPG port." +
                    "\nTerminal Port Node:\n" + terminalPortNode.toString() +
                    "Node to substitute:\n" + nodeToSubstitute.toString());

            /* Terminal node must be a CONSTANT */
            if (!(terminalPortNode.getDependentVariable() instanceof ConstantVariable))
                throw new Exception("Terminal node in PPG is not based on a constant:\nTerminal Port Node:\n" + terminalPortNode.toString());

            /* Get PORT VALUE */
            int portValue = ((ConstantVariable) terminalPortNode.getDependentVariable()).getValue().intValue();


            /* Set mapping shift for ports */
//            int mapShift = 0;
//            int substitutedPortCount = nodeToSubstitute.getHashSuccessors().size();
//            if (operandPortCount > substitutedPortCount) {
//                if (operandPortCount == (substitutedPortCount + 1)) {
//                    mapShift = -1;
//                } else throw new Exception("Port mapping error:\nNode to substitute hasn't got the value that is represented by terminal node of the operand PPG:" +
//                    "\nMax value of the node to substitute: " + nodeToSubstitute.getHashSuccessors().lastKey() +
//                    "\nTerminal node value: " + portValue);
//            }

            /* Get RELATIVE INDEX for PORT SUCCESSOR */
            /* Node to substitute must be a CONTROL NODE */
            if (nodeToSubstitute.isTerminalNodeForPSL())
                throw new Exception("Condition successors are being extracted from terminal node:\nNode to substitute:\n" + nodeToSubstitute.toString());
            /* If node to substitute hasn't got this very condition, return -1 */
            Integer portSuccessorRelIndex = nodeToSubstitute.getHashSuccessors().get(portValue /*+ mapShift*/);

            if (portSuccessorRelIndex == null) return -1; /*throw new Exception("Port mapping error:\nMax value of the node to substitute: " + nodeToSubstitute.getHashSuccessors().lastKey() +
                    "\nTerminal node value: " + portValue +
                    "\nShifted value (to be found in node to substitute): " + (portValue + mapShift));*/

            /* Return ABSOLUTE INDEX for port successor */
            return relToAbsIndices.get(portSuccessorRelIndex);
        }
    }

    private class PostponedHashings {

        private List<PostponedHashing> postponedHashings;

        private PostponedHashings() {
            postponedHashings = new LinkedList<PostponedHashing>();
        }

        /**
         * @param relToAbsIndicesLocal where to take the missing successor index from
         */
        public void fillMissingSuccessors(HashMap<Integer, Integer> relToAbsIndicesLocal) {
            for (PostponedHashing postponedHashing : postponedHashings) {
                postponedHashing.unfilledHashSuccessors.put(postponedHashing.unfilledCondition, relToAbsIndicesLocal.get(postponedHashing.missingSuccessor));
            }
        }

        /**
         *
         * @param unfilledHashSuccessors where to hash
         * @param unfilledCondition at what place to hash
         * @param missingSuccessor what to hash
         */
        public void postpone(TreeMap<Integer, Integer> unfilledHashSuccessors, int unfilledCondition, Integer missingSuccessor) {
            postponedHashings.add(new PostponedHashing(unfilledHashSuccessors, unfilledCondition, missingSuccessor));
        }

        private class PostponedHashing {
            private final TreeMap<Integer, Integer> unfilledHashSuccessors;
            private final int unfilledCondition;
            private final Integer missingSuccessor;

            public PostponedHashing(TreeMap<Integer, Integer> unfilledHashSuccessors, int unfilledCondition, Integer missingSuccessor) {
                this.unfilledHashSuccessors = unfilledHashSuccessors;
                this.unfilledCondition = unfilledCondition;
                this.missingSuccessor = missingSuccessor;
            }
        }
    }
}
