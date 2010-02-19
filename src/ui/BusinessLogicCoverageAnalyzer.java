package ui;

import ui.utils.uiWithWorker.UIWithWorker;
import ui.utils.CoverageVizualizingUI;
import ui.utils.CoverageAnalyzingUI;
import ui.utils.CoverageAnalyzingWorker;
import ui.utils.CoverageVizualizingWorker;
import ui.graphics.CoverageFrame;
import ui.io.CoverageParser;
import io.ConsoleWriter;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 22:40:00
 */
public class BusinessLogicCoverageAnalyzer {
    private final ApplicationForm applicationForm;
    private final ConsoleWriter consoleWriter;
    private File hlddFile;
    private File vhdlFile;
    private File covFile;
    private File mappingFile;
    private ByteArrayOutputStream byteArrayOutputStream;
    private static final int default_size = 32768;

    public BusinessLogicCoverageAnalyzer(ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
        this.applicationForm = applicationForm;
        this.consoleWriter = consoleWriter;
    }

    public File getHlddFile() {
        return hlddFile;
    }

    public void setHlddFile(File hlddFile) {
        this.hlddFile = hlddFile;
    }

    public void setVhdlFile(File vhdlFile) {
        this.vhdlFile = vhdlFile;
    }

    public void setCovFile(File covFile) {
        this.covFile = covFile;
        /* Clear mapping file (this file is implicit for user, so must be manipulated carefully) */
        setMappingFile(null);
    }

    public void setMappingFile(File mappingFile) {
        this.mappingFile = mappingFile;
    }

    public void processAnalyze() throws ExtendedException {
        /* Check design to be selected */
        if (hlddFile == null) {
            throw new ExtendedException("HLDD model file is missing", ExtendedException.MISSING_FILE_TEXT);
        }

        int patternCount = applicationForm.getPatternCountForCoverage();
        boolean isRandom = applicationForm.isRandomCov();
        boolean isDoAssert = applicationForm.isDoAnalyzeCoverage();
        

        /* Collect execution string */
        List<String> commandList = new ArrayList<String>(5);
        commandList.add(ApplicationForm.LIB_DIR + (com.sun.jna.Platform.isWindows() ? "beh_simul.exe" : "beh_simul"));
        if (isDoAssert) {
            commandList.add("-coverage");
        }
        if (isRandom) {
            commandList.add("-random");
            commandList.add("" + patternCount);
        }
        commandList.add(hlddFile.getAbsolutePath().replace(".agm", ""));

        /* Execute command */
        byteArrayOutputStream = new ByteArrayOutputStream(default_size);
        UIWithWorker.runUIWithWorker(
                new CoverageAnalyzingUI(applicationForm.getFrame()),
                new CoverageAnalyzingWorker(
                        commandList,
                        byteArrayOutputStream,
                        System.err,
                        this,
                        consoleWriter
                )
        );

        /* displayCoverage() is invoked in CoverageAnalyzingWorker after it ("assert") has completed its work */

    }

    public void displayCoverage() {

        CoverageParser parser = new CoverageParser(byteArrayOutputStream.toString());
        CoverageFrame coverageFrame = new CoverageFrame(
                parser.getNodeCoverage(),
                parser.getEdgeCoverage(),
                parser.getToggleCoverage(),
                generateTabTitle(hlddFile));
        applicationForm.addCoverage(generateTabTitle(hlddFile), hlddFile.getAbsolutePath(), coverageFrame.getMainPanel());
//        applicationForm.addFileViewerTab(generateTabTitle(hlddFile), hlddFile.getAbsolutePath(), coverageFrame.getMainPanel());
    }

    private String generateTabTitle(File hlddFile) {
        StringBuilder sb = new StringBuilder(hlddFile.getName());
        sb.delete(sb.lastIndexOf("."), sb.length());
        sb.append(":Coverage");
        return sb.toString();
    }

    public ApplicationForm getApplicationForm() {
        return applicationForm;
    }

    public File derivePatternsFileFrom(File hlddFile) {
        return BusinessLogic.deriveFileFrom(hlddFile, ".agm", ".tst");
    }

    public File deriveFileFrom(File sourceFile, String sourceFileExtension, String derivedFileExtension) {
        return BusinessLogic.deriveFileFrom(sourceFile, sourceFileExtension, derivedFileExtension);
    }

    public void processShow() throws ExtendedException {
        /* Check all files to be selected */
        String fileDescription = null;
        if (vhdlFile == null) {
            fileDescription = "VHDL";
        } else if (covFile == null) {
            fileDescription = "Coverage";
        } else if (mappingFile == null) {
            fileDescription = "Mapping";
            //todo: temporary fix
            mappingFile = BusinessLogic.deriveFileFrom(covFile, ".cov", ".map");
            fileDescription = mappingFile == null ? "Mapping" : null;
            //todo: temporary fix
        }
        if (fileDescription != null) {
            throw new ExtendedException(fileDescription + " file is missing", ExtendedException.MISSING_FILE_TEXT);
        }


        /*  */
        UIWithWorker.runUIWithWorker(new CoverageVizualizingUI(applicationForm.getFrame()),
                new CoverageVizualizingWorker(vhdlFile, covFile, mappingFile, applicationForm, consoleWriter));

    }
}
