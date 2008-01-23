package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;
import java.util.concurrent.atomic.*;

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class AtomicBooleanTest extends JSR166TestCase {
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(AtomicBooleanTest.class);
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
	class TUnitTestCompareAndSetInMultipleThreads extends MultithreadedTest {
    	
		AtomicBoolean ai;
		
    	public void initialize() {
    		ai = new AtomicBoolean(true);
    	}
    	
    	public void thread1() {
    		while(!ai.compareAndSet(false, true)) Thread.yield();
    	}
    	
    	public void thread2() {    	
            assertTrue(ai.compareAndSet(true, false));
    	}
    }    
    // TUNIT Untimed Block/Wait
}
