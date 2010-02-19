package ui;


import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 16.02.2010
 * <br>Time: 17:05:42
 */
public class ConfigurationHandler {

	private final static Logger LOG = Logger.getLogger(ConfigurationHandler.class.getName());

	public static final String CONFIGURATION_FILE_PATH = "configuration_file_path";
	public static final String SOURCE_FILE_PATH = "source_file_path";
	public static final String STATE_NAME = "state_name";


	private static boolean s_checkStateVariableName = true;
	private static Properties properties;

	public static void load(File sourceFile) throws ExtendedException {
		properties = getProperties();

		properties.setProperty(SOURCE_FILE_PATH, sourceFile.getAbsolutePath());

		File propFile = BusinessLogic.deriveFileFrom(sourceFile, ".vhd", ".config");

		if (propFile == null) {
			try {
				propFile = BusinessLogic.deriveFileFrom(sourceFile, ".vhdl", ".config");
			} catch (RuntimeException e) { // do nothing, leave propFile == null
			}
		}

		if (propFile != null) {

			properties.setProperty(CONFIGURATION_FILE_PATH, propFile.getAbsolutePath());

			try {

				properties.load(new BufferedReader(new FileReader(propFile)));

			} catch (IOException e) {
				LOG.info("Error while reading configuration: " + e.getMessage());
				throw new RuntimeException("Error while reading configuration file: " + e.getMessage());
			}
		} else {
			LOG.info("Configuration file is missing.");
		}

		// empty properties, in case propFile == null

		validate();
	}

	public static void validate() throws ExtendedException {
		StringBuilder message;

		if (properties == null) {
			return;
		}

		/* Check STATE variable name availability */
		String stateVarName = properties.getProperty(STATE_NAME);
		if (s_checkStateVariableName && (stateVarName == null || stateVarName.length() == 0)) {
			LOG.info("STATE variable/signal name is not defined.");
			message = new StringBuilder("STATE variable/signal name is not defined.");
			String propFilePath = properties.getProperty(CONFIGURATION_FILE_PATH);
			String inFile = " ";
			if (propFilePath == null) {
				message.append("\n\nReason: configuration file not found");
				LOG.info("Configuration file not found");
				File propFile = new File(properties.getProperty(SOURCE_FILE_PATH));
				String propName = propFile.getName();
				propFile = new File(propFile.getParentFile(), propName.substring(0, propName.lastIndexOf(".")) + ".config");
				inFile = " in file:\n" + propFile.getAbsolutePath() + "\n";
			} else {
				message.append("\n\nReason: configuration file doesn't contain mapping with key \"").append(STATE_NAME).append("\"");
				LOG.info("Configuration file doesn't contain mapping with key " + STATE_NAME);
			}
			message.append("\n\nIf you don't want to convert this HLDD (Beh) to RTL HLDD later,").
					append("\nyou can proceed without defining STATE variable name.").
					append("\nOtherwise define it by key \"").append(STATE_NAME).append("\"").append(inFile).append("and re-run the converter.");
			throw new ExtendedException(message.toString(), ExtendedException.UNDEFINED_STATE_VAR_NAME_TEXT);
		}
	}

	public static String getStateVarName() {
		return getProperties().getProperty(STATE_NAME);
	}

	private static Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
		}
		return properties;
	}

	public static void setStateVarName(String stateVarName) {
		getProperties().setProperty(STATE_NAME, stateVarName);
	}

	public static void reset() {
		setCheckStateVarName(true);
		properties = new Properties();
	}

	public static void setCheckStateVarName(boolean checkStateVarName) {
		s_checkStateVariableName = checkStateVarName;
	}
}
