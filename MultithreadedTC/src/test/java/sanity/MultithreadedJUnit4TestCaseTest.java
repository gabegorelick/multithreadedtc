package sanity;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.AfterThreads;
import edu.umd.cs.mtc.Multithreaded;
import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import edu.umd.cs.mtc.Threaded;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class MultithreadedJUnit4TestCaseTest {

	static class JUnit3Style extends MultithreadedTestCase {
		
		private AtomicInteger integer;
		
		public void initialize() {
			integer = new AtomicInteger();
			integer.set(1);
		}
		
		public void thread1() {
			// test initialize gets called before
			assertNotNull(integer);
			assertEquals(1, integer.get());
			
			integer.set(2);
		}
		
		public void finish() {
			// test that finish is called after threads terminate
			assertEquals(2, integer.get());
		}
	}
	
	/**
	 * Test that MultithreadedTestCase still works using the JUnit 3 style.
	 * @throws Throwable
	 */
	public static class TestJUnit3Style {
		@Test
		public void testJUnit3Style() throws Throwable {
			TestFramework.runOnce(new JUnit3Style());
		}
	}
	
	public static class JUnit3StyleDoesNotWork extends MultithreadedJUnit4TestCase {
		
		public void initialize() {
			fail("initialize() should not be called without an @Before annotation");
		}
		
		public void finish() {
			fail("finish() should not be called without an @AfterThreads annotation");
		}
		
		public void thread1() {
			fail("threadX methods should not be called without @Threaded annotation");
		}
		
		@Test
		public void testJUnit3StyleDoesNotWork() {
			// this space left intentionally blank
		}
	}
	
	public static class Junit3And4Mix extends MultithreadedJUnit4TestCase {
		private AtomicInteger integer;
		
		@Before
		public void shouldGetCalledBefore() {
			integer = new AtomicInteger();
			integer.set(1);
		}
		
		@Threaded
		public void thisIsAThread() {
			assertNotNull(integer);
			assertEquals(1, integer.get());
			integer.set(2);
		}
				
		public void initialize() {
			fail("initialize() should not be called without an @Before annotation");
		}
		
		public void finish() {
			fail("finish() should not be called without an @AfterThreads annotation");
		}
		
		public void thread1() {
			fail("threadX methods should not be called without @Threaded annotation");
		}
		
		@Test
		public void testJUnit3And4Mix() {
			assertEquals(2, integer.get());
			integer.set(3);
		}
		
		@After
		public void shouldGetCalledAfter() {
			assertEquals(3, integer.get());
		}
	}
	
	public static class MultithreadedAnnotationTest extends MultithreadedJUnit4TestCase {
		private static AtomicInteger integer;
		
		// just for fun, let's test that @BeforeClass works
		@BeforeClass
		public static void setUp() {
			integer = new AtomicInteger();
		}
		
		@Threaded
		public void thread() {
			integer.incrementAndGet();
		}
		
		@Test
		@Multithreaded(times = 10)
		public void testMultithreadAnnotation() {
			assertEquals(10, integer.get());
		}
	}
	
	public static class AfterThreadsAnnotationTest extends MultithreadedJUnit4TestCase {
		private AtomicInteger integer;
		
		@Threaded
		public void thread() {
			integer = new AtomicInteger(1);
		}
		
		@AfterThreads
		public void afterThreads() {
			assertEquals(1, integer.get());
			integer.set(2);
		}
		
		@Test
		public void testAfterThreadsAnnotation() {
			assertEquals(2, integer.get());
		}
	}
	
}
