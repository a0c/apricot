package ui;


import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Anton Chepurov
 */
public class ConfigurationHandler {

	private final static Logger LOGGER = Logger.getLogger(ConfigurationHandler.class.getName());

	public static final String CONFIGURATION_FILE_PATH = "configuration_file_path";
	public static final String SOURCE_FILE_PATH = "source_file_path";
	public static final String STATE_NAME = "state_name";


	private final Properties properties;

	private ConfigurationHandler(Properties properties) {
		this.properties = properties;
	}

	public static ConfigurationHandler loadConfiguration(File sourceFile) {
		Properties properties = new Properties();

		properties.setProperty(SOURCE_FILE_PATH, sourceFile.getAbsolutePath());

		File propFile = null;
		try {
			propFile = BusinessLogic.deriveFileFrom(sourceFile, ".vhd", ".config");
		} catch (RuntimeException e) { // do nothing, leave propFile == null
		}

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
				LOGGER.info("Error while reading configuration: " + e.getMessage());
				throw new RuntimeException("Error while reading configuration file: " + e.getMessage());
			}
		} else {
			LOGGER.info("Configuration file is missing.");
		}

		// empty properties, in case propFile == null
		return new ConfigurationHandler(properties);
	}

	public String getStateVarName() {
		return properties.getProperty(STATE_NAME);
	}

	public void setStateVarName(String stateVarName) {
		properties.setProperty(STATE_NAME, stateVarName);
	}

	/**
	 * This method should not be used. State signals/variables must be rather detected with analysis of their usages.
	 * todo: A different visitor should be implemented.
	 * Moreover, state-flag is only required in pure RTL graphs, where there is a Control part and DataPath part.
	 * Yes, but the flag itself should be added by VHDL2HLDD converter :)
	 *
	 * @param name signal/variable name to check for being state
	 * @return whether this is a state name
	 */
	public boolean isStateName(String name) {
		String stateName = getStateVarName();
		return stateName != null && stateName.equalsIgnoreCase(name);
	}
}
