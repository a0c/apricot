package ui.graphics;

import java.awt.*;
import java.text.ChoiceFormat;

/**
 * @author Anton Chepurov
 */
public class AssertionLine extends AbstractLine {

	private static final ChoiceFormat FAILURES_FORMAT = new ChoiceFormat(new double[]{0, 1, 2}, new String[]{"no failures", "1 failure", "2 failures"});

	public AssertionLine(String toolTip, int patternCount, char[] assertionValuesArray) {
		setBackground(Color.black);

		int failureCount = 0;
		shapes = new AssertionShape[patternCount];
		for (int i = 0; i < patternCount; i++) {
			char value = assertionValuesArray[i];
			shapes[i] = AssertionShape.createShape(value);
			if (value == 'F') {
				failureCount++;
			}
		}

		setPreferredSize(new Dimension((2 * AbstractShape.SIZE) * patternCount, AbstractShape.SIZE + 10));
		setToolTipText(toolTip + " (" + failureToString(failureCount) + ")");
	}

	private static String failureToString(int failureCount) {
		return failureCount > 2 ? failureCount + " failures" : FAILURES_FORMAT.format(failureCount);
	}

	public static void main(String[] args) {
		System.out.println(" (" + failureToString(0) + ")");
		System.out.println(" (" + failureToString(1) + ")");
		System.out.println(" (" + failureToString(2) + ")");
		System.out.println(" (" + failureToString(3) + ")");
		System.out.println(" (" + failureToString(10) + ")");

	}
}
