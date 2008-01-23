package tckversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

import junit.framework.*;
import java.util.*;
import java.util.concurrent.*;

public class LinkedBlockingDequeTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }

    public static Test suite() {
	return new TestSuite(LinkedBlockingDequeTest.class);
    }

    /**
     * Create a deque of given size containing consecutive
     * Integers 0 ... n.
     */
    private LinkedBlockingDeque populatedDeque(int n) {
        LinkedBlockingDeque q = new LinkedBlockingDeque(n);
        assertTrue(q.isEmpty());
	for(int i = 0; i < n; i++)
	    assertTrue(q.offer(new Integer(i)));
        assertFalse(q.isEmpty());
        assertEquals(0, q.remainingCapacity());
	assertEquals(n, q.size());
        return q;
    }

    /**
     * put blocks interruptibly if full
     */
    public void testBlockingPut() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        LinkedBlockingDeque q = new LinkedBlockingDeque(SIZE);
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
     * put blocks waiting for take when full
     */
    public void testPutWithTake() {
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
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

    /**
     * timed offer times out if full and elements not taken
     */
    public void testTimedOffer() {
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new Object());
                        q.put(new Object());
                        threadAssertFalse(q.offer(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.offer(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
                    } catch (InterruptedException success){}
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
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
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
                        LinkedBlockingDeque q = populatedDeque(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            assertEquals(i, ((Integer)q.take()).intValue());
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
                        LinkedBlockingDeque q = populatedDeque(SIZE);
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
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
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


    /**
     * putFirst blocks interruptibly if full
     */
    public void testBlockingPutFirst() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        LinkedBlockingDeque q = new LinkedBlockingDeque(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            q.putFirst(new Integer(i));
                            ++added;
                        }
                        q.putFirst(new Integer(SIZE));
                        threadShouldThrow();
                    } catch (InterruptedException ie){
                        threadAssertEquals(added, SIZE);
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
     * putFirst blocks waiting for take when full
     */
    public void testPutFirstWithTake() {
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.putFirst(new Object());
                        ++added;
                        q.putFirst(new Object());
                        ++added;
                        q.putFirst(new Object());
                        ++added;
                        q.putFirst(new Object());
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

    /**
     * timed offerFirst times out if full and elements not taken
     */
    public void testTimedOfferFirst() {
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.putFirst(new Object());
                        q.putFirst(new Object());
                        threadAssertFalse(q.offerFirst(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.offerFirst(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
                    } catch (InterruptedException success){}
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
     * takeFirst blocks interruptibly when empty
     */
    public void testTakeFirstFromEmpty() {
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.takeFirst();
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
     * TakeFirst removes existing elements until empty, then blocks interruptibly
     */
    public void testBlockingTakeFirst() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        LinkedBlockingDeque q = populatedDeque(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            assertEquals(i, ((Integer)q.takeFirst()).intValue());
                        }
                        q.takeFirst();
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
     * Interrupted timed pollFirst throws InterruptedException instead of
     * returning timeout status
     */
    public void testInterruptedTimedPollFirst() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        LinkedBlockingDeque q = populatedDeque(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(i, ((Integer)q.pollFirst(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)).intValue());
                        }
                        threadAssertNull(q.pollFirst(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
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
     *  timed pollFirst before a delayed offerFirst fails; after offerFirst succeeds;
     *  on interruption throws
     */
    public void testTimedPollFirstWithOfferFirst() {
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertNull(q.pollFirst(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.pollFirst(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        q.pollFirst(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
                    } catch (InterruptedException success) { }                
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            assertTrue(q.offerFirst(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }  


    /**
     * putLast blocks interruptibly if full
     */
    public void testBlockingPutLast() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        LinkedBlockingDeque q = new LinkedBlockingDeque(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            q.putLast(new Integer(i));
                            ++added;
                        }
                        q.putLast(new Integer(SIZE));
                        threadShouldThrow();
                    } catch (InterruptedException ie){
                        threadAssertEquals(added, SIZE);
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
     * putLast blocks waiting for take when full
     */
    public void testPutLastWithTake() {
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.putLast(new Object());
                        ++added;
                        q.putLast(new Object());
                        ++added;
                        q.putLast(new Object());
                        ++added;
                        q.putLast(new Object());
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

    /**
     * timed offerLast times out if full and elements not taken
     */
    public void testTimedOfferLast() {
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.putLast(new Object());
                        q.putLast(new Object());
                        threadAssertFalse(q.offerLast(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.offerLast(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
                    } catch (InterruptedException success){}
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
     * takeLast blocks interruptibly when empty
     */
    public void testTakeLastFromEmpty() {
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.takeLast();
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
     * TakeLast removes existing elements until empty, then blocks interruptibly
     */
    public void testBlockingTakeLast() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        LinkedBlockingDeque q = populatedDeque(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            assertEquals(SIZE-i-1, ((Integer)q.takeLast()).intValue());
                        }
                        q.takeLast();
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
     * Interrupted timed pollLast throws InterruptedException instead of
     * returning timeout status
     */
    public void testInterruptedTimedPollLast() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        LinkedBlockingDeque q = populatedDeque(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(SIZE-i-1, ((Integer)q.pollLast(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)).intValue());
                        }
                        threadAssertNull(q.pollLast(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
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
     *  timed poll before a delayed offerLast fails; after offerLast succeeds;
     *  on interruption throws
     */
    public void testTimedPollWithOfferLast() {
        final LinkedBlockingDeque q = new LinkedBlockingDeque(2);
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
            assertTrue(q.offerLast(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }  


    /**
     * drainTo empties full deque, unblocking a waiting put.
     */ 
    public void testDrainToWithActivePut() {
        final LinkedBlockingDeque q = populatedDeque(SIZE);
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
