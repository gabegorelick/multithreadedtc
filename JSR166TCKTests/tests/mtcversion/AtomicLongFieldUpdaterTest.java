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

public class AtomicLongFieldUpdaterTest extends JSR166TestCase {
    volatile long x = 0;
    int z;
    long w;

    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(AtomicLongFieldUpdaterTest.class);
    }



    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
	class TUnitTestCompareAndSetInMultipleThreads extends MultithreadedTest {
    	
		AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> ai;
		
    	public void initialize() {
    		x = 1;
            try {
                ai = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
            } catch (RuntimeException ok) { return; }
    	}
    	
    	public void thread1() {
    		while(!ai.compareAndSet(AtomicLongFieldUpdaterTest.this, 2, 3)) Thread.yield();
    	}
    	
    	public void thread2() {    	
           	assertTrue(ai.compareAndSet(AtomicLongFieldUpdaterTest.this, 1, 2));
    	}
    	
    	@Override public void finish() {
            assertEquals(ai.get(AtomicLongFieldUpdaterTest.this), 3);
    	}
    }    
    // TUNIT Untimed Block/Wait

}
