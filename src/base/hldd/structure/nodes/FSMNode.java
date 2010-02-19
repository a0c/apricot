package base.hldd.structure.nodes;

import base.hldd.structure.nodes.fsm.Transitions;

import java.util.ArrayList;

/**
 * <p>User: Anton Chepurov
 * <br>Date: 26.02.2007
 * <br>Time: 21:59:28
 */
public class FSMNode extends Node {
    
    // Array of Transitions, that consist of STATE transitions and CONTROL PART OUTPUTS transitions
//    private ArrayList<String[]> transitions;
    private Transitions transitions;


//    public FSMNode(ArrayList<String[]> transitions) {
//        this.transitions = new ArrayList<String[]>(transitions);
//    }

    public FSMNode(Transitions transitions) {
        this.transitions = transitions;
    }


    public String toString() {
        StringBuffer strBuf = new StringBuffer();

        strBuf.append("  ").
                append(getAbsoluteIndex()).
                append("\t").
                append(getRelativeIndex()).
                append(":  (__v_) (\t0\t0)\tVEC = ").
                append(transitions);

        return strBuf.toString();
    }


    public FSMNode clone() {
        return new FSMNode(transitions.clone());
    }

    public boolean isTerminalNode() {
        return true;
    }

    public boolean isIdenticalTo(Node comparedNode) {
        if (!(comparedNode instanceof FSMNode)) return false;

        /* Compare Transitions */
        return transitions.isIdenticalTo(((FSMNode) comparedNode).getTransitions());
    }

/*
    public boolean isIdenticalTo(FSMNode fsmNode_compared) {

        // Compare transitions
        return areTransitionsIdentical(this.transitions, fsmNode_compared.getTransitions());

    }
*/

    private static boolean areTransitionsIdentical(ArrayList<String[]> transitions1, ArrayList<String[]> transitions2) {

        if (transitions1.size() != transitions2.size()) return false;

        for (int index = 0; index < transitions1.size(); index++) {
            if (!transitions1.get(index)[1].equals(transitions2.get(index)[1])) return false;
        }
        /* All transitions are equal. FSM nodes are identical */
        return true;
    }

    /* Getters START */

    public Transitions getTransitions() {
        return transitions;
    }

    /*
    public ArrayList<String[]> getTransitions() {
        return transitions;
    }
*/

    /* Getters END */
}
