package tckversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;
import java.util.concurrent.atomic.*;

public class AtomicIntegerArrayTest extends JSR166TestCase {

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicIntegerArrayTest.class);
    }



    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        final AtomicIntegerArray a = new AtomicIntegerArray(1);
        a.set(0, 1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!a.compareAndSet(0, 2, 3)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(a.compareAndSet(0, 1, 2));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(a.get(0), 3);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }


    static final int COUNTDOWN = 100000;
    
    class Counter implements Runnable {
        final AtomicIntegerArray ai;
        volatile int counts;
        Counter(AtomicIntegerArray a) { ai = a; }
        public void run() {
            for (;;) {
                boolean done = true;
                for (int i = 0; i < ai.length(); ++i) {
                    int v = ai.get(i);
                    threadAssertTrue(v >= 0);
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
    public void testCountingInMultipleThreads() {
        try {
            final AtomicIntegerArray ai = new AtomicIntegerArray(SIZE); 
            for (int i = 0; i < SIZE; ++i) 
                ai.set(i, COUNTDOWN);
            Counter c1 = new Counter(ai);
            Counter c2 = new Counter(ai);
            Thread t1 = new Thread(c1);
            Thread t2 = new Thread(c2);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            assertEquals(c1.counts+c2.counts, SIZE * COUNTDOWN);
        }
        catch(InterruptedException ie) {
            unexpectedException();
        }
    }


}
