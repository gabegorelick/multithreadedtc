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

public class SemaphoreTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return new TestSuite(SemaphoreTest.class);
    }

    /**
     * Subclass to expose protected methods
     */
    static class PublicSemaphore extends Semaphore {
        PublicSemaphore(int p, boolean f) { super(p, f); }
        public Collection<Thread> getQueuedThreads() { 
            return super.getQueuedThreads(); 
        }
        public void reducePermits(int p) { 
            super.reducePermits(p);
        }
    }

    /**
     * A runnable calling acquire
     */
    class InterruptibleLockRunnable implements Runnable {
        final Semaphore lock;
        InterruptibleLockRunnable(Semaphore l) { lock = l; }
        public void run() {
            try {
                lock.acquire();
            } catch(InterruptedException success){}
        }
    }


    /**
     * A runnable calling acquire that expects to be
     * interrupted
     */
    class InterruptedLockRunnable implements Runnable {
        final Semaphore lock;
        InterruptedLockRunnable(Semaphore l) { lock = l; }
        public void run() {
            try {
                lock.acquire();
                threadShouldThrow();
            } catch(InterruptedException success){}
        }
    }


    /**
     * A release in one thread enables an acquire in another thread
     */
    public void testAcquireReleaseInDifferentThreads() {
        final Semaphore s = new Semaphore(0, false);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire();
                        s.release();
                        s.release();
                        s.acquire();
		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            s.release();
            s.release();
            s.acquire();
            s.acquire();
            s.release();
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A release in one thread enables an uninterruptible acquire in another thread
     */
    public void testUninterruptibleAcquireReleaseInDifferentThreads() {
        final Semaphore s = new Semaphore(0, false);
	Thread t = new Thread(new Runnable() {
		public void run() {
                    s.acquireUninterruptibly();
                    s.release();
                    s.release();
                    s.acquireUninterruptibly();
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            s.release();
            s.release();
            s.acquireUninterruptibly();
            s.acquireUninterruptibly();
            s.release();
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }


    /**
     *  A release in one thread enables a timed acquire in another thread
     */
    public void testTimedAcquireReleaseInDifferentThreads() {
        final Semaphore s = new Semaphore(1, false);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        s.release();
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        s.release();
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));

		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            s.release();
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A waiting acquire blocks interruptibly
     */
    public void testAcquire_InterruptedException() {
	final Semaphore s = new Semaphore(0, false);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire();
			threadShouldThrow();
		    } catch(InterruptedException success){}
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    
    /**
     *  A waiting timed acquire blocks interruptibly
     */
    public void testTryAcquire_InterruptedException() {
	final Semaphore s = new Semaphore(0, false);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.tryAcquire(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
		    } catch(InterruptedException success){
                    }
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * hasQueuedThreads reports whether there are waiting threads
     */
    public void testHasQueuedThreads() { 
	final Semaphore lock = new Semaphore(1, false);
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertFalse(lock.hasQueuedThreads());
            lock.acquireUninterruptibly();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            lock.release();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(lock.hasQueuedThreads());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 

    /**
     * getQueueLength reports number of waiting threads
     */
    public void testGetQueueLength() { 
	final Semaphore lock = new Semaphore(1, false);
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertEquals(0, lock.getQueueLength());
            lock.acquireUninterruptibly();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(2, lock.getQueueLength());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            lock.release();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(0, lock.getQueueLength());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 

    /**
     * getQueuedThreads includes waiting threads
     */
    public void testGetQueuedThreads() { 
	final PublicSemaphore lock = new PublicSemaphore(1, false);
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertTrue(lock.getQueuedThreads().isEmpty());
            lock.acquireUninterruptibly();
            assertTrue(lock.getQueuedThreads().isEmpty());
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.getQueuedThreads().contains(t1));
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.getQueuedThreads().contains(t1));
            assertTrue(lock.getQueuedThreads().contains(t2));
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(lock.getQueuedThreads().contains(t1));
            assertTrue(lock.getQueuedThreads().contains(t2));
            lock.release();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.getQueuedThreads().isEmpty());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 


    /**
     * A release in one thread enables an acquire in another thread
     */
    public void testAcquireReleaseInDifferentThreads_fair() {
        final Semaphore s = new Semaphore(0, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire();
                        s.acquire();
                        s.acquire();
                        s.acquire();
		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            s.release();
            s.release();
            s.release();
            s.release();
            s.release();
            s.release();
            t.join();
            assertEquals(2, s.availablePermits());
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * release(n) in one thread enables acquire(n) in another thread
     */
    public void testAcquireReleaseNInDifferentThreads_fair() {
        final Semaphore s = new Semaphore(0, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire();
                        s.release(2);
                        s.acquire();
		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            s.release(2);
            s.acquire(2);
            s.release(1);
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * release(n) in one thread enables acquire(n) in another thread
     */
    public void testAcquireReleaseNInDifferentThreads_fair2() {
        final Semaphore s = new Semaphore(0, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        s.acquire(2);
                        s.acquire(2);
                        s.release(4);
		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            s.release(6);
            s.acquire(2);
            s.acquire(2);
            s.release(2);
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }





    /**
     * release in one thread enables timed acquire in another thread
     */
    public void testTimedAcquireReleaseInDifferentThreads_fair() {
        final Semaphore s = new Semaphore(1, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));

		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
	t.start();
        try {
            s.release();
            s.release();
            s.release();
            s.release();
            s.release();
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * release(n) in one thread enables timed acquire(n) in another thread
     */
    public void testTimedAcquireReleaseNInDifferentThreads_fair() {
        final Semaphore s = new Semaphore(2, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        threadAssertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        s.release(2);
                        threadAssertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        s.release(2);
		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
	t.start();
        try {
            assertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release(2);
            assertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release(2);
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A waiting acquire blocks interruptibly
     */
    public void testAcquire_InterruptedException_fair() {
	final Semaphore s = new Semaphore(0, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire();
			threadShouldThrow();
		    } catch(InterruptedException success){}
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A waiting acquire(n) blocks interruptibly
     */
    public void testAcquireN_InterruptedException_fair() {
	final Semaphore s = new Semaphore(2, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire(3);
			threadShouldThrow();
		    } catch(InterruptedException success){}
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    
    /**
     *  A waiting tryAcquire blocks interruptibly
     */
    public void testTryAcquire_InterruptedException_fair() {
	final Semaphore s = new Semaphore(0, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.tryAcquire(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
		    } catch(InterruptedException success){
                    }
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     *  A waiting tryAcquire(n) blocks interruptibly
     */
    public void testTryAcquireN_InterruptedException_fair() {
	final Semaphore s = new Semaphore(1, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.tryAcquire(4, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
		    } catch(InterruptedException success){
                    }
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * getQueueLength reports number of waiting threads
     */
    public void testGetQueueLength_fair() { 
	final Semaphore lock = new Semaphore(1, true);
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertEquals(0, lock.getQueueLength());
            lock.acquireUninterruptibly();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(2, lock.getQueueLength());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            lock.release();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(0, lock.getQueueLength());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 


}
