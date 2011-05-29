package sanity;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

/**
 * Basic tests checking thread ordering, blocking, etc.
 * 
 * @author William Pugh
 * @author Nathaniel Ayewah
 * @since 1.0
 */
public class BasicTest extends TestCase {	
	
	/**
	 * Tests the sanity of the methods ordering with {@link #waitForTick(int)}.
	 */
	class TestMetronomeOrder extends MultithreadedTestCase {
		StringBuffer output = new StringBuffer();
		
		@Override public void initialize() {
    		output.append("^");
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		output.append("A");

    		waitForTick(3);
    		output.append("C");

    		waitForTick(6);
    		output.append("F");
    	}
    	
    	public void thread2() {
    		waitForTick(2);
    		output.append("B");
    		
    		waitForTick(5);
    		output.append("E");
    		
    		waitForTick(8);
    		output.append("H");
    	}

    	public void thread3() {
    		waitForTick(4);
    		output.append("D");
    		
    		waitForTick(7);
    		output.append("G");
    		
    		waitForTick(9);
    		output.append("I");
    	}
    	
    	@Override public void finish() {
    		output.append("$");
    	}
    }
    
    public void testMetronomeOrder() throws Throwable {
    	TestMetronomeOrder test = new TestMetronomeOrder();
		TestFramework.runOnce(test);
		assertEquals("Threads were not called in correct order",
				"^ABCDEFGHI$", test.output.toString());
    }
    
    /**
     * Checks that a test with no threads is successfully executed.
     * Check that {@link #initialize()} is called before {@link #finish()}.  
     */
    class TestWithNoThreads extends MultithreadedTestCase {
    	private AtomicInteger counter;

    	@Override public void initialize() {
    		counter = new AtomicInteger(0);
    		assertTrue(counter.compareAndSet(0, 1));
    	}
    	
    	@Override public void finish() {
    		assertTrue(counter.compareAndSet(1, 2));
    	}
    }
    
    public void testTestWithNoThreads_tunit() throws Throwable {
    	TestWithNoThreads test = new TestWithNoThreads();
		TestFramework.runOnce(test);
		assertEquals(2, test.counter.get());
    }

    /**
     * Test that the order of invocation goes: {@link #initialize()}, then the
     * threads, then {@link #finish()}.
     */
    class TestInitBeforeThreadsBeforeFinish extends MultithreadedTestCase {
    	private AtomicInteger v1 = new AtomicInteger(0);
    	private AtomicInteger v2 = new AtomicInteger(0);
    	CyclicBarrier barrier;
    	@Override public void initialize() {
    		assertTrue(v1.compareAndSet(0, 1));
    		assertTrue(v2.compareAndSet(0, 1));
    		barrier = new CyclicBarrier(2);
    	}
    	
    	public void thread1() throws Exception {
    		assertTrue(v1.compareAndSet(1, 2));
    		barrier.await();
    	}
    	
    	public void thread2() throws Exception {
    		assertTrue(v2.compareAndSet(1, 2));
    		barrier.await();
    	}

    	@Override public void finish() {
    		assertEquals(2, v1.intValue());
    		assertEquals(2, v2.intValue());
    	}
    }
    
    public void testInitBeforeThreadsBeforeFinish() throws Throwable {
    	TestFramework.runOnce(new TestInitBeforeThreadsBeforeFinish());
    }
    
    /**
     * Tests that when the other threads are blocked, a call to
     * {@link #waitForTick(int)} returns immediately.
     */
    class TestWaitForTickAdvancesWhenTestsAreBlocked extends MultithreadedTestCase {
    	CyclicBarrier barrier = new CyclicBarrier(3);
    	
    	public void thread1() throws Exception {
    		barrier.await();
    	}
    	
    	public void thread2() throws Exception {    		
    		barrier.await();
    	}

    	public void thread3() throws Exception {
    		waitForTick(1);
    		assertEquals(2, barrier.getNumberWaiting());
    		waitForTick(2); // advances quickly
    		assertEquals(2, barrier.getNumberWaiting());
    		barrier.await();
    	}
    }
    
    public void testSanityWaitForTickAdvancesWhenTestsAreBlocked() throws Throwable {
    	TestWaitForTickAdvancesWhenTestsAreBlocked test = new TestWaitForTickAdvancesWhenTestsAreBlocked();
		TestFramework.runOnce(test);
		assertEquals(0, test.barrier.getNumberWaiting());
    }
    
    /**
     * Tests that when a thread is waiting on a {@link #waitForTick(int)},
     * its state is {@value Thread.State#WAITING}. 
     */
	class TestWaitForTickBlocksThread extends MultithreadedTestCase {
    	Thread t;
    	public void thread1() {
    		t = Thread.currentThread();
    		waitForTick(2);
    	}
    	
    	public void thread2() {   
    		waitForTick(1);
    		assertEquals(Thread.State.WAITING, t.getState());
    	}
    }
        
    public void testWaitForTickBlocksThread() throws Throwable {
    	TestFramework.runOnce(new TestWaitForTickBlocksThread());
    }

    /**
     * Tests that when {@link #finish()} is called, the test threads are in
     * state {@value Thread.State#TERMINATED}.
     */
	class TestThreadTerminatesBeforeFinishIsCalled extends MultithreadedTestCase {
		Thread t1, t2;
    	public void thread1() {
    		t1 = Thread.currentThread();
    	}
    	
    	public void thread2() {
    		t2 = Thread.currentThread();
    	}

    	@Override public void finish() {
    		assertEquals(Thread.State.TERMINATED, t1.getState());
    		assertEquals(Thread.State.TERMINATED, t2.getState());
    	}
    }
        
    public void testThreadTerminatesBeforeFinishIsCalled() throws Throwable {
    	TestFramework.runOnce(new TestThreadTerminatesBeforeFinishIsCalled());
    }

    
    /**
     * Tests that the test methods are each invoked in its own thread.
     */
	class TestThreadMethodsInvokedInDifferentThreads extends MultithreadedTestCase {
		Thread t1, t2;
    	
    	public void thread1() {
    		t1 = Thread.currentThread();
    	}
    	
    	public void thread2() {    		
    		t2 = Thread.currentThread();
    	}
    }
        
    public void testThreadMethodsInvokedInDifferentThreads() throws Throwable {
    	TestThreadMethodsInvokedInDifferentThreads test = new TestThreadMethodsInvokedInDifferentThreads();
		TestFramework.runOnce(test);
		assertNotNull(test.t1);
		assertNotNull(test.t2);
		assertNotSame(test.t1, test.t2);
    }

    /**
     * Test that {@link #getThread(int)} returns the correct thread.
     */
	class TestGetThreadReturnsCorrectThread extends MultithreadedTestCase {
    	Thread expected;
    	Thread actual;
    	
    	public void thread1() {
    		expected = Thread.currentThread();
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		actual = getThread(1);
    	}
    }
        
    public void testGetThreadReturnsCorrectThread() throws Throwable {
    	TestGetThreadReturnsCorrectThread test = new TestGetThreadReturnsCorrectThread();
		TestFramework.runOnce(test);
		assertSame(test.expected, test.actual);
    }
    
    /**
     * Test that {@link #getThreadByName(String)} returns the correct thread.
     */
	class TestGetThreadByNameReturnsCorrectThread extends MultithreadedTestCase {
    	Thread expected;
    	Thread actual;
    	
    	public void threadFooey() {
    		expected = Thread.currentThread();
    	}
    	
    	public void threadBooey() {
    		waitForTick(1);
    		actual = getThreadByName("threadFooey");
    	}
    }
        
    public void testGetThreadByNameReturnsCorrectThread() throws Throwable {
    	TestGetThreadByNameReturnsCorrectThread test = new TestGetThreadByNameReturnsCorrectThread();
		TestFramework.runOnce(test);
		assertSame(test.expected, test.actual);
    }


}
