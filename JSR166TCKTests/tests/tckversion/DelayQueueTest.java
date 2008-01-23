package tckversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;
import java.util.*;
import java.util.concurrent.*;

public class DelayQueueTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }

    public static Test suite() {
	return new TestSuite(DelayQueueTest.class);
    }

    private static final int NOCAP = Integer.MAX_VALUE;

    /**
     * A delayed implementation for testing.
     * Most  tests use Pseudodelays, where delays are all elapsed
     * (so, no blocking solely for delays) but are still ordered
     */ 
    static class PDelay implements Delayed { 
        int pseudodelay;
        PDelay(int i) { pseudodelay = Integer.MIN_VALUE + i; }
        public int compareTo(PDelay y) {
            int i = pseudodelay;
            int j = ((PDelay)y).pseudodelay;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }

        public int compareTo(Delayed y) {
            int i = pseudodelay;
            int j = ((PDelay)y).pseudodelay;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }

        public boolean equals(Object other) {
            return ((PDelay)other).pseudodelay == pseudodelay;
        }
        public boolean equals(PDelay other) {
            return ((PDelay)other).pseudodelay == pseudodelay;
        }


        public long getDelay(TimeUnit ignore) {
            return pseudodelay;
        }
        public int intValue() {
            return pseudodelay;
        }

        public String toString() {
            return String.valueOf(pseudodelay);
        }
    }


    /**
     * Delayed implementation that actually delays
     */
    static class NanoDelay implements Delayed { 
        long trigger;
        NanoDelay(long i) { 
            trigger = System.nanoTime() + i;
        }
        public int compareTo(NanoDelay y) {
            long i = trigger;
            long j = ((NanoDelay)y).trigger;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }

        public int compareTo(Delayed y) {
            long i = trigger;
            long j = ((NanoDelay)y).trigger;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }

        public boolean equals(Object other) {
            return ((NanoDelay)other).trigger == trigger;
        }
        public boolean equals(NanoDelay other) {
            return ((NanoDelay)other).trigger == trigger;
        }

        public long getDelay(TimeUnit unit) {
            long n = trigger - System.nanoTime();
            return unit.convert(n, TimeUnit.NANOSECONDS);
        }

        public long getTriggerTime() {
            return trigger;
        }

        public String toString() {
            return String.valueOf(trigger);
        }
    }


    /**
     * Create a queue of given size containing consecutive
     * PDelays 0 ... n.
     */
    private DelayQueue populatedQueue(int n) {
        DelayQueue q = new DelayQueue();
        assertTrue(q.isEmpty());
	for(int i = n-1; i >= 0; i-=2)
	    assertTrue(q.offer(new PDelay(i)));
	for(int i = (n & 1); i < n; i+=2)
	    assertTrue(q.offer(new PDelay(i)));
        assertFalse(q.isEmpty());
        assertEquals(NOCAP, q.remainingCapacity());
	assertEquals(n, q.size());
        return q;
    }
 

    /**
     * put doesn't block waiting for take
     */
    public void testPutWithTake() {
        final DelayQueue q = new DelayQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.put(new PDelay(0));
                        ++added;
                        q.put(new PDelay(0));
                        ++added;
                        q.put(new PDelay(0));
                        ++added;
                        q.put(new PDelay(0));
                        ++added;
                        threadAssertTrue(added == 4);
                    } finally {
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            q.take();
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }

    /**
     * timed offer does not time out
     */
    public void testTimedOffer() {
        final DelayQueue q = new DelayQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new PDelay(0));
                        q.put(new PDelay(0));
                        threadAssertTrue(q.offer(new PDelay(0), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(q.offer(new PDelay(0), LONG_DELAY_MS, TimeUnit.MILLISECONDS));
                    } finally { }
                }
            });
        
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }


    /**
     * take blocks interruptibly when empty
     */
    public void testTakeFromEmpty() {
        final DelayQueue q = new DelayQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.take();
			threadShouldThrow();
                    } catch (InterruptedException success){ }                
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }

    /**
     * Take removes existing elements until empty, then blocks interruptibly
     */
    public void testBlockingTake() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        DelayQueue q = populatedQueue(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(new PDelay(i), ((PDelay)q.take()));
                        }
                        q.take();
                        threadShouldThrow();
                    } catch (InterruptedException success){
                    }   
                }});
        t.start();
        try { 
           Thread.sleep(SHORT_DELAY_MS); 
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
	    unexpectedException();
        }
    }


    /**
     * Interrupted timed poll throws InterruptedException instead of
     * returning timeout status
     */
    public void testInterruptedTimedPoll() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        DelayQueue q = populatedQueue(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(new PDelay(i), ((PDelay)q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)));
                        }
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException success){
                    }   
                }});
        t.start();
        try { 
           Thread.sleep(SHORT_DELAY_MS); 
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
	    unexpectedException();
        }
    }

    /**
     *  timed poll before a delayed offer fails; after offer succeeds;
     *  on interruption throws
     */
    public void testTimedPollWithOffer() {
        final DelayQueue q = new DelayQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadFail("Should block");
                    } catch (InterruptedException success) { }                
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            assertTrue(q.offer(new PDelay(0), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }  


    /**
     * drainTo empties queue
     */ 
    public void testDrainToWithActivePut() {
        final DelayQueue q = populatedQueue(SIZE);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    q.put(new PDelay(SIZE+1));
                }
            });
        try {
            t.start();
            ArrayList l = new ArrayList();
            q.drainTo(l);
            assertTrue(l.size() >= SIZE);
            t.join();
            assertTrue(q.size() + l.size() >= SIZE);
        } catch(Exception e){
            unexpectedException();
        }
    }

}
