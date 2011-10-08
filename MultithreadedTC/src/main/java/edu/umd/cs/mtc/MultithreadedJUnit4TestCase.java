package edu.umd.cs.mtc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;

@RunWith(MultithreadedRunner.class)
public class MultithreadedJUnit4TestCase extends MultithreadedTestCase {

	/**
	 * Suppresses calls to {@link #initialize()}. To get the same functionality,
	 * use JUnit 4's {@code @Before} annotation.
	 */
	@Override
	public void onInitialize() {
		// do nothing
	}

	/**
	 * Calls any methods annotated with {@link AfterThreads}.
	 */
	@Override
	public void onFinish() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {		
		// call parent @AfterThreads after child @AfterThreads
		for (Class<?> c : getSuperClasses()) {
			for (Method m : c.getDeclaredMethods()) {
				if (m.isAnnotationPresent(AfterThreads.class)) {
					try {
						m.invoke(this);
					} catch (InvocationTargetException e) {
						Throwable cause = e.getCause();
						if (cause != null && cause instanceof AssertionError) {
							// throw AssertionError so JUnit sees failed test
							// and not an error
							throw (AssertionError) cause;
						} else {
							throw e;
						}
					}
				}
			}
		}

	}

	/**
	 * Get all methods marked with {@link Threaded @Threaded}.
	 */
	@Override
	public ThreadedMethod[] getThreadedMethods() {
		List<ThreadedMethod> threadedMethods = new ArrayList<ThreadedMethod>();

		for (Class<?> eachClass : getSuperClasses()) {
			Method[] methods = eachClass.getDeclaredMethods();
			for (Method method : methods) {
				Threaded annotation = method.getAnnotation(Threaded.class);
				if (annotation != null) {
					if ("".equals(annotation.value())) {
						threadedMethods.add(new ThreadedMethod(method.getName(), method));
					} else {
						threadedMethods.add(new ThreadedMethod(annotation.value(), method));
					}
				}
			}
		}
		return threadedMethods.toArray(new ThreadedMethod[threadedMethods.size()]);
	}

	/**
	 * Get list of superclasses. The order of the resulting list is up the class
	 * hierarchy, starting with the current class. For example, if Foo extends
	 * Bar extends MulithreadedJUnit4TestCase, then calling this method on Foo
	 * will return [Foo, Bar].
	 */
	private List<Class<?>> getSuperClasses() {
		ArrayList<Class<?>> results = new ArrayList<Class<?>>();
		Class<?> current = getClass();

		// stop when we hit MultithreadedJUnit4TestCase
		while (current != null && current != MultithreadedJUnit4TestCase.class) {
			results.add(current);
			current = current.getSuperclass();
		}
		return results;
	}
}
