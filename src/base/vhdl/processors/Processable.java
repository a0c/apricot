package base.vhdl.processors;

/**
 * Interface for processing Nodes.
 *
 * @author Anton Chepurov
 */
public interface Processable {

	public void process(AbstractProcessor processor) throws Exception;

}
