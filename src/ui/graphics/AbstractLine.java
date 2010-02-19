package ui.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 22.06.2008
 * <br>Time: 18:13:07
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
        Rectangle visibleRect = ((JViewport) this.getParent().getParent()).getViewRect();
        int start = visibleRect.getLocation().x;
        int end = start + visibleRect.width;

        int visibleEnd = (int) Math.ceil((double) end / AbstractShape.FULL_SIZE);
        return new VisibleIndices(start / AbstractShape.FULL_SIZE, Math.min(visibleEnd, shapes.length));
    }

    private class VisibleIndices {
        private int start;
        private int end;
        public VisibleIndices(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

}
