package edu.umd.cs.mtc;

/**
 * Listener interface for objects that want to be notified by
 * {@link MultithreadedTestCase} whenever the clock advances.
 * 
 * @see MultithreadedTestCase
 * 
 * @author Tomas Pollak
 * @since 1.02
 */
public interface TickListener {

	/**
	 * Invoked whenever the clock advances. This method is invoked from the
	 * clock thread. This method should not block (either attempting to acquire
	 * locks or with I/O). Ideally, it should only perform fast in-memory
	 * operations. Any expensive operations should be offloaded to other
	 * threads.
	 * 
	 * @param tick
	 *            The new value of the clock tick.
	 */
	public void notifyTick(long tick);
}
