package plain_vs_mtc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.Threaded;

/** 
 * compareAndSet in one thread enables another waiting for value
 * to succeed 
 */
@RunWith(Enclosed.class)
public class CompareAndSetTest {

	/* NOTES
	 * - Plain version requires a join before final asserts
	 * - MTC version does not need to check if thread is alive
	 */
	
	// Plain Version
	
	// Enclosed requires that the test be in a nested class
	public static class PlainCompareAndSet {
		@Test
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
	}
	
	// MTC Version

	public static class MTCCompareAndSet extends MultithreadedJUnit4TestCase {

		AtomicInteger ai;

		@Before
		public void initialize() {
			ai = new AtomicInteger(1);
		}

		@Threaded
		public void thread1() {
			while(!ai.compareAndSet(2, 3)) Thread.yield();
		}

		@Threaded
		public void thread2() {    	
			assertTrue(ai.compareAndSet(1, 2));
		}
		
		@Test
		public void testMTCCompareAndSet() {
			assertEquals(ai.get(), 3);
	    }
	}
    
    
}
