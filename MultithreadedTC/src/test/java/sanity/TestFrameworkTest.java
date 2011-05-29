package sanity;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.TestCase;

/**
 * General tests for {@link TestFramework}.
 */
public class TestFrameworkTest extends TestCase {
	
	/**
	 * Test that a test is run as many times as specified.
	 */
	class TestRunThreeTimes extends MultithreadedTestCase {
		volatile int count = 0;

		public void thread1() {
    		count++;
    	}
    }
	
    public void testRunThreeTimes() throws Throwable {
    	TestRunThreeTimes test = new TestRunThreeTimes();
    	TestFramework.runManyTimes(test, 3);
    	assertEquals(test.count, 3);
    }


}
