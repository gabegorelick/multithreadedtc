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
public class AtomicMarkableReferenceTest extends JSR166TestCase{
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(AtomicMarkableReferenceTest.class);
    }
    

    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for reference value
     * to succeed
     */
	class TUnitTestCompareAndSetInMultipleThreads extends MultithreadedTest {
    	
		AtomicMarkableReference ai;
		
    	public void initialize() {
    		ai = new AtomicMarkableReference(one, false);
    	}
    	
    	public void thread1() {
    		while(!ai.compareAndSet(two, three, false, false)) Thread.yield();
    	}
    	
    	public void thread2() {    	
            assertTrue(ai.compareAndSet(one, two, false, false));
    	}
    	
    	@Override public void finish() {
            assertEquals(ai.getReference(), three);
            assertFalse(ai.isMarked());
    	}
    }   
    // TUNIT Untimed Block/Wait

        
    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for mark value
     * to succeed
     */
	class TUnitTestCompareAndSetInMultipleThreads2 extends MultithreadedTest {
    	
		AtomicMarkableReference ai;
		
    	public void initialize() {
    		ai = new AtomicMarkableReference(one, false);
    	}
    	
    	public void thread1() {
    		while(!ai.compareAndSet(one, one, true, false)) Thread.yield();
    	}
    	
    	public void thread2() {    	
           	assertTrue(ai.compareAndSet(one, one, false, true));
    	}
    	
    	@Override public void finish() {
            assertEquals(ai.getReference(), one);
            assertFalse(ai.isMarked());
    	}
    }
    
    // TUNIT Untimed Block/Wait
}
