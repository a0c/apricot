package ui;

import base.hldd.structure.models.BehModel;
import io.ConsoleWriter;
import ui.utils.ConvertingWorker;
import ui.utils.UIWorkerFinalizerImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Anton Chepurov
 */
public class BusinessLogic {

	private final ApplicationForm applicationForm;
	private final ConsoleWriter consoleWriter;

	private BehModel model;

	private String comment;

	private ConverterSettings settings;
	private File baseModelFile = null;
	private File rtlBehFile;
	private File behDDVhdlFile;
	private File behVhdlFile;
	private File ppgLibFile;
	private File pslFile;
	private File rtlRtlFile;
	private File behDDHlddFile;
	private File behHlddFile;

	public BusinessLogic(ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
		this.applicationForm = applicationForm;
		this.consoleWriter = consoleWriter;
	}

	public void processParse() throws ExtendedException {
		/* Receive data from form */
		ConverterSettings.Builder settingsBuilder = new ConverterSettings.Builder(getParserId(), getSourceFile(), getDestinationFile());
		settingsBuilder.setBaseModelFile(baseModelFile);
		settingsBuilder.setDoSimplify(applicationForm.shouldSimplify());
		settingsBuilder.setDoFlattenConditions(applicationForm.shouldFlattenCS());
		settingsBuilder.setDoCreateCSGraphs(applicationForm.shouldCreateCSGraphs());
		settingsBuilder.setDoCreateExtraCSGraphs(applicationForm.shouldCreateExtraCSGraphs());
		settingsBuilder.setHlddType(applicationForm.getHlddRepresentationType());
		settings = settingsBuilder.build();

		/* Perform PARSING and CONVERSIONS in a separate thread */
		new ConvertingWorker(new UIWorkerFinalizerImpl(this, consoleWriter), consoleWriter, settings, null).execute();

	}

	public void reset() {
		// Reset COMMENT
		comment = null;
	}

	public void addComment(String comment) {
		if (comment == null) {
			return;
		}
		if (this.comment == null) {
			this.comment = comment;
		} else {
			this.comment += comment;
		}
	}

	public void saveModel() throws ExtendedException {

		/* Save model to file */
		try {

			File outputFile = getOutputFile();

			model.toFile(new FileOutputStream(outputFile), comment, settings);

			consoleWriter.writeLn("Model saved to: " + outputFile.getAbsolutePath());

		} catch (IOException e) {
			String message = "Error while saving file:\n" + e.getMessage();
			throw new ExtendedException(message, ExtendedException.ERROR_TEXT);
		}
	}

	private File getOutputFile() {
		if (getParserId() == ParserID.PSL2THLDD) {
			return new File(baseModelFile.getAbsolutePath().replace(".agm", ".tgm"));
		}
		return getDestinationFile();
	}

	public String getProposedFileName() {
		File destinationFile = getDestinationFile();
		if (destinationFile != null) return destinationFile.getAbsolutePath();
		String proposedFileName = null;
		String sourceName;
		File sourceFile = getSourceFile();
		ParserID selectedParserId = getParserId();
		switch (selectedParserId) {
			case VhdlBeh2HlddBeh:
			case VhdlBehDd2HlddBeh:
				if (sourceFile == null) {
					return null;
				}
				sourceName = sourceFile.getAbsolutePath();
				proposedFileName = sourceName.substring(0, sourceName.lastIndexOf(".") + 1) + "agm";
				break;
			case HlddBeh2HlddRtl:
				if (sourceFile == null) {
					return null;
				}
				sourceName = sourceFile.getAbsolutePath();
				proposedFileName = sourceName.substring(0, sourceName.lastIndexOf(".")) + "_RTL.agm";
				break;
			case PSL2THLDD:
				if (baseModelFile == null) {
					return null;
				}
				sourceName = baseModelFile.getAbsolutePath();
				proposedFileName = sourceName.substring(0, sourceName.lastIndexOf(".") + 1) + "psl";
		}
		return proposedFileName;
	}

	private ParserID getParserId() {
		return applicationForm.getSelectedParserId();
	}

	public ApplicationForm getApplicationForm() {
		return applicationForm;
	}

	public void setModel(BehModel model) {
		this.model = model;
	}

	public void setBaseModelFile(File baseModelFile) {
		this.baseModelFile = baseModelFile;
	}

	public File getSourceFile() {
		ParserID parserId = getParserId();
		switch (parserId) {
			case VhdlBeh2HlddBeh:
				return behVhdlFile;
			case VhdlBehDd2HlddBeh:
				return behDDVhdlFile;
			case HlddBeh2HlddRtl:
				return rtlBehFile;
			case PSL2THLDD:
				return ppgLibFile;
			default:
				throw new RuntimeException("Cannot obtain SOURCE FILE for specified ParserID: " + parserId);
		}
	}

	public File getDestinationFile() {
		ParserID parserId = getParserId();
		switch (parserId) {
			case VhdlBeh2HlddBeh:
				return behHlddFile;
			case VhdlBehDd2HlddBeh:
				return behDDHlddFile;
			case HlddBeh2HlddRtl:
				return rtlRtlFile;
			case PSL2THLDD:
				return pslFile;
			default:
				throw new RuntimeException("Cannot obtain DESTINATION FILE for specified ParserID: " + parserId);
		}
	}

	public void doLoadHlddGraph() {
		applicationForm.doLoadHlddGraph(FileDependencyResolver.derivePngFile(getDestinationFile()));
	}

	public void setRtlBehFile(File rtlBehFile) {
		this.rtlBehFile = rtlBehFile;
	}

	public void setBehDDVhdlFile(File behDDVhdlFile) {
		this.behDDVhdlFile = behDDVhdlFile;
	}

	public void setBehVhdlFile(File behVhdlFile) {
		this.behVhdlFile = behVhdlFile;
	}

	public void setPpgLibFile(File ppgLibFile) {
		this.ppgLibFile = ppgLibFile;
	}

	public void setPslFile(File pslFile) {
		this.pslFile = pslFile;
	}

	public void setRtlRtlFile(File rtlRtlFile) {
		this.rtlRtlFile = rtlRtlFile;
	}

	public void setBehDDHlddFile(File behDDHlddFile) {
		this.behDDHlddFile = behDDHlddFile;
	}

	public void setBehHlddFile(File behHlddFile) {
		this.behHlddFile = behHlddFile;
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
	}

	@SuppressWarnings({"EnumeratedConstantNamingConvention"})
	public enum HLDDRepresentationType {
		FULL_TREE_4_RTL,
		FULL_TREE,
		REDUCED,
		MINIMIZED
	}

}
