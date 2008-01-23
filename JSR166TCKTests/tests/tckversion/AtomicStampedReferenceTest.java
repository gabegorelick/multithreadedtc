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

public class AtomicStampedReferenceTest extends JSR166TestCase{
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicStampedReferenceTest.class);
    }
    

    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for reference value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        final AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(two, three, 0, 0)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, two, 0, 0));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.getReference(), three);
            assertEquals(ai.getStamp(), 0);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for stamp value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads2() {
        final AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(one, one, 1, 2)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, one, 0, 1));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.getReference(), one);
            assertEquals(ai.getStamp(), 2);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }


}
