package edu.umd.cs.mtc;

import java.lang.reflect.Method;

/**
 * Represents a a method meant to be run in a separate thread.
 * 
 * @author Gabe Gorelick
 * 
 */
public class ThreadedMethod {

	private final String name;
	private final Method method;

	public ThreadedMethod(String name, Method method) {
		this.name = name;
		this.method = method;
	}

	public String getName() {
		return name;
	}

	public Method getMethod() {
		return method;
	}

}
