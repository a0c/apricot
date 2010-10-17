package ee.ttu.pld.apricot.verifier;

import ui.ConverterSettings;

import java.io.File;
import java.io.IOException;

/**
 * @author Anton Chepurov
 */
public class Design {
	static final String NEW_DESIGN_DIR = "verif";

	private final File hlddFile;
	private final File newDesignFile;
	private final ConverterSettings settings;
	private final File newMapFile;

	public Design(File hlddFile, ConverterSettings settings) {
		this.hlddFile = hlddFile;
		this.settings = settings;
		this.newDesignFile = new File(new File(hlddFile.getParent(), NEW_DESIGN_DIR), hlddFile.getName());
		File mapFile = settings.getMapFile();
		this.newMapFile = mapFile != null
				? new File(new File(hlddFile.getParent(), NEW_DESIGN_DIR), mapFile.getName()) : null;
	}

	public File getHlddFile() {
		return hlddFile;
	}

	public File getNewDesignFile() {
		return newDesignFile;
	}

	public File getNewMapFile() {
		return newMapFile;
	}

	public ConverterSettings getSettings() {
		return settings;
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	public void createNewDesignDir() throws IOException {
		newDesignFile.getParentFile().mkdir();
	}

}
