package ui.utils.uiWithWorker;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 16:27:14
 */
public abstract class UIWithWorker {

    public static void runUIWithWorker(UIThread ui, TaskSwingWorker worker) {

        /* Show UI dialog */
        SwingUtilities.invokeLater(ui);

        /* Exchange objects */
        ui.setWorker(worker);
        worker.setUi(ui);

        /* Start worker */
        worker.execute();
        
    }

}
