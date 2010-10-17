package ui.graphics;

import java.awt.*;
import java.util.Arrays;

/**
 * @author Anton Chepurov
 */
public class ClockAxisLine extends AbstractLine {

	public ClockAxisLine(int clockCount) {
		setBackground(Color.black);

		shapes = new ClockAxisShape[clockCount];
		Arrays.fill(shapes, ClockAxisShape.getInstance());

		setPreferredSize(new Dimension((2 * AbstractShape.SIZE) * clockCount, AbstractShape.SIZE));
	}

}
