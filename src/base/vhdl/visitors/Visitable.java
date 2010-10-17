package base.vhdl.visitors;

/**
 * @author Anton Chepurov
 */
public interface Visitable {

	public void traverse(AbstractVisitor visitor) throws Exception;

}
