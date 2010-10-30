package ui;

/**
 * @author Anton Chepurov
 */
public interface Lockable {

	boolean isLocked();

	void lock();

	void unlock();
}
