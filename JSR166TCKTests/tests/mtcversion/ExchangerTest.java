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
public class ExchangerTest extends JSR166TestCase {
   
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());	
	}
	public static Test suite() {
		return TestFramework.buildTestSuite(ExchangerTest.class);
	}

    // REVIEW Use Exchanger to pass information between threads
    /**
     * exchange exchanges objects across two threads
     */
    class TUnitTestExchange extends MultithreadedTest {
    	Exchanger e;
    	@Override public void initialize() {
    		e = new Exchanger();
    	}

    	public void thread1() throws InterruptedException {
    		Object v = e.exchange(one);
    		assertEquals(v, two);
    		Object w = e.exchange(v);
    		assertEquals(w, one);
    	}

    	public void thread2() throws InterruptedException {    		
    		Object v = e.exchange(two);
    		assertEquals(v, one);
    		Object w = e.exchange(v);
    		assertEquals(w, two);
    	}
    }    
	// TUNIT Untimed Interleave/Synchronize
    
    
    /**
     * timed exchange exchanges objects across two threads
     */
    class TUnitTestTimedExchange extends MultithreadedTest {
    	Exchanger e;
    	@Override public void initialize() {
    		e = new Exchanger();
    	}

    	public void thread1() throws InterruptedException, TimeoutException {
    		Object v = e.exchange(one, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(v, two);
            Object w = e.exchange(v, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(w, one);
    	}

    	public void thread2() throws InterruptedException, TimeoutException {    		
    		Object v = e.exchange(two, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(v, one);
            Object w = e.exchange(v, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(w, two);
    	}
    }    
	// TUNIT Untimed Interleave/Synchronize
    
    
    /**
     * interrupt during wait for exchange throws IE
     */
    class TUnitTestExchange_InterruptedException extends MultithreadedTest {
    	Exchanger e;
    	@Override public void initialize() {
    		e = new Exchanger();
    	}
    	
    	public void thread1() {
            try {
                e.exchange(one);
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
     * interrupt during wait for timed exchange throws IE
     */
    class TUnitTestTimedExchange_InterruptedException extends MultithreadedTest {
    	Exchanger e;
    	@Override public void initialize() {
    		e = new Exchanger();
    	}
    	
    	public void thread1() {
            try {
                e.exchange(null, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
                fail("should throw exception");
            } 
            catch(InterruptedException success){ assertTick(1); }
            catch(Exception e2) { fail("should throw IE"); }
    	}
    	
    	public void thread2() {
    		waitForTick(1);
    		getThread(1).interrupt();
    	}
    }    
    // TUNIT Untimed Interrupt/Cancel


    /**
     * timeout during wait for timed exchange throws TOE
     */
    class TUnitTestExchange_TimeOutException extends MultithreadedTest {
    	Exchanger e;
    	@Override public void initialize() {
    		e = new Exchanger();
    	}
    	
    	public void thread1() {
            try {
                e.exchange(null, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                fail("should throw exception");
            } catch(TimeoutException success){
            } catch(InterruptedException e2){
                fail("should throw TOE");
            }
    	}
    }    
    // TUNIT Timed Block/Wait


    /**
     * If one exchanging thread is interrupted, another succeeds.
     */
    class TUnitTestReplacementAfterExchange extends MultithreadedTest {
    	Exchanger e;
    	@Override public void initialize() {
    		e = new Exchanger();
    	}
    	
    	public void thread1() {
    		try {
    			Object v = e.exchange(one);
    			assertEquals(v, two);
    			Object w = e.exchange(v);
    			fail("should throw exception");
    		} catch(InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() throws InterruptedException {    		
			Object v = e.exchange(two);
            assertEquals(v, one);
            waitForTick(2);
            Object w = e.exchange(v);
            assertEquals(w, three);
    	}

    	public void thread3() throws InterruptedException {
    		waitForTick(2);
            Object w = e.exchange(three);
            assertEquals(w, one);
    	}
    	
    	public void thread4() { 
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    }    
    // TUNIT Untimed Interleave/Synchronize
}
