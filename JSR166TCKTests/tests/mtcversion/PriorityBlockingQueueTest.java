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

public class PriorityBlockingQueueTest extends JSR166TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}
	public static Test suite() {
		return TestFramework.buildTestSuite(PriorityBlockingQueueTest.class);
	}

    private static final int NOCAP = Integer.MAX_VALUE;

    /** Sample Comparator */
    static class MyReverseComparator implements Comparator { 
        public int compare(Object x, Object y) {
            int i = ((Integer)x).intValue();
            int j = ((Integer)y).intValue();
            if (i < j) return 1;
            if (i > j) return -1;
            return 0;
        }
    }

    /**
     * Create a queue of given size containing consecutive
     * Integers 0 ... n.
     */
    private PriorityBlockingQueue populatedQueue(int n) {
    	PriorityBlockingQueue q = new PriorityBlockingQueue(n);
    	assertTrue(q.isEmpty());
    	for(int i = n-1; i >= 0; i-=2)
    		assertTrue(q.offer(new Integer(i)));
    	for(int i = (n & 1); i < n; i+=2)
    		assertTrue(q.offer(new Integer(i)));
    	assertFalse(q.isEmpty());
    	assertEquals(NOCAP, q.remainingCapacity());
    	assertEquals(n, q.size());
    	return q;
    }


    /**
     * put doesn't block waiting for take
     */
    class TUnitTestPutWithTake extends MultithreadedTest {
    	
    	PriorityBlockingQueue q;    	
    	@Override public void initialize() {
    		q = new PriorityBlockingQueue(2);
    	}
    	
    	public void thread1() {        	
			int added = 0;
			q.put(new Integer(0));
			++added;
			q.put(new Integer(0));
			++added;
			q.put(new Integer(0));
			++added;
			q.put(new Integer(0));
			++added;
			assertTrue(added == 4);
			assertTick(0); // never blocks
    	}
    	
    	public void thread2() throws InterruptedException {    		
            waitForTick(1);
            q.take();
            getThread(1).interrupt();
    	}
    }    
    // TUNIT Untimed Interrupt/Cancel
    // TUNIT Untimed Block/Wait

    
    /**
     * timed offer does not time out
     */
    class TUnitTestTimedOffer extends MultithreadedTest {
    	PriorityBlockingQueue q;
    	
    	@Override public void initialize() {
    		q = new PriorityBlockingQueue(2);
    	}
    	
    	public void thread1() throws InterruptedException {
			q.put(new Integer(0));
			q.put(new Integer(0));
			assertTrue(q.offer(new Integer(0), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			assertTrue(q.offer(new Integer(0), LONG_DELAY_MS, TimeUnit.MILLISECONDS));
			assertTick(0); // never blocks
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }  
    // TUNIT Timed Interrupt/Cancel


    /**
     * take blocks interruptibly when empty
     */
    class TUnitTestTakeFromEmpty extends MultithreadedTest {
    	PriorityBlockingQueue q;
    	
    	@Override public void initialize() {
    		q = new PriorityBlockingQueue(2);
    	}
    	    	
    	public void thread1() {
        	try {
        		q.take();
        		fail("should throw exception");
        	} catch (InterruptedException success){ assertTick(1); }                
    	}
    	
    	public void thread2() {  
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }   
    // TUNIT Untimed Interrupt/Cancel


    /**
     * Take removes existing elements until empty, then blocks interruptibly
     */
	class TUnitTestBlockingTake extends MultithreadedTest {
    	
    	public void thread1() {
			try {
				PriorityBlockingQueue q = populatedQueue(SIZE);
				for (int i = 0; i < SIZE; ++i) {
					assertEquals(i, ((Integer)q.take()).intValue());
				}
				q.take();
				fail("should throw exception");
            } catch (InterruptedException success){ assertTick(1); }   
    	}
    	
    	public void thread2() { 
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Untimed Interrupt/Cancel


    /**
     * Interrupted timed poll throws InterruptedException instead of
     * returning timeout status
     */
    class TUnitTestInterruptedTimedPoll extends MultithreadedTest {
    	
    	public void thread1() {
            try {
				PriorityBlockingQueue q = populatedQueue(SIZE);
				for (int i = 0; i < SIZE; ++i) {
					assertEquals(i, ((Integer)q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)).intValue());
				}
				assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            } catch (InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }   
    // TUNIT Timed Interrupt/Cancel

    
    /**
     *  timed poll before a delayed offer fails; after offer succeeds;
     *  on interruption throws
     */
    class TUnitTestTimedPollWithOffer extends MultithreadedTest {
    	PriorityBlockingQueue q; 
    	
    	@Override public void initialize() {
    		q = new PriorityBlockingQueue(2);
    	}
    	
    	public void thread1() throws InterruptedException {
    		freezeClock();
			assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
    		unfreezeClock();
    		
        	try {
				q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
        		assertTick(1);
				q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
        		fail("should throw exception");
        	} catch (InterruptedException success) { assertTick(2); }                
    	}
    	
    	public void thread2() throws InterruptedException {    	
            waitForTick(1);
    		assertTrue(q.offer(new Integer(0), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            waitForTick(2);
    		getThread(1).interrupt();
    	}
    }  
    // TUNIT Timed Interrupt/Cancel
    // TUNIT Timed Block/Wait


    /**
     * drainTo empties queue
     */ 
    // REVIEW Interesting test
    /**
     * This test allows for 2 interleavings: one where q.put(...) occurs before
     * q.drainTo(l) (i.e. l.size() == SIZE+1) and one where it occurs after,
     * (i.e. l.size() == SIZE). The test passes in either case. A test like this
     * should either be broken into two tests, one testing each case, or run many times...
     * Or this could just be a bad (ambiguous) test, and the tester only intended to
     * test the former case (as in SynchronousQueueTest).
     */
    class TUnitTestDrainToWithActivePut extends MultithreadedTest {
    	PriorityBlockingQueue q;
    	@Override public void initialize() {
    		q = populatedQueue(SIZE);
    	}
    	
		public void thread1() throws InterruptedException {
			q.put(new Integer(SIZE+1));
			assertTick(0);
    	}
    	
    	ArrayList l;
    	public void thread2() {
    		waitForTick(1);
            l = new ArrayList();
    		q.drainTo(l);
    		assertTrue(l.size() >= SIZE);
    		for (int i = 0; i < SIZE; ++i) 
    			assertEquals(l.get(i), new Integer(i));
    	}

    	@Override public void finish() {
    		assertTrue(q.size() + l.size() >= SIZE);
    	}
    }   
    // TUNIT Untimed Block/Wait
}
