package sanity;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;

public class TimingTests extends TestCase {
	
    class TUnitTestClockDoesNotAdvanceWhenFrozen extends MultithreadedTestCase {
    	String s;
    	@Override public void initialize() {
    		s = "A";
    	}
    	
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

    	@Override public void finish() {
    		assertEquals(s, "B");
    	}
    }
    
    public void testClockDoesNotAdvanceWhenFrozen() throws Throwable {
    	TestFramework.runOnce( new TUnitTestClockDoesNotAdvanceWhenFrozen() );
    }

}
