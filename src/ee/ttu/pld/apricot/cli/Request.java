package ee.ttu.pld.apricot.cli;

import org.w3c.dom.Node;

/**
 * @author Anton Chepurov
 */
public abstract class Request {

	private boolean successful = false;

	public abstract Node getRequestNode();

	public void markSuccessful() {
		successful = true;
	}

	public boolean isSuccessful() {
		return successful;
	}
}
