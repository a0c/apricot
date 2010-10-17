package ui;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;

/**
 * @author Anton Chepurov
 */
public class IconAdder {
	
	private static final URL DEFAULT_ICON_URL = IconAdder.class.getResource("synchronize.jpg");

	public static void setFrameIcon(JFrame frame) {
		try {
			if (DEFAULT_ICON_URL != null) {
				frame.setIconImage(javax.imageio.ImageIO.read(DEFAULT_ICON_URL));
			}
		} catch (IOException e) {
			/* Do nothing. Will use default icon. */
		}
	}


}
