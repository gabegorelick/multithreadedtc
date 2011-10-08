package plain_vs_mtc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.Threaded;

/**
 * Controlling the order in which threads are called
 */
@RunWith(Enclosed.class)
public class ThreadControlTest {

	// MTC Version

	public static class MTCThreadOrdering extends MultithreadedJUnit4TestCase {

		AtomicInteger ai;

		@Before
		public void initialize() {
			ai = new AtomicInteger(0);
		}

		@Threaded
		public void thread1() {
			assertTrue(ai.compareAndSet(0, 1)); // S1
			waitForTick(3);
			assertEquals(ai.get(), 3);			// S4
		}

		@Threaded
		public void thread2() {   
			waitForTick(1);
			assertTrue(ai.compareAndSet(1, 2)); // S2
			waitForTick(3);
			assertEquals(ai.get(), 3);			// S4
		}

		@Threaded
		public void thread3() {
			waitForTick(2);
			assertTrue(ai.compareAndSet(2, 3)); // S3
		}
		
		@Test
		public void testMTCThreadOrdering() {
			// this space left intentionally blank
		}
	}

	
	// CountDown Latch version

	public static class LatchBasedThreadOrderingTest {
		volatile boolean threadFailed;

		@Before
		public void setUp() {
			threadFailed = false;
		}

		@After
		public void tearDown() {
			assertFalse(threadFailed);
		}

		public void unexpectedException() {
			threadFailed = true;
			fail("Unexpected exception");
		}

		@Test
		public void testLatchBasedThreadOrdering() throws InterruptedException {
			final CountDownLatch c1 = new CountDownLatch(1);
			final CountDownLatch c2 = new CountDownLatch(1);
			final CountDownLatch c3 = new CountDownLatch(1);    	
			final AtomicInteger ai = new AtomicInteger(0);

			Thread t1 = new Thread(new Runnable() {
				public void run() {
					try {
						assertTrue(ai.compareAndSet(0, 1)); // S1
						c1.countDown();
						c3.await();
						assertEquals(ai.get(), 3);			// S4
					} catch (Exception e) {  // Can't simply catch InterruptedException because we might miss some RuntimeException
						unexpectedException();
					}
				}
			});

			Thread t2 = new Thread(new Runnable() {
				public void run() {
					try {
						c1.await();
						assertTrue(ai.compareAndSet(1, 2)); // S2
						c2.countDown();
						c3.await();
						assertEquals(ai.get(), 3);			// S4
					} catch (Exception e) {
						unexpectedException();
					}
				}
			});

			t1.start();
			t2.start();

			c2.await();
			assertTrue(ai.compareAndSet(2, 3)); // S3    
			c3.countDown();

			t1.join();
			t2.join();
		}

	}
	
	
}
