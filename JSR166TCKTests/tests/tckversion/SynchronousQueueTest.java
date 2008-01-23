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

public class SynchronousQueueTest extends JSR166TestCase {

    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }

    public static Test suite() {
	return new TestSuite(SynchronousQueueTest.class);
    }

    /**
     * put blocks interruptibly if no active taker
     */
    public void testBlockingPut() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue();
                        q.put(zero);
                        threadShouldThrow();
                    } catch (InterruptedException ie){
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
     * put blocks waiting for take 
     */
    public void testPutWithTake() {
        final SynchronousQueue q = new SynchronousQueue();
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
                        assertTrue(added >= 1);
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            q.take();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }

    /**
     * timed offer times out if elements not taken
     */
    public void testTimedOffer() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {

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
        final SynchronousQueue q = new SynchronousQueue();
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
     * put blocks interruptibly if no active taker
     */
    public void testFairBlockingPut() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue(true);
                        q.put(zero);
                        threadShouldThrow();
                    } catch (InterruptedException ie){
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
     * put blocks waiting for take 
     */
    public void testFairPutWithTake() {
        final SynchronousQueue q = new SynchronousQueue(true);
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
                        assertTrue(added >= 1);
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            q.take();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }

    /**
     * timed offer times out if elements not taken
     */
    public void testFairTimedOffer() {
        final SynchronousQueue q = new SynchronousQueue(true);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {

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
    public void testFairTakeFromEmpty() {
        final SynchronousQueue q = new SynchronousQueue(true);
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
     * Interrupted timed poll throws InterruptedException instead of
     * returning timeout status
     */
    public void testInterruptedTimedPoll() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue();
                        assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
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
        final SynchronousQueue q = new SynchronousQueue();
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
     * Interrupted timed poll throws InterruptedException instead of
     * returning timeout status
     */
    public void testFairInterruptedTimedPoll() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue(true);
                        assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
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
    public void testFairTimedPollWithOffer() {
        final SynchronousQueue q = new SynchronousQueue(true);
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
     * drainTo empties queue, unblocking a waiting put.
     */ 
    public void testDrainToWithActivePut() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new Integer(1));
                    } catch (InterruptedException ie){ 
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            ArrayList l = new ArrayList();
            Thread.sleep(SHORT_DELAY_MS);
            q.drainTo(l);
            assertTrue(l.size() <= 1);
            if (l.size() > 0)
                assertEquals(l.get(0), new Integer(1));
            t.join();
            assertTrue(l.size() <= 1);
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * drainTo(c, n) empties up to n elements of queue into c
     */ 
    public void testDrainToN() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(one);
                    } catch (InterruptedException ie){ 
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(two);
                    } catch (InterruptedException ie){ 
                        threadUnexpectedException();
                    }
                }
            });

        try {
            t1.start();
            t2.start();
            ArrayList l = new ArrayList();
            Thread.sleep(SHORT_DELAY_MS);
            q.drainTo(l, 1);
            assertTrue(l.size() == 1);
            q.drainTo(l, 1);
            assertTrue(l.size() == 2);
            assertTrue(l.contains(one));
            assertTrue(l.contains(two));
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }


}
