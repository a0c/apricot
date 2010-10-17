package base.hldd.visitors;

/**
 * @author Anton Chepurov
 */
public interface Visitable {

	public void traverse(HLDDVisitor visitor) throws Exception;

}
