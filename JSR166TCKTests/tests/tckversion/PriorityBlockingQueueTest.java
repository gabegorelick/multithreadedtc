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

public class PriorityBlockingQueueTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return new TestSuite(PriorityBlockingQueueTest.class);
    }

    private static final int NOCAP = Integer.MAX_VALUE;

    /** Sample Comparator */
    static class MyReverseComparator implements Comparator { 
        public int compare(Object x, Object y) {
            int i = ((Integer)x).intValue();
            int j = ((Integer)y).intValue();
            if (i < j) return 1;
            if (i > j) return -1;
            return 0;
        }
    }

    /**
     * Create a queue of given size containing consecutive
     * Integers 0 ... n.
     */
    private PriorityBlockingQueue populatedQueue(int n) {
        PriorityBlockingQueue q = new PriorityBlockingQueue(n);
        assertTrue(q.isEmpty());
	for(int i = n-1; i >= 0; i-=2)
	    assertTrue(q.offer(new Integer(i)));
	for(int i = (n & 1); i < n; i+=2)
	    assertTrue(q.offer(new Integer(i)));
        assertFalse(q.isEmpty());
        assertEquals(NOCAP, q.remainingCapacity());
	assertEquals(n, q.size());
        return q;
    }
 

    /**
     * put doesn't block waiting for take
     */
    public void testPutWithTake() {
        final PriorityBlockingQueue q = new PriorityBlockingQueue(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.put(new Integer(0));
                        ++added;
                        q.put(new Integer(0));
                        ++added;
                        q.put(new Integer(0));
                        ++added;
                        q.put(new Integer(0));
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
        final PriorityBlockingQueue q = new PriorityBlockingQueue(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new Integer(0));
                        q.put(new Integer(0));
                        threadAssertTrue(q.offer(new Integer(0), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(q.offer(new Integer(0), LONG_DELAY_MS, TimeUnit.MILLISECONDS));
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
        final PriorityBlockingQueue q = new PriorityBlockingQueue(2);
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
                        PriorityBlockingQueue q = populatedQueue(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(i, ((Integer)q.take()).intValue());
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
                        PriorityBlockingQueue q = populatedQueue(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(i, ((Integer)q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)).intValue());
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
        final PriorityBlockingQueue q = new PriorityBlockingQueue(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
                    } catch (InterruptedException success) { }                
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            assertTrue(q.offer(new Integer(0), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
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
        final PriorityBlockingQueue q = populatedQueue(SIZE);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    q.put(new Integer(SIZE+1));
                }
            });
        try {
            t.start();
            ArrayList l = new ArrayList();
            q.drainTo(l);
            assertTrue(l.size() >= SIZE);
            for (int i = 0; i < SIZE; ++i) 
                assertEquals(l.get(i), new Integer(i));
            t.join();
            assertTrue(q.size() + l.size() >= SIZE);
        } catch(Exception e){
            unexpectedException();
        }
    }


}
