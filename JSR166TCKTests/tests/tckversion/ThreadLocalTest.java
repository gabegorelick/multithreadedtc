package tckversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;

public class ThreadLocalTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());	
    }
    
    public static Test suite() {
	return new TestSuite(ThreadLocalTest.class);
    }

    static ThreadLocal<Integer> tl = new ThreadLocal<Integer>() {
            public Integer initialValue() {
                return one;
            }
        };

    static InheritableThreadLocal<Integer> itl =
        new InheritableThreadLocal<Integer>() {
            protected Integer initialValue() {
                return zero;
            }
            
            protected Integer childValue(Integer parentValue) {
                return new Integer(parentValue.intValue() + 1);
            }
        };


    private class ITLThread extends Thread {
        final int[] x;
        ITLThread(int[] array) { x = array; }
        public void run() {
            Thread child = null;
            if (itl.get().intValue() < x.length - 1) {
                child = new ITLThread(x);
                child.start();
            }
            Thread.currentThread().yield();
            
            int threadId = itl.get().intValue();
            for (int j = 0; j < threadId; j++) {
                x[threadId]++;
                Thread.currentThread().yield();
            }
            
            if (child != null) { // Wait for child (if any)
                try {
                    child.join();
                } catch(InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        }
    }

    // REVIEW <=> Use a Thread.yield() to force one thread to wait (or loop) for another
    /**
     * InheritableThreadLocal propagates generic values.
     */
    public void testGenericITL() {
        final int threadCount = 10;
        final int x[] = new int[threadCount];
        Thread progenitor = new ITLThread(x);
        try {
            progenitor.start();
            progenitor.join();
            for(int i = 0; i < threadCount; i++) {
                assertEquals(i, x[i]);
            }
        } catch(InterruptedException e) {
            unexpectedException();
        }
    }
}

