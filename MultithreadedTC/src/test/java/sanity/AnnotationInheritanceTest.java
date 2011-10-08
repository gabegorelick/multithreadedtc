package sanity;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.umd.cs.mtc.AfterThreads;
import edu.umd.cs.mtc.MultithreadedJUnit4TestCase;
import edu.umd.cs.mtc.Threaded;

@RunWith(Enclosed.class)
public class AnnotationInheritanceTest {

	@Ignore
	// don't try to run Parent, it has no @Tests
	public static abstract class Parent extends MultithreadedJUnit4TestCase {

		protected AtomicInteger integer;

		@Before
		public void setUp() {
			integer = new AtomicInteger(1);
		}

		@Threaded
		public void thread1() {
			integer.incrementAndGet();
		}

		@AfterThreads
		public void finish() {
			// make sure parent @AfterThreads runs after child
			assertEquals(4, integer.getAndIncrement());
		}

		// no @Test, if we had a test, JUnit would call @AfterThreads before it
		// tried to run it, disregarding the subclass
	}

	public static class Child extends Parent {

		@Threaded
		public void thread2() {
			integer.incrementAndGet();
		}

		// overriding finish would cause the parent's finish to never get called
		@AfterThreads
		public void finishChild() {
			assertEquals(3, integer.getAndIncrement());
		}

		@Test
		public void testParentCalledAfterChild() {
			assertEquals(5, integer.get());
		}
	}
}
