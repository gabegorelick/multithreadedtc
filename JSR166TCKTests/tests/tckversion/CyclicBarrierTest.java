package tckversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class CyclicBarrierTest extends JSR166TestCase{
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return new TestSuite(CyclicBarrierTest.class);
    }

    private volatile int countAction;
    private class MyAction implements Runnable {
        public void run() { ++countAction; }
    }
    

    // REVIEW <=> Interleaving threads synchronized by a cyclic barrier (could test in framework but this may be simpler)
    /**
     * A 2-party/thread barrier triggers after both threads invoke await
     */
    public void testTwoParties() {
        final CyclicBarrier b = new CyclicBarrier(2);
	Thread t = new Thread(new Runnable() {
		public void run() {
                    try {
                        b.await();
                        b.await();
                        b.await();
                        b.await();
                    } catch(Exception e){
                        threadUnexpectedException();
                    }}});

        try {
            t.start();
            b.await();
            b.await();
            b.await();
            b.await();
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }


    // REVIEW <=> Simple test for blocking
    /**
     * An interruption in one party causes others waiting in await to
     * throw BrokenBarrierException
     */
    public void testAwait1_Interrupted_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();
                    } catch(InterruptedException success){}                
                    catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            t1.interrupt();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    // REVIEW <=> Simple test for blocking
    /**
     * An interruption in one party causes others waiting in timed await to
     * throw BrokenBarrierException
     */
    public void testAwait2_Interrupted_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(InterruptedException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            t1.interrupt();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    
    // REVIEW <=> Simple Test for Action that causes unblocking and exceptions (action = timeout)
    /**
     * A timeout in timed await throws TimeoutException
     */
    public void testAwait3_TimeOutException() {
        final CyclicBarrier c = new CyclicBarrier(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(TimeoutException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                        
                    }
                }
            });
        try {
            t.start();
            t.join(); 
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    // REVIEW <=> Simple Test for Action that causes unblocking and exceptions (action = timeout)
    /**
     * A timeout in one party causes others waiting in timed await to
     * throw BrokenBarrierException
     */
    public void testAwait4_Timeout_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(TimeoutException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    // REVIEW <=> Simple Test for Action that causes unblocking and exceptions (action = timeout)
    /**
     * A timeout in one party causes others waiting in await to
     * throw BrokenBarrierException
     */
    public void testAwait5_Timeout_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(TimeoutException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    // REVIEW <=> Simple Test for Action that causes unblocking and exceptions (action = reset)
    /**
     * A reset of an active barrier causes waiting threads to throw
     * BrokenBarrierException
     */
    public void testReset_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();
                    } catch(BrokenBarrierException success){}                
                    catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            c.reset();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    // REVIEW <=> Simple Test that an action does not throw exception unless threads are blocked
    /**
     * A reset before threads enter barrier does not throw
     * BrokenBarrierException
     */
    public void testReset_NoBrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            c.reset();
            t1.start();
            t2.start();
            c.await();
            t1.join(); 
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * All threads block while a barrier is broken.
     */
    public void testReset_Leakage() {
        try {
            final CyclicBarrier c = new CyclicBarrier(2);
            final AtomicBoolean done = new AtomicBoolean();
            Thread t = new Thread() {
                    public void run() {
                        while (!done.get()) {
                            try {
                                while (c.isBroken())
                                    c.reset();
                                
                                c.await();
                                threadFail("await should not return");
                            }
                            catch (BrokenBarrierException e) {
                            }
                            catch (InterruptedException ie) {
                            }
                        }
                    }
                };
            
            t.start();
            for( int i = 0; i < 4; i++) {
                Thread.sleep(SHORT_DELAY_MS);
                t.interrupt();
            }
            done.set(true);
            t.interrupt();
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    // REVIEW <=> Interleaving threads synchronized by a cyclic barrier
    /**
     * Reset of a non-broken barrier does not break barrier
     */
    public void testResetWithoutBreakage() {
        try {
            final CyclicBarrier start = new CyclicBarrier(3);
            final CyclicBarrier barrier = new CyclicBarrier(3);
            for (int i = 0; i < 3; i++) {
                Thread t1 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                
                Thread t2 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                
                
                t1.start();
                t2.start();
                try { start.await(); }
                catch (Exception ie) { threadFail("start barrier"); }
                barrier.await();
                t1.join();
                t2.join();
                assertFalse(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
                if (i == 1) barrier.reset();
                assertFalse(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
            }
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    // REVIEW <=> Interleaving threads synchronized by a cyclic barrier
    /**
     * Reset of a barrier after interruption reinitializes it.
     */
    public void testResetAfterInterrupt() {
        try {
            final CyclicBarrier start = new CyclicBarrier(3);
            final CyclicBarrier barrier = new CyclicBarrier(3);
            for (int i = 0; i < 2; i++) {
                Thread t1 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch(InterruptedException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                
                Thread t2 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch(BrokenBarrierException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                
                t1.start();
                t2.start();
                try { start.await(); }
                catch (Exception ie) { threadFail("start barrier"); }
                t1.interrupt();
                t1.join();
                t2.join();
                assertTrue(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
                barrier.reset();
                assertFalse(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
            }
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    // REVIEW <=> Interleaving threads synchronized by a cyclic barrier
    /**
     * Reset of a barrier after timeout reinitializes it.
     */
    public void testResetAfterTimeout() {
        try {
            final CyclicBarrier start = new CyclicBarrier(3);
            final CyclicBarrier barrier = new CyclicBarrier(3);
            for (int i = 0; i < 2; i++) {
                Thread t1 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS); }
                            catch(TimeoutException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                
                Thread t2 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch(BrokenBarrierException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                
                t1.start();
                t2.start();
                try { start.await(); }
                catch (Exception ie) { threadFail("start barrier"); }
                t1.join();
                t2.join();
                assertTrue(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
                barrier.reset();
                assertFalse(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
            }
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }


    // REVIEW <=> Interleaving threads synchronized by a cyclic barrier
    /**
     * Reset of a barrier after a failed command reinitializes it.
     */
    public void testResetAfterCommandException() {
        try {
            final CyclicBarrier start = new CyclicBarrier(3);
            final CyclicBarrier barrier = 
                new CyclicBarrier(3, new Runnable() {
                        public void run() { 
                            throw new NullPointerException(); }});
            for (int i = 0; i < 2; i++) {
                Thread t1 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch(BrokenBarrierException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                
                Thread t2 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch(BrokenBarrierException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                
                t1.start();
                t2.start();
                try { start.await(); }
                catch (Exception ie) { threadFail("start barrier"); }
                while (barrier.getNumberWaiting() < 2) { Thread.yield(); }
                try { barrier.await(); }
                catch (Exception ok) { }
                t1.join();
                t2.join();
                assertTrue(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
                barrier.reset();
                assertFalse(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
            }
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
}
