package ee.ttu.pld.apricot.cli;

import org.w3c.dom.Node;

import java.util.Collection;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public class CoverageRequest extends Request {

	private static final String NODE = "node";
	private static final String EDGE = "edge";
	private static final String CONDITION = "condition";
	private static final String TOGGLE = "toggle";

	private final Collection<String> metrics;

	public CoverageRequest(Node requestNode, String design, Collection<String> metrics) {
		super(requestNode, design);
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

	public boolean isNodeRequested() {
		return metrics.contains(NODE);
	}

	@Override
	public void buildCommand(List<String> cmd) {
		cmd.add("-coverage");
		cmd.add(getDirective());
		cmd.add(getHlddFile().getAbsolutePath().replace(".agm", ""));
	}
}
