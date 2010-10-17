package ui.graphics;

import java.awt.*;

/**
 * @author Anton Chepurov
 */
public class ClockAxisShape extends AbstractShape {

	public static final int OFFSET = VariableShape.TRANSITION_LENGTH / 2;

	private static final ClockAxisShape INSTANCE = new ClockAxisShape();

	/**
	 * Disable instantiation
	 */
	private ClockAxisShape() {
	}

	public static ClockAxisShape getInstance() {
		return INSTANCE;
	}

	public void paint(Graphics g, int index) {
		g.setColor(Color.yellow);
		/* Draw Lines */
		g.drawLine(
				OFFSET + index * (2 * SIZE),
				0,
				OFFSET + (index + 1) * (2 * SIZE),
				0
		);
		g.drawLine(
				OFFSET + index * (2 * SIZE),
				0,
				OFFSET + index * (2 * SIZE),
				SIZE / 2);
		/* Draw Text */
		g.drawString("" + (index + 1), OFFSET + index * (2 * SIZE) + 3, SIZE / 2);
	}

}
