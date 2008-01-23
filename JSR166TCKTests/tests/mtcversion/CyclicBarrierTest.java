package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class CyclicBarrierTest extends JSR166TestCase{
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}
	public static Test suite() {
		return TestFramework.buildTestSuite(CyclicBarrierTest.class);
	}

    private volatile int countAction;
    private class MyAction implements Runnable {
        public void run() { ++countAction; }
    }
    

    // REVIEW <=> Interleaving threads synchronized by a cyclic barrier (could test in framework but this may be simpler)
    /**
     * A 2-party/thread barrier triggers after both threads invoke await
     */
    class TUnitTestTwoParties extends MultithreadedTest {
    	CyclicBarrier b;

    	@Override public void initialize() {
    		b = new CyclicBarrier(2);
    	}

    	public void thread1() throws InterruptedException, BrokenBarrierException {
    		b.await();
    		b.await();
    		b.await();
    		b.await();
    	}

    	public void thread2() throws InterruptedException, BrokenBarrierException {    		
    		b.await();
    		b.await();
    		b.await();
    		b.await();
    	}
    }
    // TUNIT Untimed Interleave/Synchronize


    // REVIEW <=> Simple test for blocking
    /**
     * An interruption in one party causes others waiting in await to
     * throw BrokenBarrierException
     */
    class TUnitTestAwait1_Interrupted_BrokenBarrier extends MultithreadedTest {
    	CyclicBarrier c;

    	@Override public void initialize() {
    		c = new CyclicBarrier(3);
    	}

    	public void thread0() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    	public void thread1() throws BrokenBarrierException {
    		try {
    			c.await();
    			fail("should throw exception");
    		} catch(InterruptedException success){ assertTick(1); }                
    	}

    	public void thread2() throws InterruptedException {    		
    		try {
    			c.await();
    			fail("should throw exception");                        
    		} catch(BrokenBarrierException success){ assertTick(1); }
    	}
    }
    // TUNIT Untimed Interrupt/Cancel


    // REVIEW <=> Simple test for blocking
    /**
     * An interruption in one party causes others waiting in timed await to
     * throw BrokenBarrierException
     */
    class TUnitTestAwait2_Interrupted_BrokenBarrier extends MultithreadedTest {
    	CyclicBarrier c;

    	@Override public void initialize() {
    		c = new CyclicBarrier(3);
    	}

    	public void thread0() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    	public void thread1() throws BrokenBarrierException, TimeoutException {
    		try {
    			c.await(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
    			fail("should throw exception");
    		} catch(InterruptedException success){ assertTick(1); }                
    	}

    	public void thread2() throws InterruptedException, TimeoutException {    		
    		try {
    			c.await(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
    			fail("should throw exception");                        
    		} catch(BrokenBarrierException success){ assertTick(1); }
    	}
    }
    // TUNIT Timed Interrupt/Cancel


    // REVIEW <=> Simple Test for Action that causes unblocking and exceptions (action = timeout)
    /**
     * A timeout in timed await throws TimeoutException
     */
    class TUnitTestAwait3_TimeOutException extends MultithreadedTest {
    	CyclicBarrier c;
    	@Override public void initialize() {
    		c = new CyclicBarrier(2);
    	}
    	
    	public void thread1() throws InterruptedException, BrokenBarrierException {
			try {
				c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch(TimeoutException success){}
    	}
    }    
    // TUNIT Timed Block/Wait


    // REVIEW <=> Simple Test for Action that causes unblocking and exceptions (action = timeout)
    /**
     * A timeout in one party causes others waiting in timed await to
     * throw BrokenBarrierException
     */
    class TUnitTestAwait4_Timeout_BrokenBarrier extends MultithreadedTest {
    	CyclicBarrier c;
    	@Override public void initialize() {
    		c = new CyclicBarrier(3);
    	}
    	
    	public void thread1() throws InterruptedException, BrokenBarrierException {
			try {
				c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch(TimeoutException success){}
    	}
    	
    	public void thread2() throws InterruptedException, TimeoutException {    		
			try {
				c.await(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");                        
			} catch(BrokenBarrierException success){}
    	}
    }    
    // TUNIT Timed Block/Wait


    // REVIEW <=> Simple Test for Action that causes unblocking and exceptions (action = timeout)
    /**
     * A timeout in one party causes others waiting in await to
     * throw BrokenBarrierException
     */
    class TUnitTestAwait5_Timeout_BrokenBarrier extends MultithreadedTest {
    	CyclicBarrier c;
    	@Override public void initialize() {
    		c = new CyclicBarrier(3);
    	}
    	
    	public void thread1() throws InterruptedException, BrokenBarrierException {
			try {
				c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch(TimeoutException success){}
    	}
    	
    	public void thread2() throws InterruptedException {    		
			try {
				c.await();
				fail("should throw exception");                        
			} catch(BrokenBarrierException success){}
    	}
    }    
    // TUNIT Timed Block/Wait


    // REVIEW <=> Simple Test for Action that causes unblocking and exceptions (action = reset)
    /**
     * A reset of an active barrier causes waiting threads to throw
     * BrokenBarrierException
     */
    class TUnitTestReset_BrokenBarrier extends MultithreadedTest {
    	CyclicBarrier c;
    	@Override public void initialize() {
    		c = new CyclicBarrier(3);
    	}
    	
    	public void thread1() throws InterruptedException {
			try {
				c.await();
				fail("should throw exception");
			} catch(BrokenBarrierException success){ assertTick(1); }                
    	}
    	
    	public void thread2() throws InterruptedException {
			try {
				c.await();
				fail("should throw exception");
			} catch(BrokenBarrierException success){ assertTick(1); }                
    	}
    	
    	public void thread3() {    
    		waitForTick(1);
    		c.reset();
    	}
    }    
    // TUNIT Untimed Interrupt/Cancel


    // REVIEW <=> Simple Test that an action does not throw exception unless threads are blocked
    /**
     * A reset before threads enter barrier does not throw
     * BrokenBarrierException
     */
    class TUnitTestReset_NoBrokenBarrier extends MultithreadedTest {
    	CyclicBarrier c;
    	@Override public void initialize() {
    		c = new CyclicBarrier(3);
    	}
    	
    	public void thread1() throws InterruptedException, BrokenBarrierException {
    		waitForTick(1);
    		c.await();
    	}
    	
    	public void thread2() throws InterruptedException, BrokenBarrierException {
    		waitForTick(1);
    		c.await();
    	}
    	
    	public void thread3() throws InterruptedException, BrokenBarrierException {
    		c.reset();
    		waitForTick(1);
    		c.await();
    	}    	
    }    
    // TUNIT Untimed Block/Wait


    /**
     * All threads block while a barrier is broken.
     */
    class TUnitTestReset_Leakage extends MultithreadedTest {
    	CyclicBarrier c;
    	AtomicBoolean done;
    	int currentTick;
    	int oldTick;
    	@Override public void initialize() {
    		c = new CyclicBarrier(2);
    		done = new AtomicBoolean();
    		oldTick = currentTick = 0;
    	}
    	
    	public void thread1() {
			while (!done.get()) {
				try {
					while (c.isBroken())
						c.reset();

					c.await();
					fail("await should not return");
				}
				catch (BrokenBarrierException e) { assertTick(++oldTick); }
				catch (InterruptedException ie) { assertTick(++oldTick); }
			}
    	}
    	
    	public void thread2() {    		
    		for( int i = 0; i < 4; i++) {
    			waitForTick(++currentTick);
    			getThread(1).interrupt();
    		}
    		done.set(true);
			getThread(1).interrupt();
    	}
    }    
    // TUNIT Untimed Interrupt/Cancel


    // REVIEW <=> Interleaving threads synchronized by a cyclic barrier
    /**
     * Reset of a non-broken barrier does not break barrier
     */
    class TUnitTestResetWithoutBreakage extends MultithreadedTest {
    	CyclicBarrier start = new CyclicBarrier(3); // use the same barriers each time the test is run
    	CyclicBarrier barrier = new CyclicBarrier(3);
    	int i=0;
    	
    	public void thread1() throws InterruptedException, BrokenBarrierException {
			start.await();
			barrier.await();
    	}
    	
    	public void thread2() throws InterruptedException, BrokenBarrierException {    		
			start.await();
			barrier.await();
    	}

    	public void thread3() throws InterruptedException, BrokenBarrierException {    		
			start.await();
			barrier.await();
    	}

    	@Override public void finish() {
			assertFalse(barrier.isBroken());
			assertEquals(0, barrier.getNumberWaiting());
			if (i++ == 1) barrier.reset();
			assertFalse(barrier.isBroken());
			assertEquals(0, barrier.getNumberWaiting());
    	}

		@Override public void runTest() throws Throwable {
			TestFramework.runManyTimes( this, 3 ); // instead of for-loop
		}    	    	
    }    
    // TUNIT Untimed Block/Wait


    // REVIEW <=> Interleaving threads synchronized by a cyclic barrier
    /**
     * Reset of a barrier after interruption reinitializes it.
     */
    class TUnitTestResetAfterInterrupt extends MultithreadedTest {
    	CyclicBarrier start = new CyclicBarrier(3); // use the same barriers each time the test is run
    	CyclicBarrier barrier = new CyclicBarrier(3);
    	
    	public void thread1() throws InterruptedException, BrokenBarrierException {
			start.await();
			try { barrier.await(); }
			catch(InterruptedException ok) {}
    	}
    	
    	public void thread2() throws InterruptedException, BrokenBarrierException {    		
			start.await();
			try { barrier.await(); }
			catch(BrokenBarrierException ok) {}
    	}

    	public void thread3() throws InterruptedException, BrokenBarrierException {    		
			start.await();
			getThread(1).interrupt();
    	}

    	@Override public void finish() {
			assertTrue(barrier.isBroken());
			assertEquals(0, barrier.getNumberWaiting());
			barrier.reset();
			assertFalse(barrier.isBroken());
			assertEquals(0, barrier.getNumberWaiting());
    	}

    	@Override public void runTest() throws Throwable {
			TestFramework.runManyTimes( this, 3 ); // instead of for-loop
		}    	    	
    }    
    // TUNIT Untimed Block/Wait


    // REVIEW <=> Interleaving threads synchronized by a cyclic barrier
    /**
     * Reset of a barrier after timeout reinitializes it.
     */
    class TUnitTestResetAfterTimeout extends MultithreadedTest {
    	CyclicBarrier start = new CyclicBarrier(3); // use the same barriers each time the test is run
    	CyclicBarrier barrier = new CyclicBarrier(3);
    	
    	public void thread1() throws InterruptedException, BrokenBarrierException {
			start.await();
			try { barrier.await(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS); }
			catch(TimeoutException ok) {}
    	}
    	
    	public void thread2() throws InterruptedException, BrokenBarrierException {    		
			start.await();
			try { barrier.await(); }
			catch(BrokenBarrierException ok) {}
    	}

    	public void thread3() throws InterruptedException, BrokenBarrierException {    		
			start.await();
    	}

    	@Override public void finish() {
			assertTrue(barrier.isBroken());
			assertEquals(0, barrier.getNumberWaiting());
			barrier.reset();
			assertFalse(barrier.isBroken());
			assertEquals(0, barrier.getNumberWaiting());
    	}
    	
    	@Override public void runTest() throws Throwable {
			TestFramework.runManyTimes( this, 3 ); // instead of for-loop
		}    	    	    	
    }    
    // TUNIT Untimed Block/Wait


    // REVIEW <=> Interleaving threads synchronized by a cyclic barrier
    /**
     * Reset of a barrier after a failed command reinitializes it.
     */
    class TUnitTestResetAfterCommandException extends MultithreadedTest {
    	CyclicBarrier start = new CyclicBarrier(3); // use the same barriers each time the test is run
    	CyclicBarrier barrier = 
    		new CyclicBarrier(3, new Runnable() {
				public void run() { 
					throw new NullPointerException(); }});
    	
    	public void thread1() throws InterruptedException, BrokenBarrierException {
			start.await();
			try { barrier.await(); }
			catch(BrokenBarrierException ok) {}
    	}
    	
    	public void thread2() throws InterruptedException, BrokenBarrierException {    		
			start.await();
			try { barrier.await(); }
			catch(BrokenBarrierException ok) {}
    	}

    	public void thread3() throws InterruptedException, BrokenBarrierException {    		
			start.await();
			while (barrier.getNumberWaiting() < 2) { Thread.yield(); }
			try { barrier.await(); }
			catch (Exception ok) { }
    	}

    	@Override public void finish() {
			assertTrue(barrier.isBroken());
			assertEquals(0, barrier.getNumberWaiting());
			barrier.reset();
			assertFalse(barrier.isBroken());
			assertEquals(0, barrier.getNumberWaiting());
    	}
    	
    	@Override public void runTest() throws Throwable {
			TestFramework.runManyTimes( this, 3 ); // instead of for-loop
		}    	    	
    }    
    // TUNIT Untimed Block/Wait
}
