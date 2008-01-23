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

public class AtomicMarkableReferenceTest extends JSR166TestCase{
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicMarkableReferenceTest.class);
    }
    

    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for reference value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        final AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(two, three, false, false)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, two, false, false));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.getReference(), three);
            assertFalse(ai.isMarked());
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for mark value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads2() {
        final AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(one, one, true, false)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, one, false, true));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.getReference(), one);
            assertFalse(ai.isMarked());
        }
        catch(Exception e) {
            unexpectedException();
        }
    }


}
