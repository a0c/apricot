package ui;

import java.io.File;
import static ui.BusinessLogic.ParserID.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.05.2010
 * <br>Time: 9:57:53
 */
public class ConverterSettings {
	
	private BusinessLogic.ParserID parserId;
	private File sourceFile;
	private File pslFile;
	private File baseModelFile;
	private boolean doReuseConstants;
	private boolean doSimplify;
	private boolean doFlattenConditions;
	private boolean doCreateGraphsForCS;
	private boolean doCreateExtraGraphsForCS;
	private BusinessLogic.HLDDRepresentationType hlddType;

	public void setParserId(BusinessLogic.ParserID parserId) {
		this.parserId = parserId;
	}

	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}

	public void setPslFile(File pslFile) {
		this.pslFile = pslFile;
	}

	public void setBaseModelFile(File baseModelFile) {
		this.baseModelFile = baseModelFile;
	}

	public void setDoReuseConstants(boolean doReuseConstants) {
		this.doReuseConstants = doReuseConstants;
	}

	public void setDoSimplify(boolean doSimplify) {
		this.doSimplify = doSimplify;
	}

	public void setDoFlattenConditions(boolean doFlattenConditions) {
		this.doFlattenConditions = doFlattenConditions;
	}

	public void setDoCreateGraphsForCS(boolean doCreateGraphsForCS) {
		this.doCreateGraphsForCS = doCreateGraphsForCS;
	}

	public void setDoCreateExtraGraphsForCS(boolean doCreateExtraGraphs) {
		this.doCreateExtraGraphsForCS = doCreateExtraGraphs;
	}

	public void setHlddType(BusinessLogic.HLDDRepresentationType hlddType) {
		this.hlddType = hlddType;
	}

	public BusinessLogic.ParserID getParserId() {
		return parserId;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public File getPslFile() {
		return pslFile;
	}

	public File getBaseModelFile() {
		return baseModelFile;
	}

	public boolean isDoReuseConstants() {
		return doReuseConstants;
	}

	public boolean isDoSimplify() {
		return doSimplify;
	}

	public boolean isDoFlattenConditions() {
		return doFlattenConditions;
	}

	public boolean isDoCreateGraphsForCS() {
		return doCreateGraphsForCS;
	}

	public boolean isDoCreateExtraGraphsForCS() {
		return doCreateExtraGraphsForCS;
	}

	public BusinessLogic.HLDDRepresentationType getHlddType() {
		return hlddType;
	}

	public void validate() throws ExtendedException {
		/* Check files */
		String message;
		if (sourceFile == null || pslFile == null) {
			message = parserId == PSL2THLDD
					? "Either Library or PSL file is missing"
					: "Either source or destination file is missing";
			throw new ExtendedException(message, ExtendedException.MISSING_FILES_TEXT);
		}
		if (parserId == PSL2THLDD && baseModelFile == null) {
			message = "Base HLDD model file is missing";
			throw new ExtendedException(message, ExtendedException.MISSING_FILE_TEXT);
		}

	}
}
