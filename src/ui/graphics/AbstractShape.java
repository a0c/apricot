package ui.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractShape extends JComponent {
	/**
	 * Shape size
	 */
	static final int SIZE = 20;
	static final int FULL_SIZE = 2 * SIZE;
	/**
	 * Default size of the pool
	 */
	static final int DEFAULT_POOL_SIZE = 256;

	public abstract void paint(Graphics g, int index);

	protected static int[] getAtomicYes(Point... points) {
		int[] yPoints = new int[points.length];
		for (int i = 0; i < points.length; i++) {
			yPoints[i] = points[i].y;
		}
		return yPoints;
	}

	protected static int[] getAtomicXes(Point... points) {
		int[] xPoints = new int[points.length];
		for (int i = 0; i < points.length; i++) {
			xPoints[i] = points[i].x;
		}
		return xPoints;
	}

	protected static int[] adjustPointIndices(int[] pointIndices, int index) {
		int offset = index * (2 * SIZE);
		int[] newPointIndices = new int[pointIndices.length];
		for (int i = 0; i < pointIndices.length; i++) {
			newPointIndices[i] = pointIndices[i] + offset;
		}
		return newPointIndices;
	}
}
