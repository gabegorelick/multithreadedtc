package tckversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

//import dummy.AtomicInteger;
import junit.framework.*;
import java.util.concurrent.atomic.*;

public class AtomicIntegerTest extends JSR166TestCase {
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicIntegerTest.class);
    }

    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        final AtomicInteger ai = new AtomicInteger(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(2, 3)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(1, 2));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.get(), 3);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }


}
