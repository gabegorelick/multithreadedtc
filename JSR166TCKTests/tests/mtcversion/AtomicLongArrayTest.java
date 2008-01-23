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

public class AtomicLongArrayTest extends JSR166TestCase {
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(AtomicLongArrayTest.class);
    }


    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
	class TUnitTestCompareAndSetInMultipleThreads extends MultithreadedTest {
    	
		AtomicLongArray ai;
		
    	public void initialize() {
    		ai = new AtomicLongArray(1);
    		ai.set(0, 1);
    	}
    	
    	public void thread1() {
    		while(!ai.compareAndSet(0, 2, 3)) Thread.yield();
    	}
    	
    	public void thread2() {    	
            assertTrue(ai.compareAndSet(0, 1, 2));
    	}
    	
    	@Override public void finish() {
            assertEquals(ai.get(0), 3);
    	}
    }    
    // TUNIT Untimed Block/Wait
    

    static final long COUNTDOWN = 100000;
    
    class Counter implements Runnable {
        final AtomicLongArray ai;
        volatile long counts;
        Counter(AtomicLongArray a) { ai = a; }
        public void run() {
            for (;;) {
                boolean done = true;
                for (int i = 0; i < ai.length(); ++i) {
                    long v = ai.get(i);
                    assertTrue(v >= 0);
                    if (v != 0) {
                        done = false;
                        if (ai.compareAndSet(i, v, v-1))
                            ++counts;
                    }
                }
                if (done)
                    break;
            }
        }
    }

    /**
     * Multiple threads using same array of counters successfully
     * update a number of times equal to total count
     */
	class TUnitTestCountingInMultipleThreads extends MultithreadedTest {
    	
		AtomicLongArray ai;
		Counter c1, c2;
		
    	public void initialize() {
    		ai = new AtomicLongArray(SIZE);
    		for (int i = 0; i < SIZE; ++i) 
                ai.set(i, COUNTDOWN);
    		c1 = new Counter(ai);
    		c2 = new Counter(ai);
    	}
    	
    	public void thread1() {
    		c1.run();
    	}
    	
    	public void thread2() { 
            c2.run();
    	}
    	
    	@Override public void finish() {
            assertEquals(c1.counts+c2.counts, SIZE * COUNTDOWN);
    	}
    }    
    // TUNIT Untimed Interleave/Synchronize

}
