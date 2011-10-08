package sanity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.Threaded;

/**
 * Test timing-related issues, {@link MultithreadedTestCase#freezeClock()},
 * etc.
 */
@RunWith(Enclosed.class)
public class TimingTest {
	
	/**
	 * Tests that if a thread freezes the clock, it does not advance until it
	 * is unfrozen.
	 */
    public static class TestClockDoesNotAdvanceWhenFrozen extends MultithreadedJUnit4TestCase {
    	volatile String s = "A";
    	
    	@Threaded
    	public void thread1() throws InterruptedException {
    		freezeClock();
    		Thread.sleep(200);
    		assertEquals("Clock advanced while thread was sleeping", s, "A");
    		unfreezeClock();
    	}
    	
    	@Threaded
    	public void thread2() {    		
    		waitForTick(1);
    		s = "B";
    	}
    	
    	@Test
    	public void testClockDoesNotAdvanceWhenFrozen() {
    		assertEquals(s, "B");
        }
    }
    
    

    /**
     * Tests that {@link #assertTick(long)} works correctly.
     */
    public static class TestAssertTick extends MultithreadedJUnit4TestCase {
    	@Threaded
    	public void thread1() {
    		waitForTick(1);
    		assertTick(1);

    		waitForTick(2);
    		assertTick(1);
    		
    	}
    	
    	/**
    	 * This is a weird test. We expect it to fail, and when it does, it
    	 * passes.
    	 */
    	@Test(expected = AssertionError.class)
    	public void testAssertTick() {
    		// this space left intentionally blank
        }
    }

    

    /**
     * Tests that {@link #getTick()} works correctly.
     */
    public static class TestGetTick extends MultithreadedJUnit4TestCase {
    	@Threaded
    	public void thread1() {
    		waitForTick(1);
    		assertEquals(1, getTick());

    		waitForTick(2);
    		assertEquals(2, getTick());
    	}
    	
    	@Test
    	public void testGetTick() {
        	// this space left intentionally blank
        }
    }

}
