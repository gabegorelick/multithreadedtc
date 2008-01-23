package tckversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import java.util.concurrent.atomic.*;
import junit.framework.*;

public class AtomicIntegerFieldUpdaterTest extends JSR166TestCase {
    volatile int x = 0;
    int w;
    long z;
    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicIntegerFieldUpdaterTest.class);
    }


    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        x = 1;
        final AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest>a;
        try {
            a = AtomicIntegerFieldUpdater.newUpdater(AtomicIntegerFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }

        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!a.compareAndSet(AtomicIntegerFieldUpdaterTest.this, 2, 3)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(a.compareAndSet(this, 1, 2));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(a.get(this), 3);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

}
