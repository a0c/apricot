package ui.utils.uiWithWorker;

import javax.swing.*;

/**
 * @author Anton Chepurov
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
