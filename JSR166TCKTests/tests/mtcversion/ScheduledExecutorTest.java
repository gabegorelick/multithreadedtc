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

public class ScheduledExecutorTest extends JSR166TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}
	public static Test suite() {
		return TestFramework.buildTestSuite(ScheduledExecutorTest.class);
	}

    // REVIEW <=> Test to ensure an executor executes a runnable, by sleeping until the executor is done
    /**
     * execute successfully executes a runnable
     * 
     * TUnit Version: Tracked runnable waits for tick 1 instead of 
     * Thread.sleep; thread1 waits for tracked runnable to finish by 
     * waiting for tick 2
     */
    class TUnitTestExecute extends MultithreadedTest {
    	
        class TrackedTick1Runnable implements Runnable {
            volatile boolean done = false;
            public void run() {
            	waitForTick(1);
            	done = true;
            }
        }

    	TrackedTick1Runnable runnable;
    	ScheduledThreadPoolExecutor p1;
    	
    	public void initialize() {
        	runnable = new TrackedTick1Runnable();
        	p1 = new ScheduledThreadPoolExecutor(1);
    	}
    	
    	public void thread1() {
    		p1.execute(runnable);
    		assertFalse(runnable.done);

    		waitForTick(2);
    		try { p1.shutdown(); } catch(SecurityException ok) { return; }

    		waitForTick(3);
    		assertTrue(runnable.done);
    		try { p1.shutdown(); } catch(SecurityException ok) { return; }
    		joinPool(p1);
    	}
    }    
    // TUNIT Untimed Interleave/Synchronize
}
