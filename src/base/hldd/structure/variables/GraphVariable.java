package base.hldd.structure.variables;

import base.hldd.structure.Graph;
import base.hldd.structure.nodes.Node;
import base.hldd.visitors.Visitable;
import base.hldd.visitors.HLDDVisitor;
import base.Indices;
import base.Type;

/**
 * <p>User: Anton Chepurov
 * <br>Date: 26.02.2007
 * <br>Time: 15:09:48
 */
public class GraphVariable extends AbstractVariable implements Visitable /*extends Variable */{

    /* Collection of nodes ( = graph) */
    private Graph graph;

    private AbstractVariable baseVariable;


    /**
     * Valid constructor.
     * @param baseVariable base variable of the graph
     * @param rootNode root node of the graph
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

    public void setDelay(boolean isDelay){
        ((Variable) baseVariable).setDelay(isDelay);
    }

/*
    //todo: FSM!!!
    public String lengthToString() {

        // If it is a FSM Control GraphVariable:
        if (highestAndLowestIndexes == null) return "";

        return super.lengthToString();
    }
*/

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

    /* Getters START */


    public Graph getGraph() {
        return graph;
    }

    public AbstractVariable getBaseVariable() {
        return baseVariable;
    }

    public int getIndex() {
        return baseVariable.getIndex();
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

    public Indices getLength() {
        return baseVariable.getLength();
    }

    //todo: delegate EVERYTHING!!!

    /* Getters END */

    /* Delegated Methods */

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
    /* Setters START */

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

	public void setBaseVariable(AbstractVariable baseVariable) {
		this.baseVariable = baseVariable;
	}

    /* Setters END */

    public void traverse(HLDDVisitor visitor) throws Exception {
        visitor.visitGraphVariable(this);
    }
}
