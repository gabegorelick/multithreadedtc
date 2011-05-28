package plain_vs_mtc;

import java.util.concurrent.Semaphore;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;

/**
 * Test that a waiting acquire blocks interruptibly
 */
public class InterruptBlockedTest extends TestCase {

	/* NOTES
	 * - Failures in threads require additional work in setup and teardown
	 * - Relies on Thread.sleep to ensure acquire has blocked
	 * - Does not ensure that exceptions are definitely caused by interrupt
	 * - More verbose
	 * - Requires a join at the end
	 * - In MTC version, get reference to a thread using getThread(1)
	 */
	
	// Plain Version

	volatile boolean threadFailed;

	public void threadShouldThrow() {
		threadFailed = true;
		fail("should throw exception");
	}

	protected void setUp() throws Exception {
		threadFailed = false;
	}

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

	protected void tearDown() throws Exception {
		assertFalse(threadFailed);
	}


	// MTC Version

	class MTCInterruptedAcquire extends MultithreadedTestCase {
		Semaphore s;
		@Override public void initialize() {
			s = new Semaphore(0);
		}

		public void thread1() {
			try {
				s.acquire();
				fail("should throw exception");
			} catch(InterruptedException success){ assertTick(1); }
		}

		public void thread2() {
			waitForTick(1); 
			getThread(1).interrupt();
		}
	}

	public void testMTCInterruptedAcquire() throws Throwable {
		TestFramework.runOnce( new MTCInterruptedAcquire() );
	}	
}
