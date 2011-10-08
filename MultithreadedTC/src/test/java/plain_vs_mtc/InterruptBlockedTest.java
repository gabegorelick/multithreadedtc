package plain_vs_mtc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.concurrent.Semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.Threaded;

/**
 * Test that a waiting acquire blocks interruptibly
 */
@RunWith(Enclosed.class)
public class InterruptBlockedTest {

	/* NOTES
	 * - Failures in threads require additional work in setup and teardown
	 * - Relies on Thread.sleep to ensure acquire has blocked
	 * - Does not ensure that exceptions are definitely caused by interrupt
	 * - More verbose
	 * - Requires a join at the end
	 * - In MTC version, get reference to a thread using getThread(1)
	 */
	
	// Plain Version
	
	public static class PlainInterruptBlockedTest {
		volatile boolean threadFailed;

		public void threadShouldThrow() {
			threadFailed = true;
			fail("should throw exception");
		}

		@Before
		public void setUp() {
			threadFailed = false;
		}

		@Test
		public void testInterruptedAcquire() {
			final Semaphore s = new Semaphore(0);
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						s.acquire();
						threadShouldThrow();
					} catch(InterruptedException success){}
				}
			});
			t.start();
			try {
				Thread.sleep(50);
				t.interrupt();
				t.join();
			} catch(InterruptedException e){
				fail("Unexpected exception");
			}
		}

		@After
		public void tearDown() {
			assertFalse(threadFailed);
		}
	}

	

	// MTC Version

	public static class MTCInterruptedAcquire extends MultithreadedJUnit4TestCase {
		Semaphore s;
		
		@Before
		public void initialize() {
			s = new Semaphore(0);
		}

		@Threaded
		public void thread1() {
			try {
				s.acquire();
				fail("should throw exception");
			} catch(InterruptedException success){ assertTick(1); }
		}

		@Threaded
		public void thread2() {
			waitForTick(1); 
			getThread(1).interrupt();
		}
		
		@Test
		public void testMTCInterruptedAcquire() {
			// this space left intentionally blank
		}
	}

		
}
