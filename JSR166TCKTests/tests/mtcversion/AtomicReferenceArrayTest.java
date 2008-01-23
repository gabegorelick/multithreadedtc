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

@SuppressWarnings("unchecked")
public class AtomicReferenceArrayTest extends JSR166TestCase 
{
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(AtomicReferenceArrayTest.class);
    }


    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
	class TUnitTestCompareAndSetInMultipleThreads extends MultithreadedTest {
    	
		AtomicReferenceArray ai;
		
    	public void initialize() {
    		ai = new AtomicReferenceArray(1);
    		ai.set(0, one);
    	}
    	
    	public void thread1() {
    		while(!ai.compareAndSet(0, two, three)) Thread.yield();
    	}
    	
    	public void thread2() {    	
           	assertTrue(ai.compareAndSet(0, one, two));
    	}
    	
    	@Override public void finish() {
            assertEquals(ai.get(0), three);
    	}
    }    
    // TUNIT Untimed Block/Wait
}
