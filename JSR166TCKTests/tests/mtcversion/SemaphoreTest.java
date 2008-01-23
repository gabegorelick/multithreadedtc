package mtcversion;
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
import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class SemaphoreTest extends JSR166TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}
	public static Test suite() {
		return TestFramework.buildTestSuite(SemaphoreTest.class);
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
                fail("should throw exception");
            } catch(InterruptedException success){}
        }
    }


    /**
     * A release in one thread enables an acquire in another thread
     */
    class TUnitTestAcquireReleaseInDifferentThreads extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(0, false);
    	}
    	
    	public void thread1() throws InterruptedException {
			s.acquire();
			assertTick(1);
			s.release();
			s.release();
			s.acquire();
    	}
    	
    	public void thread2() throws InterruptedException {    		
    		waitForTick(1);
    		s.release();
    		s.release();
    		s.acquire();
    		s.acquire();
    		s.release();
    	}

    }   
    // TUNIT Untimed Interleave/Synchronize


    /**
     * A release in one thread enables an uninterruptible acquire in another thread
     */
    class TUnitTestUninterruptibleAcquireReleaseInDifferentThreads extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(0, false);
    	}
    	
    	public void thread1() {
			s.acquireUninterruptibly();
			assertTick(1);
			s.release();
			s.release();
			s.acquireUninterruptibly();
    	}

    	public void thread2() {    
    		waitForTick(1);
    		s.release();
    		s.release();
    		s.acquireUninterruptibly();
    		s.acquireUninterruptibly();
    		s.release();
    	}
    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     *  A release in one thread enables a timed acquire in another thread
     */
    class TUnitTestTimedAcquireReleaseInDifferentThreads extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(1, false);
    	}
    	
    	public void thread1() throws InterruptedException {
			s.release();
			assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			s.release();
			assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
    	}

    	public void thread2() throws InterruptedException {
    		assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
    		s.release();
    		assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
    		s.release();
    		s.release();
    	}
    }
    // TUNIT Timed Interleave/Synchronize


    /**
     * A waiting acquire blocks interruptibly
     */
    class TUnitTestAcquire_InterruptedException extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(0, false);
    	}
    	
    	public void thread1() {
			try {
				s.acquire();
				fail("should throw exception");
			} catch(InterruptedException success){ assertTick(1); }
    	}

    	public void thread2() {
    		waitForTick(1); 
    		getThread(1).interrupt();
    	}
    }
    // TUNIT Untimed Interrupt/Cancel


    /**
     *  A waiting timed acquire blocks interruptibly
     */
    class TUnitTestTryAcquire_InterruptedException extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(0, false);
    	}
    	
    	public void thread1() {
			try {
				s.tryAcquire(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
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
     * hasQueuedThreads reports whether there are waiting threads
     */
    class TUnitTestHasQueuedThreads extends MultithreadedTest {
    	Semaphore lock;
    	@Override public void initialize() {
    		lock = new Semaphore(1, false);
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
    		lock.acquireUninterruptibly();
    		
    		waitForTick(2);    		
    		assertTrue(lock.hasQueuedThreads());

    		waitForTick(4);    		
    		assertTrue(lock.hasQueuedThreads());
    		getThread(1).interrupt();
    		
    		waitForTick(5);
    		assertTrue(lock.hasQueuedThreads());
    		lock.release();
    	}
    	
    	@Override public void finish() {
    		assertFalse(lock.hasQueuedThreads());
    	}

    }
    // REVIEW really all 3 classifications
    // TUNIT Untimed Interleave/Synchronize


    /**
     * getQueueLength reports number of waiting threads
     */
    class TUnitTestGetQueueLength extends MultithreadedTest {
    	Semaphore lock;
    	@Override public void initialize() {
    		lock = new Semaphore(1, false);
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
    		lock.acquireUninterruptibly();
    		
    		waitForTick(2);    		
    		assertEquals(1, lock.getQueueLength());

    		waitForTick(4);    		
    		assertEquals(2, lock.getQueueLength());
    		getThread(1).interrupt();
    		
    		waitForTick(5);
    		assertEquals(1, lock.getQueueLength());
    		lock.release();
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
    	PublicSemaphore lock;
    	@Override public void initialize() {
    		lock = new PublicSemaphore(1, false);
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
    		lock.acquireUninterruptibly();
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
    		lock.release();
    	}
    	
    	@Override public void finish() {
    		assertTrue(lock.getQueuedThreads().isEmpty());
    	}
    }
    // TUNIT Untimed Interleave/Synchronize

    
    /**
     * A release in one thread enables an acquire in another thread
     */
    class TUnitTestAcquireReleaseInDifferentThreads_fair extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(0, true);
    	}
    	
    	public void thread1() throws InterruptedException {
			s.acquire();
			assertTick(1);
			s.acquire();
			s.acquire();
			s.acquire();
    	}

    	public void thread2() {
    		waitForTick(1);    	    		
    		s.release();
    		s.release();
    		s.release();
    		s.release();
    		s.release();
    		s.release();
    	}
    	
    	@Override public void finish() {
    		assertEquals(2, s.availablePermits());
    	}

    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * release(n) in one thread enables acquire(n) in another thread
     */
    class TUnitTestAcquireReleaseNInDifferentThreads_fair extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(0, true);
    	}
    	
    	public void thread1() throws InterruptedException {
			s.acquire();
			assertTick(1);
			s.release(2);
			s.acquire();
    	}

    	public void thread2() throws InterruptedException {
    		waitForTick(1);    	    		
    		s.release(2);
    		s.acquire(2);
    		s.release(1);
    	}
    }
    // TUNIT Untimed Interleave/Synchronize


    /**
     * release(n) in one thread enables acquire(n) in another thread
     */
    class TUnitTestAcquireReleaseNInDifferentThreads_fair2 extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(0, true);
    	}
    	
    	public void thread1() throws InterruptedException {
			s.acquire(2);
			assertTick(1);
			s.acquire(2);
			s.release(4);
    	}

    	public void thread2() throws InterruptedException {
    		waitForTick(1);    	    		
    		s.release(6);
    		s.acquire(2);
    		s.acquire(2);
    		s.release(2);
    	}
    }
    // TUNIT Untimed Interleave/Synchronize
    

    /**
     * release in one thread enables timed acquire in another thread
     */
    class TUnitTestTimedAcquireReleaseInDifferentThreads_fair extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(1, true);
    	}
    	
    	public void thread1() throws InterruptedException {
			assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
    	}

    	public void thread2() {
    		s.release();
    		s.release();
    		s.release();
    		s.release();
    		s.release();
    	}
    }
    // TUNIT Timed Interleave/Synchronize


    /**
     * release(n) in one thread enables timed acquire(n) in another thread
     */
    class TUnitTestTimedAcquireReleaseNInDifferentThreads_fair extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(2, true);
    	}
    	
    	public void thread1() throws InterruptedException {
			assertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			s.release(2);
			assertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			s.release(2);
    	}

    	public void thread2() throws InterruptedException {
    		assertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
    		s.release(2);
    		assertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
    		s.release(2);
    	}
    }
    // TUNIT Timed Interleave/Synchronize


    /**
     * A waiting acquire blocks interruptibly
     */
    class TUnitTestAcquire_InterruptedException_fair extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(0, true);
    	}
    	
    	public void thread1() {
			try {
				s.acquire();
				fail("should throw exception");
			} catch(InterruptedException success){ assertTick(1); }
    	}

    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();    	    		
    	}
    }
    // TUNIT Untimed Interrupt/Cancel


    /**
     * A waiting acquire(n) blocks interruptibly
     */
    class TUnitTestAcquireN_InterruptedException_fair extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(2, true);
    	}
    	
    	public void thread1() {
			try {
				s.acquire(3);
				fail("should throw exception");
			} catch(InterruptedException success){ assertTick(1); }
    	}

    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();    	    		
    	}
    }
    // TUNIT Untimed Interrupt/Cancel


    /**
     *  A waiting tryAcquire blocks interruptibly
     */
    class TUnitTestTryAcquire_InterruptedException_fair extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(0, true);
    	}
    	
    	public void thread1() {
			try {
				s.tryAcquire(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
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
     *  A waiting tryAcquire(n) blocks interruptibly
     */
    class TUnitTestTryAcquireN_InterruptedException_fair extends MultithreadedTest {
    	Semaphore s;
    	@Override public void initialize() {
    		s = new Semaphore(1, true);
    	}
    	
    	public void thread1() {
			try {
				s.tryAcquire(4, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
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
     * getQueueLength reports number of waiting threads
     */
    class TUnitTestGetQueueLength_fair extends MultithreadedTest {
    	Semaphore lock;
    	@Override public void initialize() {
    		lock = new Semaphore(1, true);
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
    		lock.acquireUninterruptibly();
    		
    		waitForTick(2);    		
    		assertEquals(1, lock.getQueueLength());

    		waitForTick(4);    		
    		assertEquals(2, lock.getQueueLength());
    		getThread(1).interrupt();
    		
    		waitForTick(5);
    		assertEquals(1, lock.getQueueLength());
    		lock.release();
    	}
    	
    	@Override public void finish() {
    		assertEquals(0, lock.getQueueLength());
    	}

    }
    // TUNIT Untimed Interleave/Synchronize
}
