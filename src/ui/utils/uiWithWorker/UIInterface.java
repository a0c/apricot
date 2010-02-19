package ui.utils.uiWithWorker;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 16:32:48
 */
public interface UIInterface {

    void hideDialog();

    void setWorker(TaskSwingWorker worker);

    void updateProgressBar(int value, int maxValue);

    void showSuccessDialog();

    void showErrorDialog(String consoleOutput);

    void setVisible(boolean visible);
}
