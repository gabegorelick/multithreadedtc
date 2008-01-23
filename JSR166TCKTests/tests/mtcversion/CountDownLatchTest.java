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

public class CountDownLatchTest extends JSR166TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}
	public static Test suite() {
		return TestFramework.buildTestSuite(CountDownLatchTest.class);
	}


	// TUNIT
    /**
     * await returns after countDown to zero, but not before
     */
    class TUnitTestAwait extends MultithreadedTest {
    	CountDownLatch l;

    	@Override public void initialize() {
    		l = new CountDownLatch(2);
    	}

    	public void thread1() throws InterruptedException {
    		assertTrue(l.getCount() > 0);
    		l.await();
    		assertTick(1);
    		assertTrue(l.getCount() == 0);
    	}

    	public void thread2() {    		
    		assertEquals(l.getCount(), 2);
    		waitForTick(1);
    		l.countDown();
    		assertEquals(l.getCount(), 1);
    		l.countDown();
    		assertEquals(l.getCount(), 0);
    	}
    }


    /**
     * timed await returns after countDown to zero
     */
    class TUnitTestTimedAwait extends MultithreadedTest {
    	CountDownLatch l;

    	@Override public void initialize() {
    		l = new CountDownLatch(2);
    	}

    	public void thread1() throws InterruptedException {
			assertTrue(l.getCount() > 0);
			assertTrue(l.await(SMALL_DELAY_MS, TimeUnit.MILLISECONDS));
			assertTick(1);
    	}

    	public void thread2() {    		
    		assertEquals(l.getCount(), 2);
    		waitForTick(1);
    		l.countDown();
    		assertEquals(l.getCount(), 1);
    		l.countDown();
    		assertEquals(l.getCount(), 0);
    	}
    }
    // TUNIT Timed Block/Wait
    
    
    /**
     * await throws IE if interrupted before counted down
     */
    class TUnitTestAwait_InterruptedException extends MultithreadedTest {
    	CountDownLatch l; 
    	@Override public void initialize() {
    		l = new CountDownLatch(1);
    	}
    	
    	public void thread1() {
			try {
				assertTrue(l.getCount() > 0);
				l.await();
				fail("should throw exception");
			} catch(InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() { 
    		waitForTick(1);
    		assertEquals(l.getCount(), 1);
    		getThread(1).interrupt();
    	}

    }    
    // TUNIT Untimed Interrupt/Cancel


    /**
     * timed await throws IE if interrupted before counted down
     */
    class TUnitTestTimedAwait_InterruptedException extends MultithreadedTest {
    	CountDownLatch l; 
    	@Override public void initialize() {
    		l = new CountDownLatch(1);
    	}
    	
    	public void thread1() {
			try {
				assertTrue(l.getCount() > 0);
				l.await(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");                        
			} catch(InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {    	
    		waitForTick(1);
    		assertEquals(l.getCount(), 1);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Timed Interrupt/Cancel


    /**
     * timed await times out if not counted down before timeout
     */
    class TUnitTestAwaitTimeout extends MultithreadedTest {
    	CountDownLatch l; 
    	@Override public void initialize() {
    		l = new CountDownLatch(1);
    	}
    	
    	public void thread1() throws InterruptedException {
			assertTrue(l.getCount() > 0);
			assertFalse(l.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			assertTrue(l.getCount() > 0);
    	}
    	
    	public void thread2() { 
    		assertEquals(l.getCount(), 1);
    	}
    }    
    // TUNIT Untimed Block/Wait
}
