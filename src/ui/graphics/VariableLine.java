package ui.graphics;

import ui.utils.uiWithWorker.UIInterface;

import javax.swing.*;
import java.awt.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 22.06.2008
 * <br>Time: 18:24:13
 */
public class VariableLine extends AbstractLine {

    public VariableLine(int patternCount, boolean isBoolean, long... variableValues) {
        /* Set Background */
        setBackground(Color.black);
        /* Set Layout */
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        shapes = new VariableShape[patternCount];
        for (int i = 0; i < patternCount; i++) {
            shapes[i] = isBoolean ?
                    VariableShape.createBooleanShape(variableValues[i],
                            VariableShape.getDirection(previousValue(i, variableValues), variableValues[i])) :
                    VariableShape.createShape(variableValues[i]);
        }
        
        /* Set TOOLTIP */
//        setToolTipText("1"); //todo...

//            /* Create and Add shapes to panel */
//            for (int i = 0; i < 1000/*variableValues.length*/; i++) {
//                int variableValue = variableValues[i];
//                add(new VariableShape(i, variableValue));
//            }
        /* Adjust the size of the panel, to enable Scroll bars to appear in JScrollPane */
        int width = (AbstractShape.FULL_SIZE) * patternCount;
        int height = AbstractShape.SIZE + 10;
        setPreferredSize(new Dimension(width, height));

    }

    private static Long previousValue(int currentIndex, long... variableValues) {
        return currentIndex == 0 ? null : variableValues[currentIndex - 1];
    }
}
