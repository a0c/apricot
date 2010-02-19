package ui;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 12.12.2008
 * <br>Time: 14:48:24
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
