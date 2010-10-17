package ui.graphics;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Anton Chepurov
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

	private static final int[] ATOMIC_X_POINTS_GO_UP = getAtomicXes(GO_UP_POINTS);
	private static final int[] ATOMIC_Y_POINTS_GO_UP = getAtomicYes(GO_UP_POINTS);
	private static final int[] ATOMIC_X_POINTS_CONTINUE_UP = getAtomicXes(CONTINUE_UP_POINTS);
	private static final int[] ATOMIC_Y_POINTS_CONTINUE_UP = getAtomicYes(CONTINUE_UP_POINTS);
	private static final int[] ATOMIC_X_POINTS_GO_DOWN = getAtomicXes(GO_DOWN_POINTS);
	private static final int[] ATOMIC_Y_POINTS_GO_DOWN = getAtomicYes(GO_DOWN_POINTS);
	private static final int[] ATOMIC_X_POINTS_CONTINUE_DOWN = getAtomicXes(CONTINUE_DOWN_POINTS);
	private static final int[] ATOMIC_Y_POINTS_CONTINUE_DOWN = getAtomicYes(CONTINUE_DOWN_POINTS);
	private static final int[] ATOMIC_X_POINTS_CROSS_UP = getAtomicXes(CROSS_UP_POINTS);
	private static final int[] ATOMIC_Y_POINTS_CROSS_UP = getAtomicYes(CROSS_UP_POINTS);
	private static final int[] ATOMIC_X_POINTS_CROSS_DOWN = getAtomicXes(CROSS_DOWN_POINTS);
	private static final int[] ATOMIC_Y_POINTS_CROSS_DOWN = getAtomicYes(CROSS_DOWN_POINTS);

	private static final Map<VariableShape, VariableShape> SHAPE_BY_SHAPE = new HashMap<VariableShape, VariableShape>(DEFAULT_POOL_SIZE);
	private static final Map<VariableShape, VariableShape> BOOLEAN_SHAPE_BY_SHAPE = new HashMap<VariableShape, VariableShape>(DEFAULT_POOL_SIZE);


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

	@SuppressWarnings({"QuestionableName"})
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

	/**
	 * Pooling mechanism
	 *
	 * @param value of the variable on the given clock
	 * @return residing in map available object with the specified value
	 * 		   or a new created object that gets added to map as well
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
			if (BOOLEAN_SHAPE_BY_SHAPE.containsKey(newVariableShape)) {
				return BOOLEAN_SHAPE_BY_SHAPE.get(newVariableShape);
			} else {
				BOOLEAN_SHAPE_BY_SHAPE.put(newVariableShape, newVariableShape);
				return newVariableShape;
			}
		} else {
			/* Look for existing object */
			if (SHAPE_BY_SHAPE.containsKey(newVariableShape)) {
				return SHAPE_BY_SHAPE.get(newVariableShape);
			} else {
				SHAPE_BY_SHAPE.put(newVariableShape, newVariableShape);
				return newVariableShape;
			}
		}

	}

	public static BooleanDirections getDirection(Long previous, long current) {
		return BooleanDirections.getDirection(previous, current);
	}

	@SuppressWarnings({"EnumeratedConstantNamingConvention"})
	private static enum BooleanDirections {
		CONTINUE_UP ( ATOMIC_X_POINTS_CONTINUE_UP, ATOMIC_Y_POINTS_CONTINUE_UP),
		CONTINUE_DOWN ( ATOMIC_X_POINTS_CONTINUE_DOWN, ATOMIC_Y_POINTS_CONTINUE_DOWN),
		GO_UP ( ATOMIC_X_POINTS_GO_UP, ATOMIC_Y_POINTS_GO_UP),
		GO_DOWN ( ATOMIC_X_POINTS_GO_DOWN, ATOMIC_Y_POINTS_GO_DOWN),
		CROSS_UP ( ATOMIC_X_POINTS_CROSS_UP, ATOMIC_Y_POINTS_CROSS_UP),
		CROSS_DOWN ( ATOMIC_X_POINTS_CROSS_DOWN, ATOMIC_Y_POINTS_CROSS_DOWN);

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
