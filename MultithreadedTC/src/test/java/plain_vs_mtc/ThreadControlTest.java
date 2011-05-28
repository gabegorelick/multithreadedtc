package plain_vs_mtc;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;

/**
 * Controlling the order in which threads are called
 */
public class ThreadControlTest extends TestCase {

	// MTC Version

	class MTCThreadOrdering extends MultithreadedTestCase {

		AtomicInteger ai;

		@Override public void initialize() {
			ai = new AtomicInteger(0);
		}

		public void thread1() {
			assertTrue(ai.compareAndSet(0, 1)); // S1
			waitForTick(3);
			assertEquals(ai.get(), 3);			// S4
		}

		public void thread2() {   
			waitForTick(1);
			assertTrue(ai.compareAndSet(1, 2)); // S2
			waitForTick(3);
			assertEquals(ai.get(), 3);			// S4
		}

		public void thread3() {
			waitForTick(2);
			assertTrue(ai.compareAndSet(2, 3)); // S3
		}
	}

	public void testMTCThreadOrdering() throws Throwable {
		TestFramework.runOnce( new MTCThreadOrdering() );
	}


	// CountDown Latch version

	volatile boolean threadFailed;

	protected void setUp() throws Exception {
		threadFailed = false;
	}

	protected void tearDown() throws Exception {
		assertFalse(threadFailed);
	}

	public void unexpectedException() {
		threadFailed = true;
		fail("Unexpected exception");
	}

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
