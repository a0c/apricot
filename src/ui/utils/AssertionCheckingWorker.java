package ui.utils;

import ui.utils.uiWithWorker.TaskSwingWorker;
import io.ConsoleWriter;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.io.OutputStream;
import java.io.File;

import ui.BusinessLogicAssertionChecker;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 19:53:13
 */
public class AssertionCheckingWorker extends TaskSwingWorker {
    private final BusinessLogicAssertionChecker businessLogic;
    private final String simulFilePath;

    public AssertionCheckingWorker(List<String> executableCommand, OutputStream infoOut, OutputStream errorOut, BusinessLogicAssertionChecker businessLogic, String simulFilePath, ConsoleWriter consoleWriter) {
        super(executableCommand, infoOut, errorOut, consoleWriter);
        this.businessLogic = businessLogic;
        this.simulFilePath = simulFilePath;
    }

    protected void done() {
        super.done();
        try {
            if (get()) {
                /* Fill Simul.file automatically */
                File simulFile = new File(simulFilePath);
                if (simulFile.exists()) {
                    businessLogic.setSimulFile(simulFile);
                    businessLogic.loadChkFile();
                }
            }
        } catch (InterruptedException e) {/* Do nothing. */} catch (ExecutionException e) {/* Do nothing. */}
    }
}
