package base.hldd.structure.nodes;

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
    protected Node[] successors;

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
        if (indices == null) {
            if (dependentVariable instanceof PartedVariable) {
                return ((PartedVariable) dependentVariable).getPartedIndices().toStringAngular(mergeIndices);
            } else
                return "";
        } else {
            return indices.toStringAngular(mergeIndices);
        }
    }

    /**
     * TemporalNode adds a Range window to the name, e.g. "p1@[1..END-4]" 
     * @return name of the variable (must be overriden for different formatting)
     */
    protected String depVarName() {
        return dependentVariable.getName();
    }

    protected String transitionsToString() {

        if (isTerminalNode()) return "(\t0\t0)";
        // if there are only 2 successors - separate with TABS; if more than 2 - separate with SPACES:
        String delim = successors.length == 2 ? "\t" : " ";
        StringBuffer strBuf;
        if (false) {
            strBuf = new StringBuffer("(");
            for (int index = 0; index < successors.length; index++) {
                Node successor = successors[index];
                if (successor != null) {
                    strBuf.append(index).append("=>").append(successor.getRelativeIndex()).append(delim);
                }
            }
        } else {
            /* Transitions merging */
            int curStart = -1, curEnd = -1, curRelIdx = -1;
            strBuf = new StringBuffer("(");
            for (int index = 0; index < successors.length; index++) {
                Node successor = successors[index];

                if (successor != null) {
                    if (curRelIdx == -1) {
                        curStart = curEnd = index;
                        curRelIdx = successor.getRelativeIndex();
                        continue;
                    }
                    if (successor.getRelativeIndex() == curRelIdx) {
                        /* Cotinue collecting identical nodes into one transition */
                        curEnd = index;
                    } else {
                        /* Print previously collected identical transitions */
                        strBuf.append(curStart);
                        if (curStart != curEnd) strBuf.append("-").append(curEnd);
                        strBuf.append("=>").append(curRelIdx).append(delim);
                        /* Start collecting a new set of identical transitions */
                        curStart = curEnd = index;
                        curRelIdx = successor.getRelativeIndex();
                    }
                } else {
                    /* Print previously collected identical transitions */
                    if (curRelIdx != -1) {
                        strBuf.append(curStart);
                        if (curStart != curEnd) strBuf.append("-").append(curEnd);
                        strBuf.append("=>").append(curRelIdx).append(delim);
                    }
                    curStart = curEnd = curRelIdx = -1;
                }
            }
            /* Print previously collected identical transitions */
            if (curRelIdx != -1) {
                strBuf.append(curStart);
                if (curStart != curEnd) strBuf.append("-").append(curEnd);
                strBuf.append("=>").append(curRelIdx).append(delim);
            }
        }
        return strBuf.append(")").toString();
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
            /* Compare NUMBER OF SUCCESSORS */
            Node[] comparedSuccessors = comparedNode.getSuccessors();
            if (successors.length != comparedSuccessors.length) return false;
            /* Compare SUCCESSORS */
            for (int index = 0; index < successors.length; index++) {
                if (!successors[index].isIdenticalTo(comparedSuccessors[index])) return false;
            }
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
            Node clonedNode = new Builder(dependentVariable).partedIndices(partedIndices).successorsCount(successors.length).vhdlLines(vhdlLines).build();
            for (int i = 0; i < successors.length; i++) {
                try {
                    clonedNode.setSuccessor(i, clone(successors[i]));
                } catch (Exception e) {
                    throw new RuntimeException("Unexpected bug while cloning the following node:\n" +
                            toString() + "\nCannot set successor.");
                }
            }
            return clonedNode;
        }
    }

    /**
     *
     * @param controlCondition condition that points to the added successor
     * @param successor successor node to be added
     * @throws  Exception if the successor is being added to a TERMINAL node
     *          or successor INDEX exceeds the number of successors in this ControlNode
     */
    public void setSuccessor(int controlCondition, Node successor) throws Exception {
        if (isControlNode()) {
            if (controlCondition > successors.length - 1) {
                throw new Exception("While adding a successor to ControlNode, control CONDITION exceeded the number of successors in ControlNode:" +
                        "\nControl condition: " + controlCondition +
                        "\nNumber of successors: " + successors.length);
            }
            successors[controlCondition] = successor;

        } else throw new Exception("A SUCCESSOR is being added to a TERMINAL node:" +
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

    public Node[] getSuccessors() {
        return successors;
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
     * @throws Exception if a TERMINAL node is being checked for emptiness
     */
    public boolean isEmptyControlNode() throws Exception {
        if (isTerminalNode()) {
            throw new Exception("TERMINAL node is being checked for emptiness: " + toString());
        }
        for (Node successor : successors) {
            if (successor != null) return false;
        }
        return true;
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
     * @throws Exception {@link base.hldd.structure.nodes.utils.Utility#fillEmptySuccessorsWith(Node, Node)}.
     */
    public void fillEmptySuccessorsWith(Node fillingNode) throws Exception {
        Utility.fillEmptySuccessorsWith(this, fillingNode);
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
                for (Node successorNode : successors) {
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
                for (Node successorNode : successors) {
                    successorNode.toArray(nodeArray);
                }
            }
        }
        return nodeArray;
    }

    public void traverse(HLDDVisitor visitor) throws Exception {
        visitor.visitNode(this);
    }

    public static class Builder {
        // Required parameters
        private final AbstractVariable dependentVariable;
        // Optional parameters -- initialized to default values
        private Node[] successors = null;
        private Indices partedIndices = null;
        private Set<Integer> vhdlLines = new TreeSet<Integer>();

        public Builder(AbstractVariable dependentVariable) {
            this.dependentVariable = dependentVariable;
        }

        public Node build() {
            return new Node(this);
        }

        public Builder successorsCount(int successorsCount) {
            if (successorsCount != 0)
                successors = new Node[successorsCount];
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
