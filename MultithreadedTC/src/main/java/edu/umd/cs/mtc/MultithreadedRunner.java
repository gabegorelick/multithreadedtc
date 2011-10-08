package edu.umd.cs.mtc;

import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * A JUnit runner which runs the threaded methods before the actual JUnit test
 * method.
 * 
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
public class MultithreadedRunner extends BlockJUnit4ClassRunner {
	
	public MultithreadedRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected Statement methodInvoker(FrameworkMethod method, Object test) {
		if (!(test instanceof MultithreadedTestCase)) {
			return super.methodInvoker(method, test);
		}

		MultithreadedTestCase testCase = (MultithreadedTestCase) test;
		Multithreaded multithreadedAnnotation = method.getAnnotation(Multithreaded.class);

		if (multithreadedAnnotation != null) {
			return new MultithreadedInvokeMethod(method, testCase, multithreadedAnnotation.times());
		} else {
			return new MultithreadedInvokeMethod(method, testCase, 1);
		}
	}

	private static class MultithreadedInvokeMethod extends InvokeMethod {

		private final MultithreadedTestCase testCase;
		private final int runs;

		public MultithreadedInvokeMethod(FrameworkMethod testMethod, MultithreadedTestCase testCase, int runs) {
			super(testMethod, testCase);
			this.testCase = testCase;
			this.runs = runs;
		}

		@Override
		public void evaluate() throws Throwable {
			// do multithreaded magic
			TestFramework.runManyTimes(testCase, runs);
			
			// invoke the actual test method afterwards
			super.evaluate();
		}

	}
}