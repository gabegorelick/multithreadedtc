package plain_vs_mtc;

import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;

/** 
 * compareAndSet in one thread enables another waiting for value
 * to succeed 
 */
public class CompareAndSetTests extends TestCase {

	/* NOTES
	 * - Plain version requires a join before final asserts
	 * - MTC version does not need to check if thread is alive
	 */
	
	// Plain Version
	
	public void testCompareAndSet() throws InterruptedException {
		final AtomicInteger ai = new AtomicInteger(1);
		Thread t = new Thread(new Runnable() {
			public void run() {
				while(!ai.compareAndSet(2, 3)) Thread.yield();
			}
		});

		t.start();
		assertTrue(ai.compareAndSet(1, 2));
		t.join(2500);
		assertFalse(t.isAlive());
		assertEquals(ai.get(), 3);
	}


	// MTC Version

	class MTCCompareAndSet extends MultithreadedTestCase {

		AtomicInteger ai;

		@Override public void initialize() {
			ai = new AtomicInteger(1);
		}

		public void thread1() {
			while(!ai.compareAndSet(2, 3)) Thread.yield();
		}

		public void thread2() {    	
			assertTrue(ai.compareAndSet(1, 2));
		}

		@Override public void finish() {
			assertEquals(ai.get(), 3);			
		}    	    	
	}
    
    public void testMTCCompareAndSet() throws Throwable {
    	TestFramework.runOnce( new MTCCompareAndSet() );
    }
}
