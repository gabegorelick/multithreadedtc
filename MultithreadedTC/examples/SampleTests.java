import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;


public class SampleTests extends TestCase {

    // -- EXAMPLE 1 --
	
	class MTCBoundedBufferTest extends MultithreadedTestCase {
		ArrayBlockingQueue<Integer> buf;
		@Override public void initialize() {
			buf = new ArrayBlockingQueue<Integer>(1); 
		}

		public void threadPutPut() throws InterruptedException {
			buf.put(42);
			buf.put(17);
			assertTick(1);
		}

		public void threadTakeTake() throws InterruptedException {
			waitForTick(1);
			assertTrue(buf.take() == 42);
			assertTrue(buf.take() == 17);
		}

		@Override public void finish() {
			assertTrue(buf.isEmpty());
		}		
	}
	
    public void testMTCBoundedBuffer() throws Throwable {
    	TestFramework.runOnce( new MTCBoundedBufferTest() );
    }
    
    // -- EXAMPLE 2 --
    
	/**
	 * Can we implement the Bounded Buffer using CountDownLatch? Nope,
	 * this causes a deadlock! But MTC can detect deadlocks. So we'll 
	 * use the CountDownLatch version to demonstrate MTC's deadlock
	 * detection capabilities.
	 */
	class MTCBoundedBufferDeadlockTest extends MultithreadedTestCase {
		ArrayBlockingQueue<Integer> buf;
		CountDownLatch c;
		
		@Override public void initialize() {
			buf = new ArrayBlockingQueue<Integer>(1); 
			c = new CountDownLatch(1);
		}

		public void threadPutPut() throws InterruptedException {
			buf.put(42);
			buf.put(17);
			c.countDown();
		}

		public void thread2() throws InterruptedException {
			c.await();
			assertEquals(Integer.valueOf(42), buf.take());
			assertEquals(Integer.valueOf(17), buf.take());
		}
	}

    public void testMTCBoundedBufferDeadlock() throws Throwable {
		try {
			TestFramework.runOnce( new MTCBoundedBufferDeadlockTest() );
			fail("Test should throw an IllegalStateException");
		} catch (IllegalStateException deadlockDetected) {}
    }
    
}
