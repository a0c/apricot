package ui.utils;

import ui.utils.uiWithWorker.TaskSwingWorker;
import io.ConsoleWriter;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.io.OutputStream;
import java.io.File;

import ui.ApplicationForm;
import ui.BusinessLogicCoverageAnalyzer;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 22:52:13
 */
public class CoverageAnalyzingWorker extends TaskSwingWorker {
    private final BusinessLogicCoverageAnalyzer businessLogic;

    public CoverageAnalyzingWorker(List<String> executableCommand, OutputStream infoOut, OutputStream errorOut, BusinessLogicCoverageAnalyzer businessLogic, ConsoleWriter consoleWriter) {
        super(executableCommand, infoOut, errorOut, consoleWriter);
        this.businessLogic = businessLogic;
    }

    protected Boolean doInBackground() {
        /* Disable HLDD BUTTON and ANALYZE BUTTON */
        setEnableUI(false);
        /* Start worker */
        return super.doInBackground();
    }

    protected void done() {
        /* Enable HLDD BUTTON and ANALYZE BUTTON */
        setEnableUI(true);

        super.done();
        try {
            if (get()) {
                /* Display coverage */
                businessLogic.displayCoverage();
                showVHDLCoverage();
            }
        } catch (InterruptedException e) {/* Do nothing. */} catch (ExecutionException e) {/* Do nothing. */}

    }

    private void setEnableUI(boolean enable) {
        ApplicationForm applicationForm = businessLogic.getApplicationForm();
        applicationForm.setEnableHlddCoverageButton(enable);
        applicationForm.setEnableAnalyzeButton(enable);
    }

    private void showVHDLCoverage() {
        /* Automatically load COV and VHDL files, if available */
        ApplicationForm applicationForm = businessLogic.getApplicationForm();
        File covFile = businessLogic.deriveFileFrom(businessLogic.getHlddFile(), ".agm", ".cov");
        if (covFile != null) {
            applicationForm.updateCovTextField(covFile.getName(), covFile.getAbsolutePath());
            businessLogic.setCovFile(covFile);
        }

        File vhdlFile = businessLogic.deriveFileFrom(businessLogic.getHlddFile(), ".agm", ".vhdl");
        if (vhdlFile == null) vhdlFile = businessLogic.deriveFileFrom(businessLogic.getHlddFile(), ".agm", ".vhd");
        if (vhdlFile != null) {
            applicationForm.updateVhdlCovTextField(vhdlFile.getName(), vhdlFile.getAbsolutePath());
            businessLogic.setVhdlFile(vhdlFile);
        }
        /* Automatically clikc Show button, if both files are set */
        if (vhdlFile != null && covFile != null) {
            applicationForm.doClickShowButton();
        }
    }
}
