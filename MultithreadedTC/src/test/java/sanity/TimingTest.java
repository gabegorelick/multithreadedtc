package sanity;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Test timing-related issues, {@link MultithreadedTestCase#freezeClock()},
 * etc.
 */
public class TimingTest extends TestCase {
	
	/**
	 * Tests that if a thread freezes the clock, it does not advance until it
	 * is unfrozen.
	 */
    class TestClockDoesNotAdvanceWhenFrozen extends MultithreadedTestCase {
    	volatile String s = "A";
    	
    	public void thread1() throws InterruptedException {
    		freezeClock();
    		Thread.sleep(200);
    		assertEquals("Clock advanced while thread was sleeping", s, "A");
    		unfreezeClock();
    	}
    	
    	public void thread2() {    		
    		waitForTick(1);
    		s = "B";
    	}
    }
    
    public void testClockDoesNotAdvanceWhenFrozen() throws Throwable {
    	TestClockDoesNotAdvanceWhenFrozen test = new TestClockDoesNotAdvanceWhenFrozen();
		TestFramework.runOnce(test);
		assertEquals(test.s, "B");
    }

    /**
     * Tests that {@link #assertTick(long)} works correctly.
     */
    class TestAssertTick extends MultithreadedTestCase {
    	public void thread1() {
    		waitForTick(1);
    		assertTick(1);

    		waitForTick(2);
    		try {
    			assertTick(1);
    		} catch (AssertionFailedError expected) {
    		}
    	}
    }

    public void testAssertTick() throws Throwable {
    	TestFramework.runOnce(new TestAssertTick());
    }

    /**
     * Tests that {@link #getTick()} works correctly.
     */
    class TestGetTick extends MultithreadedTestCase {
    	public void thread1() {
    		waitForTick(1);
    		assertEquals(1, getTick());

    		waitForTick(2);
    		assertEquals(2, getTick());
    	}
    }

    public void testGetTick() throws Throwable {
    	TestFramework.runOnce(new TestGetTick());
    }
}
