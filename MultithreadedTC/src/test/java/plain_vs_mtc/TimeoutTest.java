package plain_vs_mtc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.Threaded;

/** 
 * Timed offer times out if ArrayBlockingQueue is full and 
 * elements are not taken
 */
@RunWith(Enclosed.class)
public class TimeoutTest {
	
	/* NOTES
	 * - Uses freezeClock to prevent clock from advancing
	 * - This also guarantees that interrupt is on second offer
	 */
	
	// Plain Version

	public static class PlainTimeoutTest {
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

		@Before
		public void setUp() {
			threadFailed = false;
		}

		@Test
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

		@After
		public void tearDown() {
			assertFalse(threadFailed);
		}
	}
	
	

	// MTC Version

	/**
	 * In this test, the first offer is allowed to timeout, the second offer
	 * is interrupted. Use `freezeClock` to prevent the clock from advancing
	 * during the first offer.
	 */
	public static class MTCTimedOffer extends MultithreadedJUnit4TestCase {
		ArrayBlockingQueue<Object> q;

		@Before
		public void initialize() {
			q = new ArrayBlockingQueue<Object>(2);
		}

		@Threaded
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

		@Threaded
		public void thread2() {
			waitForTick(1);
			getThread(1).interrupt();
		}
		
		@Test
		public void testMTCTimedOffer() {
			// this space left intentionally blank
		}
	}

}
