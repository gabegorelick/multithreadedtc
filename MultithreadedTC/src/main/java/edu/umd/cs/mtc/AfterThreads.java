package edu.umd.cs.mtc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a method in a {@link MultithreadedJUnit4TestCase}
 * should be run after all {@link Threaded @Threaded} methods have terminated
 * and before any test methods are invoked.
 * 
 * @author Gabe Gorelick
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterThreads {

}
