package ui.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anton Chepurov
 */
public class TitleLine extends JPanel {
	private static final int TITLE_OFFSET = 5;
	private final static Font DEFAULT_FONT = new Font("Tahoma", Font.PLAIN, 11);
	private final String title;

	public TitleLine(String title, String longestVarName) {
		this.title = title;
		setBackground(Color.black);

		Dimension preferredSize = new Dimension(
				SwingUtilities.computeStringWidth(getFontMetrics(DEFAULT_FONT), longestVarName) + TITLE_OFFSET * 2,
				AbstractShape.SIZE + 10);
		setPreferredSize(preferredSize);

	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (title != null) {
			g.setColor(title.equalsIgnoreCase("CLOCK") ? Color.yellow : Color.green);
			g.drawString(title, TITLE_OFFSET, AbstractShape.SIZE / 2);
		}
	}
}
