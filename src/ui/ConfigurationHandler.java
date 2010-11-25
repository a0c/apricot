package ui;


import io.ConsoleWriter;

import java.io.BufferedReader;
import java.io.File;
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
	public static final String SOURCE_FILE_NAME = "source_file_name";
	public static final String STATE_NAME = "state_name";
	public static final String CLOCK_NAME = "clock_name";
	public static final String RESET_NAME = "reset_name";


	private final Properties properties;

	private ConfigurationHandler(Properties properties) {
		this.properties = properties;
	}

	public static ConfigurationHandler loadConfiguration(File sourceFile, ConsoleWriter consoleWriter) {
		Properties properties = new Properties();

		properties.setProperty(SOURCE_FILE_PATH, sourceFile.getAbsolutePath());
		properties.setProperty(SOURCE_FILE_NAME, sourceFile.getName());

		File propFile = FileDependencyResolver.deriveConfigFile(sourceFile);

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
			consoleWriter.writeLn("### WARNING ###  Configuration file is N/A for source " + properties.getProperty(SOURCE_FILE_NAME));
		}

		// empty properties, in case propFile == null
		ConfigurationHandler configurationHandler = new ConfigurationHandler(properties);
		configurationHandler.verify(consoleWriter);

		return configurationHandler;
	}

	private void verify(ConsoleWriter consoleWriter) {
		if (getStateVarName() == null) {
			consoleWriter.writeLn("### WARNING ###  STATE name is not set in config file of source " + properties.getProperty(SOURCE_FILE_NAME) +
					". Further RTL HLDD generation will be impossible.");
		}
		if (getClockName() == null) {
			consoleWriter.writeLn("### WARNING ###  CLOCK name is not set in config file of source " + properties.getProperty(SOURCE_FILE_NAME) +
					". D-flags might not be set correctly. Cyclic dependencies between signal assignments may occur.");
		}
		if (getResetName() == null) {
			consoleWriter.writeLn("### WARNING ###  RESET name is not set in config file of source " + properties.getProperty(SOURCE_FILE_NAME) +
					". Un-resettable graphs may become resettable.");
		}
	}

	public String getStateVarName() {
		return properties.getProperty(STATE_NAME);
	}

	public void setStateVarName(String stateVarName) {
		properties.setProperty(STATE_NAME, stateVarName);
	}

	private String getClockName() {
		return properties.getProperty(CLOCK_NAME);
	}

	private String getResetName() {
		return properties.getProperty(RESET_NAME);
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
		return areNamesEqual(getStateVarName(), name);
	}

	public boolean isClockName(String name) {
		return areNamesEqual(getClockName(), name);
	}

	public boolean isResetName(String name) {
		return areNamesEqual(getResetName(), name);
	}

	private boolean areNamesEqual(String targetName, String name) {
		return targetName != null && targetName.equalsIgnoreCase(name);
	}
}
