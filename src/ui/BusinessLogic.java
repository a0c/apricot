package ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import base.hldd.structure.models.BehModel;
import io.ConsoleWriter;

import ui.utils.ConvertingWorker;
import ui.utils.UIWorkerFinalizerImpl;


/**
 * @author Anton Chepurov
 */
public class BusinessLogic {

	private final ApplicationForm applicationForm;
	private final ConsoleWriter consoleWriter;

	private File sourceFile = null;
	private File destinationFile = null;
	private File baseModelFile = null;

	private BehModel model;

	private String comment;

	private ConverterSettings settings;

	public BusinessLogic(ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
		this.applicationForm = applicationForm;
		this.consoleWriter = consoleWriter;
	}

	public void processParse() throws ExtendedException {
		/* Receive data from form */
		ConverterSettings.Builder settingsBuilder = new ConverterSettings.Builder(applicationForm.getSelectedParserId(), sourceFile, destinationFile);
		settingsBuilder.setBaseModelFile(baseModelFile);
		settingsBuilder.setDoSimplify(applicationForm.shouldSimplify());
		settingsBuilder.setDoFlattenConditions(applicationForm.shouldFlattenCS());
		settingsBuilder.setDoCreateCSGraphs(applicationForm.shouldCreateCSGraphs());
		settingsBuilder.setDoCreateExtraCSGraphs(applicationForm.shouldCreateExtraCSGraphs());
		settingsBuilder.setHlddType(applicationForm.getHlddRepresentationType());
		settings = settingsBuilder.build();

		/* Perform PARSING and CONVERSIONS in a separate thread */
		new ConvertingWorker(new UIWorkerFinalizerImpl(this, consoleWriter), consoleWriter, settings).execute();

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
			changeDestinationFile(".psl", ".tgm");

			model.toFile(new FileOutputStream(destinationFile), comment, settings);
			consoleWriter.writeLn("Model saved to: " + destinationFile.getAbsolutePath());
		} catch (IOException e) {
			String message = "Error while saving file:\n" + e.getMessage();
			throw new ExtendedException(message, ExtendedException.ERROR_TEXT);
		} finally {
			/* For PSL parser, change output file back from *.TGM into *.PSL */
			changeDestinationFile(".tgm", ".psl");
		}

	}

	private void changeDestinationFile(String from, String into) {
		if (applicationForm.getSelectedParserId() == ParserID.PSL2THLDD) {
			destinationFile = new File(destinationFile.getAbsolutePath().replace(from, into));
		}
	}

	public String getProposedFileName() {
		if (destinationFile != null) return destinationFile.getAbsolutePath();
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

	public void setDestinationFile(File destinationFile) {
		this.destinationFile = destinationFile;
	}

	public void setBaseModelFile(File baseModelFile) {
		this.baseModelFile = baseModelFile;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public void clearFiles() {
		sourceFile = null;
		destinationFile = null;
		baseModelFile = null;
	}

	public void doLoadHlddGraph() {
		applicationForm.doLoadHlddGraph(FileDependencyResolver.derivePngFile(destinationFile));
	}


	public enum ParserID {
		VhdlBeh2HlddBeh("VHDL Beh => HLDD Beh"), /* VHDL Beh => HLDD Beh */
		VhdlBehDd2HlddBeh("HIF => HLDD Beh"),	/* VHDL Beh DD => HLDD Beh */
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

	@SuppressWarnings({"EnumeratedConstantNamingConvention"})
	public enum HLDDRepresentationType {
		FULL_TREE,
		REDUCED,
		MINIMIZED
	}

}
