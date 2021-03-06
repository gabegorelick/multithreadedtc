package edu.umd.cs.mtc;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is the base class for each test in the MultithreadedTC framework. To
 * create a multithreaded test case, simply extend this class. Any method with a
 * name that starts with "thread", that has no parameters and a void return type
 * is a thread method. Each thread method will be run in a seperate thread. This
 * class also defines {@link #initialize()} and {@link #finish()} methods you
 * can override.
 * 
 * <p>
 * A single run of a multithreaded test case consists of:
 * <ol>
 * <li>Running the {@link #initialize()} method
 * <li>Running each thread method in a seperate thread
 * <li>Running the {@link #finish()} method when all threads are done.
 * </ol>
 * 
 * <p>
 * The method {@link TestFramework#runOnce(MultithreadedTestCase)} can be used
 * to run a MultithreadedTestCase once. The method
 * {@link TestFramework#runManyTimes(MultithreadedTestCase, int)} can be used to
 * run a multithread test case multiple times (to see if different interleavings
 * produce different behaviors).
 * 
 * <p>
 * There are several additional methods you can use in designing test cases. The
 * MultithreadedTestCase maintains a metronome or clock, and ticks off
 * intervals. You can get the current tick with {@link #getTick()} and you can
 * wait until a particular tick with {@link #waitForTick(long)}. The metronome
 * isn't a free running clock; it only advances to the next tick when all
 * threads are blocked or waiting. Also, when all threads are blocked, if at
 * least one thread isn't waiting for the metronome to advance, the system
 * declares a deadlock to have occurred and terminates the test case (unless one
 * of the threads is in state TIMED_WAITING).
 * 
 * <p>
 * You can set a command line parameter -Dtunit.trace=true to cause tracing
 * messages to be printed by the metronome frame, or invoke
 * {@link MultithreadedTestCase#setTrace(boolean)} to turn tracing on or off.
 * 
 * <p>
 * You can set command line parameter -Dtunit.runLimit=10 to cause a test case
 * to fail if at least one thread stays in a runnable state for more than 10
 * seconds without becoming blocked or waiting for a metronome tick. Use
 * different values for shorter or longer time limits.
 * 
 * @see TestFramework
 * 
 * @author William Pugh
 * @author Nathaniel Ayewah
 * @since 1.0
 */
abstract public class MultithreadedTestCase {

	/**
	 * The metronome used to coordinate between threads. This clock is advanced
	 * by the clock thread started by {@link TestFramework}. The clock will not
	 * advance if it is frozen.
	 * 
	 * @see #waitForTick(long)
	 * @see #freezeClock()
	 * @see #unfreezeClock()
	 */
	private long clock;

	/**
	 * The primary lock to synchronize on in this test case before accessing
	 * fields in this class.
	 */
	Object lock = new Object();

	/**
	 * If true, the debugging information is printed to standard out while the
	 * test runs
	 */
	private boolean trace = Boolean.getBoolean("tunit.trace");

	/**
	 * This flag is set to true when a test fails due to deadlock or timeout.
	 * 
	 * @see TestFramework
	 */
	boolean failed;

	/**
	 * This method is invoked in a test run before any test threads have
	 * started. Subclasses can override this method to prevent calls to
	 * {@link #initialize()}.
	 * 
	 * @throws Exception
	 */
	public void onInitialize() throws Exception {
		initialize();
	}

	/**
	 * This method is invoked in a test run before any test threads have
	 * started.
	 * 
	 */
	public void initialize() throws Exception {
	}

	public void onFinish() throws Exception {
		finish();
	}

	/**
	 * This method is invoked in a test after after all test threads have
	 * finished.
	 * 
	 */
	public void finish() throws Exception {
	}

	/**
	 * @param trace
	 *            the trace to set
	 */
	public void setTrace(boolean trace) {
		this.trace = trace;
	}

	/**
	 * @return the trace
	 */
	public boolean getTrace() {
		return trace;
	}

	// =======================
	// -- Thread Management --
	// - - - - - - - - - - - -

	/**
	 * Map each thread to the clock tick it is waiting for.
	 */
	IdentityHashMap<Thread, Long> threads = new IdentityHashMap<Thread, Long>();

	/**
	 * ThreadLocal containing a reference to the current instance of this class
	 * for each thread. When a thread completes or dies, its reference to this
	 * class is removed.
	 */
	static ThreadLocal<MultithreadedTestCase> currentTestCase = new ThreadLocal<MultithreadedTestCase>();

	/**
	 * This method is called right after a new testcase thread is created by the
	 * {@link TestFramework}. It provides initial values for
	 * {@link #currentTestCase} and {@link #threads}.
	 */
	void hello() {
		currentTestCase.set(this);
		synchronized (lock) {
			Thread currentThread = Thread.currentThread();
			threads.put(currentThread, 0L);
		}

	}

	/**
	 * This method is called just before a testcase thread completes. It cleans
	 * out {@link #currentTestCase} and {@link #threads}.
	 */
	void goodbye() {
		synchronized (lock) {
			Thread currentThread = Thread.currentThread();
			threads.remove(currentThread);
		}
		currentTestCase.set(null);
	}

	/**
	 * Map a thread name to all test case threads as they are created, primarily
	 * so that they can be accessed by each other.
	 * 
	 * @see #getThreadByName(String)
	 * @see #getThread(int)
	 */
	HashMap<String, Thread> methodThreads = new HashMap<String, Thread>();

	/**
	 * Get a thread given the method name that it corresponds to. E.g. to get
	 * the thread running the contents of the method <code>thread1()</code>,
	 * call <code>getThreadByName("thread1")</code>
	 * 
	 * <p>
	 * NOTE: {@link #initialize()} is called before threads are created, so this
	 * method returns null if called from {@link #initialize()} (but not from
	 * {@link #finish()}).
	 * 
	 * @see #getThread(int)
	 * 
	 * @param methodName
	 *            the name of the method corresponding to the thread requested
	 * @return the thread corresponding to methodName
	 */
	public Thread getThreadByName(String methodName) {
		synchronized (lock) {
			return methodThreads.get(methodName);
		}
	}

	/**
	 * Get a thread corresponding to the method whose name is formed using the
	 * prefix "thread" followed by an integer (represented by <code>index</code>
	 * . e.g. getThread(1) returns the thread that <code>thread1()</code> is
	 * running in.
	 * 
	 * <p>
	 * NOTE: {@link #initialize()} is called before threads are created, so this
	 * method returns null if called from {@link #initialize()} (but not from
	 * {@link #finish()}).
	 * 
	 * @see #getThreadByName(String)
	 * 
	 * @param index
	 *            an integer following "thread" in the name of the method
	 * @return the Thread corresponding to this method
	 */
	public Thread getThread(int index) {
		return getThreadByName("thread" + index);
	}

	/**
	 * Associates a thread with given method name. If the method name is already
	 * associated with a Thread, the old thread is returned, otherwise null is
	 * returned
	 */
	public Thread putThread(String methodName, Thread t) {
		synchronized (lock) {
			return methodThreads.put(methodName, t);
		}
	}

	// ===========================
	// -- Clock tick management --
	// - - - - - - - - - - - - - -

	/**
	 * Force this thread to block until the thread metronome reaches the
	 * specified value, at which point the thread is unblocked.
	 * 
	 * @param c
	 *            the tick value to wait for
	 */
	public void waitForTick(long c) {
		synchronized (lock) {
			threads.put(Thread.currentThread(), c);
			while (!failed && clock < c)
				try {
					if (getTrace())
						System.out.println(Thread.currentThread().getName() + " is waiting for time " + c);
					lock.wait();
				} catch (InterruptedException e) {
					throw new AssertionError(e);
				}
			if (failed)
				throw new IllegalStateException("Clock never reached " + c);
			if (getTrace())
				System.out.println("Releasing " + Thread.currentThread().getName() + " at time " + clock);
		}
	}

	/**
	 * An Enum-based version of waitForTick. It simply looks up the ordinal and
	 * adds 1 to determine the clock tick to wait for.
	 * 
	 * @see #waitForTick(long)
	 * 
	 * @param e
	 *            An Enum representing the tick to wait for. The first
	 *            enumeration constant represents tick 1, the second is tick 2,
	 *            etc.
	 */
	public void waitForTick(Enum e) {
		waitForTick(e.ordinal() + 1);
	}

	/**
	 * Gets the current value of the thread metronome. Primarily useful in
	 * assert statements.
	 * 
	 * @see #assertTick(long)
	 * 
	 * @return the current tick value
	 */
	public long getTick() {
		synchronized (lock) {
			return clock;
		}
	}

	/**
	 * Advances the clock. To be invoked only by the {@link TestFramework}.
	 * 
	 * @see #getTick()
	 * 
	 * @param tick
	 *            The new clock tick value.
	 */
	void setTick(long tick) {
		long oldTick = clock;
		clock = tick;
		if (tick > oldTick) {
			notifyListeners(tick);
		}
	}

	/**
	 * Assert that the clock is in tick <code>tick</code>
	 * 
	 * @param tick
	 *            a number >= 0
	 */
	public void assertTick(long tick) {
		assertEquals(tick, getTick());
	}

	// =======================================
	// -- Components for freezing the clock --
	// - - - - - - - - - - - - - - - - - - - -

	/**
	 * Read locks are acquired when clock is frozen and must be released before
	 * the clock can advance in a waitForTick().
	 */
	final ReentrantReadWriteLock clockLock = new ReentrantReadWriteLock();

	/**
	 * When the clock is frozen, it will not advance even when all threads are
	 * blocked. Use this to block the current thread with a time limit, but
	 * prevent the clock from advancing due to a {@link #waitForTick(long)} in
	 * another thread. This statements that occur when clock is frozen should be
	 * followed by {@link #unfreezeClock()} in the same thread.
	 */
	public void freezeClock() {
		clockLock.readLock().lock();
	}

	/**
	 * Unfreeze a clock that has been frozen by {@link #freezeClock()}. Both
	 * methods must be called from the same thread.
	 */
	public void unfreezeClock() {
		clockLock.readLock().unlock();
	}

	/**
	 * Check if the clock has been frozen by any threads.
	 */
	public boolean isClockFrozen() {
		return clockLock.getReadLockCount() > 0;
	}

	// ===============================
	// -- Customized Wait Functions --
	// - - - - - - - - - - - - - - - -

	/**
	 * A boolean flag for each thread indicating whether the next call to
	 * {@link #waitOn(Object)} or {@link #awaitOn(Condition)} should return
	 * immediately.
	 * 
	 * @see #skipNextWait()
	 */
	private static ThreadLocal<Boolean> skipNextWait = new ThreadLocal<Boolean>() {
		@Override
		public Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	/**
	 * When this method is called from a thread, the next call to
	 * {@link #waitOn(Object)} or {@link #awaitOn(Condition)} will return
	 * immediately without blocking. Use this to make tests more robust.
	 */
	static public void skipNextWait() {
		skipNextWait.set(true);
	}

	/**
	 * This method is a replacement for {@link Object#wait()}. It suppresses the
	 * {@link InterruptedException} that you would otherwise have to deal with,
	 * and allows automated skipping of the next wait. The method
	 * {@link #skipNextWait()} will force that thread to immediately return from
	 * the next call to this method. Designing your tests so that they work even
	 * if {@link Object#wait()} occasionally returns immediately will make your
	 * code much more robust in face of several potential threading issues.
	 * 
	 * @param o
	 *            the object to wait on
	 */
	static public void waitOn(Object o) {
		// System.out.println("About to wait on " + System.identityHashCode(o));
		MultithreadedTestCase thisTestCase = currentTestCase.get();
		if (thisTestCase != null && thisTestCase.failed)
			throw new RuntimeException("Test case has failed");
		if (skipNextWait.get()) {
			skipNextWait.set(false);
			return;
		}
		try {
			o.wait(3000);
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		} catch (IllegalMonitorStateException e) {
			System.out.println("Got illegal monitor state exception");
		}
		if (thisTestCase != null && thisTestCase.failed)
			throw new RuntimeException("Test case has failed");
		// System.out.println("waited on " + System.identityHashCode(o));
	}

	/**
	 * This method is a replacement for {@link Condition#await()}. It suppresses
	 * the {@link InterruptedException} that you would otherwise have to deal
	 * with, and allows automated skipping of the next wait. The method
	 * {@link #skipNextWait()} will force that thread to immediately return from
	 * the next call to this method. Designing your tests so that they work even
	 * if {@link Condition#await()} occasionally returns immediately will make
	 * your code much more robust in face of several potential threading issues.
	 * 
	 * @param c
	 *            the condition to await on
	 */
	static public void awaitOn(Condition c) {
		MultithreadedTestCase thisTestCase = currentTestCase.get();
		if (thisTestCase != null && thisTestCase.failed)
			throw new RuntimeException("Test case has failed");

		if (skipNextWait.get()) {
			skipNextWait.set(false);
			return;
		}
		try {
			c.await(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			if (thisTestCase != null && thisTestCase.failed)
				throw new RuntimeException("Test case has failed");
			throw new AssertionError(e);
		}
		if (thisTestCase != null && thisTestCase.failed)
			throw new RuntimeException("Test case has failed");

	}

	// ==================
	// -- Experimental --
	// -- - - - - - - - -

	/**
	 * A ThreadLocal that contains a Random number generator. This is used in
	 * {@link #mayYield()}
	 * 
	 * @see #mayYield()
	 */
	private static ThreadLocal<Random> mtcRandomizer = new ThreadLocal<Random>() {
		@Override
		public Random initialValue() {
			return new Random();
		}
	};

	/**
	 * Calling this method from one of the test threads may cause the thread to
	 * yield. Use this between statements to generate more interleavings.
	 */
	public void mayYield() {
		mayYield(0.5);
	}

	/**
	 * Calling this method from one of the test threads may cause the thread to
	 * yield. Use this between statements to generate more interleavings.
	 * 
	 * @param probability
	 *            (a number between 0 and 1) the likelihood that Thread.yield()
	 *            is called
	 */
	public void mayYield(double probability) {
		if (mtcRandomizer.get().nextDouble() < probability)
			Thread.yield();
	}

	// ===============================
	// -- Tick Listeners --
	// - - - - - - - - - - - - - - - -

	/**
	 * Maintain a queue of clock ticks that should not be skipped.
	 */
	SortedSet<Long> ticks = new TreeSet<Long>();

	/**
	 * The internal listeners list.
	 */
	private CopyOnWriteArrayList<TickListener> listeners = new CopyOnWriteArrayList<TickListener>();

	/**
	 * Registers the given {@link TickListener} to be notified of tick events.
	 * 
	 * @param listener
	 *            The listener instance that is registered.
	 */
	public void addTickListener(TickListener listener) {
		listeners.add(listener);
	}

	private void notifyListeners(long advancedTicks) {
		for (TickListener listener : listeners) {
			listener.notifyTick(advancedTicks);
		}
	}

	/**
	 * Register a tick that should not be skipped when advancing the clock.
	 * However, unlike {@link #waitForTick(long)}, does not block the calling
	 * thread. This method is useful in combination with a {@link TickListener}.
	 * 
	 * @param c
	 *            The clock tick that should be registered.
	 */
	public void registerTick(long c) {
		synchronized (lock) {
			ticks.add(c);
		}
	}

	/**
	 * Get the thread methods in this test and their corresponding names.
	 * 
	 * By default, thread methods start with the name "thread", have no
	 * parameters and return void. Subclasses are free to determine thread
	 * methods differently though. For example,
	 * {@link MultithreadedJUnit4TestCase} uses a {@link Threaded} annotation
	 * instead of the "threadedX" naming convention.
	 * 
	 * @param test
	 *            the test case from which to extract methods
	 * @return a map of name, Method pairs
	 */
	public ThreadedMethod[] getThreadedMethods() {
		List<ThreadedMethod> threadedMethods = new ArrayList<ThreadedMethod>();
		
		for (Method m : getClass().getDeclaredMethods()) {
			if (m.getName().startsWith("thread") && m.getParameterTypes().length == 0
					&& m.getReturnType().equals(Void.TYPE)) {
				
				threadedMethods.add(new ThreadedMethod(m.getName(), m));
			}
		}
		return threadedMethods.toArray(new ThreadedMethod[threadedMethods.size()]);
	}
}
