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
public class AtomicStampedReferenceTest extends JSR166TestCase{
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(AtomicStampedReferenceTest.class);
    }
    

    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for reference value
     * to succeed
     */
   	class TUnitTestCompareAndSetInMultipleThreads extends MultithreadedTest {    	
		AtomicStampedReference ai;
    	public void initialize() {
    		ai = new AtomicStampedReference(one, 0);
    	}
    	
		public void thread1() {
    		while(!ai.compareAndSet(two, three, 0, 0)) Thread.yield();
    	}
    	
    	public void thread2() {    	
            assertTrue(ai.compareAndSet(one, two, 0, 0));
    	}
    	
    	@Override public void finish() {
            assertEquals(ai.getReference(), three);
            assertEquals(ai.getStamp(), 0);
    	}

    }    
    // TUNIT Untimed Block/Wait

    
    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for stamp value
     * to succeed
     */
	class TUnitTestCompareAndSetInMultipleThreads2 extends MultithreadedTest {    	
		AtomicStampedReference ai;		
    	public void initialize() {
    		ai = new AtomicStampedReference(one, 0);
    	}
    	
    	public void thread1() {
    		while(!ai.compareAndSet(one, one, 1, 2)) Thread.yield();
    	}
    	
    	public void thread2() {    	
           	assertTrue(ai.compareAndSet(one, one, 0, 1));
    	}
    	
    	@Override public void finish() {
            assertEquals(ai.getReference(), one);
            assertEquals(ai.getStamp(), 2);
    	}
    }    
    // TUNIT Untimed Block/Wait
}
