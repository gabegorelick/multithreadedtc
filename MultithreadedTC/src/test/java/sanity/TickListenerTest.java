package sanity;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.Threaded;
import edu.umd.cs.mtc.TickListener;

/**
 * Tests checking listener behavior. These tests can take a while, so don't
 * panic if they appear deadlocked.
 * 
 * @author Tomas Pollak
 * @since 1.02
 */
@RunWith(Enclosed.class)
public class TickListenerTest {

	private static class DummyListener implements TickListener {

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
	public static class TestListenerNotified extends MultithreadedJUnit4TestCase {
		DummyListener listener = new DummyListener();

		@Before
		public void initialize() {
			addTickListener(listener);
		}

		@Threaded
		public void thread1() {
			waitForTick(1);
			waitForTick(3);
			waitForTick(6);
		}

		@Threaded
		public void thread2() {
			waitForTick(3);
			waitForTick(6);
			waitForTick(8);
		}

		@Test
		public void testListenerNotified() {
			assertEquals(Arrays.asList(1L, 3L, 6L, 8L), listener.ticks);
		}
	}

	/**
	 * Tests that no notification is sent when the test threads sleep.
	 */
	public static class TestNoNotificationOnSleep extends MultithreadedJUnit4TestCase {
		DummyListener listener = new DummyListener();

		@Before
		public void initialize() {
			addTickListener(listener);
		}

		@Threaded
		public void thread1() throws Exception {
			Thread.sleep(100L);
		}

		@Test
		public void testNoNotificationOnSleep() {
			assertEquals(Collections.emptyList(), listener.ticks);
		}
	}

	/**
	 * Tests that no notification is sent when the test threads sleep.
	 */
	public static class TestDeadlock extends MultithreadedJUnit4TestCase {
		DummyListener listener = new DummyListener();
		ReentrantLock lockA = new ReentrantLock();
		ReentrantLock lockB = new ReentrantLock();

		@Before
		public void initialize() {
			addTickListener(listener);
		}

		@Threaded
		public void threadA() {
			lockA.lock();
			waitForTick(1);
			lockB.lock();
		}

		@Threaded
		public void threadB() {
			lockB.lock();
			waitForTick(1);
			lockA.lock();
		}

		@Test(expected = IllegalStateException.class)
		public void testDeadlock() {
			assertEquals(Arrays.asList(1L), listener.ticks);
		}
	}

	/**
	 * Test that if the clock is frozen and never unfrozen, a thread waiting for
	 * {@link #waitForTick(long)} will never return, and the test will fail.
	 */
	public static class TestMissingUnfreeze extends MultithreadedJUnit4TestCase {
		DummyListener listener = new DummyListener();

		@Before
		public void initialize() {
			addTickListener(listener);
		}

		@Threaded
		public void thread1() {
			freezeClock();
		}

		@Threaded
		public void thread2() {
			waitForTick(1);
		}

		@Test(expected = IllegalStateException.class)
		public void testMissingUnfreeze() {
			assertEquals(Collections.emptyList(), listener.ticks);
		}
	}

	/**
	 * Test that if the test registers for a tick to fall, it is not skipped.
	 */
	public static class TestRegisterTick extends MultithreadedJUnit4TestCase {
		DummyListener listener = new DummyListener();

		@Before
		public void initialize() {
			addTickListener(listener);
		}

		@Threaded
		public void thread1() {
			registerTick(4);
			registerTick(2);
			registerTick(1);
			waitForTick(3);
		}

		@Test
		public void testRegisterTick() {
			assertEquals(Arrays.asList(1L, 2L, 3L), listener.ticks);
		}
	}

}
