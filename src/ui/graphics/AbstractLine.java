package ui.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractLine extends JPanel { //todo: JPanel -> JComponent

	protected AbstractShape[] shapes;

	public void paintChildren(Graphics g) {
		AbstractLine.VisibleIndices visibleIndices = getVisibleIndices();
		for (int i = visibleIndices.start; i < visibleIndices.end; i++) {
			shapes[i].paint(g, i);
		}
	}

	public VisibleIndices getVisibleIndices() {
		Rectangle visibleRectangle = ((JViewport) this.getParent().getParent()).getViewRect();
		int start = visibleRectangle.getLocation().x;
		int end = start + visibleRectangle.width;

		int visibleEnd = (int) Math.ceil((double) end / AbstractShape.FULL_SIZE);
		return new VisibleIndices(start / AbstractShape.FULL_SIZE, Math.min(visibleEnd, shapes.length));
	}

	@SuppressWarnings({"InstanceVariableNamingConvention"})
	private class VisibleIndices {
		private int start;
		private int end;

		public VisibleIndices(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}

}
