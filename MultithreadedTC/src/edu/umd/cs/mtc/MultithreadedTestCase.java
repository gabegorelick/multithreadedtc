package edu.umd.cs.mtc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import junit.framework.Assert;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;


/**
 * This is the base class for each test in the MultithreadedTC framework.
 * To create a multithreaded test case, simply extend this class. Any method 
 * with a name that starts with "thread", that has no parameters and a void 
 * return type is a thread method. Each thread method will be run in a seperate
 * thread. This class also defines {@link #initialize()} and {@link #finish()} methods
 * you can override.
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
 * wait until a particular tick with {@link #waitForTick(int)}. The metronome
 * isn't a free running clock; it only advances to the next tick when all
 * threads are blocked or waiting. Also, when all threads are blocked, if at least one 
 * thread isn't waiting for the metronome to advance, the system declares a 
 * deadlock to have occurred and terminates the test case (unless one of the 
 * threads is in state TIMED_WAITING).
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
abstract public class MultithreadedTestCase extends Assert implements Test {
	
	/**
	 * The metronome used to coordinate between threads. This clock
	 * is advanced by the clock thread started by {@link TestFramework}.
	 * The clock will not advance if it is frozen.
	 * 
	 * @see #waitForTick(int) 
	 * @see #freezeClock()
	 * @see #unfreezeClock()
	 */
	int clock;

	/**
	 * The primary lock to synchronize on in this test case before 
	 * accessing fields in this class.
	 */
	Object lock = new Object();

	/**
	 * If true, the debugging information is printed to standard out
	 * while the test runs 
	 */
	private boolean trace = Boolean.getBoolean("tunit.trace");

	/**
	 * This flag is set to true when a test fails due to deadlock or 
	 * timeout.
	 * 
	 * @see TestFramework
	 */
	boolean failed;

	/**
	 * This method is invoked in a test run before any test threads have
	 * started.
	 * 
	 */
	public void initialize() {
	}

	/**
	 * This method is invoked in a test after after all test threads have
	 * finished.
	 * 
	 */
	public void finish() {
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
	IdentityHashMap<Thread, Integer> threads = new IdentityHashMap<Thread, Integer>();

	/**
	 * ThreadLocal containing a reference to the current instance of 
	 * this class for each thread. When a thread completes or dies, its reference
	 * to this class is removed. 
	 */
	static ThreadLocal<MultithreadedTestCase> currentTestCase = 
		new ThreadLocal<MultithreadedTestCase>();
	
	/**
	 * This method is called right after a new testcase thread is created by
	 * the {@link TestFramework}. It provides initial values for 
	 * {@link #currentTestCase} and {@link #threads}. 
	 */
	void hello() {
		currentTestCase.set(this);
		synchronized (lock) {
			Thread currentThread = Thread.currentThread();
			threads.put(currentThread, 0);
		}

	}
	
	/**
	 * This method is called just before a testcase thread completes.
	 * It cleans out {@link #currentTestCase} and {@link #threads}.
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
	 * Get a thread given the method name that it corresponds to. E.g.
	 * to get the thread running the contents of the method 
	 * <code>thread1()</code>, call <code>getThreadByName("thread1")</code>
	 * 
	 * <p>
	 * NOTE: {@link #initialize()} is called before threads are created, 
	 * so this method returns null if called from {@link #initialize()}
	 * (but not from {@link #finish()}).
	 * 
	 * @see #getThread(int)
	 * 
	 * @param methodName
	 * 			the name of the method corresponding to the thread requested
	 * @return
	 * 			the thread corresponding to methodName
	 */
	public Thread getThreadByName(String methodName) {
		synchronized (lock) {
			return methodThreads.get(methodName);
		}		
	}

	/**
	 * Get a thread corresponding to the method whose name is formed using
	 * the prefix "thread" followed by an integer (represented by 
	 * <code>index</code>. e.g. getThread(1) returns the thread 
	 * that <code>thread1()</code> is running in.
	 * 
	 * <p>
	 * NOTE: {@link #initialize()} is called before threads are created, 
	 * so this method returns null if called from {@link #initialize()}
	 * (but not from {@link #finish()}).
	 * 
	 * @see #getThreadByName(String)
	 * 
	 * @param index
	 * 			an integer following "thread" in the name of the method
	 * @return
	 * 			the Thread corresponding to this method
	 */
	public Thread getThread(int index) {
		return getThreadByName("thread" + index);
	}

	/**
	 * Associates a thread with given method name. If the method name is already
	 * associated with a Thread, the old thread is returned, otherwise null is returned
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
	public void waitForTick(int c) {
		synchronized (lock) {
			threads.put(Thread.currentThread(), c);
			while (!failed && clock < c)
				try {
					if (getTrace())
						System.out.println(Thread.currentThread().getName()
								+ " is waiting for time " + c);
					lock.wait();
				} catch (InterruptedException e) {
					throw new AssertionError(e);
				}
			if (failed)
				throw new IllegalStateException("Clock never reached " + c);
			if (getTrace())
				System.out.println("Releasing "
						+ Thread.currentThread().getName() + " at time "
						+ clock);
		}
	}
	
	/**
	 * An Enum-based version of waitForTick. It simply looks up the ordinal and 
	 * adds 1 to determine the clock tick to wait for. 
	 * 
	 * @see #waitForTick(int)
	 * 
	 * @param e
	 * 			An Enum representing the tick to wait for. The first enumeration
	 * 			constant represents tick 1, the second is tick 2, etc.
	 */
	public void waitForTick(Enum e) {
		waitForTick(e.ordinal()+1);
	}

	/**
	 * Gets the current value of the thread metronome. Primarily useful in
	 * assert statements.
	 * 
	 * @see #assertTick(int)
	 * 
	 * @return the current tick value
	 */
	public int getTick() {
		synchronized (lock) {
			return clock;
		}
	}

	/**
	 * Assert that the clock is in tick <code>tick</code>
	 * 
	 * @param tick
	 * 			a number >= 0
	 */
	public void assertTick(int tick) {
		assertEquals(tick, getTick());
	}
	
	
	// =======================================
	// -- Components for freezing the clock --
	// - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Read locks are acquired when clock is frozen and must be
	 * released before the clock can advance in a waitForTick().
	 */
	final ReentrantReadWriteLock clockLock = new ReentrantReadWriteLock();
	
	/**
	 * When the clock is frozen, it will not advance even when all threads
	 * are blocked. Use this to block the current thread with a time limit,
	 * but prevent the clock from advancing due to a {@link #waitForTick(int)} in 
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
	 * {@link #waitOn(Object)} or {@link #awaitOn(Condition)} should
	 * return immediately.
	 * 
	 *  @see #skipNextWait()
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
	 * This method is a replacement for {@link Object#wait()}. It suppresses 
	 * the {@link InterruptedException} that you would otherwise have to
	 * deal with, and allows automated skipping of the next wait. The
	 * method {@link #skipNextWait()} will force that thread to immediately return
	 * from the next call to this method.
	 * Designing your tests so that they work even if {@link Object#wait()} 
	 * occasionally returns immediately will make your code
	 * much more robust in face of several potential threading issues.
	 * 
	 * @param o
	 * 			the object to wait on
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
	 * the {@link InterruptedException} that you would otherwise have to
	 * deal with, and allows automated skipping of the next wait. The
	 * method {@link #skipNextWait()} will force that thread to immediately return
	 * from the next call to this method.
	 * Designing your tests so that they work even if {@link Condition#await()} 
	 * occasionally returns immediately will make your code
	 * much more robust in face of several potential threading issues.
	 * 
	 * @param c
	 * 			the condition to await on
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

	
	// ======================================================
	// -- Implementation of junit.framework.Test interface --
	// -- - - - - - - - - - - - - - - - - - - - - - - - - - - 		
	
	/* (non-Javadoc)
	 * @see junit.framework.Test#countTestCases()
	 */
	public int countTestCases() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see junit.framework.Test#run(junit.framework.TestResult)
	 */
	public void run(TestResult result) {
		result.startTest(this);

		Protectable p = new Protectable() {
			public void protect() throws Throwable {
				runBare();
			}
		};
		result.runProtected(this, p);

		result.endTest(this);
	}
	
	/**
	 * Runs the bare test sequence, including setUp and tearDown
	 * if available.
	 * 
	 * @throws Throwable if any exception is thrown
	 */
	protected void runBare() throws Throwable {
		if (setUpMethod != null) setUpMethod.invoke(testCase, (Object []) null);
		try {
			runTest();
		}
		finally {
			if (tearDownMethod != null) tearDownMethod.invoke(testCase, (Object []) null);
		}
	}
	
	/**
	 * This is the method that runs this test. It is equivalent to the 
	 * testXxx methods in JUnit 3. By default the test is just run once,
	 * by calling {@link TestFramework#runOnce(MultithreadedTestCase)}.
	 * To change the way the test is run, simply override this method.
	 * 
	 * @see TestFramework#runOnce(MultithreadedTestCase)
	 * @see TestFramework#runOnce(MultithreadedTestCase, Integer, Integer)
	 * @see TestFramework#runManyTimes(MultithreadedTestCase, int)
	 * @see TestFramework#runManyTimes(MultithreadedTestCase, int, Integer, Integer)
	 * 
	 * @throws Throwable if any exception is thrown
	 */
	public void runTest() throws Throwable {
		TestFramework.runOnce( this );
	}
		
	/**
	 * If this {@link MultithreadedTestCase} is added to a test suite using
	 * {@link TestFramework#buildTestSuite(Class)}, and if it is a non-static
	 * inner class of a {@link TestCase}, then the setUp and tearDown methods
	 * should be called before and after the test is run respectively. This
	 * method is used by 
	 * {@link TestFramework#addSetUpAndTearDown(MultithreadedTestCase, TestCase)}
	 * to provide the necessary references so that these methods can be invoked.
	 * 
	 * @param tc
	 * @param setUp
	 * @param tearDown
	 */
	void addSetUpAndTearDown(TestCase tc, Method setUp, Method tearDown) {
		testCase = tc;
		setUpMethod = setUp;
		tearDownMethod = tearDown;
	}
	
	/** @see #addSetUpAndTearDown(TestCase, Method, Method) */
	private TestCase testCase = null;

	/** @see #addSetUpAndTearDown(TestCase, Method, Method) */
	private Method setUpMethod = null;

	/** @see #addSetUpAndTearDown(TestCase, Method, Method) */
	private Method tearDownMethod = null;	
}
