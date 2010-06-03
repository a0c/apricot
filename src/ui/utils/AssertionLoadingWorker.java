package ui.utils;

import ui.io.AssertionCheckReader;
import ui.utils.uiWithWorker.TaskSwingWorker;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.io.File;

import ui.ExtendedException;
import ui.BusinessLogicAssertionChecker;
import ui.ApplicationForm;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 20:39:33
 */
public class AssertionLoadingWorker extends TaskSwingWorker {
    private final File simulFile;
    private final BusinessLogicAssertionChecker businessLogicAssertionChecker;
    private AssertionCheckReader simulReader;

    public AssertionLoadingWorker(final File simulFile, BusinessLogicAssertionChecker businessLogicAC) {
        this.simulFile = simulFile;
        this.businessLogicAssertionChecker = businessLogicAC;
        executableRunnable = new Runnable() {
            public void run() {
                try {
                    enableUI(false);
                    simulReader = new AssertionCheckReader(simulFile, uiHolder, 1);
                    simulReader.readAssertions();
                    isProcessFinished = true;

                } catch (Exception e) {
                    businessLogicAssertionChecker.getApplicationForm().updateChkFileTextField(null);
                    occurredException = new ExtendedException("IO Error occurred while reading simulation file:\n" +
                    e.getMessage(), ExtendedException.IO_ERROR_TEXT);
                    isProcessFinished = false;
                }
            }
        };
    }

//    protected Boolean doInBackground() {
//        try {
//            simulReader = new AssertionCheckReader(simulFile, uiHolder, 1);
//            simulReader.readAssertions();
//            return true;
//        } catch (Exception e) {
//            businessLogicAssertionChecker.getApplicationForm().updateChkFileTextField("", null);
//            occurredException = new ExtendedException("IO Error occurred while reading simulation file:\n" +
//                    e.getMessage(), "I/O Error");
//            return false;
//        }
//    }

//    protected void process(List<Integer> chunks) {
//        businessLogicAssertionChecker.getApplicationForm();
//
//    }

    protected void done() {
        super.done();
        try {
            if (get()) {
                updateUI(simulFile, simulReader.getPatternsSize());
                /* Set simulReader */
                businessLogicAssertionChecker.setSimulReader(simulReader);
            }
        } catch (InterruptedException e) {/* Do nothing. */} catch (ExecutionException e) {/* Do nothing. */}
        catch (CancellationException e) {
            /* Do nothing  */
        }

    }

    private void enableUI(boolean enable) {
        businessLogicAssertionChecker.getApplicationForm().setEnableDrawButton(enable);
    }    

    private void updateUI(File simulFile, int maxValue) {
        ApplicationForm applicationForm = businessLogicAssertionChecker.getApplicationForm();
        /* Update text field and spinner */
        if (simulFile != null) {
            applicationForm.updateChkFileTextField(simulFile);
            applicationForm.updateDrawSpinner(maxValue);
        } else {
            applicationForm.updateChkFileTextField(null);
        }

        /* In any case, enable UI */
        enableUI(true);
    }

    public void stopWorker() {
        super.stopWorker();
        /* Update UI and BL */
        businessLogicAssertionChecker.setSimulFile(null);
        updateUI(null, 0);
    }
}
