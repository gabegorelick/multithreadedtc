package sanity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.Thread.State;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.Threaded;

/**
 * Basic tests checking thread ordering, blocking, etc.
 * 
 * @author William Pugh
 * @author Nathaniel Ayewah
 * @since 1.0
 */
@RunWith(Enclosed.class)
public class BasicTest {	
	
	/**
	 * Tests the sanity of the methods ordering with {@link #waitForTick(long)}.
	 */
	// Enclosed test runner can only run public static nested classes
	public static class TestMetronomeOrder extends MultithreadedJUnit4TestCase {
		StringBuffer output = new StringBuffer();
		
		@Before 
		public void initialize() {
    		output.append("^");
    	}
    	
		@Threaded
    	public void thread1() {
    		waitForTick(1);
    		output.append("A");

    		waitForTick(3);
    		output.append("C");

    		waitForTick(6);
    		output.append("F");
    	}
    	
		@Threaded
    	public void thread2() {
    		waitForTick(2);
    		output.append("B");
    		
    		waitForTick(5);
    		output.append("E");
    		
    		waitForTick(8);
    		output.append("H");
    	}

		@Threaded
    	public void thread3() {
    		waitForTick(4);
    		output.append("D");
    		
    		waitForTick(7);
    		output.append("G");
    		
    		waitForTick(9);
    		output.append("I");
    	}
    	    	
    	@Test
        public void testMetronomeOrder() {
    		assertEquals("Threads were not called in correct order",
    				"^ABCDEFGHI", output.toString());
        }
    }
    
    /**
     * Checks that a test with no threads is successfully executed.
     * Check that {@link #initialize()} is called before {@link #finish()}.  
     */
    public static class TestWithNoThreads extends MultithreadedJUnit4TestCase {
    	private AtomicInteger counter;
 
    	@Before 
    	public void initialize() {
    		counter = new AtomicInteger(0);
    		assertTrue(counter.compareAndSet(0, 1));
    	}
    	    	
    	@Test
    	public void testTestWithNoThreads_tunit() {
    		assertEquals(1, counter.get());
        }
    }

    /**
     * Test that the order of invocation goes: {@link #initialize()}, then the
     * threads, then {@link #finish()}.
     */
    public static class TestInitBeforeThreadsBeforeFinish extends MultithreadedJUnit4TestCase {
    	private AtomicInteger v1 = new AtomicInteger(0);
    	private AtomicInteger v2 = new AtomicInteger(0);
    	CyclicBarrier barrier;
    	
    	@Before
    	public void initialize() {
    		assertTrue(v1.compareAndSet(0, 1));
    		assertTrue(v2.compareAndSet(0, 1));
    		barrier = new CyclicBarrier(2);
    	}
    	
    	@Threaded
    	public void thread1() throws Exception {
    		assertTrue(v1.compareAndSet(1, 2));
    		barrier.await();
    	}
    	
    	@Threaded
    	public void thread2() throws Exception {
    		assertTrue(v2.compareAndSet(1, 2));
    		barrier.await();
    	}
    	
    	@Test
        public void testInitBeforeThreadsBeforeFinish() {
    		assertEquals(2, v1.intValue());
    		assertEquals(2, v2.intValue());
        }
    }
        
    /**
     * Tests that when the other threads are blocked, a call to
     * {@link #waitForTick(long)} returns immediately.
     */
    public static class TestWaitForTickAdvancesWhenTestsAreBlocked extends MultithreadedJUnit4TestCase {
    	CyclicBarrier barrier = new CyclicBarrier(3);
    	
    	@Threaded
    	public void thread1() throws Exception {
    		barrier.await();
    	}
    	
    	@Threaded
    	public void thread2() throws Exception {    		
    		barrier.await();
    	}

    	@Threaded
    	public void thread3() throws Exception {
    		waitForTick(1);
    		assertEquals(2, barrier.getNumberWaiting());
    		waitForTick(2); // advances quickly
    		assertEquals(2, barrier.getNumberWaiting());
    		barrier.await();
    	}
    	
    	@Test
        public void testSanityWaitForTickAdvancesWhenTestsAreBlocked() {
    		assertEquals(0, barrier.getNumberWaiting());
        }
    }
    
    /**
     * Tests that when a thread is waiting on a {@link #waitForTick(long)},
     * its state is {@value Thread.State#WAITING}. 
     */
	public static class TestWaitForTickBlocksThread extends MultithreadedJUnit4TestCase {
    	Thread t;
    	
    	@Threaded
    	public void thread1() {
    		t = Thread.currentThread();
    		waitForTick(2);
    	}
    	
    	@Threaded
    	public void thread2() {   
    		waitForTick(1);
    		State state = t.getState();
    		assertTrue(state == Thread.State.WAITING || state == Thread.State.BLOCKED);
    	}
    	
    	@Test
    	public void testWaitForTickBlocksThread() {
        	// this space left intentionally blank
        }
    }

    /**
     * Tests that when {@link #finish()} is called, the test threads are in
     * state {@value Thread.State#TERMINATED}.
     */
	public static class TestThreadTerminatesBeforeFinishIsCalled extends MultithreadedJUnit4TestCase {
		Thread t1, t2;
		
		@Threaded
    	public void thread1() {
    		t1 = Thread.currentThread();
    	}
    	
		@Threaded
    	public void thread2() {
    		t2 = Thread.currentThread();
    	}

    	@Test
    	public void testThreadTerminatesBeforeFinishIsCalled() {
    		assertEquals(Thread.State.TERMINATED, t1.getState());
    		assertEquals(Thread.State.TERMINATED, t2.getState());
        }
    }
    
    /**
     * Tests that the test methods are each invoked in its own thread.
     */
	public static class TestThreadMethodsInvokedInDifferentThreads extends MultithreadedJUnit4TestCase {
		Thread t1, t2;
    	
		@Threaded
    	public void thread1() {
    		t1 = Thread.currentThread();
    	}
    	
		@Threaded
    	public void thread2() {    		
    		t2 = Thread.currentThread();
    	}
        
		@Test
		public void testThreadMethodsInvokedInDifferentThreads() {
			assertNotNull(t1);
			assertNotNull(t2);
			assertNotSame(t1, t2);
		}

    }

    /**
     * Test that {@link #getThread(int)} returns the correct thread.
     */
	public static class TestGetThreadReturnsCorrectThread extends MultithreadedJUnit4TestCase {
    	Thread expected;
    	Thread actual;
    	
    	@Threaded
    	public void thread1() {
    		expected = Thread.currentThread();
    	}
    	
    	@Threaded
    	public void thread2() {
    		waitForTick(1);
    		actual = getThread(1);
    	}
    	
    	@Test
    	public void testGetThreadReturnsCorrectThread() {
    		assertSame(expected, actual);
        }
    }
    
    /**
     * Test that {@link #getThreadByName(String)} returns the correct thread.
     */
	public static class TestGetThreadByNameReturnsCorrectThread extends MultithreadedJUnit4TestCase {
    	Thread expected;
    	Thread actual;
    
    	@Threaded
    	public void threadFooey() {
    		expected = Thread.currentThread();
    	}
    	
    	@Threaded
    	public void threadBooey() {
    		waitForTick(1);
    		actual = getThreadByName("threadFooey");
    	}
    	
    	@Test
    	public void testGetThreadByNameReturnsCorrectThread() {
    		assertSame(expected, actual);
        }
    }
}
