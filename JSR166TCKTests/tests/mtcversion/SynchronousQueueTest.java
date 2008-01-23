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

@SuppressWarnings("unchecked")
public class SynchronousQueueTest extends JSR166TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}

	public static Test suite() {
		return TestFramework.buildTestSuite(SynchronousQueueTest.class);
	}

    /**
     * put blocks interruptibly if no active taker
     */
	class TUnitTestBlockingPut extends MultithreadedTest {
    	
    	public void thread1() {
            try {
                SynchronousQueue q = new SynchronousQueue();
                q.put(zero);
                fail("should throw exception");
            } catch (InterruptedException ie){ assertTick(1); }   
    	}
    	
    	public void thread2() { 
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    } 
    // TUNIT Untimed Interrupt/Cancel


    /**
     * put blocks waiting for take 
     */
    class TUnitTestPutWithTake extends MultithreadedTest {
    	
    	SynchronousQueue q;
    	
    	@Override public void initialize() {
    		q = new SynchronousQueue();
    	}
    	
    	public void thread1() {
        	int added = 0;
        	try {
        		q.put(new Object());
        		++added;
        		assertTick(1); 
        		q.put(new Object());
        		++added;
        		q.put(new Object());
        		++added;
        		q.put(new Object());
        		++added;
        		fail("should throw exception");
        	} catch (InterruptedException e){
        		assertTrue(added >= 1);
        		assertTick(2); 
        	}
    	}
    	
    	public void thread2() throws InterruptedException {    		
    		waitForTick(1);
            q.take();
            waitForTick(2);
            getThread(1).interrupt();
    	}

    	@Override public void finish() {
    	}
    }  
    // TUNIT Untimed Interrupt/Cancel
    // TUNIT Untimed Block/Wait


    /**
     * timed offer times out if elements not taken
     */
    class TUnitTestTimedOffer extends MultithreadedTest {
    	SynchronousQueue q;
    	
    	@Override public void initialize() {
    		q = new SynchronousQueue();
    	}
    	
    	public void thread1() throws InterruptedException {
			freezeClock();
			assertFalse(q.offer(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			unfreezeClock();
				
			try {
				q.offer(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch (InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    	@Override public void finish() {
    	}
    }  
    // TUNIT Timed Interrupt/Cancel


    /**
     * take blocks interruptibly when empty
     */
    class TUnitTestTakeFromEmpty extends MultithreadedTest {
    	SynchronousQueue q;
    	
    	@Override public void initialize() {
    		q = new SynchronousQueue();
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

    	@Override public void finish() {
    	}
    }   
    // TUNIT Untimed Interrupt/Cancel


    /**
     * put blocks interruptibly if no active taker
     */
	class TUnitTestFairBlockingPut extends MultithreadedTest {
    	
    	@Override public void initialize() {
    	}
    	
    	public void thread1() {
            try {
                SynchronousQueue q = new SynchronousQueue(true);
                q.put(zero);
                fail("should throw exception");
            } catch (InterruptedException ie){ assertTick(1); }   
    	}
    	
    	public void thread2() { 
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    	@Override public void finish() {
    	}
    }   
    // TUNIT Untimed Interrupt/Cancel


    /**
     * put blocks waiting for take 
     */
    class TUnitTestFairPutWithTake extends MultithreadedTest {
    	
    	SynchronousQueue q;
    	
    	@Override public void initialize() {
    		q = new SynchronousQueue(true);
    	}
    	
    	public void thread1() {
        	int added = 0;
        	try {
        		q.put(new Object());
        		++added;
        		assertTick(1); 
        		q.put(new Object());
        		++added;
        		q.put(new Object());
        		++added;
        		q.put(new Object());
        		++added;
        		fail("should throw exception");
        	} catch (InterruptedException e){
        		assertTrue(added >= 1);
        		assertTick(2);
        	}
    	}
    	
    	public void thread2() throws InterruptedException {    		
    		waitForTick(1);
            q.take();
            waitForTick(2);
            getThread(1).interrupt();
    	}

    	@Override public void finish() {
    	}
    }  
    // TUNIT Untimed Interrupt/Cancel
    // TUNIT Untimed Block/Wait


    /**
     * timed offer times out if elements not taken
     */
    class TUnitTestFairTimedOffer extends MultithreadedTest {
    	SynchronousQueue q;
    	
    	@Override public void initialize() {
    		q = new SynchronousQueue(true);
    	}
    	
    	public void thread1() throws InterruptedException {
			freezeClock();
			assertFalse(q.offer(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
			unfreezeClock();
				
			try {
				q.offer(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
				fail("should throw exception");
			} catch (InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    	@Override public void finish() {
    	}
    }
    // TUNIT Timed Interrupt/Cancel


    /**
     * take blocks interruptibly when empty
     */
    class TUnitTestFairTakeFromEmpty extends MultithreadedTest {
    	SynchronousQueue q;
    	
    	@Override public void initialize() {
    		q = new SynchronousQueue(true);
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

    	@Override public void finish() {
    	}
    }  
    // TUNIT Untimed Interrupt/Cancel


    /**
     * Interrupted timed poll throws InterruptedException instead of
     * returning timeout status
     */
    class TUnitTestInterruptedTimedPoll extends MultithreadedTest {
    	
    	@Override public void initialize() {
    	}
    	
    	public void thread1() {
            try {
                SynchronousQueue q = new SynchronousQueue();
                assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            } catch (InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    	@Override public void finish() {
    	}
    }   
    // TUNIT Timed Interrupt/Cancel


    /**
     *  timed poll before a delayed offer fails; after offer succeeds;
     *  on interruption throws
     */
    class TUnitTestTimedPollWithOffer extends MultithreadedTest {
    	SynchronousQueue q; 
    	
    	@Override public void initialize() {
    		q = new SynchronousQueue();
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

    	@Override public void finish() {
    	}
    } 
    // TUNIT Timed Interrupt/Cancel
    // TUNIT Timed Block/Wait


    /**
     * Interrupted timed poll throws InterruptedException instead of
     * returning timeout status
     */
    class TUnitTestFairInterruptedTimedPoll extends MultithreadedTest {
    	
    	@Override public void initialize() {
    	}
    	
    	public void thread1() {
            try {
                SynchronousQueue q = new SynchronousQueue(true);
                assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            } catch (InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    	@Override public void finish() {
    	}
    }
    // TUNIT Timed Interrupt/Cancel


    /**
     *  timed poll before a delayed offer fails; after offer succeeds;
     *  on interruption throws
     */
    class TUnitTestFairTimedPollWithOffer extends MultithreadedTest {
    	SynchronousQueue q; 
    	
    	@Override public void initialize() {
    		q = new SynchronousQueue(true);
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

    	@Override public void finish() {
    	}
    } 
    // TUNIT Timed Interrupt/Cancel
    // TUNIT Timed Block/Wait


    /**
     * drainTo empties queue, unblocking a waiting put.
     */ 
    class TUnitTestDrainToWithActivePut extends MultithreadedTest {
    	SynchronousQueue q;
    	@Override public void initialize() {
    		q = new SynchronousQueue();
    	}
    	
		public void thread1() throws InterruptedException {
            q.put(new Integer(1));
            assertTick(1);
    	}
    	
    	ArrayList l;
    	public void thread2() {    		
            l = new ArrayList();
            waitForTick(1);
            q.drainTo(l);
            assertTrue(l.size() <= 1);
            if (l.size() > 0)
                assertEquals(l.get(0), new Integer(1));
    	}

    	@Override public void finish() {
            assertTrue(l.size() <= 1);
    	}
    }  
    // TUNIT Untimed Block/Wait


    /**
     * drainTo(c, n) empties up to n elements of queue into c
     */ 
    class TUnitTestDrainToN extends MultithreadedTest {    	
    	SynchronousQueue q;
    	@Override public void initialize() {
    		q = new SynchronousQueue();
    	}
    	    	
    	public void thread1() throws InterruptedException {
    		q.put(one);
    		assertTick(1);
    	}
    	
    	public void thread2() throws InterruptedException {    		
    		q.put(two);
    		assertTick(1);
    	}

    	public void thread3() {  
            ArrayList l = new ArrayList();
            waitForTick(1);
            q.drainTo(l, 1);
            assertTrue(l.size() == 1);
            q.drainTo(l, 1);
            assertTrue(l.size() == 2);
            assertTrue(l.contains(one));
            assertTrue(l.contains(two));
    	}
    	
    	@Override public void finish() {
    	}
    }   
    // TUNIT Untimed Block/Wait
}
