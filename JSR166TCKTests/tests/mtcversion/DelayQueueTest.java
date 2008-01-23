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

public class DelayQueueTest extends JSR166TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}

	public static Test suite() {
		return TestFramework.buildTestSuite(DelayQueueTest.class);
	}

    private static final int NOCAP = Integer.MAX_VALUE;

    /**
     * A delayed implementation for testing.
     * Most  tests use Pseudodelays, where delays are all elapsed
     * (so, no blocking solely for delays) but are still ordered
     */ 
    static class PDelay implements Delayed { 
        int pseudodelay;
        PDelay(int i) { pseudodelay = Integer.MIN_VALUE + i; }
        public int compareTo(PDelay y) {
            int i = pseudodelay;
            int j = ((PDelay)y).pseudodelay;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }

        public int compareTo(Delayed y) {
            int i = pseudodelay;
            int j = ((PDelay)y).pseudodelay;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }

        public boolean equals(Object other) {
            return ((PDelay)other).pseudodelay == pseudodelay;
        }
        public boolean equals(PDelay other) {
            return ((PDelay)other).pseudodelay == pseudodelay;
        }


        public long getDelay(TimeUnit ignore) {
            return pseudodelay;
        }
        public int intValue() {
            return pseudodelay;
        }

        public String toString() {
            return String.valueOf(pseudodelay);
        }
    }


    /**
     * Delayed implementation that actually delays
     */
    static class NanoDelay implements Delayed { 
        long trigger;
        NanoDelay(long i) { 
            trigger = System.nanoTime() + i;
        }
        public int compareTo(NanoDelay y) {
            long i = trigger;
            long j = ((NanoDelay)y).trigger;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }

        public int compareTo(Delayed y) {
            long i = trigger;
            long j = ((NanoDelay)y).trigger;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }

        public boolean equals(Object other) {
            return ((NanoDelay)other).trigger == trigger;
        }
        public boolean equals(NanoDelay other) {
            return ((NanoDelay)other).trigger == trigger;
        }

        public long getDelay(TimeUnit unit) {
            long n = trigger - System.nanoTime();
            return unit.convert(n, TimeUnit.NANOSECONDS);
        }

        public long getTriggerTime() {
            return trigger;
        }

        public String toString() {
            return String.valueOf(trigger);
        }
    }


    /**
     * Create a queue of given size containing consecutive
     * PDelays 0 ... n.
     */
    private DelayQueue populatedQueue(int n) {
        DelayQueue q = new DelayQueue();
        assertTrue(q.isEmpty());
	for(int i = n-1; i >= 0; i-=2)
	    assertTrue(q.offer(new PDelay(i)));
	for(int i = (n & 1); i < n; i+=2)
	    assertTrue(q.offer(new PDelay(i)));
        assertFalse(q.isEmpty());
        assertEquals(NOCAP, q.remainingCapacity());
	assertEquals(n, q.size());
        return q;
    }
 

    /**
     * put doesn't block waiting for take
     */
    class TUnitTestPutWithTake extends MultithreadedTest {
    	DelayQueue q;
    	@Override public void initialize() {
    		q = new DelayQueue();
    	}
    	
    	public void thread1() {
            int added = 0;
            try {
                q.put(new PDelay(0));
                ++added;
                q.put(new PDelay(0));
                ++added;
                q.put(new PDelay(0));
                ++added;
                q.put(new PDelay(0));
                ++added;
                assertTrue(added == 4);
            } finally {
				assertTick(0);
            }			
    	}
    	
    	public void thread2() throws InterruptedException {    		
    		waitForTick(1);
    		q.take();
    		getThread(1).interrupt();    
    	}
    }    
    // TUNIT Untimed Interrupt/Cancel

    
    /**
     * timed offer does not time out
     */
    class TUnitTestTimedOffer extends MultithreadedTest {
    	DelayQueue q;
    	
    	@Override public void initialize() {
    		q = new DelayQueue();
    	}
    	
    	public void thread1() throws InterruptedException {
            try {
                q.put(new PDelay(0));
                q.put(new PDelay(0));
                assertTrue(q.offer(new PDelay(0), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                assertTrue(q.offer(new PDelay(0), LONG_DELAY_MS, TimeUnit.MILLISECONDS));
            } finally { assertTick(0); }
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
    	DelayQueue q;
    	
    	@Override public void initialize() {
    		q = new DelayQueue();
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
                DelayQueue q = populatedQueue(SIZE);
                for (int i = 0; i < SIZE; ++i) {
                    assertEquals(new PDelay(i), ((PDelay)q.take()));
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
                DelayQueue q = populatedQueue(SIZE);
                for (int i = 0; i < SIZE; ++i) {
                    assertEquals(new PDelay(i), ((PDelay)q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)));
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
    	DelayQueue q; 
    	
    	@Override public void initialize() {
    		q = new DelayQueue();
    	}
    	
    	public void thread1() throws InterruptedException {
    		freezeClock();
            assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
    		unfreezeClock();
    		
        	try {
        		q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
        		assertTick(1);
        		q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                fail("Should block");
        	} catch (InterruptedException success) { assertTick(2); }                
    	}
    	
    	public void thread2() throws InterruptedException {    	
            waitForTick(1);
            assertTrue(q.offer(new PDelay(0), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            waitForTick(2);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Timed Interrupt/Cancel
    // TUNIT Timed Block/Wait


    /**
     * drainTo empties queue
     */ 
    class TUnitTestDrainToWithActivePut extends MultithreadedTest {
    	DelayQueue q;
    	@Override public void initialize() {
    		q = populatedQueue(SIZE);
    	}
    	
		public void thread1() throws InterruptedException {
			q.put(new PDelay(SIZE+1));
			assertTick(0);
    	}
    	
    	ArrayList l;
    	public void thread2() {
    		waitForTick(1);
            l = new ArrayList();
    		q.drainTo(l);
    		assertTrue(l.size() >= SIZE);
    	}

    	@Override public void finish() {
    		assertTrue(q.size() + l.size() >= SIZE);
    	}
    }    
    // TUNIT Untimed Block/Wait
}
