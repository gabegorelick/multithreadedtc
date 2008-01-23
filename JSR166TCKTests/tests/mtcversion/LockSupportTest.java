package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;
import java.util.*;
import java.util.concurrent.locks.*;

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class LockSupportTest extends JSR166TestCase{
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return TestFramework.buildTestSuite(LockSupportTest.class);
    }

    /**
     * park is released by unpark occurring after park
     */
    class TUnitTestPark extends MultithreadedTest {
    	
    	public void thread1() {
    		LockSupport.park();
    		assertTick(1);
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		LockSupport.unpark(getThread(1));
    	}
    }    
    // TUNIT Untimed Block/Wait


    /**
     * park is released by unpark occurring before park
     */
    // REVIEW My first try at this test would sometimes fail because
    // it is possible that 'unpark' is called before thread1 is even started,
    // in which case the correct behavior of 'unpark' is not guaranteed. This led
    // to deadlock (because 'park' was not informed that 'unpark' had been called).
    // To fix this, I use waitForTick to ensure both threads have started.
    class TUnitTestPark2 extends MultithreadedTest {
    	
    	public void thread1() {
    		waitForTick(1); // ensure both threads have started
    		
    		waitForTick(2);
    		LockSupport.park();
    		assertTick(2); // still in tick 2
    	}
    	
    	public void thread2() {
    		waitForTick(1); // ensure both threads have started
    		
    		LockSupport.unpark(getThread(1));
    	}
    }    
    // TUNIT Untimed Block/Wait

    
    /**
     * park is released by interrupt 
     */
    class TUnitTestPark3 extends MultithreadedTest {
    	
    	public void thread1() {
    		LockSupport.park();
    		assertTick(1);
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Untimed Block/Wait (because interrupt does not cause exception)

    
    /**
     * park returns if interrupted before park
     */
    // The interrupt occurs when the thread is disabled and so does not
    // cause an exception, however it causes the park to return immediately
    // (without it park would block). 
    // REVIEW In both cases we have a state of deadlock and the test should fail
    class TUnitTestPark4 extends MultithreadedTest {
    	ReentrantLock lock;
    	@Override public void initialize() {
    		//setTrace(true);
    		lock = new ReentrantLock();    		
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		lock.lock();
			LockSupport.park();
    	}
    	
    	public void thread2() { 
    		lock.lock();
    		waitForTick(1);
    		getThread(1).interrupt();
    		lock.unlock();
    	}
    }    
    // TUNIT Untimed Block/Wait


    /**
     * parkNanos times out if not unparked
     */
    class TUnitTestParkNanos extends MultithreadedTest {    	
    	public void thread1() {
    		LockSupport.parkNanos(1000);
    	}
    }    
    // TUNIT Timed Block/Wait


    /**
     * parkUntil times out if not unparked
     */
    class TUnitTestParkUntil extends MultithreadedTest {
    	public void thread1() {
    		long d = new Date().getTime() + 100;
			LockSupport.parkUntil(d);
    	}
    }    
    // TUNIT Timed Block/Wait
}
