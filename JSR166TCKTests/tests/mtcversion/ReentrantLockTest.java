package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import java.util.*;
import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class ReentrantLockTest extends JSR166TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}
	public static Test suite() {
		return TestFramework.buildTestSuite(ReentrantLockTest.class);
	}

    /**
     * A runnable calling lockInterruptibly
     */
    class InterruptibleLockRunnable implements Runnable {
        final ReentrantLock lock;
        InterruptibleLockRunnable(ReentrantLock l) { lock = l; }
        public void run() {
            try {
                lock.lockInterruptibly();
            } catch(InterruptedException success){}
        }
    }


    /**
     * A runnable calling lockInterruptibly that expects to be
     * interrupted
     */
    class InterruptedLockRunnable implements Runnable {
        final ReentrantLock lock;
        InterruptedLockRunnable(ReentrantLock l) { lock = l; }
        public void run() {
            try {
                lock.lockInterruptibly();
                fail("should throw exception");
            } catch(InterruptedException success){}
        }
    }

    /**
     * Subclass to expose protected methods
     */
    static class PublicReentrantLock extends ReentrantLock {
        PublicReentrantLock() { super(); }
        public Collection<Thread> getQueuedThreads() { 
            return super.getQueuedThreads(); 
        }
        public Collection<Thread> getWaitingThreads(Condition c) { 
            return super.getWaitingThreads(c); 
        }


    }


    /**
     * hasQueuedThreads reports whether there are waiting threads
     */
    class TUnitTesthasQueuedThreads extends MultithreadedTest {
    	ReentrantLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		new InterruptedLockRunnable(lock).run();
    		assertTick(4);
    	}

    	public void thread2() {
    		waitForTick(3);
    		new InterruptibleLockRunnable(lock).run();
    		assertTick(5);
    	}

    	public void thread3() {
    		assertFalse(lock.hasQueuedThreads());
            lock.lock();
    		
            waitForTick(2);
    		assertTrue(lock.hasQueuedThreads());
    		
    		waitForTick(4);
    		assertTrue(lock.hasQueuedThreads());
            getThread(1).interrupt();
            
            waitForTick(5);
            assertTrue(lock.hasQueuedThreads());
            lock.unlock();            
    	}
    	
    	@Override public void finish() {
    		assertFalse(lock.hasQueuedThreads());
    	}

    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * getQueueLength reports number of waiting threads
     */
    class TUnitTestGetQueueLength extends MultithreadedTest {
    	ReentrantLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		new InterruptedLockRunnable(lock).run();
    		assertTick(4);
    	}

    	public void thread2() {
    		waitForTick(3);
    		new InterruptibleLockRunnable(lock).run();
    		assertTick(5);
    	}

    	public void thread3() {
    		assertEquals(0, lock.getQueueLength());
            lock.lock();
    		
            waitForTick(2);
            assertEquals(1, lock.getQueueLength());
    		
    		waitForTick(4);
    		assertEquals(2, lock.getQueueLength());
            getThread(1).interrupt();
            
            waitForTick(5);
            assertEquals(1, lock.getQueueLength());
            lock.unlock();            
    	}
    	
    	@Override public void finish() {
    		assertEquals(0, lock.getQueueLength());
    	}

    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * getQueueLength reports number of waiting threads
     */
    class TUnitTestGetQueueLength_fair extends MultithreadedTest {
    	ReentrantLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantLock(true);
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		new InterruptedLockRunnable(lock).run();
    		assertTick(4);
    	}

    	public void thread2() {
    		waitForTick(3);
    		new InterruptibleLockRunnable(lock).run();
    		assertTick(5);
    	}

    	public void thread3() {
    		assertEquals(0, lock.getQueueLength());
            lock.lock();
    		
            waitForTick(2);
            assertEquals(1, lock.getQueueLength());
    		
    		waitForTick(4);
    		assertEquals(2, lock.getQueueLength());
            getThread(1).interrupt();
            
            waitForTick(5);
            assertEquals(1, lock.getQueueLength());
            lock.unlock();            
    	}
    	
    	@Override public void finish() {
    		assertEquals(0, lock.getQueueLength());
    	}

    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * hasQueuedThread reports whether a thread is queued.
     */
    class TUnitTestHasQueuedThread extends MultithreadedTest {
    	ReentrantLock sync;
    	@Override public void initialize() {
    		sync = new ReentrantLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		new InterruptedLockRunnable(sync).run();
    		assertTick(4);
    	}

    	public void thread2() {
    		waitForTick(3);
    		new InterruptibleLockRunnable(sync).run();
    		assertTick(5);
    	}

    	public void thread3() {
    		assertFalse(sync.hasQueuedThread(getThread(1)));
            assertFalse(sync.hasQueuedThread(getThread(2)));
    		sync.lock();
    		
            waitForTick(2);
            assertTrue(sync.hasQueuedThread(getThread(1)));
    		
    		waitForTick(4);
    		assertTrue(sync.hasQueuedThread(getThread(1)));
            assertTrue(sync.hasQueuedThread(getThread(2)));
            getThread(1).interrupt();
            
            waitForTick(5);
            assertFalse(sync.hasQueuedThread(getThread(1)));
            assertTrue(sync.hasQueuedThread(getThread(2)));
            sync.unlock();            
    	}
    	
    	@Override public void finish() {
    		assertFalse(sync.hasQueuedThread(getThread(1)));
    		assertFalse(sync.hasQueuedThread(getThread(2)));
    	}

    }
    // TUNIT Untimed Interleave/Synchronize
    

    /**
     * getQueuedThreads includes waiting threads
     */
    class TUnitTestGetQueuedThreads extends MultithreadedTest {
    	PublicReentrantLock lock;
    	@Override public void initialize() {
    		lock = new PublicReentrantLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		new InterruptedLockRunnable(lock).run();
    		assertTick(4);
    	}

    	public void thread2() {
    		waitForTick(3);
    		new InterruptibleLockRunnable(lock).run();
    		assertTick(5);
    	}

    	public void thread3() {
    		assertTrue(lock.getQueuedThreads().isEmpty());
            lock.lock();
            assertTrue(lock.getQueuedThreads().isEmpty());
    		
            waitForTick(2);
            assertTrue(lock.getQueuedThreads().contains(getThread(1)));
    		
    		waitForTick(4);
    		assertTrue(lock.getQueuedThreads().contains(getThread(1)));
            assertTrue(lock.getQueuedThreads().contains(getThread(2)));
            getThread(1).interrupt();
            
            waitForTick(5);
            assertFalse(lock.getQueuedThreads().contains(getThread(1)));
            assertTrue(lock.getQueuedThreads().contains(getThread(2)));
            lock.unlock();            
    	}
    	
    	@Override public void finish() {
    		assertTrue(lock.getQueuedThreads().isEmpty());
    	}

    }

    // TUNIT Untimed Interleave/Synchronize


    /**
     * timed tryLock is interruptible.
     */
    class TUnitTestInterruptedException2 extends MultithreadedTest {
    	ReentrantLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		lock.lock();
    	}
    	
    	public void thread1() {    		
    		try {
    			lock.tryLock(MEDIUM_DELAY_MS,TimeUnit.MILLISECONDS);
    			fail("should throw exception");
    		} catch(InterruptedException success){ assertTick(1); }    		
    	}

    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }
    // TUNIT Timed Interrupt/Cancel


    /**
     * TryLock on a locked lock fails
     */
    class TUnitTestTryLockWhenLocked extends MultithreadedTest {
    	ReentrantLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		lock.lock();
    	}
    	
    	public void thread1() {    		
    		assertFalse(lock.tryLock());
    	}
    	
    	@Override public void finish() {
    		lock.unlock();
    	}
    }
    // TUNIT Untimed No block or interrupt


    /**
     * Timed tryLock on a locked lock times out
     */
    class TUnitTestTryLock_Timeout extends MultithreadedTest {
    	ReentrantLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		lock.lock();
    	}
    	
    	public void thread1() throws InterruptedException {    		
    		assertFalse(lock.tryLock(1, TimeUnit.MILLISECONDS));
    	}
    	
    	@Override public void finish() {
    		lock.unlock();
    	}
    }
    // TUNIT Timed Block/Wait

   
    /**
     * isLocked is true when locked and false when not
     */
    class TUnitTestIsLocked extends MultithreadedTest {
    	ReentrantLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		lock.lock();
    		assertTrue(lock.isLocked());
    		lock.unlock();
    		assertFalse(lock.isLocked());
    	}
    	
    	public void thread1() {
		    lock.lock();
		    waitForTick(2);
		    lock.unlock();
    	}

    	public void thread2() {
		    waitForTick(1);
		    assertTrue(lock.isLocked());
    	}
    	
    	@Override public void finish() {
            assertFalse(lock.isLocked());
    	}

    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * lockInterruptibly is interruptible.
     */
    class TUnitTestLockInterruptibly1 extends MultithreadedTest {
    	ReentrantLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantLock();    		
    		lock.lock();
    	}
    	
    	public void thread1() {
    		new InterruptedLockRunnable(lock).run();
    		assertTick(1);
    	}

    	public void thread2() {
            waitForTick(1);
            getThread(1).interrupt();            
    	}
    	
    	@Override public void finish() {
            lock.unlock();            
    	}
    }
    // TUNIT Untimed Interrupt/Cancel


    /**
     * lockInterruptibly succeeds when unlocked, else is interruptible
     */
    class TUnitTestLockInterruptibly2 extends MultithreadedTest {
    	ReentrantLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		new InterruptedLockRunnable(lock).run();
    		assertTick(2);
    	}

    	public void thread2() throws InterruptedException {
    		lock.lockInterruptibly();
    		waitForTick(2);
            getThread(1).interrupt();            
            assertTrue(lock.isLocked());
            assertTrue(lock.isHeldByCurrentThread());
    	}
    }
    // TUNIT Untimed Interrupt/Cancel


    /**
     * await returns when signalled
     */
    class TUnitTestAwait extends MultithreadedTest {
    	ReentrantLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		c = lock.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			lock.lock();
            c.await();
            assertTick(1);
            lock.unlock();
    	}

    	public void thread2() throws InterruptedException {
            waitForTick(1);
            lock.lock();
            c.signal();
            lock.unlock();
            getThread(1).join(SHORT_DELAY_MS);
            assertFalse(getThread(1).isAlive());
    	}
    }
    // TUNIT Untimed Block/Wait


    /**
     * hasWaiters returns true when a thread is waiting, else false
     */
    class TUnitTestHasWaiters extends MultithreadedTest {
    	ReentrantLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		c = lock.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			lock.lock();
			assertFalse(lock.hasWaiters(c));
			assertEquals(0, lock.getWaitQueueLength(c));
			c.await();
			assertTick(1);
			lock.unlock();
    	}

    	public void thread2() throws InterruptedException {
    		waitForTick(1);
    		lock.lock();
    		assertTrue(lock.hasWaiters(c));
    		assertEquals(1, lock.getWaitQueueLength(c));
    		c.signal();
    		lock.unlock();
    		
    		waitForTick(2);
    		lock.lock();
    		assertFalse(lock.hasWaiters(c));
    		assertEquals(0, lock.getWaitQueueLength(c));
    		lock.unlock();
    		
    		getThread(1).join(SHORT_DELAY_MS);
    		assertFalse(getThread(1).isAlive());
    	}
    }
    // TUNIT Untimed Interrupt/Cancel
    // TUNIT Untimed Block/Wait


    /**
     * getWaitQueueLength returns number of waiting threads
     */
    class TUnitTestGetWaitQueueLength extends MultithreadedTest {
    	ReentrantLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		c = lock.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			lock.lock();
            assertFalse(lock.hasWaiters(c));
            assertEquals(0, lock.getWaitQueueLength(c));
            c.await();

            assertTick(2);
            lock.unlock();
    	}

    	public void thread2() throws InterruptedException {
    		waitForTick(1);
			lock.lock();
            assertTrue(lock.hasWaiters(c));
            assertEquals(1, lock.getWaitQueueLength(c));
            c.await();

            assertTick(2);
            lock.unlock();
    	}

    	public void thread3() {
            waitForTick(2);
            lock.lock();
            assertTrue(lock.hasWaiters(c));
            assertEquals(2, lock.getWaitQueueLength(c));
            c.signalAll();
            lock.unlock();

            waitForTick(3);
            lock.lock();
            assertFalse(lock.hasWaiters(c));
            assertEquals(0, lock.getWaitQueueLength(c));
            lock.unlock();
    	}
    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * getWaitingThreads returns only and all waiting threads
     */
    class TUnitTestGetWaitingThreads extends MultithreadedTest {
    	PublicReentrantLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new PublicReentrantLock();
    		c = lock.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
    		waitForTick(1);
			lock.lock();
            assertTrue(lock.getWaitingThreads(c).isEmpty());
            c.await();

            assertTick(3);
            lock.unlock();
    	}

    	public void thread2() throws InterruptedException {
    		waitForTick(2);
			lock.lock();
            assertFalse(lock.getWaitingThreads(c).isEmpty());
            c.await();
    		
            assertTick(3);
            lock.unlock();
    	}

    	public void thread3() {
            lock.lock();
            assertTrue(lock.getWaitingThreads(c).isEmpty());
            lock.unlock();

    		waitForTick(3);
            lock.lock();
            assertTrue(lock.hasWaiters(c));
            assertTrue(lock.getWaitingThreads(c).contains(getThread(1)));
            assertTrue(lock.getWaitingThreads(c).contains(getThread(2)));
            c.signalAll();
            lock.unlock();
            
    		waitForTick(4);
            lock.lock();
            assertFalse(lock.hasWaiters(c));
            assertTrue(lock.getWaitingThreads(c).isEmpty());
            lock.unlock();
    	}
    }
    // TUNIT Untimed Interleave/Synchronize


    /** A helper class for uninterruptible wait tests - Multithreaded Version*/
    class UninterruptableThread_MT extends Thread {
        private ReentrantLock lock;
        private Condition c;
        private MultithreadedTest mt;
        private int tick; // the tick to wait for
        
        public volatile boolean canAwake = false;
        public volatile boolean interrupted = false;
        
        public UninterruptableThread_MT(ReentrantLock lock, Condition c, 
        		MultithreadedTest mt, int tick) {
            this.lock = lock;
            this.c = c;
            this.mt = mt;
        }
        
        public synchronized void run() {
            lock.lock();
            mt.waitForTick(tick);
            
            while (!canAwake) {
                c.awaitUninterruptibly();
            }
            
            interrupted = isInterrupted();
            lock.unlock();
        }
    }

    /**
     * awaitUninterruptibly doesn't abort on interrupt
     */
    class TUnitTestAwaitUninterruptibly extends MultithreadedTest {
    	ReentrantLock lock;
    	Condition c;
    	UninterruptableThread_MT thread;
    	final int LOCK_STARTED = 1;
    	
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		c = lock.newCondition();
    		thread = new UninterruptableThread_MT(lock, c, this, LOCK_STARTED);
    	}
    	
    	public void thread1() throws InterruptedException {
            thread.start();

            waitForTick(LOCK_STARTED); // synchronize both threads

            lock.lock();
            try {
                thread.interrupt();
                thread.canAwake = true;
                c.signal();
            } finally {
                lock.unlock();
            }
    	}
    	
    	@Override public void finish() {
            assertTrue(thread.interrupted);
            assertFalse(thread.isAlive());
    	}
    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * await is interruptible
     */
    class TUnitTestAwait_Interrupt extends MultithreadedTest {
    	ReentrantLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		c = lock.newCondition();
    	}
    	
    	public void thread1() {
			try {
				lock.lock();
				c.await();
				lock.unlock();
				fail("should throw exception");
			}
			catch(InterruptedException success) { assertTick(1); }
    	}

    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }
    // TUNIT Untimed Interrupt/Cancel


    /**
     * awaitNanos is interruptible
     */
    class TUnitTestAwaitNanos_Interrupt extends MultithreadedTest {
    	ReentrantLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		c = lock.newCondition();
    	}
    	
    	public void thread1() {
			try {
				lock.lock();
				c.awaitNanos(1000 * 1000 * 1000); // 1 sec
				lock.unlock();
				fail("should throw exception");
			}
			catch(InterruptedException success) { assertTick(1); }
    	}

    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }
    // TUNIT Timed Interrupt/Cancel


    /**
     * awaitUntil is interruptible
     */
    class TUnitTestAwaitUntil_Interrupt extends MultithreadedTest {
    	ReentrantLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		c = lock.newCondition();
    	}
    	
    	public void thread1() {
			try {
				lock.lock();
				java.util.Date d = new java.util.Date();
				c.awaitUntil(new java.util.Date(d.getTime() + 10000));
				lock.unlock();
				fail("should throw exception");
			}
			catch(InterruptedException success) { assertTick(1); }
    	}

    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }
    // TUNIT Timed Interrupt/Cancel


    /**
     * signalAll wakes up all threads
     */
    class TUnitTestSignalAll extends MultithreadedTest {
    	ReentrantLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		c = lock.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			lock.lock();
			c.await();
			assertTick(1);
			lock.unlock();
    	}

    	public void thread2() throws InterruptedException {
			lock.lock();
			c.await();
			assertTick(1);
			lock.unlock();
    	}

    	public void thread3() {
    		waitForTick(1);
    		lock.lock();
    		c.signalAll();
    		lock.unlock();
    	}
    }
    // TUNIT Untimed Block/Wait


    /**
     * await after multiple reentrant locking preserves lock count
     */
    class TUnitTestAwaitLockCount extends MultithreadedTest {
    	ReentrantLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantLock();
    		c = lock.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			lock.lock();
			assertEquals(1, lock.getHoldCount());
			c.await();
			
			assertTick(1);
			assertEquals(1, lock.getHoldCount());
			lock.unlock();
    	}

    	public void thread2() throws InterruptedException {
			lock.lock();
			lock.lock();
			assertEquals(2, lock.getHoldCount());
			c.await();
			
			assertTick(1);
			assertEquals(2, lock.getHoldCount());
			lock.unlock();
			lock.unlock();
    	}

    	public void thread3() {
    		waitForTick(1);
    		lock.lock();
    		c.signalAll();
    		lock.unlock();
    	}
    }
    // TUNIT Untimed Block/Wait

}
