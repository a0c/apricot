package ui.utils.uiWithWorker;

/**
 * @author Anton Chepurov
 */
public interface UIInterface {

	void hideDialog();

	void setWorker(TaskSwingWorker worker);

	void updateProgressBar(int value, int maxValue);

	void showSuccessDialog();

	void showErrorDialog(String consoleOutput);

	void setVisible(boolean visible);
}
