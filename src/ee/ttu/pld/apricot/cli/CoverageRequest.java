package ee.ttu.pld.apricot.cli;

import org.w3c.dom.Node;
import ui.FileDependencyResolver;

import java.io.File;
import java.util.Collection;

/**
 * @author Anton Chepurov
 */
public class CoverageRequest implements Request {

	private static final String NODE = "node";
	private static final String EDGE = "edge";
	private static final String CONDITION = "condition";
	private static final String TOGGLE = "toggle";

	private final Node requestNode;

	private final File designFile;

	private final Collection<String> metrics;

	public CoverageRequest(Node requestNode, String design, Collection<String> metrics) {
		this.requestNode = requestNode;
		design = new File(design).toURI().getPath();
		this.designFile = new File(design);
		this.metrics = metrics;
	}

	public String getDirective() {
		StringBuilder sb = new StringBuilder();
		for (String metric : metrics) {
			if (metric.equals(NODE)) {
				sb.append("n");
			} else if (metric.equals(EDGE)) {
				sb.append("e");
			} else if (metric.equals(CONDITION)) {
				sb.append("c");
			} else if (metric.equals(TOGGLE)) {
				sb.append("t");
			}
		}
		return sb.toString();
	}

	public File getHlddFile() {
		return FileDependencyResolver.deriveHlddFile(designFile);
	}

	public boolean isNodeRequested() {
		return metrics.contains(NODE);
	}

	@Override
	public Node getRequestNode() {
		return requestNode;
	}

	public boolean isBroken() {
		return !designFile.exists();
	}

	public void printError() {
		System.out.println("ERROR: specified design file does not exist: " + designFile.getAbsolutePath());
	}
}
