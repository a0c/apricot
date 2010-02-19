package ui.graphics;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 22.06.2008
 * <br>Time: 18:17:49
 */
public class ClockAxisLine extends AbstractLine {

    public ClockAxisLine(int clockCount) {
        setBackground(Color.black);

        shapes = new ClockAxisShape[clockCount];
        Arrays.fill(shapes, ClockAxisShape.getInstance());

        setPreferredSize(new Dimension((2 * AbstractShape.SIZE) * clockCount, AbstractShape.SIZE));
    }

}
