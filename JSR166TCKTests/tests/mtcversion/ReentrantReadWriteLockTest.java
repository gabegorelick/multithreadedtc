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

public class ReentrantReadWriteLockTest extends JSR166TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}
	public static Test suite() {
		return TestFramework.buildTestSuite(ReentrantReadWriteLockTest.class);
	}

    /**
     * A runnable calling lockInterruptibly
     */
    class InterruptibleLockRunnable implements Runnable {
        final ReentrantReadWriteLock lock;
        InterruptibleLockRunnable(ReentrantReadWriteLock l) { lock = l; }
        public void run() {
            try {
                lock.writeLock().lockInterruptibly();
            } catch(InterruptedException success){}
        }
    }


    /**
     * A runnable calling lockInterruptibly that expects to be
     * interrupted
     */
    class InterruptedLockRunnable implements Runnable {
        final ReentrantReadWriteLock lock;
        InterruptedLockRunnable(ReentrantReadWriteLock l) { lock = l; }
        public void run() {
            try {
                lock.writeLock().lockInterruptibly();
                fail("should throw exception");
            } catch(InterruptedException success){}
        }
    }

    /**
     * Subclass to expose protected methods
     */
    static class PublicReentrantReadWriteLock extends ReentrantReadWriteLock {
        PublicReentrantReadWriteLock() { super(); }
        public Collection<Thread> getQueuedThreads() { 
            return super.getQueuedThreads(); 
        }
        public Collection<Thread> getWaitingThreads(Condition c) { 
            return super.getWaitingThreads(c); 
        }
    }


    /**
     * write-lockInterruptibly is interruptible
     */  
    class TUnitTestWriteLockInterruptibly_Interrupted extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			try {
				lock.writeLock().lockInterruptibly();				
				lock.writeLock().unlock();
				lock.writeLock().lockInterruptibly();
				lock.writeLock().unlock();
			} catch(InterruptedException success){ assertTick(2); }
    	}

    	public void thread2() {
    		lock.writeLock().lock();
    		
    		waitForTick(2);
    		getThread(1).interrupt();
    		
    		waitForTick(3);
    		lock.writeLock().unlock();            
    	}
    }
    // TUNIT Untimed Interrupt/Cancel


    /**
     * timed write-tryLock is interruptible
     */
    class TUnitTestWriteTryLock_Interrupted extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			try {
				lock.writeLock().tryLock(1000,TimeUnit.MILLISECONDS);
			} catch(InterruptedException success){ assertTick(2); }
    	}
    	
    	public void thread2() {  
    		lock.writeLock().lock();
    		
    		waitForTick(2);
    		getThread(1).interrupt();
    		lock.writeLock().unlock();
    	}
    }   
    // TUNIT Timed Interrupt/Cancel


    /**
     * read-lockInterruptibly is interruptible
     */
    class TUnitTestReadLockInterruptibly_Interrupted extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			try {
				lock.readLock().lockInterruptibly();				
			} catch(InterruptedException success){ assertTick(2); }
    	}

    	public void thread2() {
    		lock.writeLock().lock();
    		
    		waitForTick(2);
    		getThread(1).interrupt();
    		
    		waitForTick(3);
    		lock.writeLock().unlock();            
    	}
    }
    // TUNIT Untimed Interrupt/Cancel


    /**
     * timed read-tryLock is interruptible
     */
    class TUnitTestReadTryLock_Interrupted extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			try {
				lock.readLock().tryLock(1000,TimeUnit.MILLISECONDS);
			} catch(InterruptedException success){ assertTick(2); }
    	}
    	
    	public void thread2() {  
    		lock.writeLock().lock();
    		
    		waitForTick(2);
    		getThread(1).interrupt();
    	}
    }  
    // TUNIT Timed Interrupt/Cancel


    /**
     * write-tryLock fails if locked
     */
    class TUnitTestWriteTryLockWhenLocked extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		assertFalse(lock.writeLock().tryLock());
    	}
    	
    	public void thread2() { 
    		lock.writeLock().lock();
    		waitForTick(2);
    		lock.writeLock().unlock();
    	}
    }   
    // TUNIT Slave thread exhibits specific behavior because of an action in Master thread


    /**
     * read-tryLock fails if locked
     */
    class TUnitTestReadTryLockWhenLocked extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		assertFalse(lock.readLock().tryLock());
    	}
    	
    	public void thread2() { 
    		lock.writeLock().lock();
    		waitForTick(2);
    		lock.writeLock().unlock();
    	}
    }   
    // TUNIT Slave thread exhibits specific behavior because of an action in Master thread


    /**
     * Multiple threads can hold a read lock when not write-locked
     */
    class TUnitTestMultipleReadLocks extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			assertTrue(lock.readLock().tryLock());
			lock.readLock().unlock();
    	}
    	
    	public void thread2() {   
    		lock.readLock().lock();
    		waitForTick(2);
    		lock.readLock().unlock();
    	}
    }   
    // TUNIT Untimed Interleave/Synchronize


    /**
     * A writelock succeeds after reading threads unlock
     */
    class TUnitTestWriteAfterMultipleReadLocks extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);    		
    		lock.readLock().lock();
			lock.readLock().unlock();
    	}
    	
    	public void thread2() {   
    		waitForTick(1);    		
    		lock.writeLock().lock();
			lock.writeLock().unlock();
			assertTick(2);
    	}

    	public void thread3() {   
    		lock.readLock().lock();
    		waitForTick(2);
    		lock.readLock().unlock();
    	}
    } 
    // TUNIT Untimed Interleave/Synchronize


    /**
     * Readlocks succeed after a writing thread unlocks
     */
    class TUnitTestReadAfterWriteLock extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			lock.readLock().lock();
			assertTick(2);
			lock.readLock().unlock();
    	}
    	
    	public void thread2() {
    		waitForTick(1);
			lock.readLock().lock();
			assertTick(2);
			lock.readLock().unlock();
    	}

    	public void thread3() {   
    		lock.writeLock().lock();
    		waitForTick(2);
    		lock.writeLock().unlock();
    	}
    }  
    // TUNIT Untimed Block/Wait


    /**
     * Read lock succeeds if write locked by current thread even if
     * other threads are waiting for readlock
     */
    class TUnitTestReadHoldingWriteLock2 extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			lock.readLock().lock();
			assertTick(2);
			lock.readLock().unlock();
    	}
    	
    	public void thread2() {
    		waitForTick(1);
			lock.readLock().lock();
			assertTick(2);
			lock.readLock().unlock();
    	}

    	public void thread3() {   
    		lock.writeLock().lock();
    		waitForTick(1);
    		lock.readLock().lock();
    		lock.readLock().unlock();
    		waitForTick(2);
    		lock.readLock().lock();
    		lock.readLock().unlock();
    		lock.writeLock().unlock();
    	}
    }  
    // TUNIT Untimed Block/Wait


    /**
     *  Read lock succeeds if write locked by current thread even if
     * other threads are waiting for writelock
     */
    class TUnitTestReadHoldingWriteLock3 extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			lock.writeLock().lock();
			assertTick(2);
			lock.writeLock().unlock();
    	}
    	
    	public void thread2() {
    		waitForTick(1);
			lock.writeLock().lock();
			assertTick(2);
			lock.writeLock().unlock();
    	}

    	public void thread3() {   
    		lock.writeLock().lock();
    		waitForTick(1);
    		lock.readLock().lock();
    		lock.readLock().unlock();
    		waitForTick(2);
    		lock.readLock().lock();
    		lock.readLock().unlock();
    		lock.writeLock().unlock();
    	}
    } 
    // TUNIT Untimed Block/Wait


    /**
     *  Write lock succeeds if write locked by current thread even if
     * other threads are waiting for writelock
     */
    class TUnitTestReadHoldingWriteLock4 extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			lock.writeLock().lock();
			assertTick(2);
			lock.writeLock().unlock();
    	}
    	
    	public void thread2() {
    		waitForTick(1);
			lock.writeLock().lock();
			assertTick(2);
			lock.writeLock().unlock();
    	}

    	public void thread3() {   
    		lock.writeLock().lock();
    		waitForTick(1);
    		lock.writeLock().lock();
    		lock.writeLock().unlock();
    		waitForTick(2);
    		lock.writeLock().lock();
    		lock.writeLock().unlock();
    		lock.writeLock().unlock();
    	}
    }   
    // TUNIT Untimed Block/Wait


    /**
     * Fair Read lock succeeds if write locked by current thread even if
     * other threads are waiting for readlock
     */
    class TUnitTestReadHoldingWriteLockFair2 extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock(true);
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			lock.readLock().lock();
			assertTick(2);
			lock.readLock().unlock();
    	}
    	
    	public void thread2() {
    		waitForTick(1);
			lock.readLock().lock();
			assertTick(2);
			lock.readLock().unlock();
    	}

    	public void thread3() {   
    		lock.writeLock().lock();
    		waitForTick(1);
    		lock.readLock().lock();
    		lock.readLock().unlock();
    		waitForTick(2);
    		lock.readLock().lock();
    		lock.readLock().unlock();
    		lock.writeLock().unlock();
    	}
    }   
    // TUNIT Untimed Block/Wait


    /**
     * Fair Read lock succeeds if write locked by current thread even if
     * other threads are waiting for writelock
     */
    class TUnitTestReadHoldingWriteLockFair3 extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock(true);
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			lock.writeLock().lock();
			assertTick(2);
			lock.writeLock().unlock();
    	}
    	
    	public void thread2() {
    		waitForTick(1);
			lock.writeLock().lock();
			assertTick(2);
			lock.writeLock().unlock();
    	}

    	public void thread3() {   
    		lock.writeLock().lock();
    		waitForTick(1);
    		lock.readLock().lock();
    		lock.readLock().unlock();
    		waitForTick(2);
    		lock.readLock().lock();
    		lock.readLock().unlock();
    		lock.writeLock().unlock();
    	}
    }    
    // TUNIT Untimed Block/Wait


    /**
     * Fair Write lock succeeds if write locked by current thread even if
     * other threads are waiting for writelock
     */
    class TUnitTestReadHoldingWriteLockFair4 extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock(true);
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			lock.writeLock().lock();
			assertTick(2);
			lock.writeLock().unlock();
    	}
    	
    	public void thread2() {
    		waitForTick(1);
			lock.writeLock().lock();
			assertTick(2);
			lock.writeLock().unlock();
    	}

    	public void thread3() {   
    		lock.writeLock().lock();
    		waitForTick(2);
    		assertTrue(lock.isWriteLockedByCurrentThread());
    		assertTrue(lock.getWriteHoldCount() == 1);
    		lock.writeLock().lock();
    		assertTrue(lock.getWriteHoldCount() == 2);
    		lock.writeLock().unlock();
    		lock.writeLock().lock();
    		lock.writeLock().unlock();
    		lock.writeLock().unlock();
    	}
    }    
    // TUNIT Untimed Block/Wait


    /**
     * Read tryLock succeeds if readlocked but not writelocked
     */
    class TUnitTestTryLockWhenReadLocked extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		assertTrue(lock.readLock().tryLock());
			lock.readLock().unlock();
    	}
    	
    	public void thread2() {  
    		lock.readLock().lock();
    		waitForTick(2);
    		lock.readLock().unlock();
    	}
    }  
    // TUNIT Untimed Interleave/Synchronize


    /**
     * write tryLock fails when readlocked
     */
    class TUnitTestWriteTryLockWhenReadLocked extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		assertFalse(lock.writeLock().tryLock());
    	}
    	
    	public void thread2() {  
    		lock.readLock().lock();
    		waitForTick(2);
    		lock.readLock().unlock();
    	}
    } 
    // TUNIT Untimed Interleave/Synchronize

    
    /**
     * Fair Read tryLock succeeds if readlocked but not writelocked
     */
    class TUnitTestTryLockWhenReadLockedFair extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock(true);
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		assertTrue(lock.readLock().tryLock());
			lock.readLock().unlock();
    	}
    	
    	public void thread2() {  
    		lock.readLock().lock();
    		waitForTick(2);
    		lock.readLock().unlock();
    	}
    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * Fair write tryLock fails when readlocked
     */
    class TUnitTestWriteTryLockWhenReadLockedFair extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock(true);
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		assertFalse(lock.writeLock().tryLock());
    	}
    	
    	public void thread2() {  
    		lock.readLock().lock();
    		waitForTick(2);
    		lock.readLock().unlock();
    	}
    } 
    // TUNIT Untimed Interleave/Synchronize


    /**
     * write timed tryLock times out if locked
     */
    class TUnitTestWriteTryLock_Timeout extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() throws InterruptedException {
    		waitForTick(1);
    		freezeClock();
    		assertFalse(lock.writeLock().tryLock(1, TimeUnit.MILLISECONDS));
    		unfreezeClock();
    	}
    	
    	public void thread2() {  
    		lock.writeLock().lock();
    		waitForTick(2);
    		lock.writeLock().unlock();
    	}
    } 
    // TUNIT Timed Interleave/Synchronize

    
    /**
     * read timed tryLock times out if write-locked
     */
    class TUnitTestReadTryLock_Timeout extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() throws InterruptedException {
    		waitForTick(1);
    		freezeClock();
    		assertFalse(lock.readLock().tryLock(1, TimeUnit.MILLISECONDS));
    		unfreezeClock();
    	}
    	
    	public void thread2() {  
    		lock.writeLock().lock();
    		waitForTick(2);
    		lock.writeLock().unlock();
    	}
    }   
    // TUNIT Timed Interleave/Synchronize


    /**
     * write lockInterruptibly succeeds if lock free else is interruptible
     */
    class TUnitTestWriteLockInterruptibly extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			try {
				lock.writeLock().lockInterruptibly();
				fail("should throw exception");
			}
			catch(InterruptedException success) { assertTick(2); }
    	}
    	
    	public void thread2() throws InterruptedException { 
    		lock.writeLock().lockInterruptibly();
    		waitForTick(2);
    		getThread(1).interrupt();
    		waitForTick(3);
    		lock.writeLock().unlock();
    	}
    }  
    // TUNIT Untimed Interrupt/Cancel


    /**
     *  read lockInterruptibly succeeds if lock free else is interruptible
     */
    class TUnitTestReadLockInterruptibly extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
			try {
				lock.readLock().lockInterruptibly();
				fail("should throw exception");
			}
			catch(InterruptedException success) { assertTick(2); }
    	}
    	
    	public void thread2() throws InterruptedException { 
    		lock.writeLock().lockInterruptibly();
    		waitForTick(2);
    		getThread(1).interrupt();
    		waitForTick(3);
    		lock.writeLock().unlock();
    	}
    }  
    // TUNIT Untimed Interrupt/Cancel


    /**
     * await returns when signalled
     */
    class TUnitTestAwait extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    		c = lock.writeLock().newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			lock.writeLock().lock();
			c.await();
			assertTick(1);
			lock.writeLock().unlock();
    	}

    	public void thread2() {
    		waitForTick(1);
    		lock.writeLock().lock();
    		c.signal();
    		lock.writeLock().unlock();
    	}
    }
    // TUNIT Untimed Block/Wait


    /** A helper class for uninterruptible wait tests - Multithreaded Version*/
    class UninterruptableThread_MT extends Thread {
        private Lock lock;
        private Condition c;
        private MultithreadedTest mt;
        private int tick; // the tick to wait for
        
        public volatile boolean canAwake = false;
        public volatile boolean interrupted = false;
        
        public UninterruptableThread_MT(Lock lock, Condition c, 
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
    	ReentrantReadWriteLock lock;
    	Condition c;
    	UninterruptableThread_MT thread;
    	final int LOCK_STARTED = 1;
    	
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    		c = lock.writeLock().newCondition();
    		thread = new UninterruptableThread_MT(lock.writeLock(), c, this, LOCK_STARTED);
    	}
    	
    	public void thread1() throws InterruptedException {
            thread.start();

            waitForTick(LOCK_STARTED); // synchronize both threads

            lock.writeLock().lock();
            try {
                thread.interrupt();
                thread.canAwake = true;
                c.signal();
            } finally {
            	lock.writeLock().unlock();
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
    	ReentrantReadWriteLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    		c = lock.writeLock().newCondition();
    	}
    	
    	public void thread1() {
			try {
				lock.writeLock().lock();
				c.await();
				lock.writeLock().unlock();
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
    	ReentrantReadWriteLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    		c = lock.writeLock().newCondition();
    	}
    	
    	public void thread1() {
			try {
				lock.writeLock().lock();
				c.awaitNanos(SHORT_DELAY_MS * 2 * 1000000);
				lock.writeLock().unlock();
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
    	ReentrantReadWriteLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    		c = lock.writeLock().newCondition();
    	}
    	
    	public void thread1() {
			try {
				lock.writeLock().lock();
				java.util.Date d = new java.util.Date();
				c.awaitUntil(new java.util.Date(d.getTime() + 10000));
				lock.writeLock().unlock();
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
    	ReentrantReadWriteLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    		c = lock.writeLock().newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			lock.writeLock().lock();
			c.await();
			assertTick(1);
			lock.writeLock().unlock();
    	}

    	public void thread2() throws InterruptedException {
			lock.writeLock().lock();
			c.await();
			assertTick(1);
			lock.writeLock().unlock();
    	}

    	public void thread3() {
    		waitForTick(1);
    		lock.writeLock().lock();
    		c.signalAll();
    		lock.writeLock().unlock();
    	}
    }
    // TUNIT Untimed Block/Wait


    /**
     * hasQueuedThreads reports whether there are waiting threads
     */
    class TUnitTesthasQueuedThreads extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
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
            lock.writeLock().lock();
    		
            waitForTick(2);
    		assertTrue(lock.hasQueuedThreads());
    		
    		waitForTick(4);
    		assertTrue(lock.hasQueuedThreads());
            getThread(1).interrupt();
            
            waitForTick(5);
            assertTrue(lock.hasQueuedThreads());
            lock.writeLock().unlock();            
    	}
    	
    	@Override public void finish() {
    		assertFalse(lock.hasQueuedThreads());
    	}
    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * hasQueuedThread reports whether a thread is queued.
     */
    class TUnitTestHasQueuedThread extends MultithreadedTest {
    	ReentrantReadWriteLock sync;
    	@Override public void initialize() {
    		sync = new ReentrantReadWriteLock();
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
    		sync.writeLock().lock();
    		
            waitForTick(2);
            assertTrue(sync.hasQueuedThread(getThread(1)));
    		
    		waitForTick(4);
    		assertTrue(sync.hasQueuedThread(getThread(1)));
            assertTrue(sync.hasQueuedThread(getThread(2)));
            getThread(1).interrupt();
            
            waitForTick(5);
            assertFalse(sync.hasQueuedThread(getThread(1)));
            assertTrue(sync.hasQueuedThread(getThread(2)));
            sync.writeLock().unlock();            
    	}
    	
    	@Override public void finish() {
    		assertFalse(sync.hasQueuedThread(getThread(1)));
    		assertFalse(sync.hasQueuedThread(getThread(2)));
    	}

    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * getQueueLength reports number of waiting threads
     */
    class TUnitTestGetQueueLength extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
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
            lock.writeLock().lock();
    		
            waitForTick(2);
            assertEquals(1, lock.getQueueLength());
    		
    		waitForTick(4);
    		assertEquals(2, lock.getQueueLength());
            getThread(1).interrupt();
            
            waitForTick(5);
            assertEquals(1, lock.getQueueLength());
            lock.writeLock().unlock();            
    	}
    	
    	@Override public void finish() {
    		assertEquals(0, lock.getQueueLength());
    	}

    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * getQueuedThreads includes waiting threads
     */
    class TUnitTestGetQueuedThreads extends MultithreadedTest {
    	PublicReentrantReadWriteLock lock;
    	@Override public void initialize() {
    		lock = new PublicReentrantReadWriteLock();
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
            lock.writeLock().lock();
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
            lock.writeLock().unlock();            
    	}
    	
    	@Override public void finish() {
    		assertTrue(lock.getQueuedThreads().isEmpty());
    	}

    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * hasWaiters returns true when a thread is waiting, else false
     */
    class TUnitTestHasWaiters extends MultithreadedTest {
    	ReentrantReadWriteLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    		c = lock.writeLock().newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			lock.writeLock().lock();
			assertFalse(lock.hasWaiters(c));
			assertEquals(0, lock.getWaitQueueLength(c));
			c.await();
			assertTick(1);
			lock.writeLock().unlock();
    	}

    	public void thread2() throws InterruptedException {
    		waitForTick(1);
    		lock.writeLock().lock();
    		assertTrue(lock.hasWaiters(c));
    		assertEquals(1, lock.getWaitQueueLength(c));
    		c.signal();
    		lock.writeLock().unlock();
    		
    		waitForTick(2);
    		lock.writeLock().lock();
    		assertFalse(lock.hasWaiters(c));
    		assertEquals(0, lock.getWaitQueueLength(c));
    		lock.writeLock().unlock();
    		
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
    	ReentrantReadWriteLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new ReentrantReadWriteLock();
    		c = lock.writeLock().newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			lock.writeLock().lock();
            assertFalse(lock.hasWaiters(c));
            assertEquals(0, lock.getWaitQueueLength(c));
            c.await();

            assertTick(2);
            lock.writeLock().unlock();
    	}

    	public void thread2() {
            waitForTick(2);
            lock.writeLock().lock();
            assertTrue(lock.hasWaiters(c));
            assertEquals(1, lock.getWaitQueueLength(c));
            c.signalAll();
            lock.writeLock().unlock();

            waitForTick(3);
            lock.writeLock().lock();
            assertFalse(lock.hasWaiters(c));
            assertEquals(0, lock.getWaitQueueLength(c));
            lock.writeLock().unlock();
    	}
    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * getWaitingThreads returns only and all waiting threads
     */
    class TUnitTestGetWaitingThreads extends MultithreadedTest {
    	PublicReentrantReadWriteLock lock;
    	Condition c;
    	@Override public void initialize() {
    		lock = new PublicReentrantReadWriteLock();
    		c = lock.writeLock().newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
    		waitForTick(1);
			lock.writeLock().lock();
            assertTrue(lock.getWaitingThreads(c).isEmpty());
            c.await();

            assertTick(3);
            lock.writeLock().unlock();
    	}

    	public void thread2() throws InterruptedException {
    		waitForTick(2);
			lock.writeLock().lock();
            assertFalse(lock.getWaitingThreads(c).isEmpty());
            c.await();
    		
            assertTick(3);
            lock.writeLock().unlock();
    	}

    	public void thread3() {
            lock.writeLock().lock();
            assertTrue(lock.getWaitingThreads(c).isEmpty());
            lock.writeLock().unlock();

    		waitForTick(3);
            lock.writeLock().lock();
            assertTrue(lock.hasWaiters(c));
            assertTrue(lock.getWaitingThreads(c).contains(getThread(1)));
            assertTrue(lock.getWaitingThreads(c).contains(getThread(2)));
            c.signalAll();
            lock.writeLock().unlock();
            
    		waitForTick(4);
            lock.writeLock().lock();
            assertFalse(lock.hasWaiters(c));
            assertTrue(lock.getWaitingThreads(c).isEmpty());
            lock.writeLock().unlock();
    	}

    }
    // TUNIT
}
