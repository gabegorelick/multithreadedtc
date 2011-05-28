import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.Test;
import junit.framework.TestCase;


/**
 * The same tests as SampleTests, but using MultithreadedTest
 * instead of MultithreadedTestCase. This means we can eliminate
 * the junit methods for each test, and just provide a single
 * `suite()` method that calls {@link TestFramework#buildTestSuite(Class)}
 */
public class Sample2Test extends TestCase {

	public static Test suite() {
		return TestFramework.buildTestSuite(Sample2Test.class);
	}
	
	class MTCBoundedBufferTest extends MultithreadedTest {
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
			assertEquals(Integer.valueOf(42), buf.take());
			assertEquals(Integer.valueOf(17), buf.take());
		}

		@Override public void finish() {
			assertTrue(buf.isEmpty());
		}		
	}
	
	/**
	 * Can we implement the Bounded Buffer using CountDownLatch? Nope,
	 * this causes a deadlock! But MTC can detect deadlocks. So we'll 
	 * use the CountDownLatch version to demonstrate MTC's deadlock
	 * detection capabilities.
	 */
	class MTCBoundedBufferDeadlockTest extends MultithreadedTest {
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

		@Override public void runTest() throws Throwable {
			try {
				TestFramework.runOnce(this);
				fail("Test should throw an IllegalStateException");
			} catch (IllegalStateException deadlockDetected) {}
		}
	}

}
