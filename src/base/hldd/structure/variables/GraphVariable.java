package base.hldd.structure.variables;

import base.Range;
import base.hldd.structure.Graph;
import base.hldd.structure.nodes.Node;
import base.hldd.visitors.Visitable;
import base.hldd.visitors.HLDDVisitor;
import base.Type;

/**
 * @author Anton Chepurov
 */
public class GraphVariable extends AbstractVariable implements Visitable {

	/* Collection of nodes ( = graph) */
	private Graph graph;

	private AbstractVariable baseVariable;


	/**
	 * Valid constructor.
	 *
	 * @param baseVariable base variable of the graph
	 * @param rootNode	 root node of the graph
	 */
	public GraphVariable(AbstractVariable baseVariable, Node rootNode) {
		this.baseVariable = baseVariable;
		graph = new Graph(rootNode);
	}


	public String toString() {
		return baseVariable + "\n" + graph;
	}

	public boolean isReset() {
		return baseVariable.isReset();
	}

	public boolean isFSM() {
		return baseVariable.isFSM();
	}

	public boolean isCout() {
		return baseVariable.isCout();
	}

	public boolean isSigned() {
		return baseVariable.isSigned();
	}

	public void setDelay(boolean isDelay) {
		((Variable) baseVariable).setDelay(isDelay);
	}

	public boolean isIdenticalTo(AbstractVariable comparedAbsVariable) {
		/* Compare links */
		if (this == comparedAbsVariable) return true;
		/* Compare classes */
		if (!(comparedAbsVariable instanceof GraphVariable)) return false;

		GraphVariable comprdGraphVariable = (GraphVariable) comparedAbsVariable;
		/* Compare BASE variables */
		if (!baseVariable.isIdenticalTo(comprdGraphVariable.getBaseVariable())) return false;
		/* todo: Compare GRAPHS */

		return true;
	}

	public Graph getGraph() {
		return graph;
	}

	public AbstractVariable getBaseVariable() {
		return baseVariable;
	}

	public int getIndex() {
		return baseVariable.getIndex();
	}

	@Override
	public void setDefaultValue(ConstantVariable defaultValue) {
		baseVariable.setDefaultValue(defaultValue);
	}

	@Override
	public ConstantVariable getDefaultValue() {
		return baseVariable.getDefaultValue();
	}

	public boolean isOutput() {
		return baseVariable.isOutput();
	}

	public boolean isState() {
		return baseVariable.isState();
	}

	public boolean isDelay() {
		return baseVariable.isDelay();
	}

	@Override
	public boolean isExpansion() {
		return baseVariable.isExpansion();
	}

	public String lengthToString() {
		return baseVariable.lengthToString();
	}

	public Type getType() {
		return baseVariable.getType();
	}

	public boolean isInput() {
		return baseVariable.isInput();
	}

	public boolean isMemory() {
		return baseVariable.isMemory();
	}

	public Range getLength() {
		return baseVariable.getLength();
	}

	public String getName() {
		return baseVariable.getName();
	}

	public String getPureName() {
		return baseVariable.getPureName();
	}

	public void forceSetIndex(int index) {
		baseVariable.forceSetIndex(index);
	}

	public void setIndex(int index) {
		baseVariable.setIndex(index);
	}

	public void addNamePrefix(String namePrefix) {
		baseVariable.addNamePrefix(namePrefix);
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public void setBaseVariable(AbstractVariable baseVariable) {
		this.baseVariable = baseVariable;
	}

	public void traverse(HLDDVisitor visitor) throws Exception {
		visitor.visitGraphVariable(this);
	}
}
