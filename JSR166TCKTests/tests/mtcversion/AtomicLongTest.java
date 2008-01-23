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

public class AtomicLongTest extends JSR166TestCase {
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(AtomicLongTest.class);
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
	class TUnitTestCompareAndSetInMultipleThreads extends MultithreadedTest {
    	
		AtomicLong ai;
		
    	public void initialize() {
    		ai = new AtomicLong(1);
    	}
    	
    	public void thread1() {
    		while(!ai.compareAndSet(2, 3)) Thread.yield();
    	}
    	
    	public void thread2() {    	
    		assertTrue(ai.compareAndSet(1, 2));
    	}
    	
    	@Override public void finish() {
            assertEquals(ai.get(), 3);
    	}
    }    
    // TUNIT Untimed Block/Wait
}
