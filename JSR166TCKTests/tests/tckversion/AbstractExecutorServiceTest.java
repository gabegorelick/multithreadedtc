package tckversion;
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

public class AbstractExecutorServiceTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AbstractExecutorServiceTest.class);
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
    public void testInterruptedSubmit() {
        final ThreadPoolExecutor p = new ThreadPoolExecutor(1,1,60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        p.submit(new Callable<Object>() {
                                public Object call() {
                                    try {
                                        Thread.sleep(MEDIUM_DELAY_MS);
                                        shouldThrow();
                                    } catch(InterruptedException e){
                                    }
                                    return null;
                                }
                            }).get();
                    } catch(InterruptedException success){
                    } catch(Exception e) {
                        unexpectedException();
                    }

                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
        } catch(Exception e){
            unexpectedException();
        }
        joinPool(p);
    }

    /**
     *  get of submitted callable throws Exception if callable
     *  interrupted
     */
    public void testSubmitIE() {
        final ThreadPoolExecutor p = new ThreadPoolExecutor(1,1,60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));

        final Callable c = new Callable() {
                public Object call() {
                    try {
                        p.submit(new SmallCallable()).get();
                        shouldThrow();
                    } catch(InterruptedException e){}
                    catch(RejectedExecutionException e2){}
                    catch(ExecutionException e3){}
                    return Boolean.TRUE;
                }
            };



        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.call();
                    } catch(Exception e){}
                }
          });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }

        joinPool(p);
    }

}
