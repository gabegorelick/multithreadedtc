package sanity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;

public class BasicTests extends TestCase {	
	
	// Test
	class SanityMetronomeOrder extends MultithreadedTestCase {
		String s;
		
    	@Override public void initialize() {
    		s = "";
    	}
    	
    	public void thread1() {
    		waitForTick(1);
    		s += "A";

    		waitForTick(3);
    		s += "C";

    		waitForTick(6);
    		s += "F";
    	}
    	
    	public void thread2() {
    		waitForTick(2);
    		s += "B";
    		
    		waitForTick(5);
    		s += "E";
    		
    		waitForTick(8);
    		s += "H";
    	}

    	public void thread3() {
    		waitForTick(4);
    		s += "D";
    		
    		waitForTick(7);
    		s += "G";
    		
    		waitForTick(9);
    		s += "I";
    	}
    	
    	@Override public void finish() {
    		assertEquals("Threads were not called in correct order",
    				s, "ABCDEFGHI");
    	}
    }
    
    public void testMetronomeOrder() throws Throwable {
    	TestFramework.runOnce( new SanityMetronomeOrder() );
    }
    
    // Test
    class TUnitTestTestWithNoThreads extends MultithreadedTestCase {
    	private AtomicInteger v1;
    	@Override public void initialize() {
    		v1 = new AtomicInteger(0);
    		assertTrue(v1.compareAndSet(0, 1));
    	}
    	
    	@Override public void finish() {
    		assertTrue(v1.compareAndSet(1, 2));
    	}
    }
    
    public void testTestWithNoThreads_tunit() throws Throwable {
    	TestFramework.runOnce( new TUnitTestTestWithNoThreads() );
    }

    // Test order called is init, then thread, then finish
    class SanityInitBeforeThreadsBeforeFinish extends MultithreadedTestCase {
    	private AtomicInteger v1, v2;
    	CountDownLatch c;
    	@Override public void initialize() {
    		v1 = new AtomicInteger(0);
    		v2 = new AtomicInteger(0);
    		assertTrue(v1.compareAndSet(0, 1));
    		assertTrue(v2.compareAndSet(0, 1));
    		c = new CountDownLatch(2);
    	}
    	
    	public void thread1() throws InterruptedException {
    		assertTrue(v1.compareAndSet(1, 2));
    		c.countDown();
    		c.await();
    	}
    	
    	public void thread2() throws InterruptedException {
    		assertTrue(v2.compareAndSet(1, 2));
    		c.countDown();
    		c.await();
    	}

    	@Override public void finish() {
    		assertEquals(2, v1.intValue());
    		assertEquals(2, v2.intValue());
    	}
    }
    
    public void testSanityInitBeforeThreadsBeforeFinish() throws Throwable {
    	TestFramework.runOnce( new SanityInitBeforeThreadsBeforeFinish() );
    }
    
    // Test
    class SanityWaitForTickAdvancesWhenTestsAreBlocked extends MultithreadedTestCase {
    	CountDownLatch c;
    	@Override public void initialize() {
    		c = new CountDownLatch(3);
    	}
    	
    	public void thread1() throws InterruptedException {
    		c.countDown();
    		c.await();
    	}
    	
    	public void thread2() throws InterruptedException {    		
    		c.countDown();
    		c.await();
    	}

    	public void thread3() {
    		waitForTick(1);
    		assertEquals(1, c.getCount());
    		waitForTick(2); // advances quickly
    		assertEquals(1, c.getCount());
    		c.countDown();
    	}

    	@Override public void finish() {
    		assertEquals(0, c.getCount());
    	}
    }
    
    public void testSanityWaitForTickAdvancesWhenTestsAreBlocked() throws Throwable {
    	TestFramework.runOnce( new SanityWaitForTickAdvancesWhenTestsAreBlocked() );
    }
    
    // Test
	class SanityWaitForTickBlocksThread extends MultithreadedTestCase {
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
        
    public void testSanityWaitForTickBlocksThread() throws Throwable {
    	TestFramework.runOnce( new SanityWaitForTickBlocksThread() );
    }

    // Test
	class SanityThreadTerminatesBeforeFinishIsCalled extends MultithreadedTestCase {
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
        
    public void testSanityThreadTerminatesBeforeFinishIsCalled() throws Throwable {
    	TestFramework.runOnce( new SanityThreadTerminatesBeforeFinishIsCalled() );
    }

    
    // Test
	class SanityThreadMethodsInvokedInDifferentThreads extends MultithreadedTestCase {
		Thread t1, t2;
    	
    	public void thread1() {
    		t1 = Thread.currentThread();
    		waitForTick(2);    		
    	}
    	
    	public void thread2() {    		
    		t2 = Thread.currentThread();
    		waitForTick(2);
    	}

    	public void thread3() {    		
    		waitForTick(1);
    		assertNotSame(t1, t2);
    	}
    }
        
    public void testSanityThreadMethodsInvokedInDifferentThreads() throws Throwable {
    	TestFramework.runOnce( new SanityThreadMethodsInvokedInDifferentThreads() );
    }

    // Test
	class SanityGetThreadReturnsCorrectThread extends MultithreadedTestCase {
    	Thread t;
    	
    	public void thread1() {
    		t = Thread.currentThread();
    		waitForTick(2);
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		assertSame(getThread(1), t);
    	}
    }
        
    public void testSanityGetThreadReturnsCorrectThread() throws Throwable {
    	TestFramework.runOnce( new SanityGetThreadReturnsCorrectThread() );
    }
    
    // Test
	class SanityGetThreadByNameReturnsCorrectThread extends MultithreadedTestCase {
    	Thread t;
    	
    	public void threadFooey() {
    		t = Thread.currentThread();
    		waitForTick(2);
    	}
    	
    	public void threadBooey() {
    		waitForTick(1);
    		assertSame(getThreadByName("threadFooey"), t);
    	}
    }
        
    public void testSanityGetThreadByNameReturnsCorrectThread() throws Throwable {
    	TestFramework.runOnce( new SanityGetThreadByNameReturnsCorrectThread() );
    }


}
