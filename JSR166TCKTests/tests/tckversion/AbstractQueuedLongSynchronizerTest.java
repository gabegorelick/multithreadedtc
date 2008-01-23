package tckversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */


import junit.framework.*;
import java.util.concurrent.locks.*;

public class AbstractQueuedLongSynchronizerTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AbstractQueuedLongSynchronizerTest.class);
    }

    /**
     * A simple mutex class, adapted from the
     * AbstractQueuedLongSynchronizer javadoc.  Exclusive acquire tests
     * exercise this as a sample user extension.  Other
     * methods/features of AbstractQueuedLongSynchronizerTest are tested
     * via other test classes, including those for ReentrantLock,
     * ReentrantReadWriteLock, and Semaphore
     */
    static class Mutex extends AbstractQueuedLongSynchronizer {
        // Use value > 32 bits for locked state
        static final long LOCKED = 1 << 48;
        public boolean isHeldExclusively() { 
            return getState() == LOCKED; 
        }
        
        public boolean tryAcquire(long acquires) {
            return compareAndSetState(0, LOCKED);
        }
        
        public boolean tryRelease(long releases) {
            if (getState() == 0) throw new IllegalMonitorStateException();
            setState(0);
            return true;
        }
        
        public AbstractQueuedLongSynchronizer.ConditionObject newCondition() { return new AbstractQueuedLongSynchronizer.ConditionObject(); }

    }

    
    /**
     * A simple latch class, to test shared mode.
     */
    static class BooleanLatch extends AbstractQueuedLongSynchronizer { 
        public boolean isSignalled() { return getState() != 0; }

        public long tryAcquireShared(long ignore) {
            return isSignalled()? 1 : -1;
        }
        
        public boolean tryReleaseShared(long ignore) {
            setState(1 << 62);
            return true;
        }
    }

    /**
     * A runnable calling acquireInterruptibly
     */
    class InterruptibleSyncRunnable implements Runnable {
        final Mutex sync;
        InterruptibleSyncRunnable(Mutex l) { sync = l; }
        public void run() {
            try {
                sync.acquireInterruptibly(1);
            } catch(InterruptedException success){}
        }
    }


    /**
     * A runnable calling acquireInterruptibly that expects to be
     * interrupted
     */
    class InterruptedSyncRunnable implements Runnable {
        final Mutex sync;
        InterruptedSyncRunnable(Mutex l) { sync = l; }
        public void run() {
            try {
                sync.acquireInterruptibly(1);
                threadShouldThrow();
            } catch(InterruptedException success){}
        }
    }


    /**
     * hasQueuedThreads reports whether there are waiting threads
     */
    public void testhasQueuedThreads() { 
	final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertFalse(sync.hasQueuedThreads());
            sync.acquire(1);
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasQueuedThreads());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasQueuedThreads());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasQueuedThreads());
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.hasQueuedThreads());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 


    /**
     * isQueued reports whether a thread is queued.
     */
    public void testIsQueued() { 
	final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertFalse(sync.isQueued(t1));
            assertFalse(sync.isQueued(t2));
            sync.acquire(1);
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.isQueued(t1));
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.isQueued(t1));
            assertTrue(sync.isQueued(t2));
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.isQueued(t1));
            assertTrue(sync.isQueued(t2));
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.isQueued(t1));
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.isQueued(t2));
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 

    /**
     * getFirstQueuedThread returns first waiting thread or null if none
     */
    public void testGetFirstQueuedThread() { 
	final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertNull(sync.getFirstQueuedThread());
            sync.acquire(1);
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(t1, sync.getFirstQueuedThread());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(t1, sync.getFirstQueuedThread());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(t2, sync.getFirstQueuedThread());
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertNull(sync.getFirstQueuedThread());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 


    /**
     * hasContended reports false if no thread has ever blocked, else true
     */
    public void testHasContended() { 
	final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertFalse(sync.hasContended());
            sync.acquire(1);
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasContended());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasContended());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasContended());
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasContended());
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
	final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertTrue(sync.getQueuedThreads().isEmpty());
            sync.acquire(1);
            assertTrue(sync.getQueuedThreads().isEmpty());
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getQueuedThreads().contains(t1));
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getQueuedThreads().contains(t1));
            assertTrue(sync.getQueuedThreads().contains(t2));
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.getQueuedThreads().contains(t1));
            assertTrue(sync.getQueuedThreads().contains(t2));
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getQueuedThreads().isEmpty());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 

    /**
     * getExclusiveQueuedThreads includes waiting threads
     */
    public void testGetExclusiveQueuedThreads() { 
	final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertTrue(sync.getExclusiveQueuedThreads().isEmpty());
            sync.acquire(1);
            assertTrue(sync.getExclusiveQueuedThreads().isEmpty());
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getExclusiveQueuedThreads().contains(t1));
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getExclusiveQueuedThreads().contains(t1));
            assertTrue(sync.getExclusiveQueuedThreads().contains(t2));
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.getExclusiveQueuedThreads().contains(t1));
            assertTrue(sync.getExclusiveQueuedThreads().contains(t2));
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getExclusiveQueuedThreads().isEmpty());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 

    /**
     * getSharedQueuedThreads does not include exclusively waiting threads
     */
    public void testGetSharedQueuedThreads() { 
	final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        try {
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            sync.acquire(1);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 

    /**
     * tryAcquireNanos is interruptible.
     */
    public void testInterruptedException2() { 
	final Mutex sync = new Mutex();
	sync.acquire(1);
	Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
			sync.tryAcquireNanos(1, MEDIUM_DELAY_MS * 1000 * 1000);
			threadShouldThrow();
		    } catch(InterruptedException success){}
		}
	    });
        try {
            t.start();
            t.interrupt();
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * TryAcquire on exclusively held sync fails
     */
    public void testTryAcquireWhenSynced() { 
	final Mutex sync = new Mutex();
	sync.acquire(1);
	Thread t = new Thread(new Runnable() {
                public void run() {
                    threadAssertFalse(sync.tryAcquire(1));
		}
	    });
        try {
            t.start();
            t.join();
            sync.release(1);
        } catch(Exception e){
            unexpectedException();
        }
    } 

    /**
     * tryAcquireNanos on an exclusively held sync times out
     */
    public void testAcquireNanos_Timeout() { 
	final Mutex sync = new Mutex();
	sync.acquire(1);
	Thread t = new Thread(new Runnable() {
                public void run() {
		    try {
                        threadAssertFalse(sync.tryAcquireNanos(1, 1000 * 1000));
                    } catch (Exception ex) {
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            t.join();
            sync.release(1);
        } catch(Exception e){
            unexpectedException();
        }
    } 
    
   
    /**
     * getState is true when acquired and false when not
     */
    public void testGetState() {
	final Mutex sync = new Mutex();
	sync.acquire(1);
	assertTrue(sync.isHeldExclusively());
	sync.release(1);
	assertFalse(sync.isHeldExclusively());
	Thread t = new Thread(new Runnable() { 
		public void run() {
		    sync.acquire(1);
		    try {
			Thread.sleep(SMALL_DELAY_MS);
		    }
		    catch(Exception e) {
                        threadUnexpectedException();
                    }
		    sync.release(1);
		}
	    });
	try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.isHeldExclusively());
            t.join();
            assertFalse(sync.isHeldExclusively());
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * acquireInterruptibly is interruptible.
     */
    public void testAcquireInterruptibly1() { 
	final Mutex sync = new Mutex();
	sync.acquire(1);
	Thread t = new Thread(new InterruptedSyncRunnable(sync));
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            sync.release(1);
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    } 

    /**
     * acquireInterruptibly succeeds when released, else is interruptible
     */
    public void testAcquireInterruptibly2() {
	final Mutex sync = new Mutex();	
	try {
            sync.acquireInterruptibly(1);
        } catch(Exception e) {
            unexpectedException();
        }
	Thread t = new Thread(new InterruptedSyncRunnable(sync));
        try {
            t.start();
            t.interrupt();
            assertTrue(sync.isHeldExclusively());
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * await returns when signalled
     */
    public void testAwait() {
	final Mutex sync = new Mutex();	
        final AbstractQueuedLongSynchronizer.ConditionObject c = sync.newCondition();
	Thread t = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        c.await();
                        sync.release(1);
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            c.signal();
            sync.release(1);
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }



    /**
     * hasWaiters returns true when a thread is waiting, else false
     */
    public void testHasWaiters() {
	final Mutex sync = new Mutex();	
        final AbstractQueuedLongSynchronizer.ConditionObject c = sync.newCondition();
	Thread t = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        threadAssertFalse(sync.hasWaiters(c));
                        threadAssertEquals(0, sync.getWaitQueueLength(c));
                        c.await();
                        sync.release(1);
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertTrue(sync.hasWaiters(c));
            assertEquals(1, sync.getWaitQueueLength(c));
            c.signal();
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertFalse(sync.hasWaiters(c));
            assertEquals(0, sync.getWaitQueueLength(c));
            sync.release(1);
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * getWaitQueueLength returns number of waiting threads
     */
    public void testGetWaitQueueLength() {
	final Mutex sync = new Mutex();	
        final AbstractQueuedLongSynchronizer.ConditionObject c = sync.newCondition();
	Thread t1 = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        threadAssertFalse(sync.hasWaiters(c));
                        threadAssertEquals(0, sync.getWaitQueueLength(c));
                        c.await();
                        sync.release(1);
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

	Thread t2 = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        threadAssertTrue(sync.hasWaiters(c));
                        threadAssertEquals(1, sync.getWaitQueueLength(c));
                        c.await();
                        sync.release(1);
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertTrue(sync.hasWaiters(c));
            assertEquals(2, sync.getWaitQueueLength(c));
            c.signalAll();
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertFalse(sync.hasWaiters(c));
            assertEquals(0, sync.getWaitQueueLength(c));
            sync.release(1);
            t1.join(SHORT_DELAY_MS);
            t2.join(SHORT_DELAY_MS);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * getWaitingThreads returns only and all waiting threads
     */
    public void testGetWaitingThreads() {
	final Mutex sync = new Mutex();	
        final AbstractQueuedLongSynchronizer.ConditionObject c = sync.newCondition();
	Thread t1 = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        threadAssertTrue(sync.getWaitingThreads(c).isEmpty());
                        c.await();
                        sync.release(1);
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

	Thread t2 = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        threadAssertFalse(sync.getWaitingThreads(c).isEmpty());
                        c.await();
                        sync.release(1);
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            sync.acquire(1);
            assertTrue(sync.getWaitingThreads(c).isEmpty());
            sync.release(1);
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertTrue(sync.hasWaiters(c));
            assertTrue(sync.getWaitingThreads(c).contains(t1));
            assertTrue(sync.getWaitingThreads(c).contains(t2));
            c.signalAll();
            sync.release(1);
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            assertFalse(sync.hasWaiters(c));
            assertTrue(sync.getWaitingThreads(c).isEmpty());
            sync.release(1);
            t1.join(SHORT_DELAY_MS);
            t2.join(SHORT_DELAY_MS);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }



    /**
     * awaitUninterruptibly doesn't abort on interrupt
     */
    public void testAwaitUninterruptibly() {
	final Mutex sync = new Mutex();	
        final AbstractQueuedLongSynchronizer.ConditionObject c = sync.newCondition();
	Thread t = new Thread(new Runnable() { 
		public void run() {
                    sync.acquire(1);
                    c.awaitUninterruptibly();
                    sync.release(1);
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            sync.acquire(1);
            c.signal();
            sync.release(1);
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * await is interruptible
     */
    public void testAwait_Interrupt() {
	final Mutex sync = new Mutex();	
        final AbstractQueuedLongSynchronizer.ConditionObject c = sync.newCondition();
	Thread t = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        c.await();
                        sync.release(1);
                        threadShouldThrow();
		    }
		    catch(InterruptedException success) {
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * awaitNanos is interruptible
     */
    public void testAwaitNanos_Interrupt() {
	final Mutex sync = new Mutex();	
        final AbstractQueuedLongSynchronizer.ConditionObject c = sync.newCondition();
	Thread t = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        c.awaitNanos(1000 * 1000 * 1000); // 1 sec
                        sync.release(1);
                        threadShouldThrow();
		    }
		    catch(InterruptedException success) {
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * awaitUntil is interruptible
     */
    public void testAwaitUntil_Interrupt() {
	final Mutex sync = new Mutex();	
        final AbstractQueuedLongSynchronizer.ConditionObject c = sync.newCondition();
	Thread t = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        java.util.Date d = new java.util.Date();
                        c.awaitUntil(new java.util.Date(d.getTime() + 10000));
                        sync.release(1);
                        threadShouldThrow();
		    }
		    catch(InterruptedException success) {
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * signalAll wakes up all threads
     */
    public void testSignalAll() {
	final Mutex sync = new Mutex();	
        final AbstractQueuedLongSynchronizer.ConditionObject c = sync.newCondition();
	Thread t1 = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        c.await();
                        sync.release(1);
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

	Thread t2 = new Thread(new Runnable() { 
		public void run() {
		    try {
			sync.acquire(1);
                        c.await();
                        sync.release(1);
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            sync.acquire(1);
            c.signalAll();
            sync.release(1);
            t1.join(SHORT_DELAY_MS);
            t2.join(SHORT_DELAY_MS);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }



    /**
     * acquireSharedInterruptibly returns after release, but not before
     */
    public void testAcquireSharedInterruptibly() {
	final BooleanLatch l = new BooleanLatch();

	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        threadAssertFalse(l.isSignalled());
			l.acquireSharedInterruptibly(0);
                        threadAssertTrue(l.isSignalled());
		    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
		}
	    });
	try {
            t.start();
            assertFalse(l.isSignalled());
            Thread.sleep(SHORT_DELAY_MS);
            l.releaseShared(0);
            assertTrue(l.isSignalled());
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
    

    /**
     * acquireSharedTimed returns after release
     */
    public void testAsquireSharedTimed() {
	final BooleanLatch l = new BooleanLatch();

	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        threadAssertFalse(l.isSignalled());
			threadAssertTrue(l.tryAcquireSharedNanos(0, MEDIUM_DELAY_MS* 1000 * 1000));
                        threadAssertTrue(l.isSignalled());

		    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
		}
	    });
	try {
            t.start();
            assertFalse(l.isSignalled());
            Thread.sleep(SHORT_DELAY_MS);
            l.releaseShared(0);
            assertTrue(l.isSignalled());
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
    
    /**
     * acquireSharedInterruptibly throws IE if interrupted before released
     */
    public void testAcquireSharedInterruptibly_InterruptedException() {
        final BooleanLatch l = new BooleanLatch();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(l.isSignalled());
                        l.acquireSharedInterruptibly(0);
                        threadShouldThrow();
                    } catch(InterruptedException success){}
                }
            });
	t.start();
	try {
            assertFalse(l.isSignalled());
            t.interrupt();
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * acquireSharedTimed throws IE if interrupted before released
     */
    public void testAcquireSharedNanos_InterruptedException() {
        final BooleanLatch l = new BooleanLatch();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(l.isSignalled());
                        l.tryAcquireSharedNanos(0, SMALL_DELAY_MS* 1000 * 1000);
                        threadShouldThrow();                        
                    } catch(InterruptedException success){}
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(l.isSignalled());
            t.interrupt();
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * acquireSharedTimed times out if not released before timeout
     */
    public void testAcquireSharedNanos_Timeout() {
        final BooleanLatch l = new BooleanLatch();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(l.isSignalled());
                        threadAssertFalse(l.tryAcquireSharedNanos(0, SMALL_DELAY_MS* 1000 * 1000));
                    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(l.isSignalled());
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }

    
}
