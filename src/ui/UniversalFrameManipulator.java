package ui;

import javax.swing.*;
import java.awt.*;

/**
 * Class locates the specified frame in different ways provided by its static methods.
 * 1) on the center of the screen.
 * 2) on the top of the screen.
 * 3) max height
 *
 * @author Anton Chepurov
 */
public class UniversalFrameManipulator {

	@SuppressWarnings({"UnusedDeclaration"})
	public static void centerFrame(JFrame frameToCenter) {
		frameToCenter.setLocationRelativeTo(null);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	public static void topFrame(JFrame frameToCenter) {
		Rectangle maxBounds = getMaximumBounds();
		frameToCenter.setSize(maxBounds.width, frameToCenter.getHeight());

	}

	public static void maximize(JFrame frameToCenter) {
		frameToCenter.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	public static void maxHeight(JFrame frameToLocate) {
		Rectangle maxBounds = getMaximumBounds();
		frameToLocate.setSize(frameToLocate.getPreferredSize().width, maxBounds.height);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	public static void halfHeight(JFrame frameToLocate) {
		Rectangle maxBounds = getMaximumBounds();
		frameToLocate.setSize(frameToLocate.getPreferredSize().width, maxBounds.height / 2);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	public static void horizontalCenterFrame(JFrame frameToCenter) {
		Point centerPoint = getCenterPoint();
		frameToCenter.setLocation(
				centerPoint.x - (frameToCenter.getPreferredSize().width / 2),
				frameToCenter.getLocation().y
		);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	public static void verticalCenterFrame(JFrame frameToCenter) {
		Point centerPoint = getCenterPoint();
		frameToCenter.setLocation(
				frameToCenter.getLocation().x,
				centerPoint.y - (frameToCenter.getPreferredSize().height / 2)
		);
	}

	private static Point getCenterPoint() {
		return getLocalGraphicsEnvironment().getCenterPoint();
	}

	private static GraphicsEnvironment getLocalGraphicsEnvironment() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment();
	}

	private static Rectangle getMaximumBounds() {
		return getLocalGraphicsEnvironment().getMaximumWindowBounds();
	}
}
