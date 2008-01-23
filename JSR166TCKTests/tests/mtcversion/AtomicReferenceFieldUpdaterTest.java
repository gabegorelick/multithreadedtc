package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import java.util.concurrent.atomic.*;

import junit.framework.*;

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class AtomicReferenceFieldUpdaterTest extends JSR166TestCase{
    volatile Integer x = null;
    Object z;
    Integer w;

    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(AtomicReferenceFieldUpdaterTest.class);
    }


    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
	class TUnitTestCompareAndSetInMultipleThreads extends MultithreadedTest {
    	
		AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer> ai;
		
    	public void initialize() {
    		x = one;
            try {
                ai = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
            } catch (RuntimeException ok) { return; }
    	}
    	
    	public void thread1() {
    		while(!ai.compareAndSet(AtomicReferenceFieldUpdaterTest.this, two, three)) Thread.yield();
    	}
    	
    	public void thread2() {    	
           	assertTrue(ai.compareAndSet(AtomicReferenceFieldUpdaterTest.this, one, two));
    	}
    	
    	@Override public void finish() {
            assertEquals(ai.get(AtomicReferenceFieldUpdaterTest.this), three);
    	}
    }    
    // TUNIT Untimed Block/Wait
}
