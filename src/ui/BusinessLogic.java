package ui;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import base.hldd.structure.models.BehModel;
import io.ConsoleWriter;

import ui.utils.ConvertingWorker;


/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 21.02.2008
 * <br>Time: 16:26:06
 */
public class BusinessLogic {

	private static final Logger LOG = Logger.getLogger(BusinessLogic.class.getName());

    private final ApplicationForm applicationForm;
    private final ConsoleWriter consoleWriter;

    private File sourceFile = null;
    private File destFile = null;
    private File baseModelFile = null;

    private BehModel model;

    /* Auxiliary fields */
    private String message;
    private String comment;

    public BusinessLogic(ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
        this.applicationForm = applicationForm;
        this.consoleWriter = consoleWriter;
    }

    public void processParse() throws ExtendedException {
        /* Receive data from form */
        ParserID parserID = applicationForm.getSelectedParserId();
        boolean shouldReuseConstants = applicationForm.shouldReuseConstants();
        boolean shouldSimplify = applicationForm.shouldSimplify();
        boolean doFlattenConditions = applicationForm.shouldFlattenCS();
        boolean doCreateGraphsForCS = applicationForm.shouldAsGraphCS();
        boolean doCreateSubGraphs = applicationForm.shouldUseSubGraphs();
        HLDDRepresentationType hlddType = applicationForm.getHlddRepresentationType();

        /* Check files */
        if (sourceFile == null || destFile == null) {
            message = parserID == ParserID.PSL2THLDD
                    ? "Either Library or PSL file is missing" 
                    : "Either source or destination file is missing";
            throw new ExtendedException(message, ExtendedException.MISSING_FILES_TEXT);
        }
        if (parserID == ParserID.PSL2THLDD && baseModelFile == null) {
            message = "Base HLDD model file is missing";
            throw new ExtendedException(message, ExtendedException.MISSING_FILE_TEXT);
        }


        /* Perform PARSING and CONVERSIONS in a separate thread */
        new ConvertingWorker(this, parserID, consoleWriter, sourceFile, destFile, baseModelFile,
                shouldReuseConstants, doFlattenConditions, doCreateGraphsForCS, doCreateSubGraphs, hlddType, shouldSimplify).execute();

    }

    public void reset() {
		// Reset COMMENT
        comment = null;
    }

    public void addComment(String comment) {
        if (this.comment == null) {
            this.comment = comment;
        } else {
            this.comment += comment;
        }
    }

    public void saveModel() throws ExtendedException {

        /* Save model to file */
        try {
            /* For PSL parser, change output file from *.PSL into *.TGM */
            changeDestFile(".psl", ".tgm");

            model.toFile(destFile, comment);
            consoleWriter.writeLn("Model saved to: " + destFile.getAbsolutePath());
        } catch (IOException e) {
            message = "Error while saving file:\n" + e.getMessage();
            throw new ExtendedException(message, ExtendedException.ERROR_TEXT);
        } finally {
            /* For PSL parser, change output file back from *.TGM into *.PSL */
            changeDestFile(".tgm", ".psl");
        }

    }

    private void changeDestFile(String from, String into) {
        if (applicationForm.getSelectedParserId() == ParserID.PSL2THLDD) {
            destFile = new File(destFile.getAbsolutePath().replace(from, into));
        }
    }

    public String getProposedFileName() {
        if (destFile != null) return destFile.getAbsolutePath();
        if (sourceFile == null) return null;
        String proposedFileName = null;
        String sourceName = sourceFile.getAbsolutePath();
        ParserID selectedParserId = applicationForm.getSelectedParserId();
        switch (selectedParserId) {
            case VhdlBeh2HlddBeh:
            case VhdlBehDd2HlddBeh:
                proposedFileName = sourceName.substring(0, sourceName.lastIndexOf(".") + 1) + "agm";
                break;
            case HlddBeh2HlddRtl:
                proposedFileName = sourceName.substring(0, sourceName.lastIndexOf(".")) + "_RTL.agm";
        }
        return proposedFileName;
    }

    public ApplicationForm getApplicationForm() {
        return applicationForm;
    }

    public void setModel(BehModel model) {
        this.model = model;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setDestFile(File destFile) {
        this.destFile = destFile;
    }

    public void setBaseModelFile(File baseModelFile) {
        this.baseModelFile = baseModelFile;
    }

    public void clearFiles() {
        sourceFile = null;
        destFile = null;
        baseModelFile = null;
    }

    public File deriveBaseModelFileFrom(File pslFile) {
        return deriveFileFrom(pslFile, ".psl", ".agm");
    }

    /**
     *
     * @param sourceFile
     * @param sourceFileExtension
     * @param derivedFileExtension
     * @return derived file, if it exists, or <code>null</code> if it doesn't exist.
     */
    public static File deriveFileFrom(File sourceFile, String sourceFileExtension, String derivedFileExtension) {
		String sourcePath = sourceFile.getAbsolutePath().toUpperCase();
		sourceFileExtension = sourceFileExtension.toUpperCase();
		if (sourcePath.endsWith(sourceFileExtension)) {
			File derivedFile = new File(sourcePath.replace(sourceFileExtension, derivedFileExtension));
			return derivedFile.exists() ? derivedFile : null;
		} else {
			LOG.finer("Mismatch between real souceFile extension and provided sourceFileExtension: \"" +
					sourcePath + "\" and \"" + sourceFileExtension + "\"");
			throw new RuntimeException("Mismatch between real souceFile extension and provided sourceFileExtension: \"" +
					sourcePath + "\" and \"" + sourceFileExtension + "\"");
			// using replace() only may result in leaving the sourcePath unchanged, then
			// creating a File from it and returning it, stating that it exists.   
		}
	}

    public void doLoadHlddGraph() {
        applicationForm.doLoadHlddGraph(deriveFileFrom(destFile, ".agm", ".png"));
    }


	public enum ParserID {
        VhdlBeh2HlddBeh("VHDL Beh => HLDD Beh"), /* VHDL Beh => HLDD Beh */
        VhdlBehDd2HlddBeh("HIF => HLDD Beh"),    /* VHDL Beh DD => HLDD Beh */ 
        HlddBeh2HlddRtl("HLDD Beh => HLDD RTL"), /* HLDD Beh => HLDD RTL */
        PSL2THLDD("PSL => THLDD"); /* PSL => THLDD */
                                                       
        private final String title;

        ParserID(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public static ParserID getSelected(int selectedIndex) {
            switch (selectedIndex) {
                case 0:
                    return VhdlBeh2HlddBeh;
                case 1:
                    return VhdlBehDd2HlddBeh;
                case 2:
                    return HlddBeh2HlddRtl;
                default:
                    return PSL2THLDD;
            }
        }
    }

    public enum HLDDRepresentationType {
        FULL_TREE,
        REDUCED,
        MINIMIZED
    }
    
}
