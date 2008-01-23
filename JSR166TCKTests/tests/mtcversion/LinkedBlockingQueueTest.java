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

public class LinkedBlockingQueueTest extends JSR166TestCase {

    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }

    public static Test suite() {
	return TestFramework.buildTestSuite(LinkedBlockingQueueTest.class);
    }


    /**
     * Create a queue of given size containing consecutive
     * Integers 0 ... n.
     */
    private LinkedBlockingQueue populatedQueue(int n) {
        LinkedBlockingQueue q = new LinkedBlockingQueue(n);
        assertTrue(q.isEmpty());
	for(int i = 0; i < n; i++)
	    assertTrue(q.offer(new Integer(i)));
        assertFalse(q.isEmpty());
        assertEquals(0, q.remainingCapacity());
	assertEquals(n, q.size());
        return q;
    }
 

     // REVIEW <=> Simple test for blocking
    /**
     * put blocks interruptibly if full
     */
    class TUnitTestBlockingPut extends MultithreadedTest {    	
    	public void thread1() {
            int added = 0;
            try {
                LinkedBlockingQueue q = new LinkedBlockingQueue(SIZE);
                for (int i = 0; i < SIZE; ++i) {
                    q.put(new Integer(i));
                    ++added;
                }
                q.put(new Integer(SIZE));
                fail("should throw exception");
            } catch (InterruptedException ie){
                assertEquals(added, SIZE);
                assertTick(1);
            }   
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Untimed Interrupt/Cancel


    // REVIEW <=> Test for blocking plus possible unblocking
    /**
     * put blocks waiting for take when full
     */
    class TUnitTestPutWithTake extends MultithreadedTest {
    	LinkedBlockingQueue q;
    	@Override public void initialize() {
    		q = new LinkedBlockingQueue(2);
    	}
    	
    	public void thread1() {
			int added = 0;
			try {
				q.put(new Object());
				++added;
				q.put(new Object());
				++added;
				q.put(new Object());
				++added;
				q.put(new Object());
				++added;
				fail("should throw exception");
			} catch (InterruptedException e){
				assertTrue(added >= 2);
				assertTick(1);
			}
    	}
    	
    	public void thread2() throws InterruptedException {    		
    		waitForTick(1);
    		q.take();
    		getThread(1).interrupt();    
    	}
    }    
    // TUNIT Untimed Interrupt/Cancel


    // REVIEW <=> Simple test for blocking. This tests asserts a timeout that may not occur
    /**
     * timed offer times out if full and elements not taken
     */
    class TUnitTestTimedOffer extends MultithreadedTest {
    	LinkedBlockingQueue q;
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingQueue(2);
    	}
    	
    	public void thread1() throws InterruptedException {
			try {
				q.put(new Object());
				q.put(new Object());
				
				freezeClock();
				assertFalse(q.offer(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
				unfreezeClock();
				
				q.offer(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch (InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }   
    // TUNIT Timed Interrupt/Cancel


    // REVIEW <=> Simple test for blocking
    /**
     * take blocks interruptibly when empty
     */
    class TUnitTestTakeFromEmpty extends MultithreadedTest {
    	LinkedBlockingQueue q;
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingQueue(2);
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


    // REVIEW <=> Simple test for blocking
    /**
     * Take removes existing elements until empty, then blocks interruptibly
     */
	class TUnitTestBlockingTake extends MultithreadedTest {
    	
    	public void thread1() {
            try {
				LinkedBlockingQueue q = populatedQueue(SIZE);
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


    // REVIEW <=> Simple test for blocking (using poll)
    /**
     * Interrupted timed poll throws InterruptedException instead of
     * returning timeout status
     */
    class TUnitTestInterruptedTimedPoll extends MultithreadedTest {
    	
    	public void thread1() {
			try {
				LinkedBlockingQueue q = populatedQueue(SIZE);
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


    // REVIEW <=> Test for blocking plus possible unblocking
    /**
     *  timed poll before a delayed offer fails; after offer succeeds;
     *  on interruption throws
     */
    class TUnitTestTimedPollWithOffer extends MultithreadedTest {
    	LinkedBlockingQueue q; 
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingQueue(2);
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
            assertTrue(q.offer(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            waitForTick(2);
    		getThread(1).interrupt();
    	}
    }   
    // TUNIT Timed Interrupt/Cancel
    // TUNIT Timed Block/Wait


    // REVIEW <=> Test for blocking plus possible unblocking (with some correctness checks)
    /**
     * drainTo empties full queue, unblocking a waiting put.
     */ 
    class TUnitTestDrainToWithActivePut extends MultithreadedTest {
    	LinkedBlockingQueue q;
    	@Override public void initialize() {
    		q = populatedQueue(SIZE);
    	}
    	
		public void thread1() throws InterruptedException {
			q.put(new Integer(SIZE+1));
			assertTick(1);
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
