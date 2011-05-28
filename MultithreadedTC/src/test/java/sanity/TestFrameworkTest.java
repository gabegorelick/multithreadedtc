package sanity;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;

public class TestFrameworkTest extends TestCase {
	
	class TUnitTestRunThreeTimes extends MultithreadedTestCase {
		int i=0;
    	public void thread1() {
    		i++;
    	}
    	
    	public void thread2() {   
    		waitForTick(1);
    		i++;
    	}
    }
	
    public void testRunThreeTimes() throws Throwable {
    	TUnitTestRunThreeTimes test = new TUnitTestRunThreeTimes();
    	TestFramework.runManyTimes( test, 3 );
    	assertEquals(test.i, 6);
    }


}
