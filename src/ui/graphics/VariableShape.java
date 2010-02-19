package ui.graphics;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 20.06.2008
 * <br>Time: 9:59:09
 */
public class VariableShape extends AbstractShape {
    public final static int TRANSITION_LENGTH = 5;
    private final static Long X_VALUE = Long.MIN_VALUE;
    /* Points of the shape */

    private static final Point[] GO_UP_POINTS = new Point[]{
            new Point(0, SIZE),
            new Point(TRANSITION_LENGTH, 0),
            new Point(2 * SIZE, 0)
    };
    private static final Point[] CONTINUE_UP_POINTS = new Point[]{
            new Point(0, 0),
            new Point(2 * SIZE, 0)
    };
    private static final Point[] GO_DOWN_POINTS = new Point[]{
            new Point(0, 0),
            new Point(TRANSITION_LENGTH, SIZE),
            new Point(2 * SIZE, SIZE),
    };
    private static final Point[] CONTINUE_DOWN_POINTS = new Point[]{
            new Point(0, SIZE),
            new Point(2 * SIZE, SIZE),
    };
    private static final Point[] CROSS_UP_POINTS = new Point[]{
            new Point(0, SIZE),
            new Point(2 * SIZE, 0),
    };
    private static final Point[] CROSS_DOWN_POINTS = new Point[]{
            new Point(0, 0),
            new Point(2 * SIZE, SIZE),
    };

    private static final int[] atomicXPointsGoUp = getAtomicXes(GO_UP_POINTS);
    private static final int[] atomicYPointsGoUp = getAtomicYes(GO_UP_POINTS);
    private static final int[] atomicXPointsContinueUp = getAtomicXes(CONTINUE_UP_POINTS);
    private static final int[] atomicYPointsContinueUp = getAtomicYes(CONTINUE_UP_POINTS);
    private static final int[] atomicXPointsGoDown = getAtomicXes(GO_DOWN_POINTS);
    private static final int[] atomicYPointsGoDown = getAtomicYes(GO_DOWN_POINTS);
    private static final int[] atomicXPointsContinueDown = getAtomicXes(CONTINUE_DOWN_POINTS);
    private static final int[] atomicYPointsContinueDown = getAtomicYes(CONTINUE_DOWN_POINTS);
    private static final int[] atomicXPointsCrossUp = getAtomicXes(CROSS_UP_POINTS);
    private static final int[] atomicYPointsCrossUp = getAtomicYes(CROSS_UP_POINTS);
    private static final int[] atomicXPointsCrossDown = getAtomicXes(CROSS_DOWN_POINTS);
    private static final int[] atomicYPointsCrossDown = getAtomicYes(CROSS_DOWN_POINTS);

    private static final Map<VariableShape, VariableShape> shapeByShape = new HashMap<VariableShape, VariableShape>(DEFAULT_POOL_SIZE);
    private static final Map<VariableShape, VariableShape> booleanShapeByShape = new HashMap<VariableShape, VariableShape>(DEFAULT_POOL_SIZE);


    private final long variableValue;
    private final boolean isBoolean;
    private final BooleanDirections direction;

    private VariableShape(long variableValue, boolean isBoolean, BooleanDirections direction) {
        this.variableValue = variableValue;
        this.isBoolean = isBoolean;
        this.direction = direction;
    }

    public void paint(Graphics g, int index) {
        g.setColor(Color.green);
        if (variableValue == X_VALUE) {
            g.setColor(Color.blue);
            /* Cross Up Line */
            drawLine(g, index, BooleanDirections.CROSS_UP);
            /* Cross Down Line */
            drawLine(g, index, BooleanDirections.CROSS_DOWN);

        } else if (isBoolean) {
            if (direction == BooleanDirections.CONTINUE_DOWN || direction == BooleanDirections.GO_DOWN) {
                g.setColor(Color.RED);
            }
            /* Up or Down Line */
            drawLine(g, index, direction);
        } else {
            /* Up Line */
            int[] adjustedXPoints = drawLine(g, index, BooleanDirections.GO_UP);
            /* Down Line */
            drawLine(g, index, BooleanDirections.GO_DOWN);
            /* Number String */
            g.drawString("" + variableValue, adjustedXPoints[1] + 5, SIZE / 2);
        }
    }

    private int[] drawLine(Graphics g, int index, BooleanDirections direction) {
        int[] adjustedXPointsUp = adjustPointIndices(direction.getXPoints(), index);
        g.drawPolyline(adjustedXPointsUp, direction.getYPoints(), direction.getXPoints().length);
        return adjustedXPointsUp;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        VariableShape that = (VariableShape) obj;

        return isBoolean == that.isBoolean && variableValue == that.variableValue && direction == that.direction;

    }

    public int hashCode() {
        int result;
        result = (int) (variableValue ^ (variableValue >>> 32));
        result = 31 * result + (isBoolean ? 1 : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        return result;
    }

    //    public boolean equals(Object obj) {
//        /* Dont check pointers here, since the creation of objects is managed */
//        if (obj == null || getClass() != obj.getClass()) return false;
//
//        VariableShape that = (VariableShape) obj;
//
//        return isBoolean == that.isBoolean && variableValue == that.variableValue && direction == that.direction;
//    }
//
//
//    public int hashCode() {
//        int result;
//        result = variableValue;
//        result = 31 * result + (isBoolean ? 1 : 0);
//        result = 31 * result + (direction != null ? direction.hashCode() : 0);
//        return result;
//    }

    /**
     * Pooling mechanism
     * @param value of the variable on the given clock
     * @return residing in map available object with the specified value
     *          or a new created object that gets added to map as well
     */
    public static VariableShape createShape(long value) {
        return createShape(value, false, null);
    }

    public static VariableShape createBooleanShape(long value, BooleanDirections direction) {
        return createShape(value, true, direction);
    }

    private static VariableShape createShape(long value, boolean isBoolean, BooleanDirections direction) {
        /* Create new object */
        VariableShape newVariableShape = new VariableShape(value, isBoolean, direction);
        if (isBoolean) {
            /* Look for existing object */
            if (booleanShapeByShape.containsKey(newVariableShape)) {
                return booleanShapeByShape.get(newVariableShape);
            } else {
                booleanShapeByShape.put(newVariableShape, newVariableShape);
                return newVariableShape;
            }
        } else {
            /* Look for existing object */
            if (shapeByShape.containsKey(newVariableShape)) {
                return shapeByShape.get(newVariableShape);
            } else {
                shapeByShape.put(newVariableShape, newVariableShape);
                return newVariableShape;
            }
        }

    }

    public static BooleanDirections getDirection(Long previous, long current) {
        return BooleanDirections.getDirection(previous, current);
    }

    public static void printPoolSize() {
        System.out.println("Pool size: " + booleanShapeByShape.size() + ", " + shapeByShape.size());
    }

    private static enum BooleanDirections {
        CONTINUE_UP (atomicXPointsContinueUp, atomicYPointsContinueUp),
        CONTINUE_DOWN (atomicXPointsContinueDown, atomicYPointsContinueDown),
        GO_UP(atomicXPointsGoUp, atomicYPointsGoUp),
        GO_DOWN(atomicXPointsGoDown, atomicYPointsGoDown),
        CROSS_UP(atomicXPointsCrossUp, atomicYPointsCrossUp),
        CROSS_DOWN(atomicXPointsCrossDown, atomicYPointsCrossDown);
        
        private final int[] xPoints;
        private final int[] yPoints;

        BooleanDirections(int[] xPoints, int[] yPoints) {
            this.xPoints = xPoints;
            this.yPoints = yPoints;
        }

        public int[] getXPoints() {
            return xPoints;
        }

        public int[] getYPoints() {
            return yPoints;
        }

        public static BooleanDirections getDirection(Long previous, long current) {
            if (previous == null) {
                return current == 1 ? CONTINUE_UP : CONTINUE_DOWN;
            } else if (previous == 1) {
                if (current == 1) {
                    return CONTINUE_UP;
                } else {
                    return GO_DOWN;
                }
            } else if (previous == 0) {
                if (current == 0) {
                    return CONTINUE_DOWN;
                } else {
                    return GO_UP;
                }
            } else if (previous.equals(X_VALUE)) {
                if (current == 0) {
                    return CONTINUE_DOWN;
                } else {
                    return CONTINUE_UP;
                }
            } else return null;
        }
    }
}
