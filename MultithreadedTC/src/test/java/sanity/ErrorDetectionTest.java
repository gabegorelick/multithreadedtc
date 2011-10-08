package sanity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.Threaded;

/**
 * Tests for all the error conditions detected by the {@link TestFramework}.
 */
@RunWith(Enclosed.class)
public class ErrorDetectionTest {
	
	/**
	 * Tests that a deadlock fails the test.
	 */
    public static class TestDeadlockDetected extends MultithreadedJUnit4TestCase {
    	ReentrantLock lockA = new ReentrantLock();
    	ReentrantLock lockB = new ReentrantLock();
    	
    	@Threaded
    	public void threadA() {
    		lockA.lock();
    		waitForTick(1);
    		lockB.lock();
    	}
    	
    	@Threaded
    	public void threadB() {    		
    		lockB.lock();
    		waitForTick(1);
    		lockA.lock();
    	}
    	
    	@Test(expected = IllegalStateException.class)
    	public void testDeadlockDetected() {
        	// this space left intentionally blank
        }
    }
    
    
    
    /**
     * Test that if the clock is frozen and never unfrozen, a thread waiting
     * for {@link #waitForTick(long)} will never return, and the test will fail.
     */
    public static class TestMissingUnfreeze extends MultithreadedJUnit4TestCase {    	
    	
    	@Threaded
    	public void thread1() throws InterruptedException {
    		freezeClock();
    		Thread.sleep(200);
    	}
    	
    	@Threaded
    	public void thread2() {   
    		waitForTick(1);
    	}
    	
    	@Test(expected = IllegalStateException.class)
    	public void testMissingUnfreeze() {
        	// this space left intentionally blank
        }
    }
    
    
    
    
    public static class TestLiveLockTimesOut extends MultithreadedJUnit4TestCase {
		AtomicInteger ai = new AtomicInteger(1);
		
		@Threaded
    	public void thread1() {
    		while(!ai.compareAndSet(2, 3)) Thread.yield();
    	}
    	
		@Threaded
    	public void thread2() {    	
    		while(!ai.compareAndSet(3, 2)) Thread.yield();
    	}
		
		@Test(expected = IllegalStateException.class)
	    public void testLiveLockTimesOut() {
			// this space left intentionally blank
	    }
    }

}
