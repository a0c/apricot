package ee.ttu.pld.apricot.cli;

import org.w3c.dom.Node;

/**
 * @author Anton Chepurov
 */
@SuppressWarnings({"InterfaceNamingConvention"})
public interface Request {

	public Node getRequestNode();
}
