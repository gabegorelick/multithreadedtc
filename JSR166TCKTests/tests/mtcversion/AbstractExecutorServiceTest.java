package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */


import junit.framework.*;
import java.util.*;
import java.util.concurrent.*;

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class AbstractExecutorServiceTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(AbstractExecutorServiceTest.class);
    }

    /** 
     * A no-frills implementation of AbstractExecutorService, designed
     * to test the submit methods only.
     */
    static class DirectExecutorService extends AbstractExecutorService {
        public void execute(Runnable r) { r.run(); }
        public void shutdown() { shutdown = true; }
        public List<Runnable> shutdownNow() { shutdown = true; return Collections.EMPTY_LIST; }
        public boolean isShutdown() { return shutdown; }
        public boolean isTerminated() { return isShutdown(); }
        public boolean awaitTermination(long timeout, TimeUnit unit) { return isShutdown(); }
        private volatile boolean shutdown = false;
    }


    /**
     *  Blocking on submit(callable) throws InterruptedException if
     *  caller interrupted.
     */
    class TUnitTestInterruptedSubmit extends MultithreadedTest {
    	ThreadPoolExecutor p;
    	@Override public void initialize() {
    		AbstractExecutorServiceTest.this.setUp();
    		p = new ThreadPoolExecutor(1,1,60, TimeUnit.SECONDS, 
    				new ArrayBlockingQueue<Runnable>(10));
    	}
    	
    	public void thread1() throws ExecutionException {
            try {
                p.submit(new Callable<Object>() {
                        public Object call() {
                            try {
                            	Thread.sleep(MEDIUM_DELAY_MS);
                                shouldThrow();
                            } catch(InterruptedException e){}
                            return null;
                        }
                    }).get();
            } catch(InterruptedException success){ assertTick(1); }
    	}
    	
    	public void thread2() { 
    		waitForTick(1);
    		getThread(1).interrupt();
    		joinPool(p);
    	}
    }
    // TUNIT Timed interrupt
    


    /**
     *  get of submitted callable throws Exception if callable
     *  interrupted
     */
    class TUnitTestSubmitIE extends MultithreadedTest {
    	ThreadPoolExecutor p;
    	Callable c;
    	@Override public void initialize() {
    		p = new ThreadPoolExecutor(1,1,60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
    		c = new Callable() {
                public Object call() {
                    try {
                        p.submit(new SmallCallable()).get();
                        shouldThrow();
                    } catch(InterruptedException e){ assertTick(1); }
                    catch(RejectedExecutionException e2){}
                    catch(ExecutionException e3){}
                    return Boolean.TRUE;
                }
            };
    	}
    	
    	public void thread1() {
            try {
                c.call();
            } catch(Exception e){}
    	}
    	
    	public void thread2() { 
    		waitForTick(1);
    		getThread(1).interrupt();
    	}

    	@Override public void finish() {
    		joinPool(p);
    	}
    }
    // TUNIT Timed interrupt
    
}
