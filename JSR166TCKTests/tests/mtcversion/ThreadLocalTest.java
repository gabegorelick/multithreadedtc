package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;
import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class ThreadLocalTest extends JSR166TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());	
	}

	public static Test suite() {
		return TestFramework.buildTestSuite(ThreadLocalTest.class);
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
                    fail("Unexpected exception");
                }
            }
        }
    }

    // REVIEW <=> Simulates complex functionality
    /**
     * InheritableThreadLocal propagates generic values.
     */
	class TUnitTestGenericITL extends MultithreadedTest {
		
		int threadCount = 10;
		int x[];
		Thread progenitor;
		
    	@Override public void initialize() {
    		x = new int[threadCount];
    		progenitor = new ITLThread(x);
    	}
    	
    	public void thread1() throws InterruptedException {
            progenitor.start();
            //register(progenitor);
            progenitor.join();
    	}
    	
    	@Override public void finish() {
            for(int i = 0; i < threadCount; i++) {
                assertEquals(i, x[i]);
            }
    	}
    }    
    // TUNIT Untimed Interleave/Synchronize
}

