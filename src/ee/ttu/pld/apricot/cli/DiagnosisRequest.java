package ee.ttu.pld.apricot.cli;

import org.w3c.dom.Node;

import java.util.List;

/**
 * @author Anton Chepurov
 */
public class DiagnosisRequest extends Request {

	public DiagnosisRequest(Node requestNode, String design) {
		super(requestNode, design);
	}

	@Override
	public void buildCommand(List<String> cmd) {
		cmd.add("-diagnosis");
		cmd.add(getHlddFile().getAbsolutePath().replace(".agm", ""));
	}
}
