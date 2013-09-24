package base.hldd.structure.models;

import base.hldd.structure.Graph;
import base.hldd.structure.variables.*;
import base.hldd.structure.variables.utils.DefaultGraphVariableCreator;
import base.hldd.structure.variables.utils.GraphVariableCreator;
import base.hldd.visitors.SourceLocationCollector;
import io.QuietCloser;
import io.scan.HLDDScanner;
import parsers.hldd.StructureBuilder;
import parsers.hldd.StructureParser;
import ui.ConverterSettings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Class representing AGM BEH RTL DD.
 *
 * @author Anton Chepurov
 */
public class BehModel {

	protected int nodeCount;
	protected int varCount;
	protected int graphCount;
	protected int inpCount;
	protected int outpCount;
	protected int constCount;
	protected int funcCount;

	private TreeMap<Integer, AbstractVariable> variableByIndex = new TreeMap<Integer, AbstractVariable>();
	private LinkedList<AbstractVariable> variables = new LinkedList<AbstractVariable>();
	private LinkedList<ConstantVariable> constants = new LinkedList<ConstantVariable>();

	private String mode;


	/**
	 * @param hlddSource		   {@link io.scan.HLDDScanner#HLDDScanner(Object)}
	 * @param graphVariableCreator creator for {@link GraphVariable}-s.
	 *                             By default {@link base.hldd.structure.variables.utils.DefaultGraphVariableCreator}
	 *                             is used.
	 * @return parsed BehModel
	 * @throws Exception {@link io.scan.HLDDScanner#HLDDScanner(Object)}.
	 */
	public static BehModel parseHlddStructure(Object hlddSource, GraphVariableCreator graphVariableCreator) throws Exception {
		/* Parse HLDD structure */
		HLDDScanner scanner = new HLDDScanner(hlddSource);
		StructureBuilder structureBuilder = new StructureBuilder(graphVariableCreator);
		StructureParser parser = new StructureParser(scanner, structureBuilder);
		parser.parse();
		return structureBuilder.getModel();
	}

	/**
	 * @param hlddSource {@link io.scan.HLDDScanner#HLDDScanner(Object)}
	 * @return parsed BehModel
	 * @throws Exception .{@link io.scan.HLDDScanner#HLDDScanner (Object)}
	 */
	public static BehModel parseHlddStructure(Object hlddSource) throws Exception {
		return parseHlddStructure(hlddSource, new DefaultGraphVariableCreator());
	}

	/**
	 * Constructor based on a collection of variables
	 *
	 * @param variables collection of variables containing both constants and variables
	 */
	public BehModel(Collection<AbstractVariable> variables) {
		// fill convenience collections with variables
		storeVariables(variables);
	}

	public AbstractVariable getVariableByIndex(int index) {
		return variableByIndex.get(index);
	}

	private void storeVariables(Collection<AbstractVariable> variablesCollection) {

		LinkedList<AbstractVariable> variablesList = new LinkedList<AbstractVariable>(variablesCollection);

		Collections.sort(variablesList);

		for (AbstractVariable absVariable : variablesList) {
			/* map by index */
			variableByIndex.put(absVariable.getIndex(), absVariable);

			/* keep variables and constants separately for speed-up of requests */
			if (absVariable instanceof ConstantVariable) {
				constants.add((ConstantVariable) absVariable);
			} else {
				variables.add(absVariable);
			}

			addStat(absVariable);
		}
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

			if (graphVariable.getGraph() != null) nodeCount += graphVariable.getGraph().getSize();
			if (variable.isOutput()) outpCount++;
		}
	}

	public void toFile(OutputStream outputStream, String comment, ConverterSettings settings) throws IOException {

		String fileAsString = composeFileString(comment, settings);

		writeStringToFile(outputStream, fileAsString);

		QuietCloser.closeQuietly(outputStream);
	}

	private void writeStringToFile(OutputStream outputStream, String fileAsString) throws IOException {
		BufferedWriter outBufWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
		outBufWriter.write(fileAsString);
		outBufWriter.flush();
	}

	protected String composeFileString(String comment, ConverterSettings settings) {

		StringBuilder sb = new StringBuilder();

		// add SETTINGS, if any
		if (settings != null) {
			settings.writeSmartComment(sb);
		}
		// add COMMENT if exists
		String newLine = System.getProperty("line.separator");
		if (comment != null && !comment.isEmpty()) {
			sb.append(";").append("#########################").append(newLine);
			sb.append(";").append(newLine);
			String[] lines = comment.split("\n");
			for (String line : lines) {
				sb.append(";").append("\t").append(line).append(newLine);
			}
		}

		sb.append("\nSTAT#\t").append(nodeCount).append(" Nods,  ");
		sb.append(varCount).append(" Vars,  ");
		sb.append(graphCount).append(" Grps,  ");
		sb.append(inpCount).append(" Inps,  ");
		sb.append(outpCount).append(" Outs,  ");
		sb.append(constCount).append(" Cons,  ");
		sb.append(funcCount).append(" Funs  ");

		sb.append("\n\nMODE#\t");
		sb.append(mode == null ? "RTL" : mode);
		sb.append("\n\n");

		for (int i = 0; i < varCount; i++) {

			if (i == inpOffset())
				sb.append(";inputs\n");
			if (i == constOffset())
				sb.append("\n\n;constants\n");
			if (i == funcOffset())
				sb.append("\n\n;functions\n");
			if (i == graphOffset())
				sb.append("\n\n;graphs\n");

			sb.append(getVariableByIndex(i)).append("\n");

		}

		return sb.toString();
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
		if (outputStream == null) {
			return;
		}
		SourceLocationCollector sourceCollector = new SourceLocationCollector();
		for (int index = funcOffset(), n = funcOffset() + funcCount; index < n; index++) {
			AbstractVariable absVar = getVariableByIndex(index);
			if (absVar instanceof FunctionVariable) {
				sourceCollector.visitFunctionVariable((FunctionVariable) absVar);
			}
		}
		for (int index = graphOffset(); index < varCount; index++) {
			AbstractVariable absVar = getVariableByIndex(index);
			if (absVar instanceof GraphVariable) {
				((GraphVariable) absVar).traverse(sourceCollector);
			}
		}

		writeStringToFile(outputStream, sourceCollector.getSourceAsString());

		QuietCloser.closeQuietly(outputStream);
	}

	public int inpOffset() {
		return 0;
	}

	public int constOffset() {
		return inpOffset() + inpCount;
	}

	public int funcOffset() {
		return constOffset() + constCount;
	}

	public int graphOffset() {
		return funcOffset() + funcCount;
	}

	/* Getters START */

	public int getNodeCount() {
		return nodeCount;
	}

	public int getVarCount() {
		return varCount;
	}

	public int getGraphCount() {
		return graphCount;
	}

	public Collection<AbstractVariable> getVariables() {
		return variables;
	}

	public Collection<ConstantVariable> getConstants() {
		return constants;
	}

	public Collection<Variable> getInputPorts() {
		ArrayList<Variable> inPorts = new ArrayList<Variable>(inpCount);
		for (int i = 0; i < inpCount; i++) {
			inPorts.add((Variable) getVariableByIndex(i));
		}
		return inPorts;
	}

	public Collection<GraphVariable> getOutputPorts() {
		ArrayList<GraphVariable> outPorts = new ArrayList<GraphVariable>(outpCount);
		for (int i = graphOffset(); i < varCount; i++) {
			GraphVariable graphVariable = (GraphVariable) getVariableByIndex(i);
			if (graphVariable.isOutput()) {
				outPorts.add(graphVariable);
			}
		}
		return outPorts;
	}

	/* Getters END */

	/* Setters START*/

	public void setMode(String mode) {
		this.mode = mode;
	}

	/* Setters END*/

}
