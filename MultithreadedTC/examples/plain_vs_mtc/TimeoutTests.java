package plain_vs_mtc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;

/** 
 * Timed offer times out if ArrayBlockingQueue is full and 
 * elements are not taken
 */
public class TimeoutTests extends TestCase {
	
	/* NOTES
	 * - Uses freezeClock to prevent clock from advancing
	 * - This also guarantees that interrupt is on second offer
	 */
	
	// Plain Version

	volatile boolean threadFailed;

	public void threadShouldThrow() {
		threadFailed = true;
		fail("should throw exception");
	}

	public void threadAssertFalse(boolean b) {
		if (b) {
			threadFailed = true;
			assertFalse(b);
		}
	}

	protected void setUp() throws Exception {
		threadFailed = false;
	}

	public void testTimedOffer() {
		final ArrayBlockingQueue<Object> q = new ArrayBlockingQueue<Object>(2);
		Thread t = new Thread(new Runnable() {    		
			public void run() {
				try {
					q.put(new Object());
					q.put(new Object());
					threadAssertFalse(q.offer(new Object(), 25, TimeUnit.MILLISECONDS));
					q.offer(new Object(), 2500, TimeUnit.MILLISECONDS);
					threadShouldThrow();
				} catch (InterruptedException success){}
			}
		});

		try {
			t.start();
			Thread.sleep(50);
			t.interrupt();
			t.join();
		} catch (Exception e) {
			fail("Unexpected exception");
		}
	}

	protected void tearDown() throws Exception {
		assertFalse(threadFailed);
	}


	// MTC Version

	/**
	 * In this test, the first offer is allowed to timeout, the second offer
	 * is interrupted. Use `freezeClock` to prevent the clock from advancing
	 * during the first offer.
	 */
	class MTCTimedOffer extends MultithreadedTestCase {
		ArrayBlockingQueue<Object> q;

		@Override public void initialize() {
			q = new ArrayBlockingQueue<Object>(2);
		}

		public void thread1() {
			try {
				q.put(new Object());
				q.put(new Object());

				freezeClock();
				assertFalse(q.offer(new Object(), 25, TimeUnit.MILLISECONDS));
				unfreezeClock();

				q.offer(new Object(), 2500, TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch (InterruptedException success){ assertTick(1); }
		}

		public void thread2() {
			waitForTick(1);
			getThread(1).interrupt();
		}
	}

	public void testMTCTimedOffer() throws Throwable {
		TestFramework.runOnce( new MTCTimedOffer() );
	}
}
