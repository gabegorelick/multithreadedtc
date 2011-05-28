package sanity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;

public class ErrorDetectionTest extends TestCase {
	
	boolean trace = false;
	
    class TUnitTestDeadlockDetected extends MultithreadedTestCase {
    	ReentrantLock lockA;	
    	ReentrantLock lockB;
    	
    	@Override public void initialize() {
    		lockA = new ReentrantLock();	
    		lockB = new ReentrantLock();
    	}
    	
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
    		TestFramework.runOnce( new TUnitTestDeadlockDetected() );
    		fail("should throw exception");
    	} catch (IllegalStateException success) {
    		if (trace) success.printStackTrace();
    	}
    }
    
    // - - - -

    
    class TUnitTestMissingUnfreeze extends MultithreadedTestCase {    	
    	public void thread1() throws InterruptedException {
    		freezeClock();
    		Thread.sleep(200);
    	}
    	
    	public void thread2() {   
    		waitForTick(1);
    	}
    }
    
    public void testMissingUnfreeze_tunit() throws Throwable {
    	try {
    		// Set test to timeout after 2 seconds
    		TestFramework.runOnce( new TUnitTestMissingUnfreeze(), null, 2 );
    		fail("should throw exception");
    	} catch (IllegalStateException success) {
    		if (trace) success.printStackTrace();
    	}
    }
    
    // - - - -

    
    class TUnitTestLiveLockTimesOut extends MultithreadedTestCase {
		AtomicInteger ai;
		
		@Override public void initialize() {
    		ai = new AtomicInteger(1);
    		if (false) ai.compareAndSet(1, 2);
    	}
    	
    	public void thread1() {
    		while(!ai.compareAndSet(2, 3)) Thread.yield();
    	}
    	
    	public void thread2() {    	
    		while(!ai.compareAndSet(3, 2)) Thread.yield();
    	}

		@Override public void finish() {
            assertTrue(ai.get() == 2 || ai.get() == 3);			
		}    	    	
    }
    
    public void testLiveLockTimesOut() throws Throwable {
    	try {
    		// Set test to timeout after 2 seconds
    		TestFramework.runOnce( new TUnitTestLiveLockTimesOut(), null, 2 );
    		fail("should throw exception");
    	} catch (IllegalStateException success) {
    		if (trace) success.printStackTrace();
    	}
    }

}
