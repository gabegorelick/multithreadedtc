package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

import junit.framework.*;
import java.util.*;
import java.util.concurrent.*;

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class LinkedBlockingDequeTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }

    public static Test suite() {
	return TestFramework.buildTestSuite(LinkedBlockingDequeTest.class);
    }

    /**
     * Create a deque of given size containing consecutive
     * Integers 0 ... n.
     */
    private LinkedBlockingDeque populatedDeque(int n) {
        LinkedBlockingDeque q = new LinkedBlockingDeque(n);
        assertTrue(q.isEmpty());
	for(int i = 0; i < n; i++)
	    assertTrue(q.offer(new Integer(i)));
        assertFalse(q.isEmpty());
        assertEquals(0, q.remainingCapacity());
	assertEquals(n, q.size());
        return q;
    }

    /**
     * put blocks interruptibly if full
     */
    class TUnitTestBlockingPut extends MultithreadedTest {    	
    	public void thread1() {
            int added = 0;
            try {
                LinkedBlockingDeque q = new LinkedBlockingDeque(SIZE);
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

    
    /**
     * put blocks waiting for take when full
     */
    class TUnitTestPutWithTake extends MultithreadedTest {
    	LinkedBlockingDeque q;
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
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


    /**
     * timed offer times out if full and elements not taken
     */
    class TUnitTestTimedOffer extends MultithreadedTest {
    	LinkedBlockingDeque q;
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
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


    /**
     * take blocks interruptibly when empty
     */
    class TUnitTestTakeFromEmpty extends MultithreadedTest {
    	LinkedBlockingDeque q;
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
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
            	LinkedBlockingDeque q = populatedDeque(SIZE);
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
				LinkedBlockingDeque q = populatedDeque(SIZE);
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
    	LinkedBlockingDeque q; 
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
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


    /**
     * putFirst blocks interruptibly if full
     */
    class TUnitTestBlockingPutFirst extends MultithreadedTest {    	
    	public void thread1() {
            int added = 0;
            try {
                LinkedBlockingDeque q = new LinkedBlockingDeque(SIZE);
                for (int i = 0; i < SIZE; ++i) {
                    q.putFirst(new Integer(i));
                    ++added;
                }
                q.putFirst(new Integer(SIZE));
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


    /**
     * putFirst blocks waiting for take when full
     */
    class TUnitTestPutFirstWithTake extends MultithreadedTest {
    	LinkedBlockingDeque q;
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
    	}
    	
    	public void thread1() {
			int added = 0;
			try {
				q.putFirst(new Object());
				++added;
				q.putFirst(new Object());
				++added;
				q.putFirst(new Object());
				++added;
				q.putFirst(new Object());
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


    /**
     * timed offerFirst times out if full and elements not taken
     */
    class TUnitTestTimedOfferFirst extends MultithreadedTest {
    	LinkedBlockingDeque q;
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
    	}
    	
    	public void thread1() throws InterruptedException {
			try {
				q.putFirst(new Object());
				q.putFirst(new Object());
				
				freezeClock();
				assertFalse(q.offerFirst(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
				unfreezeClock();
				
				q.offerFirst(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch (InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Timed Interrupt/Cancel


    /**
     * takeFirst blocks interruptibly when empty
     */
    class TUnitTestTakeFirstFromEmpty extends MultithreadedTest {
    	LinkedBlockingDeque q;
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
    	}
    	    	
    	public void thread1() {
        	try {
        		q.takeFirst();
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
     * TakeFirst removes existing elements until empty, then blocks interruptibly
     */
	class TUnitTestBlockingTakeFirst extends MultithreadedTest {
    	
    	public void thread1() {
            try {
            	LinkedBlockingDeque q = populatedDeque(SIZE);
				for (int i = 0; i < SIZE; ++i) {
					assertEquals(i, ((Integer)q.takeFirst()).intValue());
				}
				q.takeFirst();
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
     * Interrupted timed pollFirst throws InterruptedException instead of
     * returning timeout status
     */
    class TUnitTestInterruptedTimedPollFirst extends MultithreadedTest {
    	
    	public void thread1() {
			try {
				LinkedBlockingDeque q = populatedDeque(SIZE);
				for (int i = 0; i < SIZE; ++i) {
					assertEquals(i, ((Integer)q.pollFirst(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)).intValue());
				}
				assertNull(q.pollFirst(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            } catch (InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Timed Interrupt/Cancel


    /**
     *  timed pollFirst before a delayed offerFirst fails; after offerFirst succeeds;
     *  on interruption throws
     */
    class TUnitTestTimedPollFirstWithOfferFirst extends MultithreadedTest {
    	LinkedBlockingDeque q; 
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
    	}
    	
    	public void thread1() throws InterruptedException {
    		freezeClock();
    		assertNull(q.pollFirst(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
    		unfreezeClock();
    		
        	try {
        		q.pollFirst(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
        		assertTick(1);
        		q.pollFirst(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
        		fail("should throw exception");
        	} catch (InterruptedException success) { assertTick(2); }                
    	}
    	
    	public void thread2() throws InterruptedException {    	
            waitForTick(1);
            assertTrue(q.offerFirst(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            waitForTick(2);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Timed Interrupt/Cancel
    // TUNIT Timed Block/Wait


    /**
     * putLast blocks interruptibly if full
     */
    class TUnitTestBlockingPutLast extends MultithreadedTest {    	
    	public void thread1() {
            int added = 0;
            try {
                LinkedBlockingDeque q = new LinkedBlockingDeque(SIZE);
                for (int i = 0; i < SIZE; ++i) {
                    q.putLast(new Integer(i));
                    ++added;
                }
                q.putLast(new Integer(SIZE));
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

    
    /**
     * putLast blocks waiting for take when full
     */
    class TUnitTestPutLastWithTake extends MultithreadedTest {
    	LinkedBlockingDeque q;
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
    	}
    	
    	public void thread1() {
			int added = 0;
			try {
				q.putLast(new Object());
				++added;
				q.putLast(new Object());
				++added;
				q.putLast(new Object());
				++added;
				q.putLast(new Object());
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


    /**
     * timed offerLast times out if full and elements not taken
     */
    class TUnitTestTimedOfferLast extends MultithreadedTest {
    	LinkedBlockingDeque q;
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
    	}
    	
    	public void thread1() throws InterruptedException {
			try {
				q.putLast(new Object());
				q.putLast(new Object());
				
				freezeClock();
				assertFalse(q.offerLast(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
				unfreezeClock();
				
				q.offerLast(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch (InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Timed Interrupt/Cancel


    /**
     * takeLast blocks interruptibly when empty
     */
    class TUnitTestTakeLastFromEmpty extends MultithreadedTest {
    	LinkedBlockingDeque q;
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
    	}
    	    	
    	public void thread1() {
        	try {
        		q.takeLast();
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
     * TakeLast removes existing elements until empty, then blocks interruptibly
     */
	class TUnitTestBlockingTakeLast extends MultithreadedTest {
    	
    	public void thread1() {
            try {
            	LinkedBlockingDeque q = populatedDeque(SIZE);
				for (int i = 0; i < SIZE; ++i) {
					assertEquals(SIZE-i-1, ((Integer)q.takeLast()).intValue());
				}
				q.takeLast();
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
     * Interrupted timed pollLast throws InterruptedException instead of
     * returning timeout status
     */
    class TUnitTestInterruptedTimedPollLast extends MultithreadedTest {
    	
    	public void thread1() {
			try {
				LinkedBlockingDeque q = populatedDeque(SIZE);
				for (int i = 0; i < SIZE; ++i) {
					assertEquals(SIZE-i-1, ((Integer)q.pollLast(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)).intValue());
				}
				assertNull(q.pollLast(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            } catch (InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Timed Interrupt/Cancel


    /**
     *  timed poll before a delayed offerLast fails; after offerLast succeeds;
     *  on interruption throws
     */
    class TUnitTestTimedPollWithOfferLast extends MultithreadedTest {
    	LinkedBlockingDeque q; 
    	
    	@Override public void initialize() {
    		q = new LinkedBlockingDeque(2);
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
            assertTrue(q.offerLast(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            waitForTick(2);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Timed Interrupt/Cancel
    // TUNIT Timed Block/Wait


    /**
     * drainTo empties full deque, unblocking a waiting put.
     */ 
    class TUnitTestDrainToWithActivePut extends MultithreadedTest {
    	LinkedBlockingDeque q;
    	@Override public void initialize() {
    		q = populatedDeque(SIZE);
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
