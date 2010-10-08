package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static ui.BusinessLogic.ParserID.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.05.2010
 * <br>Time: 9:57:53
 */
public class ConverterSettings {
	
	public static final String PARSE_FAILED_FOR = "ConverterSettings parse failed for ";
	public static final String VALIDATION_FAILED_FOR = "ConverterSettings validation failed for ";
	public static final String PARSER_ID_IS_NULL = "Mandatory field ParserId is null.";
	public static final String MISSING_LIBRARY_FILE = "Library file is missing";
	public static final String MISSING_SOURCE_FILE = "Source file is missing";
	public static final String MISSING_PSL_FILE = "PSL file is missing";
	public static final String MISSING_DESTINATION_FILE = "Destination/PSL file is missing";
	public static final String MISSING_BASE_HLDD_MODEL_FILE = "Base HLDD model file is missing";
	public static final String HLDD_TYPE_IS_NULL = "Mandatory field hlddType is null.";

	/* Immutable fields */
	private final BusinessLogic.ParserID parserId;
	private final File sourceFile;
	private final File pslFile;
	private final File baseModelFile;
	private final File mapFile;
	private final boolean doSimplify;
	private final boolean doFlattenConditions;
	private final boolean doCreateCSGraphs;
	private final boolean doCreateExtraCSGraphs;
	private final BusinessLogic.HLDDRepresentationType hlddType;
	/* Mutable fields */
	private OutputStream mapFileStream;

	private ConverterSettings(Builder builder) {
		parserId = builder.parserId;
		sourceFile = builder.sourceFile;
		pslFile = builder.pslFile;
		baseModelFile = builder.baseModelFile;
		mapFile = builder.mapFile;
		doSimplify = builder.doSimplify;
		doFlattenConditions = builder.doFlattenConditions;
		doCreateCSGraphs = builder.doCreateCSGraphs;
		doCreateExtraCSGraphs = builder.doCreateExtraCSGraphs;
		hlddType = builder.hlddType;
	}

	public void setMapFileStream(OutputStream mapFileStream) {
		this.mapFileStream = mapFileStream;
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

	public File getMapFile() {
		return mapFile;
	}

	public boolean isDoSimplify() {
		return doSimplify;
	}

	/**
	 * @return whether to inline Composite conditions into a set of Control Nodes (true value),
	 * 		or create a single node function (false value)
	 */
	public boolean isDoFlattenConditions() {
		return doFlattenConditions;
	}

	public boolean isDoCreateCSGraphs() {
		return doCreateCSGraphs;
	}

	public boolean isDoCreateExtraCSGraphs() {
		return doCreateExtraCSGraphs;
	}

	public BusinessLogic.HLDDRepresentationType getHlddType() {
		return hlddType;
	}

	public OutputStream getMapFileStream() throws ExtendedException {
		if (!Builder.isMapFileAllowed(parserId)) {
			return null;
		}
		if (mapFileStream == null) {
			mapFileStream = generateMapFileStream();
		}
		return mapFileStream;
	}

	private OutputStream generateMapFileStream() throws ExtendedException {
		try {
			return new FileOutputStream(mapFile);
		} catch (FileNotFoundException e) {
			throw new ExtendedException("Could not generate map file stream", ExtendedException.FILE_NOT_FOUND_TEXT);
		}
	}

	public static ConverterSettings parse(String filePath) throws ConverterSettingsParseException {
		final File originalFile = new File(filePath);
		String fileName = originalFile.getName().toUpperCase();
		File baseModelFile = null;
		File sourceFile = null;

		BusinessLogic.ParserID parserId;

		if (fileName.endsWith(".AGM")) {
			parserId = hasAmongstParents("trees", originalFile) ? VhdlBehDd2HlddBeh : VhdlBeh2HlddBeh;
		} else if (fileName.endsWith(".TGM")) {
			parserId = PSL2THLDD;
			baseModelFile = new File(originalFile.getAbsolutePath().replaceFirst(".tgm$", ".agm"));
			sourceFile = new File(originalFile.getAbsolutePath().replaceFirst(".tgm$", ".psl"));
		} else {
			throw new ConverterSettingsParseException(PARSE_FAILED_FOR + filePath
					+ ": illegal extension. Expected: '.agm' or '.tgm'");
		}

		boolean doSimplify = false;
		boolean doFlattenConditions =  false;
		boolean doCreateCSGraphs = false;
		boolean doCreateExCSGraphs = false;
		BusinessLogic.HLDDRepresentationType hlddType = null;

		if (parserId != PSL2THLDD) {
			fileName = cutFromEnd(fileName, ".AGM"); // cut extension
			// parse RTL
			if (fileName.endsWith("_RTL")) {
				parserId = BusinessLogic.ParserID.HlddBeh2HlddRtl;
				fileName = cutFromEnd(fileName, "_RTL"); // cut RTL
				sourceFile = new File(originalFile.getParent(), fileName + ".agm");
			}
			// parse Conditional Statements
			if (fileName.endsWith("_FU")) {
				// do nothing (no such flag in ConverterSettings)
				fileName = cutFromEnd(fileName, "_FU");
			} else if (fileName.endsWith("_GR")) {
				doCreateCSGraphs = true;
				fileName = cutFromEnd(fileName, "_GR");
			} else if (fileName.endsWith("_FL")) {
				doFlattenConditions = true;
				fileName = cutFromEnd(fileName, "_FL");
			} else if (fileName.endsWith("_EX")) {
				doCreateExCSGraphs = true;
				fileName = cutFromEnd(fileName, "_EX");
			} else {
				throw new ConverterSettingsParseException(PARSE_FAILED_FOR + filePath
						+ ": unknown ConditionalStatements mode. Expected: '_FU', '_GR', '_FL' or '_EX'");
			}
			// parse Model Compactness
			if (fileName.endsWith("_F")) {
				hlddType = BusinessLogic.HLDDRepresentationType.FULL_TREE;
				fileName = cutFromEnd(fileName, "_F");
			} else if (fileName.endsWith("_M")) {
				hlddType = BusinessLogic.HLDDRepresentationType.MINIMIZED;
				fileName = cutFromEnd(fileName, "_M");
			} else if (fileName.endsWith("_R")) {
				hlddType = BusinessLogic.HLDDRepresentationType.REDUCED;
				fileName = cutFromEnd(fileName, "_R");
			} else {
				throw new ConverterSettingsParseException(PARSE_FAILED_FOR + filePath
						+ ": unknown Model Compactness mode. Expected: '_F', '_M' or '_R'");
			}

			if (sourceFile == null) {
				sourceFile = new File(originalFile.getParent(), fileName + ".vhd");
			}
		}

		Builder settingsBuilder = new Builder(parserId, sourceFile, originalFile);
		settingsBuilder.setBaseModelFile(baseModelFile);
		settingsBuilder.setDoSimplify(doSimplify);
		settingsBuilder.setDoFlattenConditions(doFlattenConditions);
		settingsBuilder.setDoCreateCSGraphs(doCreateCSGraphs);
		settingsBuilder.setDoCreateExtraCSGraphs(doCreateExCSGraphs);
		settingsBuilder.setHlddType(hlddType);

		ConverterSettings settings;
		try {
			settings = settingsBuilder.build();
		} catch (ExtendedException e) {
			/* Should not happen */
			throw new RuntimeException(VALIDATION_FAILED_FOR + filePath
					+ ": " + e.getTitle() + ": " + e.getMessage());
		}
		return settings;
	}

	static boolean hasAmongstParents(String parentDirName, File originalFile) {
		File file = originalFile;
		while ((file = file.getParentFile()) != null) {
			if (file.getName().equals(parentDirName)) {
				return true;
			}
		}
		return false;
	}

	private static String cutFromEnd(String fileName, String postfix) {
		return fileName.substring(0, fileName.length() - postfix.length());
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("ConverterSettings");
		sb.append("{parserId=").append(parserId);
		sb.append(", sourceFile=").append(sourceFile);
		sb.append(", pslFile=").append(pslFile);
		sb.append(", baseModelFile=").append(baseModelFile);
		sb.append(", mapFile=").append(mapFile);
		sb.append(", doSimplify=").append(doSimplify);
		sb.append(", doFlattenConditions=").append(doFlattenConditions);
		sb.append(", doCreateCSGraphs=").append(doCreateCSGraphs);
		sb.append(", doCreateExtraCSGraphs=").append(doCreateExtraCSGraphs);
		sb.append(", hlddType=").append(hlddType);
		sb.append(", mapFileStream=").append(mapFileStream);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		ConverterSettings settings = (ConverterSettings) obj;

		if (doCreateCSGraphs != settings.doCreateCSGraphs) return false;
		if (doCreateExtraCSGraphs != settings.doCreateExtraCSGraphs) return false;
		if (doFlattenConditions != settings.doFlattenConditions) return false;
		if (doSimplify != settings.doSimplify) return false;
		if (baseModelFile != null ? !baseModelFile.equals(settings.baseModelFile) : settings.baseModelFile != null)
			return false;
		if (hlddType != settings.hlddType) return false;
		if (mapFile != null ? !mapFile.equals(settings.mapFile) : settings.mapFile != null) return false;
		if (mapFileStream != null ? !mapFileStream.equals(settings.mapFileStream) : settings.mapFileStream != null)
			return false;
		if (parserId != settings.parserId) return false;
		if (pslFile != null ? !pslFile.equals(settings.pslFile) : settings.pslFile != null) return false;
		//noinspection RedundantIfStatement
		if (sourceFile != null ? !sourceFile.equals(settings.sourceFile) : settings.sourceFile != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = parserId != null ? parserId.hashCode() : 0;
		result = 31 * result + (sourceFile != null ? sourceFile.hashCode() : 0);
		result = 31 * result + (pslFile != null ? pslFile.hashCode() : 0);
		result = 31 * result + (baseModelFile != null ? baseModelFile.hashCode() : 0);
		result = 31 * result + (mapFile != null ? mapFile.hashCode() : 0);
		result = 31 * result + (doSimplify ? 1 : 0);
		result = 31 * result + (doFlattenConditions ? 1 : 0);
		result = 31 * result + (doCreateCSGraphs ? 1 : 0);
		result = 31 * result + (doCreateExtraCSGraphs ? 1 : 0);
		result = 31 * result + (hlddType != null ? hlddType.hashCode() : 0);
		result = 31 * result + (mapFileStream != null ? mapFileStream.hashCode() : 0);
		return result;
	}

	public static class ConverterSettingsParseException extends Exception {
		public ConverterSettingsParseException(String message) {
			super(message);
		}
	}

	public static class Builder {
		// mandatory fields
		private final BusinessLogic.ParserID parserId;
		private File sourceFile;
		private final File pslFile;
		// optional fields
		private File baseModelFile;
		private File mapFile;
		private boolean doSimplify;
		private boolean doFlattenConditions;
		private boolean doCreateCSGraphs;
		private boolean doCreateExtraCSGraphs;
		private BusinessLogic.HLDDRepresentationType hlddType;

		public Builder(BusinessLogic.ParserID parserId, File sourceFile, File pslFile) {
			this.parserId = parserId;
			this.sourceFile = sourceFile;
			this.pslFile = pslFile;
		}

		public Builder(ConverterSettings defaultSettings) {
			this(defaultSettings.parserId, defaultSettings.sourceFile, defaultSettings.pslFile);
			setBaseModelFile(defaultSettings.baseModelFile);
			setDoSimplify(defaultSettings.doSimplify);
			setDoFlattenConditions(defaultSettings.doFlattenConditions);
			setDoCreateCSGraphs(defaultSettings.doCreateCSGraphs);
			setDoCreateExtraCSGraphs(defaultSettings.doCreateExtraCSGraphs);
			setHlddType(defaultSettings.hlddType);
		}

		public ConverterSettings build() throws ExtendedException {
			validate();
			// generate Map file
			if (isMapFileAllowed(parserId)) {
				mapFile = new File(pslFile.getAbsolutePath().replaceFirst(".agm$", ".map"));
			}

			return new ConverterSettings(this);
		}

		private static boolean isMapFileAllowed(final BusinessLogic.ParserID parserId) {
			return parserId == VhdlBeh2HlddBeh || parserId == VhdlBehDd2HlddBeh;
		}

		public Builder setSourceFile(File sourceFile) {
			this.sourceFile = sourceFile;
			return this;
		}

		public Builder setBaseModelFile(File baseModelFile) {
			this.baseModelFile = baseModelFile;
			return this;
		}

		public Builder setDoSimplify(boolean doSimplify) {
			this.doSimplify = doSimplify;
			return this;
		}

		public Builder setDoFlattenConditions(boolean doFlattenConditions) {
			this.doFlattenConditions = doFlattenConditions;
			return this;
		}

		public Builder setDoCreateCSGraphs(boolean doCreateCSGraphs) {
			this.doCreateCSGraphs = doCreateCSGraphs;
			return this;
		}

		public Builder setDoCreateExtraCSGraphs(boolean doCreateExtraGraphs) {
			this.doCreateExtraCSGraphs = doCreateExtraGraphs;
			return this;
		}

		public Builder setHlddType(BusinessLogic.HLDDRepresentationType hlddType) {
			this.hlddType = hlddType;
			return this;
		}

		private void validate() throws ExtendedException {
			/* Check MANDATORY field parserId */
			if (parserId == null) {
				throw new ExtendedException(PARSER_ID_IS_NULL, "Converter is not selected (parserId == null)");
			}
			/* Check files */
			String message;
			if (sourceFile == null) {
				message = parserId == PSL2THLDD
						? MISSING_LIBRARY_FILE
						: MISSING_SOURCE_FILE;
				throw new ExtendedException(message, ExtendedException.MISSING_FILE_TEXT);
			}
			if (pslFile == null) {
				message = parserId == PSL2THLDD
						? MISSING_PSL_FILE
						: MISSING_DESTINATION_FILE;
				throw new ExtendedException(message, ExtendedException.MISSING_FILE_TEXT);
			}
			if (parserId == PSL2THLDD && baseModelFile == null) {
				throw new ExtendedException(MISSING_BASE_HLDD_MODEL_FILE, ExtendedException.MISSING_FILE_TEXT);
			}
			/* Check HLDDType */
			if (parserId == VhdlBeh2HlddBeh || parserId == VhdlBehDd2HlddBeh) {
				if (hlddType == null) {
					throw new ExtendedException(HLDD_TYPE_IS_NULL, "Model Compactness mode is not selected (hlddType == null)");
				}
			}
		}
	}
}
