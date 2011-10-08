import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.Threaded;

@RunWith(Enclosed.class)
public class SampleTest {

    // -- EXAMPLE 1 --
	
	public static class MTCBoundedBufferTest extends MultithreadedJUnit4TestCase {
		ArrayBlockingQueue<Integer> buf;
		
		@Before
		public void initialize() {
			buf = new ArrayBlockingQueue<Integer>(1); 
		}

		@Threaded
		public void threadPutPut() throws InterruptedException {
			buf.put(42);
			buf.put(17);
			assertTick(1);
		}

		@Threaded
		public void threadTakeTake() throws InterruptedException {
			waitForTick(1);
			assertTrue(buf.take() == 42);
			assertTrue(buf.take() == 17);
		}

		@Test
		public void testMTCBoundedBuffer() {
			assertTrue(buf.isEmpty());
	    }
	}
	
    
    
    // -- EXAMPLE 2 --
    
	/**
	 * Can we implement the Bounded Buffer using CountDownLatch? Nope,
	 * this causes a deadlock! But MTC can detect deadlocks. So we'll 
	 * use the CountDownLatch version to demonstrate MTC's deadlock
	 * detection capabilities.
	 */
	public static class MTCBoundedBufferDeadlockTest extends MultithreadedJUnit4TestCase {
		ArrayBlockingQueue<Integer> buf;
		CountDownLatch c;
		
		@Before
		public void initialize() {
			setTrace(true);
			buf = new ArrayBlockingQueue<Integer>(1); 
			c = new CountDownLatch(1);
		}

		@Threaded
		public void threadPutPut() throws InterruptedException {
			buf.put(42);
			buf.put(17);
			c.countDown();
		}

		@Threaded
		public void thread2() throws InterruptedException {
			c.await();
			assertEquals(Integer.valueOf(42), buf.take());
			assertEquals(Integer.valueOf(17), buf.take());
		}
		
		@Test(expected = IllegalStateException.class)
		public void testMTCBoundedBufferDeadlock() {
			// this space left intentionally blank
	    }
	}
    
}
