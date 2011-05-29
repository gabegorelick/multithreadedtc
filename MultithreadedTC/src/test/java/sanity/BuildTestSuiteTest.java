package sanity;

import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Some simple tests with different kinds of inner classes all to be detected.
 */
public class BuildTestSuiteTest extends TestCase {

	public static Test suite() {
		TestSuite s = TestFramework.buildTestSuite(BuildTestSuiteTest.class);
		assertEquals(13, s.countTestCases());
		return s;
	}
	
	static AtomicInteger value1, value2;
	
	// tear down and setup should be called before each test.
	// Note that test classes declared static cannot call setup/tearDown
	
	@Override
	protected void setUp() throws Exception {
		value1 = new AtomicInteger(0);
		value2 = new AtomicInteger(0);
	}

	@Override
	protected void tearDown() throws Exception {
		assertTrue(value1.compareAndSet(3, 4));
		assertTrue(value2.compareAndSet(3, 4));
	}
	
	// Test
	@SuppressWarnings("unused")
	private class PrivateClass extends MultithreadedTest {
    	
    	@Override public void runTest() throws Throwable {
			super.runTest();
		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }
    
	// Test
	class PackageClass extends MultithreadedTest {
    	
    	@Override public void runTest() throws Throwable {
			super.runTest();
		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }
	
	// Test
	protected class ProtectedClass extends MultithreadedTest {
    	
    	@Override public void runTest() throws Throwable {
			super.runTest();
		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }

	// Test
	public class PublicClass extends MultithreadedTest {
    	
    	@Override public void runTest() throws Throwable {
			super.runTest();
		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }

	// Test
	public class PublicClassPublicConstructor extends MultithreadedTest {
    	public PublicClassPublicConstructor() {}
		
    	@Override public void runTest() throws Throwable {
			super.runTest();
		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }

	// Test
	public class PublicClassPrivateConstructor extends MultithreadedTest {
    	private PublicClassPrivateConstructor() {}
		
    	@Override public void runTest() throws Throwable {
			super.runTest();
		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }

	// Test
	@SuppressWarnings("unused")
	private class PrivateClassPrivateConstructor extends MultithreadedTest {
    	private PrivateClassPrivateConstructor() {}
		
    	@Override public void runTest() throws Throwable {
			super.runTest();
		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }

	// Test
	@SuppressWarnings("unused")
	private class PrivateClassPublicConstructor extends MultithreadedTest {
    	public PrivateClassPublicConstructor() {}
		
    	@Override public void runTest() throws Throwable {
			super.runTest();
		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }


	// Test
	public static class PublicStaticClass extends MultithreadedTest {
    	
    	@Override public void runTest() throws Throwable {
    		value1 = new AtomicInteger(0);
    		value2 = new AtomicInteger(0);
			super.runTest();
			assertTrue(value1.compareAndSet(3, 4));
			assertTrue(value2.compareAndSet(3, 4));

		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }

	// Test
	static class PackageStaticClass extends MultithreadedTest {
    	
    	@Override public void runTest() throws Throwable {
    		value1 = new AtomicInteger(0);
    		value2 = new AtomicInteger(0);
			super.runTest();
			assertTrue(value1.compareAndSet(3, 4));
			assertTrue(value2.compareAndSet(3, 4));

		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }

	// Test
	protected static class ProtectedStaticClass extends MultithreadedTest {
    	
    	@Override public void runTest() throws Throwable {
    		value1 = new AtomicInteger(0);
    		value2 = new AtomicInteger(0);
			super.runTest();
			assertTrue(value1.compareAndSet(3, 4));
			assertTrue(value2.compareAndSet(3, 4));

		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }

	// Test
	@SuppressWarnings("unused")
	private static class PrivateStaticClass extends MultithreadedTest {
    	
    	@Override public void runTest() throws Throwable {
    		value1 = new AtomicInteger(0);
    		value2 = new AtomicInteger(0);
			super.runTest();
			assertTrue(value1.compareAndSet(3, 4));
			assertTrue(value2.compareAndSet(3, 4));

		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }

	// Test
	@SuppressWarnings("unused")
	private static final class PrivateStaticFinalClassWithPrivateConstructor extends MultithreadedTest {
    	
		private PrivateStaticFinalClassWithPrivateConstructor() {}
		
    	@Override public void runTest() throws Throwable {
    		value1 = new AtomicInteger(0);
    		value2 = new AtomicInteger(0);
			super.runTest();
			assertTrue(value1.compareAndSet(3, 4));
			assertTrue(value2.compareAndSet(3, 4));

		}

		@Override public void initialize() {			
			assertTrue(value1.compareAndSet(0, 1));
			assertTrue(value2.compareAndSet(0, 1));
		}

		public void thread1() {
    		assertTrue(value1.compareAndSet(1, 2));
    	}
    	
    	public void thread2() {
    		assertTrue(value2.compareAndSet(1, 2));
    	}
    	
		@Override public void finish() {
			assertTrue(value1.compareAndSet(2, 3));
			assertTrue(value2.compareAndSet(2, 3));
		}
    }
	
}
