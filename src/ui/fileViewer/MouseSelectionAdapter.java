package ui.fileViewer;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 16.04.2009
 * <br>Time: 18:04:19
 */
public class MouseSelectionAdapter extends MouseAdapter {
    protected JTabbedPane tabbedPane;

    public MouseSelectionAdapter(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    public void mouseClicked(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof Component) {
            tabbedPane.setSelectedIndex(tabbedPane.indexOfTabComponent((Component) source));
        }
    }
}
