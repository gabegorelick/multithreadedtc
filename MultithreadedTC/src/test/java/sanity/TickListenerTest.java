package sanity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import junit.framework.TestCase;
import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import edu.umd.cs.mtc.TickListener;

/**
 * Tests checking listener behavior.
 * 
 * @author Tomas Pollak
 * @since 1.02
 */
public class TickListenerTest extends TestCase {

	class DummyListener implements TickListener {

		List<Long> ticks = new ArrayList<Long>();

		@Override
		public synchronized void notifyTick(long advancedTicks) {
			ticks.add(Long.valueOf(advancedTicks));
		}
	}

	/**
	 * Tests that the correct notifications are sent when the clock advances via
	 * {@link #waitForTick(long)}.
	 */
	class TestListenerNotified extends MultithreadedTestCase {
		DummyListener listener = new DummyListener();

		@Override
		public void initialize() {
			addTickListener(listener);
		}

		public void thread1() {
			waitForTick(1);
			waitForTick(3);
			waitForTick(6);
		}

		public void thread2() {
			waitForTick(3);
			waitForTick(6);
			waitForTick(8);
		}
	}

	public void testListenerNotified() throws Throwable {
		TestListenerNotified test = new TestListenerNotified();
		TestFramework.runOnce(test);
		assertEquals(Arrays.asList(1L, 3L, 6L, 8L), test.listener.ticks);
	}

	/**
	 * Tests that no notification is sent when the test threads sleep.
	 */
	class TestNoNotificationOnSleep extends MultithreadedTestCase {
		DummyListener listener = new DummyListener();

		@Override
		public void initialize() {
			addTickListener(listener);
		}

		public void thread1() throws Exception {
			Thread.sleep(100L);
		}
	}

	public void testNoNotificationOnSleep() throws Throwable {
		TestNoNotificationOnSleep test = new TestNoNotificationOnSleep();
		TestFramework.runOnce(test);
		assertEquals(Collections.emptyList(), test.listener.ticks);
	}
	
	/**
	 * Tests that no notification is sent when the test threads sleep.
	 */
	class TestDeadlock extends MultithreadedTestCase {
		DummyListener listener = new DummyListener();
		ReentrantLock lockA = new ReentrantLock();
		ReentrantLock lockB = new ReentrantLock();

		@Override
		public void initialize() {
			addTickListener(listener);
		}

    	public void threadA() {
    		lockA.lock();
    		waitForTick(1);
    		lockB.lock();
    	}
    	
    	public void threadB() {    		
    		lockB.lock();
    		waitForTick(1);
    		lockA.lock();
    	}
	}

    public void testDeadlock() throws Throwable {
    	TestDeadlock test = new TestDeadlock();
    	try {
			TestFramework.runOnce(test, null, 2);
    		fail("should throw exception");
    	} catch (IllegalStateException expected) {
    		assertEquals(Arrays.asList(1L), test.listener.ticks);
    	}
    }

    /**
     * Test that if the clock is frozen and never unfrozen, a thread waiting
     * for {@link #waitForTick(long)} will never return, and the test will fail.
     */
    class TestMissingUnfreeze extends MultithreadedTestCase {    	
		DummyListener listener = new DummyListener();

		@Override
		public void initialize() {
			addTickListener(listener);
		}

		public void thread1() {
    		freezeClock();
    	}
    	
    	public void thread2() {   
    		waitForTick(1);
    	}
    }
    
    public void testMissingUnfreeze() throws Throwable {
    	TestMissingUnfreeze test = new TestMissingUnfreeze();
    	try {
    		// Set test to timeout after 2 seconds
			TestFramework.runOnce(test, null, 2);
    		fail("should throw exception");
    	} catch (IllegalStateException expected) {
    		assertEquals(Collections.emptyList(), test.listener.ticks);
    	}
    }

    /**
     * Test that if the test registers for a tick to fall, it is not skipped.
     */
    class TestRegisterTick extends MultithreadedTestCase {    	
		DummyListener listener = new DummyListener();

		@Override
		public void initialize() {
			addTickListener(listener);
		}

		public void thread1() {
    		registerTick(4);
    		registerTick(2);
    		registerTick(1);
    		waitForTick(3);
    	}
    }
    
    public void testRegisterTick() throws Throwable {
    	TestRegisterTick test = new TestRegisterTick();
		TestFramework.runOnce(test);
		assertEquals(Arrays.asList(1L, 2L, 3L), test.listener.ticks);
    }
}
