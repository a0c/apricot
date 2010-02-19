package ui.utils;

import ui.utils.uiWithWorker.TaskSwingWorker;
import ui.BusinessLogicAssertionChecker;
import ui.ExtendedException;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 19.12.2008
 * <br>Time: 21:56:54
 */
public class AssertionLoadingWaiter extends TaskSwingWorker {

    public AssertionLoadingWaiter(final BusinessLogicAssertionChecker businessLogicAC) {
        executableRunnable = new Runnable() {
            public void run() {
                /* Wait until simulReader is initiated by AssertionLoadingWorker */
                while (!Thread.interrupted()) {
                    try {
                        if (businessLogicAC.isChkFileLoaded()) {
                            /* Wait until CHK file is loaded */
                            break;
                        }
                        if (businessLogicAC.getSimulFile() == null) {
                            /* If loading of CHK file is cancelled, stop waiting and terminate the thread*/
                            Thread.currentThread().interrupt();
                        }
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                try {
                    uiHolder.setVisible(true);
                    businessLogicAC.drawWaveform();
                } catch (ExtendedException e) {
                    occurredException = e;
                }

            }
        };
    }

    public void stopWorker() {
        Thread.currentThread().interrupt();
        super.stopWorker();
    }
}
