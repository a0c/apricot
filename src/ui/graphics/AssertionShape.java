package ui.graphics;

import ui.base.AssertionStatus;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 22.06.2008
 * <br>Time: 18:59:02
 */
public class AssertionShape extends AbstractShape {
    private static final int OFFSET = ClockAxisShape.OFFSET;
    /* INACTIVE */
    private static final Point[] INACTIVE_POINTS = new Point[]{
            new Point(OFFSET, SIZE),
            new Point(OFFSET + 2 * SIZE, SIZE)
    };
    private static final int[] inactiveAtomicXPoints = getAtomicXes(INACTIVE_POINTS);
    private static final int[] inactiveAtomicYPoints = getAtomicYes(INACTIVE_POINTS);
    /* CHECKING */
    private static final Point[] CHECKING_POINTS = new Point[]{
            new Point(OFFSET, SIZE * 2 / 3 + 1),
            new Point(OFFSET + 2 * SIZE, SIZE * 2 / 3 + 1)
    };
    private static final int[] checkingAtomicXPoints = getAtomicXes(CHECKING_POINTS);
    private static final int[] checkingAtomicYPoints = getAtomicYes(CHECKING_POINTS);
    /* FAIL */
    private static final Point[] FAIL_POINTS = new Point[]{
            new Point(OFFSET - SIZE / 3, 0),
            new Point( OFFSET + SIZE / 3, 0),
            new Point(OFFSET, SIZE * 2 / 3),
    };
    private static final int[] failAtomicXPoints = getAtomicXes(FAIL_POINTS);
    private static final int[] failAtomicYPoints = getAtomicYes(FAIL_POINTS);
    /* PASS */
    private static final Point[] PASS_POINTS = new Point[]{
            new Point( OFFSET, 0),
            new Point( OFFSET + SIZE / 3, SIZE * 2 / 3),
            new Point( OFFSET - SIZE / 3, SIZE * 2 / 3),
    };
    private static final int[] passAtomicXPoints = getAtomicXes(PASS_POINTS);
    private static final int[] passAtomicYPoints = getAtomicYes(PASS_POINTS);


    private static final Map<Character, AssertionShape> shapeByValue = new HashMap<Character, AssertionShape>(DEFAULT_POOL_SIZE);

    private AssertionStatus assertionStatus;

    public AssertionShape(char assertionValue) {
        assertionStatus = AssertionStatus.statusOfShortcut("" + assertionValue);
    }

    public AssertionStatus getAssertionStatus() {
        return assertionStatus;
    }

    public void paint(Graphics g, int index) {
        switch (assertionStatus) {
            case INACTIVE:
                drawInactive(g, index);
                break;
            case CHECKING:
                g.setColor(Color.green);
                g.drawPolyline(adjustPointIndices(checkingAtomicXPoints, index), checkingAtomicYPoints, checkingAtomicXPoints.length);
                break;
            case FAIL:
                drawInactive(g, index);
                drawFail(g, index);
                break;
            case PASS:
                drawInactive(g, index);
                drawPass(g, index);
                break;
            case PASS_AND_FAIL:
                drawFail(g, index);
                drawPass(g, index);
                break;
        }
    }

    private static void drawPass(Graphics g, int index) {
        g.setColor(Color.green);
        g.fillPolygon(adjustPointIndices(passAtomicXPoints, index), passAtomicYPoints, passAtomicXPoints.length);
    }

    private static void drawFail(Graphics g, int index) {
        g.setColor(Color.red);
        g.fillPolygon(adjustPointIndices(failAtomicXPoints, index), failAtomicYPoints, failAtomicXPoints.length);
    }

    private static void drawInactive(Graphics g, int index) {
        g.setColor(Color.blue);
        g.drawPolyline(adjustPointIndices(inactiveAtomicXPoints, index), inactiveAtomicYPoints, inactiveAtomicXPoints.length);
    }

    /**
     * Pooling mechanism
     * @param assertionValue of the variable on the given clock
     * @return residing in map available object with the specified assertionValue
     *          or a new created object that gets added to map as well
     */
    public static AssertionShape createShape(char assertionValue) { //todo: generalize!!!
        /* Look for existing object */
        AssertionShape assertionShape = shapeByValue.get(assertionValue);
        if (assertionShape != null) return assertionShape;
        /* Create new object */
        assertionShape = new AssertionShape(assertionValue);
        shapeByValue.put(assertionValue, assertionShape);
        return assertionShape;
    }

}
