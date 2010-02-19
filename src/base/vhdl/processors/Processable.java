package base.vhdl.processors;

/**
 * Interface for processing Nodes.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 08.10.2008
 * <br>Time: 23:20:53
 */
public interface Processable {

    public void process(AbstractProcessor processor) throws Exception;

}
