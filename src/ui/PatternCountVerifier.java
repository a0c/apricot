package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 26.06.2008
 * <br>Time: 11:59:29
 */
public class PatternCountVerifier extends InputVerifier implements ActionListener {

    public boolean verify(JComponent input) {
        if (input instanceof JTextField) {
            String text = ((JTextField) input).getText();
            try {
                Integer.parseInt(text);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldYieldFocus(JComponent input) {
        if (input instanceof JTextField) {
            boolean isOk = verify(input);

            if (isOk) {
                return true;
            } else {
                ((JTextField) input).selectAll();
                Toolkit.getDefaultToolkit().beep();
                return false;
            }
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        JTextField source = (JTextField) e.getSource();
        if (shouldYieldFocus(source)) {
            source.transferFocus();
        }
    }
}
