package base.hldd.structure.models;

import base.hldd.structure.variables.*;
import base.hldd.structure.variables.utils.DefaultGraphVariableCreator;
import base.hldd.structure.variables.utils.GraphVariableCreator;
import base.hldd.structure.Graph;
import base.hldd.visitors.VHDLLinesCollector;
import io.ExtendedBufferedReader;
import io.QuietCloser;
import io.scan.HLDDScanner;

import java.io.*;
import java.util.HashMap;
import java.util.TreeMap;

import parsers.hldd.HLDDStructureBuilder;
import parsers.hldd.HLDDStructureParser;

/**
 * Class representing AGM BEH RTL DD.
 * 
 * User: Anton Chepurov
 * Date: 05.11.2007
 * Time: 17:57:20
 */
public class BehModel {

    protected int nodeCount;
    protected int varCount;
    protected int graphCount;
    protected int inpCount;
    protected int outpCount;
    protected int constCount;
    protected int funcCount;
    
    protected ExtendedBufferedReader reader;

    protected TreeMap<String, AbstractVariable> vars;
    protected TreeMap<String, ConstantVariable> consts;
    protected TreeMap<Integer, String> varNameByIndex;

    private String mode;


    /**
     *
     * @param hlddSource {@link io.scan.HLDDScanner#HLDDScanner(Object)}
     * @param graphVariableCreator creator for {@link GraphVariable}-s.
     *        By default {@link base.hldd.structure.variables.utils.DefaultGraphVariableCreator}
     *        is used.
     * @return parsed BehModel
     * @throws Exception {@link io.scan.HLDDScanner#HLDDScanner(Object)}.
     */
    public static BehModel parseHlddStructure(Object hlddSource, GraphVariableCreator graphVariableCreator) throws Exception {
        /* Parse HLDD structure */
        HLDDScanner scanner = new HLDDScanner(hlddSource);
        HLDDStructureBuilder structureBuilder = new HLDDStructureBuilder(graphVariableCreator);
        HLDDStructureParser parser = new HLDDStructureParser(scanner, structureBuilder);
        parser.parse();
        return structureBuilder.getModel();
    }

    /**
     *
     * @param hlddSource {@link io.scan.HLDDScanner#HLDDScanner(Object)}
     * @return parsed BehModel
     * @throws Exception .{@link io.scan.HLDDScanner#HLDDScanner (Object)}
     */
    public static BehModel parseHlddStructure(Object hlddSource) throws Exception {
        return parseHlddStructure(hlddSource, new DefaultGraphVariableCreator());
    }

    /**
     * Constructor for an object to be filled with variables later on
     */
    public BehModel() {
        vars = new TreeMap<String, AbstractVariable>();
        varNameByIndex = new TreeMap<Integer, String>();
        consts = new TreeMap<String, ConstantVariable>();
    }

    /**
     * Constructor based on a map of variables
     * @param variableByIndex map of variables, where <b>key</b> is an <u>index</u> of the variable and
     * <b>value</b> is the <u>variable itself</u>
     */
    public BehModel(HashMap<Integer, AbstractVariable> variableByIndex) {
        // Init all class fields
        this();
        // Fill vars with Variables
        for (Integer index : variableByIndex.keySet()) {
            setVariableByIndex(index, variableByIndex.get(index));
        }
    }
    
    public AbstractVariable getVariableByIndex(int index) {
        return vars.containsKey(varNameByIndex.get(index)) ? vars.get(varNameByIndex.get(index)) : consts.get(varNameByIndex.get(index));
    }

    public void setVariableByIndex(int index, AbstractVariable absVariable) {

        String varName = absVariable.getName();
        if (!(absVariable instanceof ConstantVariable)) {
            vars.put(varName, absVariable);
        } else {
            consts.put(varName, (ConstantVariable) absVariable);
        }
        varNameByIndex.put(index, varName);

        addStat(absVariable);

    }

    protected void addStat(AbstractVariable variable) {
        /* Calculate STAT (statistical data) */
        varCount++;
        if (variable.isInput()) inpCount++;
        else if (variable instanceof ConstantVariable) constCount++;
        else if (variable instanceof FunctionVariable) funcCount++;
        else if (variable instanceof GraphVariable) {
            graphCount++;
            GraphVariable graphVariable = (GraphVariable) variable;

            if (graphVariable.getGraph() != null)  nodeCount += graphVariable.getGraph().getSize();
            if (variable.isOutput()) outpCount++;
        }
    }

    public void toFile(OutputStream outputStream, String comment) throws IOException {

        String fileAsString = composeFileString(comment);

        writeStringToFile(outputStream, fileAsString);

		QuietCloser.closeQuietly(outputStream);
    }

    private void writeStringToFile(OutputStream outputStream, String fileAsString) throws IOException {
		BufferedWriter outBufWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
		outBufWriter.write(fileAsString);
		outBufWriter.flush();
    }

    protected String composeFileString(String comment) {

        StringBuffer str = new StringBuffer();

        // add COMMENT if exists
        if (comment != null && !comment.equals("")) {
            String[] lines = comment.split("\n");
            for (String line : lines) {
                str.append(";").append(line).append("\n");
            }
        }

        str.append("\nSTAT#\t").append(nodeCount).append(" Nods,  ");
        str.append(varCount).append(" Vars,  ");
        str.append(graphCount).append(" Grps,  ");
        str.append(inpCount).append(" Inps,  ");
        str.append(outpCount).append(" Outs,  ");
        str.append(constCount).append(" Cons,  ");
        str.append(funcCount).append(" Funs  ");

        str.append("\n\nMODE#\t");
        str.append(mode == null ? "RTL" : mode);
        str.append("\n\n");

        for (int i = 0; i < varCount; i++) {

            if (i == inpOffset())
                str.append(";inputs\n");
            if (i == constOffset())
                str.append("\n\n;constants\n");
            if (i == funcOffset())
                str.append("\n\n;functions\n");
            if (i == graphOffset())
                str.append("\n\n;graphs\n");

            str.append(vars.containsKey(varNameByIndex.get(i)) ? vars.get(varNameByIndex.get(i)).toString() : consts.get(varNameByIndex.get(i))).append("\n");

        }

        return str.toString();
    }

    /**
     * Removes redundant (recurring) nodes.
     */
    public void minimize() {
        /* Minimize rootNodes for every GraphVariable */
        int rootNodeAbsIndex = 0;
        for (int index = graphOffset(); index < varCount; index++) {
            AbstractVariable absVar = getVariableByIndex(index);
            if (absVar instanceof GraphVariable) {
                GraphVariable graphVariable = (GraphVariable) absVar;
				if (!graphVariable.isExpansion()) {
					graphVariable.getGraph().getRootNode().minimize(rootNodeAbsIndex);
				}
				rootNodeAbsIndex += graphVariable.getGraph().getSize();
            }
        }
        /* Refresh internal state variable */
        nodeCount = rootNodeAbsIndex;
    }

    public void reduce() {
        /* Reduce rootNodes for every GraphVariable */
        int rootNodeAbsIndex = 0;
        for (int index = graphOffset(); index < varCount; index++) {
            AbstractVariable absVar = getVariableByIndex(index);
            if (absVar instanceof GraphVariable) {
                Graph graph = ((GraphVariable) absVar).getGraph();
				if (!absVar.isExpansion()) {
					graph.setRootNode(graph.getRootNode().reduce(rootNodeAbsIndex));
				}
				rootNodeAbsIndex += graph.getSize();
            }
        }
        /* Refresh internal state variable */
        nodeCount = rootNodeAbsIndex;
    }

    public void printMapFile(OutputStream outputStream) throws Exception {
        VHDLLinesCollector vhdlLinesCollector = new VHDLLinesCollector();
        for (int index = graphOffset(); index < varCount; index++) {
            AbstractVariable absVar = getVariableByIndex(index);
            if (absVar instanceof GraphVariable) {
                ((GraphVariable) absVar).traverse(vhdlLinesCollector);
            }
        }

		writeStringToFile(outputStream, vhdlLinesCollector.getVhdlLinesAsString());

		QuietCloser.closeQuietly(outputStream);
    }
    
    public AbstractVariable getVarByName(String name) {

        return vars.containsKey(name) ? vars.get(name) : consts.containsKey(name) ? consts.get(name) : null;

    }

    public int inpOffset() { return 0; }

    public int constOffset() { return inpOffset() + inpCount; }

    public int funcOffset() { return constOffset() + constCount; }

    public int graphOffset() { return funcOffset() + funcCount; }

    /* Getters START */

    public ExtendedBufferedReader getReader() {
        return reader;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getVarCount() {
        return varCount;
    }

    public int getGraphCount() {
        return graphCount;
    }

    public int getInpCount() {
        return inpCount;
    }

    public int getOutpCount() {
        return outpCount;
    }

    public int getConstCount() {
        return constCount;
    }

    public int getFuncCount() {
        return funcCount;
    }

    public TreeMap<String, AbstractVariable> getVars() {
        return vars;
    }

    public TreeMap<String, ConstantVariable> getConsts() {
        return consts;
    }

    /* Getters END */

    /* Setters START*/

    public void setMode(String mode) {
        this.mode = mode;
    }

    /* Setters END*/

}
