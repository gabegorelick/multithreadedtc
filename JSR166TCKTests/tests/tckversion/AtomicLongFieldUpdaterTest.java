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

public class AtomicLongFieldUpdaterTest extends JSR166TestCase {
    volatile long x = 0;
    int z;
    long w;

    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicLongFieldUpdaterTest.class);
    }



    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        x = 1;
        final AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest>a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }

        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!a.compareAndSet(AtomicLongFieldUpdaterTest.this, 2, 3)) Thread.yield();
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
