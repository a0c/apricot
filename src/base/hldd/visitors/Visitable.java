package base.hldd.visitors;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 14.02.2008
 * <br>Time: 23:39:07
 */
public interface Visitable {

    public void traverse(HLDDVisitor visitor) throws Exception;

}
