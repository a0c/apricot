package base.hldd.structure.nodes;

import base.HLDDException;
import base.hldd.structure.nodes.utils.Successors;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.PartedVariable;
import base.hldd.structure.nodes.utils.Utility;
import base.hldd.visitors.Visitable;
import base.hldd.visitors.HLDDVisitor;
import base.Indices;

import java.util.*;
/**
 * Class represents a NODE as it is defined in AGM.
 *
 * <p>User: Anton Chepurov
 * <br>Date: 25.02.2007
 * <br>Time: 22:18:01
 */
public class Node implements Visitable, Cloneable {
    /**
     * The VARIABLE that the node depends on
     */
    protected AbstractVariable dependentVariable;
    /**
     * Successors of CONTROL node. For TERMINAL node successors == null.
     */
	private Successors successors;

    /**
     * Parted indices of {@link #dependentVariable}. <p> todo: consider using a ready {@link PartedVariable} as {@link #dependentVariable} and removing this field (partedIndices) at all  
     */
    private Indices partedIndices;
    /**
     * Line numbers in VHDL file this Node was created from
     */
    private Set<Integer> vhdlLines;

    /* ==================================
    *  |  Fields set during indexation  |
    *  ================================== */
    /**
     * Node's ABSOLUTE index. Used only by simulator (here -- only for printing, or at least should be so :) ).
     */
    private int absoluteIndex;
    /**
     * Node's RELATIVE index.
     */
    private int relativeIndex = -1; // '-1' is used during indexation of the Graph (if a node has already been indexed, then it won't be indexed again (and the index won't be incremented))

	/**
     * Constructor for OVERRIDING in inherited classes (FSMNode)
     */
    protected Node() {}

    /**
     * Main (and virually only) constructor
     * @param builder from which to build the Node
     */
    protected Node(Builder builder) {
        dependentVariable = builder.dependentVariable;
		successors = builder.successors;
        partedIndices = builder.partedIndices;
        vhdlLines = builder.vhdlLines;
    }
    

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("  ");
        sb.append(absoluteIndex);
        sb.append("\t");
        sb.append(relativeIndex);
        sb.append(":  ");
        sb.append(nodeTypeToString());
        sb.append(" ");
        sb.append(transitionsToString());
        sb.append("\tV = ");
        sb.append(dependentVariable.getIndex());
        sb.append("\t\"");
        sb.append(depVarName());
        sb.append(indicesToString(partedIndices, true));
        sb.append("\"\t");
        sb.append(partedIndices == null ? dependentVariable.lengthToString() : indicesToString(partedIndices, false));

        return sb.toString();
    }

    private String indicesToString(Indices indices, boolean mergeIndices) {
		return indices == null ? "" : indices.toStringAngular(mergeIndices);
    }

    /**
     * TemporalNode adds a Range window to the name, e.g. "p1@[1..END-4]".<br>
	 * PartedVariable adds indices "(8 DOWNTO 3)", but via its
	 * {@link base.hldd.structure.variables.PartedVariable#getName()} method.
     * @return name of the variable (must be overriden for different formatting)
     */
    protected String depVarName() {
        return dependentVariable.getName();
    }

    protected String transitionsToString() {

        if (isTerminalNode()) return "(\t0\t0)";

		return successors.toString();
    }

    protected String nodeTypeToString() {

        return isTerminalNode() ? "(____)" : "(n___)";

    }

    public boolean isIdenticalTo(Node comparedNode) {

        if (this == comparedNode) return true;

        /* Terminal and Control nodes are not identical */
        if (isTerminalNode() ^ comparedNode.isTerminalNode()) return false;

        /* Here BOTH either Terminal or Control */
        /* Compare DEPENDENT VARIABLES */
        if (!dependentVariable.isIdenticalTo(comparedNode.getDependentVariable())) return false;
        /* Compare SUCCESSORS */
        if (isControlNode()) {
			if (!successors.isIdenticalTo(comparedNode.successors)) return false;
        }

        /* Compare PARTED INDICES */
        if (!Indices.equals(partedIndices, comparedNode.partedIndices)) return false;

        /* All tests passed. */
        return true;
    }

    public static Node clone(Node nodeToClone) {
        return nodeToClone == null ? null : nodeToClone.clone();
    }

    public Node clone() {
        if (isTerminalNode()) {
            return new Builder(dependentVariable).partedIndices(partedIndices).vhdlLines(vhdlLines).build();
        } else {
            Node clonedNode = new Builder(dependentVariable).partedIndices(partedIndices).createSuccessors(successors.getConditionValuesCount()).vhdlLines(vhdlLines).build();
			clonedNode.successors.cloneFrom(successors);
            return clonedNode;
        }
    }

	public void setSuccessor(Condition condition, Node successor) throws HLDDException {
		if (isControlNode()) {
			successors.setSuccessor(condition, successor);
		} else throw new HLDDException("A SUCCESSOR is being added to a TERMINAL node:" +
				"\nSuccessor: " + successor.toString() +
				"\nTerminal: " + this.toString());
	}

    /* Getters START */

    public boolean isTerminalNode() {
        return successors == null;
    }

    public boolean isControlNode() {
        return !isTerminalNode();
    }

    public int getAbsoluteIndex() {
        return absoluteIndex;
    }

    public int getRelativeIndex() {
        return relativeIndex;
    }

    public AbstractVariable getDependentVariable() {
        return dependentVariable;
    }

	public Node getSuccessorInternal(Condition condition) throws HLDDException {
		return successors.getSuccessorInternal(condition);
	}

	public int getConditionValuesCount() {
		return successors.getConditionValuesCount();
	}

	public int getConditionsCount() {
		return successors.getConditionsCount();
	}

	public Condition getCondition(int idx) throws HLDDException {
		return successors.getCondition(idx);
	}

	public Node getSuccessor(Condition condition) {
		return successors.getSuccessor(condition);
	}

	public Collection<Node> getSuccessors() {
        return successors.asCollection();
    }

    public Indices getPartedIndices() {
        return partedIndices;
    }

    public Set<Integer> getVhdlLines() {
        return vhdlLines;
    }

    /* Getters END */

    /* Setters START */

    public void setAbsoluteIndex(int absoluteIndex) {
        this.absoluteIndex = absoluteIndex;
    }

    public void setRelativeIndex(int relativeIndex) {
        this.relativeIndex = relativeIndex;
    }

    public void setDependentVariable(AbstractVariable dependentVariable) {
        this.dependentVariable = dependentVariable;
    }

    public void setVhdlLines(Set<Integer> vhdlLines) {
        this.vhdlLines = vhdlLines;
    }

    public void addVhdlLines(Set<Integer> newVhdlLines) {
		/* Explicit lines are added.
		* Implicit lines  */
		if (vhdlLines == null) {
			return; // todo: current case for Beh2RTLTransformer
		}
		vhdlLines.addAll(newVhdlLines);
	}

    /* Setters END */


    /**
     *
     * @return  <code>true</code> if NONE of the node's successors IS filled.
     *          Otherwise <code>false</code> is returned.
     * @throws HLDDException if a TERMINAL node is being checked for emptiness
     */
    public boolean isEmptyControlNode() throws HLDDException {
        if (isTerminalNode()) {
            throw new HLDDException("TERMINAL node is being checked for emptiness: " + toString());
        }
		return successors.isEmpty();
    }

	public Condition getOthers() throws HLDDException {
		return successors.getOthers();
	}

	public void compact() throws HLDDException {
		if (isTerminalNode()) {
			return;
		}
		successors.compact();
	}

	/**
	 * Splits complex conditions like {0-2,8,19} into separate 'decompacted' conditions {0},{1},{2},{8},{19}.
	 * @param condition whose holder (complex condition) is to be decompacted
	 * @throws HLDDException if array-condition is specified as a parameter, or if called on a terminal node,
	 * 		or if this node doesn't contain the specified condition
	 */
	public void decompact(Condition condition) throws HLDDException {
		if (isTerminalNode()) {
			throw new HLDDException("Node: decompact(): trying to decompact a terminal node");
		}
		successors.decompact(condition);
	}

	public void indexate(int startingIndex) {
        Utility.indexate(this, startingIndex);
    }

    public void minimize(int rootNodeAbsIndex) {
        Utility.minimize(this, rootNodeAbsIndex);
    }

    public Node reduce(int rootNodeAbsIndex) {
        return Utility.reduce(this, rootNodeAbsIndex);
    }

    /**
     * @param fillingNode node to fill missing successors with
     */
    public void fillEmptySuccessorsWith(Node fillingNode) {
		/* Check the node to be a CONTROL node*/
		if (isTerminalNode()) return;
		/* Don't fill emptyNodes */
		try {
			if (isEmptyControlNode()) return;
		} catch (HLDDException e) {
			throw new RuntimeException(e); // Should not occur
		}

		successors.fillEmptyWith(fillingNode, getVhdlLines());
    }

    /**
     * Counts the size of the graph with <code>this</code> node being the rootNode of the graph.
     * <br> Tree of this node must be indexed.
     * <p> 
     * Pay attention to the difference between {@link #getSize()} and {@link #getUnindexedSize()}!
     *
     * @return size of the tree with this node as a root of the tree
     */
    public int getSize() {
        return Utility.getSize(this);
    }

    public int getUnindexedSize() {
        return Utility.getUnindexedSize(this);
    }

    public String[] toStringArray(String[] stringArray) {
        if (stringArray == null) {
            stringArray = new String[getSize()];
        }
        /* Only fill the array, if the destination is empty.
        * Otherwise infinite loop will occur in case of cyclic HLDDs (THLDDs) */
        if (stringArray[relativeIndex] == null) {
            stringArray[relativeIndex] = toString();
            if (isControlNode()) {
                for (Node successorNode : getSuccessors()) {
                    successorNode.toStringArray(stringArray);
                }
            }
        }
        return stringArray;
    }

    public Node[] toArray(Node[] nodeArray) {
        if (nodeArray == null) {
            nodeArray = new Node[getSize()];
        }
        if (nodeArray[relativeIndex] == null) {
            nodeArray[relativeIndex] = this;
            if (isControlNode()) {
                for (Node successorNode : getSuccessors()) {
                    successorNode.toArray(nodeArray);
                }
            }
        }
        return nodeArray;
    }

    public void traverse(HLDDVisitor visitor) throws Exception {
        visitor.visitNode(this);
    }

	public boolean hasIdenticalConditionsWith(Node second) {
		return successors.hasIdenticalConditionsWith(second.successors);
	}

	public static class Builder {
        // Required parameters
        private final AbstractVariable dependentVariable;
        // Optional parameters -- initialized to default values
        private Successors successors = null;
        private Indices partedIndices = null;
        private Set<Integer> vhdlLines = new TreeSet<Integer>();

		public Builder(AbstractVariable dependentVariable) {
            this.dependentVariable = dependentVariable;
        }

        public Node build() {
            return new Node(this);
        }

        public Builder createSuccessors(int conditionValuesCount) {
			successors = new Successors(conditionValuesCount);
			return this;
		}

        public Builder partedIndices(Indices partedIndices) {
            this.partedIndices = partedIndices;
            return this;
        }

        public Builder vhdlLines(Set<Integer> vhdlLines) {
            this.vhdlLines = vhdlLines;
            return this;
        }
    }

}
