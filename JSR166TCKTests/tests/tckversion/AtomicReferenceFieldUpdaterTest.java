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

public class AtomicReferenceFieldUpdaterTest extends JSR166TestCase{
    volatile Integer x = null;
    Object z;
    Integer w;

    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicReferenceFieldUpdaterTest.class);
    }


    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        x = one;
        final AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }

        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!a.compareAndSet(AtomicReferenceFieldUpdaterTest.this, two, three)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(a.compareAndSet(this, one, two));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(a.get(this), three);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }


}
