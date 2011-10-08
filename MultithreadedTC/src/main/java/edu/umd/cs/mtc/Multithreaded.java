package edu.umd.cs.mtc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a test triggers a multithreaded test case and
 * that the threads should be run multiple times.
 * 
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Multithreaded {
	int times() default 1;
}