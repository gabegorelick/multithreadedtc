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

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

@SuppressWarnings("unchecked")
public class FutureTaskTest extends JSR166TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}
	public static Test suite() {
		return TestFramework.buildTestSuite(FutureTaskTest.class);
	}

    /**
     * Subclass to expose protected methods
     */
    static class PublicFutureTask extends FutureTask {
        public PublicFutureTask(Callable r) { super(r); }
        public boolean runAndReset() { return super.runAndReset(); }
        public void set(Object x) { super.set(x); }
        public void setException(Throwable t) { super.setException(t); }
    }

    // = = Group 1 = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

    // REVIEW Simple Test for Action that causes unblocking and exceptions
    /**
     * cancel(true) interrupts a running task
     */
    /**
     * TUnit Version: use waitForTick(2) in Future task thread to ensure
     * that FutureTask is still blocked when task.cancel is called.
     * Note that we have to catch an AssertionError instead of an
     * InterruptedException because waitForTick does not throw this exception
     * when it is interrupted
     */
    class TUnitTestCancelInterrupt extends MultithreadedTest {

    	FutureTask task; 

    	@Override public void initialize() {
    		task = new FutureTask( new Callable() {
    			public Object call() {
    				try {
    					waitForTick(2);
    					fail("should throw exception");
    				}
    				catch (AssertionError success) { assertTick(1); }
    				return Boolean.TRUE;
    			} });
    	}

    	public void thread1() {
    		task.run();
    	}

    	public void thread2() {
    		waitForTick(1);
    		assertTrue(task.cancel(true));
    	}

    	@Override public void finish() {
    		assertTrue(task.isDone());
    		assertTrue(task.isCancelled());
    	}

    }
    // TUNIT Untimed Interrupt/Cancel


    // = = Group 2 = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

    // REVIEW Simple Test for Action that causes unblocking (but no exceptions)
    /**
     * cancel(false) does not interrupt a running task
     * TUnit Version: use waitForTick(2) in Future task thread to ensure
     * that FutureTask is still blocked when task.cancel is called.
     */
    class TUnitTestCancelNoInterrupt extends MultithreadedTest {

    	FutureTask task; 

    	public void initialize() {
    		task = new FutureTask( new Callable() {
    			public Object call() {
   					waitForTick(2);
    				return Boolean.TRUE;
    			} });
    	}

    	public void thread1() {
    		task.run();
    	}

    	public void thread2() {
    		waitForTick(1);
   			assertTrue(task.cancel(false));
    	}
    	
    	@Override public void finish() {
			assertTrue(task.isDone());
			assertTrue(task.isCancelled());
    	}
    }
    // TUNIT Untimed Block/Wait


    // = = Group 3 = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

    // REVIEW Test for blocking plus eventual unblocking
    /**
     * set in one thread causes get in another thread to retrieve value
     * TUnit Version: Use metronome instead of Thread.sleep to synchronize
     * the different threads
     */
    class TUnitTestGet1 extends MultithreadedTest {

    	FutureTask ft; 

    	public void initialize() {
    		ft = new FutureTask(new Callable() {
    			public Object call() {
    				waitForTick(2);
    				return Boolean.TRUE;
    			}
    		});
    		assertFalse(ft.isDone());
    		assertFalse(ft.isCancelled());
    	}

    	public void thread1() throws Exception {
    		ft.get();
    		assertTick(2);
    	}

    	public void thread2() {
    		waitForTick(1);
    		ft.run();
    	}

    	@Override public void finish() {
    		assertTrue(ft.isDone());
    		assertFalse(ft.isCancelled());
    	}
    }
    // TUNIT Untimed Block/Wait


    // = = Group 4 = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

    // REVIEW Test for blocking until timeout
    /**
     * set in one thread causes timed get in another thread to retrieve value
     * 
     * TUnit Version: Use metronome instead of Thread.sleep to synchronize
     * the different threads. 
     * 
     * Since the original test depended on delay
     * constants, it allowed the test to pass both if ft.get times out (the 
     * expected outcome) or if ft.call() finishes and returns a value before
     * ft.get() times out. In this version we can more precisely ensure that
     * ft.get() times out before ft.call() finishes. To do this we have to ensure
     * that while ft.get() is blocking, the metronome does not advance to the
     * next tick. We use freezeClock to do this.
     * (We could also do this by waiting on a condition)
     */
    class TUnitTestTimedGet1 extends MultithreadedTest {

    	FutureTask ft; 

    	public void initialize() {
    		ft = new FutureTask(new Callable() {
    			public Object call() {
   					waitForTick(2);
    				return Boolean.TRUE;
    			}
    		});
    		assertFalse(ft.isDone());
    		assertFalse(ft.isCancelled());
    	}

    	public void thread1() throws Exception {
    		freezeClock();
    		try {
    			ft.get(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);				
    			shouldThrow(); // We can now add this because of freezeClock()				
    		} catch(TimeoutException success) { assertTick(0); }
    		unfreezeClock();
    	}

    	public void thread2() {
    		waitForTick(1);
    		ft.run();
    	}

    	@Override public void finish() {
    		assertTrue(ft.isDone());
    		assertFalse(ft.isCancelled());
    	}
    }
    // TUNIT Timed Block/Wait


    // = = Group 5 = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

    /**
     *  Cancelling a task causes timed get in another thread to throw CancellationException
     */
    class TUnitTestTimedGet_Cancellation extends MultithreadedTest {
    	FutureTask ft; 
    	@Override public void initialize() {
    		ft = new FutureTask(new Callable() {
    			public Object call() {
        			try {
        				waitForTick(2);
        				fail("should throw exception");
        			} catch(AssertionError success) { assertTick(1); }   					
    				return Boolean.TRUE;
    			}
    		});
    	}
    	
    	public void thread1() throws InterruptedException, ExecutionException, TimeoutException {
			try {
				ft.get(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch(CancellationException success) { assertTick(1); }
    	}
    	
    	public void thread2() {  
    		ft.run();
    	}

    	public void thread3() {  
    		waitForTick(1);
    		ft.cancel(true);
    	}
    }    
    // TUNIT Untimed Interrupt/Cancel


    /**
     * Cancelling a task causes get in another thread to throw CancellationException
     */
    class TUnitTestGet_Cancellation extends MultithreadedTest {
    	FutureTask ft; 
    	@Override public void initialize() {
    		ft = new FutureTask(new Callable() {
    			public Object call() {
        			try {
        				waitForTick(2);
        				fail("should throw exception");
        			} catch(AssertionError success) { assertTick(1); }   					
    				return Boolean.TRUE;
    			}
    		});
    	}
    	
    	public void thread1() throws InterruptedException, ExecutionException {
			try {
				ft.get();
				fail("should throw exception");
			} catch(CancellationException success) { assertTick(1); }
    	}
    	
    	public void thread2() {  
    		ft.run();
    	}

    	public void thread3() {  
    		waitForTick(1);
    		ft.cancel(true);
    	}
    }    
    // TUNIT Untimed Interrupt/Cancel


    /**
     * Interrupting a waiting get causes it to throw InterruptedException
     */
    class TUnitTestGet_InterruptedException extends MultithreadedTest {
    	FutureTask ft; 
    	@Override public void initialize() {
    		ft = new FutureTask(new NoOpCallable());
    	}
    	
    	public void thread1() throws ExecutionException {
			try {
				ft.get();
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
     *  Interrupting a waiting timed get causes it to throw InterruptedException
     */
    class TUnitTestTimedGet_InterruptedException2 extends MultithreadedTest {
    	FutureTask ft; 
    	@Override public void initialize() {
    		ft = new FutureTask(new NoOpCallable());
    	}
    	
    	public void thread1() throws ExecutionException, TimeoutException {
			try {
				ft.get(LONG_DELAY_MS,TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch(InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {    		
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Timed Interrupt/Cancel
}
