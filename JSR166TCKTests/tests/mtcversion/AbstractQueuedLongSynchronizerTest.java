package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */


import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;

import junit.framework.Test;
import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;


public class AbstractQueuedLongSynchronizerTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(AbstractQueuedLongSynchronizerTest.class);
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
                fail("should throw exception");
            } catch(InterruptedException success){}
        }
    }


    /**
     * hasQueuedThreads reports whether there are waiting threads
     */
    class TUnitTesthasQueuedThreads extends MultithreadedTest {
    	Mutex sync;
    	
    	@Override public void initialize() {
    		sync = new Mutex();
    		assertFalse(sync.hasQueuedThreads());
    		sync.acquire(1);
    	}
    	
    	public void thread0() {
    		waitForTick(1);
    		assertTrue(sync.hasQueuedThreads());
    		waitForTick(3);
    		assertTrue(sync.hasQueuedThreads());
    		getThread(1).interrupt();    		
    		waitForTick(4);
    		assertTrue(sync.hasQueuedThreads());
    		sync.release(1);    		
    	}
    	
    	public void thread1() {
    		new InterruptedSyncRunnable(sync).run();
    		assertEquals(getTick(), 3); // interrupted in tick 3
    	}
    	
    	public void thread2() {
    		waitForTick(2);
    		new InterruptibleSyncRunnable(sync).run();
    		assertTick(4); // released in tick 4
    	}

    	@Override public void finish() {
    		assertFalse(sync.hasQueuedThreads());
    	}
    }    
    // TUNIT Untimed Unblocking/Interrupting

    /**
     * isQueued reports whether a thread is queued.
     */
    class TUnitTestIsQueued extends MultithreadedTest {
    	Mutex sync;
    	
    	public void initialize() {
    		sync = new Mutex();
    	}
    	
    	public void thread0() {    		
    		assertFalse(sync.isQueued(getThread(1)));
            assertFalse(sync.isQueued(getThread(2)));
            sync.acquire(1);
            
            waitForTick(2);
            assertTrue(sync.isQueued(getThread(1)));

            waitForTick(4);
            assertTrue(sync.isQueued(getThread(1)));
            assertTrue(sync.isQueued(getThread(2)));
            getThread(1).interrupt();
            
            waitForTick(5);
            assertFalse(sync.isQueued(getThread(1)));
            assertTrue(sync.isQueued(getThread(2)));
            sync.release(1);
    	}

    	public void thread1() {
    		waitForTick(1);
    		new InterruptedSyncRunnable(sync).run();
    		assertTick(4); // released in tick 4
    	}
    	
    	public void thread2() {    		
    		waitForTick(3);
    		new InterruptibleSyncRunnable(sync).run();
            assertTick(5);
    	}

    	@Override public void finish() {
            assertFalse(sync.isQueued(getThread(1)));
            assertFalse(sync.isQueued(getThread(2)));
    	}
    }    
    // TUNIT Untimed Unblocking/Interrupting

    
    /**
     * getFirstQueuedThread returns first waiting thread or null if none
     */
    class TUnitTestGetFirstQueuedThread extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		new InterruptedSyncRunnable(sync).run();
    		assertTick(4);
    	}

    	public void thread2() {
    		waitForTick(3);
    		new InterruptibleSyncRunnable(sync).run();
    		assertTick(5);
    	}

    	public void thread3() {
            assertNull(sync.getFirstQueuedThread());
            sync.acquire(1);
    		
            waitForTick(2);
            assertEquals(getThread(1), sync.getFirstQueuedThread());
    		
    		waitForTick(4);
            assertEquals(getThread(1), sync.getFirstQueuedThread());
            getThread(1).interrupt();
            
            waitForTick(5);
            assertEquals(getThread(2), sync.getFirstQueuedThread());
            sync.release(1);
    	}
    	
    	@Override public void finish() {
    		assertNull(sync.getFirstQueuedThread());
    	}

    }
    // TUNIT Untimed Unblocking/Interrupting


    /**
     * hasContended reports false if no thread has ever blocked, else true
     */
    class TUnitTestHasContended extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();
            assertFalse(sync.hasContended());
            sync.acquire(1);
    	}
    	
    	public void thread1() {
    		new InterruptedSyncRunnable(sync).run();
    		assertTick(3);
    	}

    	public void thread2() {
    		waitForTick(2);
    		new InterruptibleSyncRunnable(sync).run();
    		assertTick(4);
    	}

    	public void thread3() {    		
            waitForTick(1);
            assertTrue(sync.hasContended());
    		
    		waitForTick(3);
            assertTrue(sync.hasContended());
            getThread(1).interrupt();
            
            waitForTick(4);
            assertTrue(sync.hasContended());
            sync.release(1);
    	}
    	
    	@Override public void finish() {
            assertTrue(sync.hasContended());
    	}

    }
    // TUNIT Untimed Unblocking/Interrupting


    /**
     * getQueuedThreads includes waiting threads
     */
    class TUnitTestGetQueuedThreads extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		new InterruptedSyncRunnable(sync).run();
    		assertTick(4);
    	}

    	public void thread2() {
    		waitForTick(3);
    		new InterruptibleSyncRunnable(sync).run();
    		assertTick(5);
    	}

    	public void thread3() {
    		assertTrue(sync.getQueuedThreads().isEmpty());
    		sync.acquire(1);
            assertTrue(sync.getQueuedThreads().isEmpty());
    		
            waitForTick(2);
            assertTrue(sync.getQueuedThreads().contains(getThread(1)));
    		
    		waitForTick(4);
    		assertTrue(sync.getQueuedThreads().contains(getThread(1)));
            assertTrue(sync.getQueuedThreads().contains(getThread(2)));
            getThread(1).interrupt();
            
            waitForTick(5);
            assertFalse(sync.getQueuedThreads().contains(getThread(1)));
            assertTrue(sync.getQueuedThreads().contains(getThread(2)));
            sync.release(1);     
    	}
    	
    	@Override public void finish() {
    		assertTrue(sync.getQueuedThreads().isEmpty());
    	}
    }
    // TUNIT Untimed Unblocking/Interrupting


    /**
     * getExclusiveQueuedThreads includes waiting threads
     */
    class TUnitTestGetExclusiveQueuedThreads extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();
            assertTrue(sync.getExclusiveQueuedThreads().isEmpty());
            sync.acquire(1);
            assertTrue(sync.getExclusiveQueuedThreads().isEmpty());
    	}
    	
    	public void thread1() {
    		new InterruptedSyncRunnable(sync).run();
    		assertTick(3);
    	}

    	public void thread2() {
    		waitForTick(2);
    		new InterruptibleSyncRunnable(sync).run();
    		assertTick(4);
    	}

    	public void thread3() {
            waitForTick(1);
            assertTrue(sync.getExclusiveQueuedThreads().contains(getThread(1)));
    		
    		waitForTick(3);
            assertTrue(sync.getExclusiveQueuedThreads().contains(getThread(1)));
            assertTrue(sync.getExclusiveQueuedThreads().contains(getThread(2)));
            getThread(1).interrupt();
            
            waitForTick(4);                       
            assertFalse(sync.getExclusiveQueuedThreads().contains(getThread(1)));
            assertTrue(sync.getExclusiveQueuedThreads().contains(getThread(2)));
            sync.release(1);
    	}
    	
    	@Override public void finish() {
            assertTrue(sync.getExclusiveQueuedThreads().isEmpty());            
    	}
    }
    // TUNIT Untimed Unblocking/Interrupting
    

    /**
     * getSharedQueuedThreads does not include exclusively waiting threads
     */
    class TUnitTestGetSharedQueuedThreads extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            sync.acquire(1);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
    	}
    	
    	public void thread1() {
    		new InterruptedSyncRunnable(sync).run();
    		assertTick(3);
    	}

    	public void thread2() {
    		waitForTick(2);
    		new InterruptibleSyncRunnable(sync).run();
    		assertTick(4);
    	}

    	public void thread3() {
            waitForTick(1);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
    		
    		waitForTick(3);
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            getThread(1).interrupt();
            
            waitForTick(4);                       
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
            sync.release(1);
    	}
    	
    	@Override public void finish() {
            assertTrue(sync.getSharedQueuedThreads().isEmpty());
    	}

    }
    // TUNIT Untimed Unblocking/Interrupting

    
    /**
     * tryAcquireNanos is interruptible.
     */
    class TUnitTestInterruptedException2 extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();
    		sync.acquire(1);
    	}
    	
    	public void thread1() {    		
    		try {
    			sync.tryAcquireNanos(1, MEDIUM_DELAY_MS * 1000 * 1000);
    			fail("should throw exception");
    		} catch(InterruptedException success){ assertTick(1); }    		
    	}

    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }
    // TUNIT Timed Interrupt


    /**
     * TryAcquire on exclusively held sync fails
     */
    class TUnitTestTryAcquireWhenSynced extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();
    		sync.acquire(1);
    	}
    	
    	public void thread1() {    		
    		assertFalse(sync.tryAcquire(1));		
    	}
    	
    	@Override public void finish() {
    		sync.release(1);
    	}
    }
    // TUNIT Untimed interleave


    /**
     * tryAcquireNanos on an exclusively held sync times out
     */
    class TUnitTestAcquireNanos_Timeout extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();
    		sync.acquire(1);
    	}
    	
    	public void thread1() throws InterruptedException {    		
    		assertFalse(sync.tryAcquireNanos(1, 1000 * 1000));	
    	}
    	
    	@Override public void finish() {
    		sync.release(1);
    	}
    }
    // TUNIT Timed interleave
    
   
    /**
     * getState is true when acquired and false when not
     */
    class TUnitTestGetState extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();
    	}
    	
    	public void thread1() throws InterruptedException {    	
    		waitForTick(1);
		    sync.acquire(1);
		    waitForTick(3);
		    sync.release(1);
    	}
    	
    	public void thread2() {
    		sync.acquire(1);
    		assertTrue(sync.isHeldExclusively());
    		sync.release(1);
    		assertFalse(sync.isHeldExclusively());

    		waitForTick(2);
            assertTrue(sync.isHeldExclusively());
            
            waitForTick(4);
            assertFalse(sync.isHeldExclusively());
    	}
    }
    // TUNIT Untimed interleave


    /**
     * acquireInterruptibly is interruptible.
     */
    class TUnitTestAcquireInterruptibly1 extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();    		
    		sync.acquire(1);
    	}
    	
    	public void thread1() {
    		new InterruptedSyncRunnable(sync).run();
    		assertTick(1);
    	}

    	public void thread2() {
            waitForTick(1);
            getThread(1).interrupt();            
    	}
    	
    	@Override public void finish() {
            sync.release(1);            
    	}
    }
    // TUNIT Untimed Interrupt/Cancel


    /**
     * acquireInterruptibly succeeds when released, else is interruptible
     */
    class TUnitTestAcquireInterruptibly2 extends MultithreadedTest {
    	Mutex sync;
    	@Override public void initialize() {
    		sync = new Mutex();
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		new InterruptedSyncRunnable(sync).run();
    		assertTick(2);
    	}

    	public void thread2() throws InterruptedException {
    		sync.acquireInterruptibly(1);
    		waitForTick(2);
            getThread(1).interrupt();            
            assertTrue(sync.isHeldExclusively());
    	}
    }
    // TUNIT Untimed Interrupt


    /**
     * await returns when signalled
     */
    class TUnitTestAwait extends MultithreadedTest {
    	Mutex sync;
    	AbstractQueuedLongSynchronizer.ConditionObject c;
    	@Override public void initialize() {
    		sync = new Mutex();
    		c = sync.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			sync.acquire(1);
			c.await();
			sync.release(1);
    	}

    	public void thread2() throws InterruptedException {
            waitForTick(1);
    		sync.acquire(1);
    		c.signal();
    		sync.release(1);
    		getThread(1).join(SHORT_DELAY_MS);
    		assertFalse(getThread(1).isAlive());
    	}
    }
    // TUNIT Untimed Block/Wait


    /**
     * hasWaiters returns true when a thread is waiting, else false
     */
    class TUnitTestHasWaiters extends MultithreadedTest {
    	Mutex sync;
    	AbstractQueuedLongSynchronizer.ConditionObject c;
    	@Override public void initialize() {
    		sync = new Mutex();
    		c = sync.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			sync.acquire(1);
            assertFalse(sync.hasWaiters(c));
            assertEquals(0, sync.getWaitQueueLength(c));
            c.await();
			assertTick(1);
            sync.release(1);
    	}

    	public void thread2() throws InterruptedException {
    		waitForTick(1);
            sync.acquire(1);
            assertTrue(sync.hasWaiters(c));
            assertEquals(1, sync.getWaitQueueLength(c));
            c.signal();
            sync.release(1);
    		
    		waitForTick(2);
            sync.acquire(1);
            assertFalse(sync.hasWaiters(c));
            assertEquals(0, sync.getWaitQueueLength(c));
            sync.release(1);
    		
    		getThread(1).join(SHORT_DELAY_MS);
    		assertFalse(getThread(1).isAlive());
    	}
    }
    // TUNIT Untimed Block


    /**
     * getWaitQueueLength returns number of waiting threads
     */
    class TUnitTestGetWaitQueueLength extends MultithreadedTest {
    	Mutex sync;
    	AbstractQueuedLongSynchronizer.ConditionObject c;
    	@Override public void initialize() {
    		sync = new Mutex();
    		c = sync.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			sync.acquire(1);
			assertFalse(sync.hasWaiters(c));
			assertEquals(0, sync.getWaitQueueLength(c));
			c.await();

            assertTick(2);
			sync.release(1);
    	}

    	public void thread2() throws InterruptedException {
    		waitForTick(1);
			sync.acquire(1);
			assertTrue(sync.hasWaiters(c));
			assertEquals(1, sync.getWaitQueueLength(c));
			c.await();

            assertTick(2);
			sync.release(1);
    	}

    	public void thread3() {
            waitForTick(2);
        	sync.acquire(1);
        	assertTrue(sync.hasWaiters(c));
        	assertEquals(2, sync.getWaitQueueLength(c));
        	c.signalAll();
        	sync.release(1);

            waitForTick(3);
        	sync.acquire(1);
        	assertFalse(sync.hasWaiters(c));
        	assertEquals(0, sync.getWaitQueueLength(c));
        	sync.release(1);
    	}
    }
    // TUNIT Untimed Block


    /**
     * getWaitingThreads returns only and all waiting threads
     */
    class TUnitTestGetWaitingThreads extends MultithreadedTest {
    	Mutex sync;
    	AbstractQueuedLongSynchronizer.ConditionObject c;
    	@Override public void initialize() {
    		sync = new Mutex();
    		c = sync.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
    		waitForTick(1);
			sync.acquire(1);
			assertTrue(sync.getWaitingThreads(c).isEmpty());
			c.await();

            assertTick(3);
			sync.release(1);
    	}

    	public void thread2() throws InterruptedException {
    		waitForTick(2);
			sync.acquire(1);
			assertFalse(sync.getWaitingThreads(c).isEmpty());
			c.await();
    		
            assertTick(3);
			sync.release(1);
    	}

    	public void thread3() {
    		sync.acquire(1);
    		assertTrue(sync.getWaitingThreads(c).isEmpty());
    		sync.release(1);

    		waitForTick(3);
    		sync.acquire(1);
    		assertTrue(sync.hasWaiters(c));
    		assertTrue(sync.getWaitingThreads(c).contains(getThread(1)));
    		assertTrue(sync.getWaitingThreads(c).contains(getThread(2)));
    		c.signalAll();
    		sync.release(1);
            
    		waitForTick(4);
    		sync.acquire(1);
    		assertFalse(sync.hasWaiters(c));
    		assertTrue(sync.getWaitingThreads(c).isEmpty());
    		sync.release(1);
    	}
    }
    // TUNIT Untimed Block


    /**
     * awaitUninterruptibly doesn't abort on interrupt
     */
    class TUnitTestAwaitUninterruptibly extends MultithreadedTest {
    	Mutex sync;
    	AbstractQueuedLongSynchronizer.ConditionObject c;
    	
    	@Override public void initialize() {
    		sync = new Mutex();
    		c = sync.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
            sync.acquire(1);
            c.awaitUninterruptibly();
            assertTick(1);
            sync.release(1);
    	}
    	
    	public void thread2() throws InterruptedException {
    		waitForTick(1);
            getThread(1).interrupt();
            sync.acquire(1);
            c.signal();
            sync.release(1);
    	}
    }
    // TUNIT Untimed UnBlock


    /**
     * await is interruptible
     */
    class TUnitTestAwait_Interrupt extends MultithreadedTest {
    	Mutex sync;
    	AbstractQueuedLongSynchronizer.ConditionObject c;
    	@Override public void initialize() {
    		sync = new Mutex();
    		c = sync.newCondition();
    	}
    	
    	public void thread1() {
			try {
				sync.acquire(1);
				c.await();
				sync.release(1);
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
    	Mutex sync;
    	AbstractQueuedLongSynchronizer.ConditionObject c;
    	@Override public void initialize() {
    		sync = new Mutex();
    		c = sync.newCondition();
    	}
    	
    	public void thread1() {
			try {
				sync.acquire(1);
				c.awaitNanos(1000 * 1000 * 1000); // 1 sec
				sync.release(1);
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
     * awaitUntil is interruptible
     */
    class TUnitTestAwaitUntil_Interrupt extends MultithreadedTest {
    	Mutex sync;
    	AbstractQueuedLongSynchronizer.ConditionObject c;
    	@Override public void initialize() {
    		sync = new Mutex();
    		c = sync.newCondition();
    	}
    	
    	public void thread1() {
			try {
				sync.acquire(1);
				java.util.Date d = new java.util.Date();
				c.awaitUntil(new java.util.Date(d.getTime() + 10000));
				sync.release(1);
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
     * signalAll wakes up all threads
     */
    class TUnitTestSignalAll extends MultithreadedTest {
    	Mutex sync;
    	AbstractQueuedLongSynchronizer.ConditionObject c;
    	@Override public void initialize() {
    		sync = new Mutex();
    		c = sync.newCondition();
    	}
    	
    	public void thread1() throws InterruptedException {
			sync.acquire(1);
			c.await();
			assertTick(1);
			sync.release(1);
    	}

    	public void thread2() throws InterruptedException {
			sync.acquire(1);
			c.await();
			assertTick(1);
			sync.release(1);
    	}

    	public void thread3() {
    		waitForTick(1);
    		sync.acquire(1);
    		c.signalAll();
    		sync.release(1);
    	}
    }
    // TUNIT Untimed Interrupt/Cancel
    

    /**
     * acquireSharedInterruptibly returns after release, but not before
     */
    class TUnitTestAcquireSharedInterruptibly extends MultithreadedTest {
    	BooleanLatch l;
    	@Override public void initialize() {
    		l = new BooleanLatch();
    	}
    	
    	public void thread1() throws InterruptedException {
			assertFalse(l.isSignalled());
			l.acquireSharedInterruptibly(0);
			assertTick(1);
			assertTrue(l.isSignalled());
    	}

    	public void thread2() {
    		assertFalse(l.isSignalled());
    		waitForTick(1);
    		l.releaseShared(0);
    		assertTrue(l.isSignalled());
    	}
    }
    // TUNIT Untimed Interrupt/Cancel
    

    /**
     * acquireSharedTimed returns after release
     */
    class TUnitTestAsquireSharedTimed extends MultithreadedTest {
    	BooleanLatch l;
    	@Override public void initialize() {
    		l = new BooleanLatch();
    	}
    	
    	public void thread1() throws InterruptedException {
            assertFalse(l.isSignalled());
			assertTrue(l.tryAcquireSharedNanos(0, MEDIUM_DELAY_MS* 1000 * 1000));
			assertTick(1);
            assertTrue(l.isSignalled());
    	}

    	public void thread2() {
    		assertFalse(l.isSignalled());
    		waitForTick(1);
    		l.releaseShared(0);
    		assertTrue(l.isSignalled());
    	}
    }
    // TUNIT Untimed Interrupt/Cancel
    
    
    /**
     * acquireSharedInterruptibly throws IE if interrupted before released
     */
    class TUnitTestAcquireSharedInterruptibly_InterruptedException extends MultithreadedTest {
    	BooleanLatch l;
    	@Override public void initialize() {
    		l = new BooleanLatch();
    	}
    	
    	public void thread1() {
            try {
                assertFalse(l.isSignalled());
                l.acquireSharedInterruptibly(0);
                fail("should throw exception");
            } catch(InterruptedException success){ assertTick(1); }
    	}

    	public void thread2() {
    		waitForTick(1);
            assertFalse(l.isSignalled());
            getThread(1).interrupt();
    	}
    }
    // TUNIT Untimed Interrupt/Cancel
    

    /**
     * acquireSharedTimed throws IE if interrupted before released
     */
    class TUnitTestAcquireSharedNanos_InterruptedException extends MultithreadedTest {
    	BooleanLatch l;
    	@Override public void initialize() {
    		l = new BooleanLatch();
    	}
    	
    	public void thread1() {
            try {
                assertFalse(l.isSignalled());
                l.tryAcquireSharedNanos(0, SMALL_DELAY_MS* 1000 * 1000);
                fail("should throw exception");
            } catch(InterruptedException success){ assertTick(1); }
    	}

    	public void thread2() {
    		waitForTick(1);
            assertFalse(l.isSignalled());
            getThread(1).interrupt();
    	}
    }
    // TUNIT Timed Interrupt/Cancel
    

    /**
     * acquireSharedTimed times out if not released before timeout
     */
    class TUnitTestAcquireSharedNanos_Timeout extends MultithreadedTest {
    	BooleanLatch l;
    	@Override public void initialize() {
    		l = new BooleanLatch();
    	}
    	
    	public void thread1() throws InterruptedException {
            assertFalse(l.isSignalled());
            assertFalse(l.tryAcquireSharedNanos(0, SMALL_DELAY_MS* 1000 * 1000));
            assertTick(1);
    	}

    	public void thread2() {
    		waitForTick(1);
            assertFalse(l.isSignalled());
    	}
    }
    // TUNIT Untimed Interrupt/Cancel
}
