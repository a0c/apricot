package ui.utils;

import ui.utils.uiWithWorker.TaskSwingWorker;
import ui.io.HLDD2VHDLMappingReader;
import ui.io.CoverageReader;
import ui.base.HLDD2VHDLMapping;
import ui.base.NodeItem;
import io.ConsoleWriter;

import java.io.File;
import java.util.Collection;

import ui.ApplicationForm;
import ui.ExtendedException;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 18.12.2008
 * <br>Time: 1:01:06
 */
public class CoverageVizualizingWorker extends TaskSwingWorker {
    private final File vhdlFile;
    private final File covFile;
    private final File mappingFile;
    private final ApplicationForm applicationForm;
    private final ConsoleWriter consoleWriter;


    public CoverageVizualizingWorker(File vhdlFile, File covFile, File mappingFile, ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
        this.vhdlFile = vhdlFile;
        this.covFile = covFile;
        this.mappingFile = mappingFile;
        this.applicationForm = applicationForm;
        this.consoleWriter = consoleWriter;
        executableRunnable = createRunnable();
    }

    private Runnable createRunnable() {
        return new Runnable() {
            public void run() {
                try {
                    /* Read HLDD-2-VHDL mapping */
                    consoleWriter.write("Mapping HLDD to VHDL..."); //todo: is there sense in  concealing the fact of mannual mapping?
                    HLDD2VHDLMapping hldd2VHDLMapping = new HLDD2VHDLMappingReader(mappingFile).getMapping();
                    consoleWriter.done();

                    /* Read COV file */
                    consoleWriter.write("Reading coverage file...");
                    CoverageReader coverageReader = new CoverageReader(covFile);
                    Collection<NodeItem> uncoveredNodeItems = coverageReader.getUncoveredNodeItems();
                    consoleWriter.done();

                    /* Extract lines for uncovered nodes */
                    Collection<Integer> uncoveredNodeLines = hldd2VHDLMapping.getLinesFor(uncoveredNodeItems);

                    /* Add tab to the FileViewer */
                    applicationForm.addFileViewerTabFromFile(vhdlFile, uncoveredNodeLines, null, null);

                    isProcessFinished = true;
                } catch (Exception e) {
                    occurredException = ExtendedException.create(e);
                    isProcessFinished = false;
                }

            }
        };
    }

    protected Boolean doInBackground() {
        /* Disable UI */
        enableUI(false);

        return super.doInBackground();
    }

    protected void done() {
        /* Enable UI */
        enableUI(true);

        super.done();
    }

    private void enableUI(boolean enable) {
        applicationForm.setEnabledVhdlCoverageButton(enable);
        applicationForm.setEnabledCovButton(enable);
        applicationForm.setEnabledShowButton(enable);
    }
}
