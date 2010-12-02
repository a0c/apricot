package ui;

import java.io.File;

/**
 * @author Anton Chepurov
 */
public class FileDependencyResolver {

	public static File deriveVhdlFile(File file) {

		File vhdlFile = null;

		if (isHLDD(file)) {
			ConverterSettings settings = ConverterSettings.loadSmartComment(file);

			if (settings != null) {

				vhdlFile = settings.getSourceFile();
			}
		}

		if (vhdlFile == null)
			vhdlFile = deriveFileFrom(file, "vhdl");

		if (vhdlFile == null)
			vhdlFile = deriveFileFrom(file, "vhd");

		return vhdlFile;
	}

	public static File deriveHlddFile(File file) {

		return deriveFileFrom(file, "agm");
	}

	public static File deriveDgnFile(File file) {

		return deriveFileFrom(file, "dgn");
	}

	public static File deriveCovFile(File file) {

		return deriveFileFrom(file, "cov");
	}

	public static File deriveTstFile(File file) {

		return deriveFileFrom(file, "tst");
	}

	public static File deriveBaseModelFile(File file) {

		return deriveHlddFile(file);
	}

	public static File deriveTgmFile(File file) {

		return deriveFileFrom(file, "tgm");
	}

	public static File deriveMapFile(File file) {

		return deriveFileFrom(file, "map");
	}

	//todo:...

	public static File derivePngFile(File file) {

		return deriveFileFrom(file, "png");
	}

	public static File deriveConfigFile(File file) {

		return deriveFileFrom(file, "config");
	}

	/**
	 * @param sourceFile		   file to derive from
	 * @param derivedFileExtension target extension
	 * @return derived file, if it exists, or <code>null</code> if it doesn't exist.
	 */
	public static File deriveFileFrom(File sourceFile, String derivedFileExtension) {
		String derivedPath = deriveFilePathFrom(sourceFile, derivedFileExtension);
		if (derivedPath != null) {
			File derivedFile = new File(derivedPath);
			return derivedFile.exists() ? derivedFile : null;
		}
		return null;
	}

	public static String deriveFilePathFrom(File sourceFile, String derivedExtension) {
		String sourcePath = sourceFile.getAbsolutePath();
		if (sourcePath.contains(".") && !sourcePath.endsWith(".")) {
			return sourcePath.substring(0, sourcePath.lastIndexOf(".") + 1) + derivedExtension;
		}
		return null;
	}

	private static boolean endsWith(File file, String suffix) {
		return file.getName().toLowerCase().endsWith(suffix);
	}

	public static boolean isVHDL(File file) {
		return endsWith(file, ".vhdl") || endsWith(file, ".vhd");
	}

	public static boolean isCOV(File file) {
		return endsWith(file, ".cov");
	}

	public static boolean isHLDD(File file) {
		return endsWith(file, ".agm");
	}

	public static boolean isPPG(File file) {
		return endsWith(file, ".lib");
	}

	public static boolean isWaveform(File file) {
		return isCHK(file) || isSIM(file) || isTST(file);
	}

	public static boolean isCHK(File file) {
		return endsWith(file, ".chk");
	}

	public static boolean isSIM(File file) {
		return endsWith(file, ".sim");
	}

	public static boolean isTST(File file) {
		return endsWith(file, ".tst");
	}

	public static boolean isPSL(File file) {
		return endsWith(file, ".psl");
	}

	public static boolean isDGN(File file) {
		return endsWith(file, ".dgn");
	}
}
