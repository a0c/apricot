package base.hldd.structure.nodes.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.Variable;
import base.hldd.structure.Flags;
import base.Indices;
import base.Type;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 30.09.2008
 * <br>Time: 20:00:20
 */
public class MaxRelativeIndexCounterTest {

    private static final Indices nullArray = null;

//    0	0:	(n___) (0=>1 1=>2)	V = 0 "BOP"	<1:0>
//    1	1:	(n___) (0=>3 1=>4)	V = 1 "TOP"	<1:0>
//    2	2:	(____) (	0	0)	V = 2 "FAIL"	<1:0>
//    3	3:	(____) (	0	0)	V = 3 "PASS"	<1:0>
//    4	4:	(____) (	0	0)	V = 4 "CHECKING"	<1:0>
    private static Node uniqueNode;
//    0	0:	(n___) (0=>1 1=>2)	V = 0 "BOP"	<1:0>
//    1	1:	(n___) (0=>2 1=>3)	V = 1 "TOP"	<1:0>
//    2	2:	(____) (	0	0)	V = 2 "FAIL"	<1:0>
//    3	3:	(____) (	0	0)	V = 3 "PASS"	<1:0>
    private static Node duplicateNode;
//    0	0:	(n___) (0=>1 1=>2)	V = 0 "BOP"	<1:0>
//    1	1:	(n___) (0=>0 1=>2)	V = 1 "TOP"	<1:0>
//    2	2:	(____) (	0	0)	V = 2 "FAIL"	<1:0>
    private static Node cyclicNode;

    static {
        try {
            /* UNIQUE */
            uniqueNode = new Node.Builder(createVariable("U1", 1, new Flags())).createSuccessors(2).build();

            Node nodeU2 = new Node.Builder(createVariable("U2", 1, new Flags())).createSuccessors(2).build();
            Node nodeU3 = new Node.Builder(createVariable("U3", 1, new Flags())).partedIndices(nullArray).build();
            uniqueNode.setSuccessor(Condition.FALSE, nodeU2);
            uniqueNode.setSuccessor(Condition.TRUE, nodeU3);

            Node nodeU4 = new Node.Builder(createVariable("U4", 1, new Flags())).partedIndices(nullArray).build();
            Node nodeU5 = new Node.Builder(createVariable("U5", 1, new Flags())).partedIndices(nullArray).build();
            nodeU2.setSuccessor(Condition.FALSE, nodeU4);
            nodeU2.setSuccessor(Condition.TRUE, nodeU5);

            uniqueNode.setRelativeIndex(0);
            nodeU2.setRelativeIndex(1);
            nodeU3.setRelativeIndex(2);
            nodeU4.setRelativeIndex(3);
            nodeU5.setRelativeIndex(4);
            uniqueNode.setAbsoluteIndex(0);
            nodeU2.setAbsoluteIndex(1);
            nodeU3.setAbsoluteIndex(2);
            nodeU4.setAbsoluteIndex(3);
            nodeU5.setAbsoluteIndex(4);

            /* DUPLICATE */
            duplicateNode = new Node.Builder(createVariable("D1", 2, new Flags())).createSuccessors(2).build();

            Node nodeD2 = new Node.Builder(createVariable("D2", 2, new Flags())).createSuccessors(2).build();
            Node nodeD3 = new Node.Builder(createVariable("D3", 2, new Flags())).partedIndices(nullArray).build();
            duplicateNode.setSuccessor(Condition.FALSE, nodeD2);
            duplicateNode.setSuccessor(Condition.TRUE, nodeD3);

            Node nodeD4 = new Node.Builder(createVariable("D4", 2, new Flags())).partedIndices(nullArray).build();
            nodeD2.setSuccessor(Condition.FALSE, nodeD3);
            nodeD2.setSuccessor(Condition.TRUE, nodeD4);

            duplicateNode.setRelativeIndex(0);
            nodeD2.setRelativeIndex(1);
            nodeD3.setRelativeIndex(2);
            nodeD4.setRelativeIndex(3);
            duplicateNode.setAbsoluteIndex(0);
            nodeD2.setAbsoluteIndex(1);
            nodeD3.setAbsoluteIndex(2);
            nodeD4.setAbsoluteIndex(3);

            /* CYCLIC + DUPLICATE*/
            cyclicNode = new Node.Builder(createVariable("C1", 3, new Flags())).createSuccessors(2).build();
            Node nodeC2 = new Node.Builder(createVariable("C2", 2, new Flags())).createSuccessors(2).build();
            Node nodeC3 = new Node.Builder(createVariable("C3", 2, new Flags())).partedIndices(nullArray).build();
            cyclicNode.setSuccessor(Condition.FALSE, nodeC2);
            cyclicNode.setSuccessor(Condition.TRUE, nodeC3);

            nodeC2.setSuccessor(Condition.FALSE, cyclicNode);
            nodeC2.setSuccessor(Condition.TRUE, nodeC3);

            cyclicNode.setRelativeIndex(0);
            nodeC2.setRelativeIndex(1);
            nodeC3.setRelativeIndex(2);
            cyclicNode.setAbsoluteIndex(0);
            nodeC2.setAbsoluteIndex(1);
            nodeC3.setAbsoluteIndex(2);


        } catch (Exception e) {

        }
    }

    private static Variable createVariable(String varName, int highestSB, Flags flags) {
        return new Variable(varName, new Type(new Indices(highestSB, 0)), flags);
    }


    @Test
    public void someTest() {
        assertEquals(4, new MaxRelativeIndexCounter(uniqueNode).count());
        assertEquals(3, new MaxRelativeIndexCounter(duplicateNode).count());
        assertEquals(2, new MaxRelativeIndexCounter(cyclicNode).count());
    }
}
