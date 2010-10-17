package ui;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.IOException;
import java.io.File;
import java.net.URL;

/**
 * @author Anton Chepurov
 */
public class PicturePanel extends JPanel {

	private static final URL MISSING_FILE_URL = PicturePanel.class.getResource("Ansip_Meie pole suudi.png");
	private BufferedImage image;

	public PicturePanel(File pictureFile) {
		try {
			if (pictureFile == null) {
				image = javax.imageio.ImageIO.read(MISSING_FILE_URL);
			} else {
				image = javax.imageio.ImageIO.read(pictureFile);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
	}

	public void add(Component comp, Object constraints) {
		((JComponent) comp).setOpaque(false);

		if (comp instanceof JScrollPane) {
			JScrollPane scrollPane = (JScrollPane) comp;
			JViewport viewPort = scrollPane.getViewport();
			viewPort.setOpaque(false);
			Component component = viewPort.getView();

			if (component instanceof JComponent) {
				((JComponent) component).setOpaque(false);
			}
		}

		super.add(comp, constraints);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawToViewPort(g);
	}

	private void drawToViewPort(Graphics g) {
		g.drawImage(image, 0, 0, this);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	private void drawScaled(Graphics g) {
		Dimension size = getSize();
		g.drawImage(image, 0, 0, size.width / 3, size.height, null);
	}

	@SuppressWarnings({"UnusedDeclaration"})
	private void drawImage(Graphics g) {
		Dimension size = getSize();
		int x = size.width - image.getWidth();
		int y = size.height - image.getHeight();
		g.drawImage(image, x, y, this);
	}
}
