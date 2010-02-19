package base.vhdl.visitors;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 22:56:52
 */
public interface Visitable {

    public void traverse(AbstractVisitor visitor) throws Exception;

}
