package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */


import junit.framework.*;
import java.util.concurrent.*;
import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class TimeUnitTest extends JSR166TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());	
	}

	public static Test suite() {
		return TestFramework.buildTestSuite(TimeUnitTest.class);
	}

    
    /**
     *  Timed wait without holding lock throws
     *  IllegalMonitorStateException
     */
	class TUnitTestTimedWait_IllegalMonitorException extends MultithreadedTest {
    	
    	@Override public void initialize() {
    	}
    	
    	public void thread1() throws InterruptedException {
            Object o = new Object();
            TimeUnit tu = TimeUnit.MILLISECONDS;
            try {
                tu.timedWait(o,LONG_DELAY_MS);
                fail("should throw exception");
            } catch(IllegalMonitorStateException success) {  } //assertTick(1);
    	}
    	
    	public void thread2() {  
    		waitForTick(1);
    		//getThread(1).interrupt();
    	}

    	@Override public void finish() {
    	}
    } 
    // TUNIT Timed Interrupt/Cancel
    
    
    /**
     * timedWait throws InterruptedException when interrupted
     */
	class TUnitTestTimedWait extends MultithreadedTest {
    	
    	@Override public void initialize() {
    	}
    	
    	public void thread1() throws InterruptedException {
    		Object o = new Object();		    
    		TimeUnit tu = TimeUnit.MILLISECONDS;
    		try {
    			synchronized(o) {
    				tu.timedWait(o,MEDIUM_DELAY_MS);
    			}
    			fail("should throw exception");
    		}
    		catch(InterruptedException success) { assertTick(1); } 
    	}
    	
    	public void thread2() {  
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    	@Override public void finish() {
    	}
    } 
    // TUNIT Timed Interrupt/Cancel

    
    /**
     * timedJoin throws InterruptedException when interrupted
     */
	class TUnitTestTimedJoin extends MultithreadedTest {
    	
    	@Override public void initialize() {
    	}
    	
    	public void thread0() {    		
    		waitForTick(2);
			getThread(1).interrupt();
    	}

    	public void thread1() {
    		waitForTick(1);
			TimeUnit tu = TimeUnit.MILLISECONDS;
			try {
				tu.timedJoin(getThread(2),MEDIUM_DELAY_MS);
				fail("should throw exception");
			}
			catch(Exception e) { assertTick(2); }
    	}
    	
    	public void thread2() {    		
			try {
				Thread.sleep(MEDIUM_DELAY_MS);
			} catch(InterruptedException success){}
    	}

    	@Override public void finish() {
    	}
    }    
    // TUNIT Timed Interrupt/Cancel

    
    /**
     *  timedSleep throws InterruptedException when interrupted
     */
	class TUnitTestTimedSleep extends MultithreadedTest {
    	
    	@Override public void initialize() {
    	}
    	
    	public void thread1() {
			TimeUnit tu = TimeUnit.MILLISECONDS;
			try {
				tu.sleep(MEDIUM_DELAY_MS);
				fail("should throw exception");
			}
			catch(InterruptedException success) { assertTick(1); } 
    	}
    	
    	public void thread2() {   
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    	@Override public void finish() {
    	}
    } 
    // TUNIT Timed Interrupt/Cancel
}
