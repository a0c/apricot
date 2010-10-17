package ui.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anton Chepurov
 */
public class VariableLine extends AbstractLine {

	public VariableLine(int patternCount, boolean isBoolean, long... variableValues) {
		/* Set Background */
		setBackground(Color.black);
		/* Set Layout */
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		shapes = new VariableShape[patternCount];
		for (int i = 0; i < patternCount; i++) {
			shapes[i] = isBoolean ?
					VariableShape.createBooleanShape(variableValues[i],
							VariableShape.getDirection(previousValue(i, variableValues), variableValues[i])) :
					VariableShape.createShape(variableValues[i]);
		}

		/* Set TOOLTIP */
//		setToolTipText("1"); //todo...

		/* Adjust the size of the panel, to enable Scroll bars to appear in JScrollPane */
		int width = (AbstractShape.FULL_SIZE) * patternCount;
		int height = AbstractShape.SIZE + 10;
		setPreferredSize(new Dimension(width, height));
	}

	private static Long previousValue(int currentIndex, long... variableValues) {
		return currentIndex == 0 ? null : variableValues[currentIndex - 1];
	}
}
