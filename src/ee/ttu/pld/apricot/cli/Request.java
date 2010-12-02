package ee.ttu.pld.apricot.cli;

import org.w3c.dom.Node;
import ui.FileDependencyResolver;

import java.io.File;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public abstract class Request {

	private final Node requestNode;

	private final File designFile;

	private boolean successful = false;

	public Request(Node requestNode, String design) {
		this.requestNode = requestNode;
		design = new File(design).toURI().getPath();
		designFile = new File(design);
	}

	public Node getRequestNode() {
		return requestNode;
	}

	public void markSuccessful() {
		successful = true;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public boolean isBroken() {
		return !designFile.exists();
	}

	public void printBroken() {
		System.out.println("[APRICOT] ### ERROR ###: specified design file does not exist: " + designFile.getAbsolutePath());
	}

	public File getHlddFile() {
		return FileDependencyResolver.deriveHlddFile(designFile);
	}

	public abstract void buildCommand(List<String> cmd);
}
