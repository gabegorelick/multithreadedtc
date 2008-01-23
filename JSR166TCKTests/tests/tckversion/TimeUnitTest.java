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

public class TimeUnitTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());	
    }
    
    public static Test suite() {
	return new TestSuite(TimeUnitTest.class);
    }

    // (loops to 88888 check increments at all time divisions.)


    
    /**
     *  Timed wait without holding lock throws
     *  IllegalMonitorStateException
     */
    public void testTimedWait_IllegalMonitorException() {
	//created a new thread with anonymous runnable

        Thread t = new Thread(new Runnable() {
                public void run() {
                    Object o = new Object();
                    TimeUnit tu = TimeUnit.MILLISECONDS;
                    try {
                        tu.timedWait(o,LONG_DELAY_MS);
                        threadShouldThrow();
                    }
                    catch (InterruptedException ie) {
                        threadUnexpectedException();
                    } 
                    catch(IllegalMonitorStateException success) {
                    }
                    
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(Exception e) {
            unexpectedException();
        }
    }
    
    /**
     * timedWait throws InterruptedException when interrupted
     */
    public void testTimedWait() {
	Thread t = new Thread(new Runnable() {
		public void run() {
		    Object o = new Object();
		    
		    TimeUnit tu = TimeUnit.MILLISECONDS;
		    try {
			synchronized(o) {
			    tu.timedWait(o,MEDIUM_DELAY_MS);
			}
                        threadShouldThrow();
		    }
		    catch(InterruptedException success) {} 
		    catch(IllegalMonitorStateException failure) {
			threadUnexpectedException();
		    }
		}
	    });
	t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(Exception e) {
            unexpectedException();
        }
    }
    
    
    /**
     * timedJoin throws InterruptedException when interrupted
     */
    public void testTimedJoin() {
	Thread t = new Thread(new Runnable() {
		public void run() {
		    TimeUnit tu = TimeUnit.MILLISECONDS;	
		    try {
			Thread s = new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        Thread.sleep(MEDIUM_DELAY_MS);
                                    } catch(InterruptedException success){}
                                }
                            });
			s.start();
			tu.timedJoin(s,MEDIUM_DELAY_MS);
                        threadShouldThrow();
		    }
		    catch(Exception e) {}
		}
	    });
	t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(Exception e) {
            unexpectedException();
        }
    }
    
    /**
     *  timedSleep throws InterruptedException when interrupted
     */
    public void testTimedSleep() {
	//created a new thread with anonymous runnable

	Thread t = new Thread(new Runnable() {
		public void run() {
		    TimeUnit tu = TimeUnit.MILLISECONDS;
		    try {
			tu.sleep(MEDIUM_DELAY_MS);
                        threadShouldThrow();
		    }
		    catch(InterruptedException success) {} 
		}
	    });
	t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(Exception e) {
            unexpectedException();
        }
    }


}
