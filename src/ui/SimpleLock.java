package ui;

/**
 * @author Anton Chepurov
 */
public class SimpleLock implements Lockable {

	boolean isLocked = false;

	@Override
	public boolean isLocked() {
		return isLocked;
	}

	@Override
	public void lock() {
		isLocked = true;
	}

	@Override
	public void unlock() {
		isLocked = false;
	}
}