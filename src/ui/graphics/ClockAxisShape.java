package ui.graphics;

import java.awt.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 22.06.2008
 * <br>Time: 0:59:46
 */
public class ClockAxisShape extends AbstractShape {
    public static final int OFFSET = VariableShape.TRANSITION_LENGTH / 2;
    private static final ClockAxisShape instance = new ClockAxisShape();
    /* Disable instantiation */
    private ClockAxisShape() {}

    public static ClockAxisShape getInstance() {
        return instance;
    }

    public void paint(Graphics g, int index) {
        g.setColor(Color.yellow);
        /* Draw Lines */
        g.drawLine(
                OFFSET + index * (2 * SIZE),
                0,
                OFFSET + (index + 1) * (2 * SIZE),
                0
        );
        g.drawLine(
                OFFSET + index * (2 * SIZE),
                0,
                OFFSET + index * (2 * SIZE),
                SIZE / 2);
        /* Draw Text */
        g.drawString("" + (index + 1), OFFSET + index * (2 * SIZE) + 3, SIZE / 2);
    }

}
