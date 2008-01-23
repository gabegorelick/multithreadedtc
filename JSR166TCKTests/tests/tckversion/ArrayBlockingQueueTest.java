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

public class ArrayBlockingQueueTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return new TestSuite(ArrayBlockingQueueTest.class);
    }

    /**
     * Create a queue of given size containing consecutive
     * Integers 0 ... n.
     */
    private ArrayBlockingQueue populatedQueue(int n) {
        ArrayBlockingQueue q = new ArrayBlockingQueue(n);
        assertTrue(q.isEmpty());
	for(int i = 0; i < n; i++)
	    assertTrue(q.offer(new Integer(i)));
        assertFalse(q.isEmpty());
        assertEquals(0, q.remainingCapacity());
	assertEquals(n, q.size());
        return q;
    }
 

    // REVIEW <=> Simple test for blocking
    /**
     * put blocks interruptibly if full
     */
    public void testBlockingPut() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        for (int i = 0; i < SIZE; ++i) {
                            q.put(new Integer(i));
                            ++added;
                        }
                        q.put(new Integer(SIZE));
                        threadShouldThrow();
                    } catch (InterruptedException ie){
                        threadAssertEquals(added, SIZE);
                    }   
                }});
        try { 
            t.start();
           Thread.sleep(MEDIUM_DELAY_MS); 
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
	    unexpectedException();
        }
    }

    // REVIEW <=> Test for blocking plus possible unblocking
    /**
     * put blocks waiting for take when full
     */
    public void testPutWithTake() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
			threadShouldThrow();
                    } catch (InterruptedException e){
                        threadAssertTrue(added >= 2);
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

    // REVIEW <=> Simple test for blocking. Particularly hazardous as this delays on the interaction of SHORT_DELAY_MS / 2
    /**
     * timed offer times out if full and elements not taken
     */
    public void testTimedOffer() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new Object());
                        q.put(new Object());
                        threadAssertFalse(q.offer(new Object(), SHORT_DELAY_MS/2, TimeUnit.MILLISECONDS));
                        q.offer(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
                    } catch (InterruptedException success){}
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

    // REVIEW <=> Simple test for blocking
    /**
     * take blocks interruptibly when empty
     */
    public void testTakeFromEmpty() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(2);
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

    // REVIEW <=> Simple test for blocking
    /**
     * Take removes existing elements until empty, then blocks interruptibly
     */
    public void testBlockingTake() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        ArrayBlockingQueue q = populatedQueue(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(i, ((Integer)q.take()).intValue());
                        }
                        q.take();
                        threadShouldThrow();
                    } catch (InterruptedException success){
                    }   
                }});
        try { 
            t.start();
            Thread.sleep(SHORT_DELAY_MS); 
            t.interrupt();
            t.join();
        }
        catch (InterruptedException ie) {
	    unexpectedException();
        }
    }



    // REVIEW <=> Simple test for blocking (using poll)
    /**
     * Interrupted timed poll throws InterruptedException instead of
     * returning timeout status
     */
    public void testInterruptedTimedPoll() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        ArrayBlockingQueue q = populatedQueue(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(i, ((Integer)q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)).intValue());
                        }
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException success){
                    }   
                }});
        try { 
            t.start();
            Thread.sleep(SHORT_DELAY_MS); 
            t.interrupt();
            t.join();
        }
        catch (InterruptedException ie) {
	    unexpectedException();
        }
    }

    // REVIEW <=> Test for blocking plus possible unblocking
    /**
     *  timed poll before a delayed offer fails; after offer succeeds;
     *  on interruption throws
     */
    public void testTimedPollWithOffer() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(2);
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
            assertTrue(q.offer(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }  


    // REVIEW <=> Test for blocking plus possible unblocking (with some correctness checks)
    /**
     * drainTo empties full queue, unblocking a waiting put.
     */ 
    public void testDrainToWithActivePut() {
        final ArrayBlockingQueue q = populatedQueue(SIZE);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new Integer(SIZE+1));
                    } catch (InterruptedException ie){ 
                        threadUnexpectedException();
                    }
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
