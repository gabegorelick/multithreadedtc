package edu.umd.cs.mtc;

import java.lang.reflect.Method;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * Extends {@link MultithreadedTestCase} by implementing 
 * {@link junit.framework.Test} so that tests can be added to a 
 * TestSuite
 * 
 * @see MultithreadedTestCase
 * 
 * @author William Pugh
 * @author Nathaniel Ayewah
 * @since 1.0
 */
public abstract class MultithreadedTest extends MultithreadedTestCase implements Test {
	
	/* (non-Javadoc)
	 * @see junit.framework.Test#countTestCases()
	 */
	public int countTestCases() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see junit.framework.Test#run(junit.framework.TestResult)
	 */
	public void run(TestResult result) {
		result.startTest(this);

		Protectable p = new Protectable() {
			public void protect() throws Throwable {
				runBare();
			}
		};
		result.runProtected(this, p);

		result.endTest(this);
	}
	
	/**
	 * Runs the bare test sequence, including setUp and tearDown
	 * if available.
	 * 
	 * @throws Throwable if any exception is thrown
	 */
	protected void runBare() throws Throwable {
		if (setUpMethod != null) setUpMethod.invoke(testCase, (Object []) null);
		try {
			runTest();
		}
		finally {
			if (tearDownMethod != null) tearDownMethod.invoke(testCase, (Object []) null);
		}
	}
	
	/**
	 * This is the method that runs this test. It is equivalent to the 
	 * testXxx methods in JUnit 3. By default the test is just run once,
	 * by calling {@link TestFramework#runOnce(MultithreadedTestCase)}.
	 * To change the way the test is run, simply override this method.
	 * 
	 * @see TestFramework#runOnce(MultithreadedTestCase)
	 * @see TestFramework#runOnce(MultithreadedTestCase, Integer, Integer)
	 * @see TestFramework#runManyTimes(MultithreadedTestCase, int)
	 * @see TestFramework#runManyTimes(MultithreadedTestCase, int, Integer, Integer)
	 * 
	 * @throws Throwable if any exception is thrown
	 */
	public void runTest() throws Throwable {
		//TestFramework.runOnce( this );
		TestFramework.runManyTimes(this, 20);
	}
		
	/**
	 * If this {@link MultithreadedTest} is added to a test suite using
	 * {@link TestFramework#buildTestSuite(Class)}, and if it is a non-static
	 * inner class of a {@link TestCase}, then the setUp and tearDown methods
	 * should be called before and after the test is run respectively. This
	 * method is used by 
	 * {@link TestFramework#addSetUpAndTearDown(MultithreadedTest, TestCase)}
	 * to provide the necessary references so that these methods can be invoked.
	 * 
	 * @param tc
	 * @param setUp
	 * @param tearDown
	 */
	void addSetUpAndTearDown(TestCase tc, Method setUp, Method tearDown) {
		testCase = tc;
		setUpMethod = setUp;
		tearDownMethod = tearDown;
	}
	
	/** @see #addSetUpAndTearDown(TestCase, Method, Method) */
	private TestCase testCase = null;

	/** @see #addSetUpAndTearDown(TestCase, Method, Method) */
	private Method setUpMethod = null;

	/** @see #addSetUpAndTearDown(TestCase, Method, Method) */
	private Method tearDownMethod = null;	

}
