package sanity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;

/**
 * Tests for all the error conditions detected by the {@link TestFramework}.
 */
public class ErrorDetectionTest extends TestCase {
	
	/**
	 * Tests that a deadlock fails the test.
	 */
    class TestDeadlockDetected extends MultithreadedTestCase {
    	ReentrantLock lockA = new ReentrantLock();
    	ReentrantLock lockB = new ReentrantLock();
    	
    	public void threadA() {
    		lockA.lock();
    		waitForTick(1);
    		lockB.lock();
    	}
    	
    	public void threadB() {    		
    		lockB.lock();
    		waitForTick(1);
    		lockA.lock();
    	}
    }
    
    public void testDeadlockDetected() throws Throwable {
    	try {
    		TestFramework.runOnce( new TestDeadlockDetected() );
    		fail("should throw exception");
    	} catch (IllegalStateException expected) {
    	}
    }
    
    /**
     * Test that if the clock is frozen and never unfrozen, a thread waiting
     * for {@link #waitForTick(int)} will never return, and the test will fail.
     */
    class TestMissingUnfreeze extends MultithreadedTestCase {    	
    	public void thread1() throws InterruptedException {
    		freezeClock();
    		Thread.sleep(200);
    	}
    	
    	public void thread2() {   
    		waitForTick(1);
    	}
    }
    
    public void testMissingUnfreeze() throws Throwable {
    	try {
    		// Set test to timeout after 2 seconds
    		TestFramework.runOnce(new TestMissingUnfreeze(), null, 2);
    		fail("should throw exception");
    	} catch (IllegalStateException expected) {
    	}
    }
    
    
    class TestLiveLockTimesOut extends MultithreadedTestCase {
		AtomicInteger ai = new AtomicInteger(1);
		
    	public void thread1() {
    		while(!ai.compareAndSet(2, 3)) Thread.yield();
    	}
    	
    	public void thread2() {    	
    		while(!ai.compareAndSet(3, 2)) Thread.yield();
    	}
    }
    
    public void testLiveLockTimesOut() throws Throwable {
    	try {
    		// Set test to timeout after 2 seconds
    		TestFramework.runOnce(new TestLiveLockTimesOut(), null, 2);
    		fail("should throw exception");
    	} catch (IllegalStateException expected) {
    	}
    }

}
