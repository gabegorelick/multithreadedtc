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

public class CountDownLatchTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return new TestSuite(CountDownLatchTest.class);
    }


    /**
     * await returns after countDown to zero, but not before
     */
    public void testAwait() {
	final CountDownLatch l = new CountDownLatch(2);

	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        threadAssertTrue(l.getCount() > 0);
			l.await();
                        threadAssertTrue(l.getCount() == 0);
		    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
		}
	    });
	t.start();
	try {
            assertEquals(l.getCount(), 2);
            Thread.sleep(SHORT_DELAY_MS);
            l.countDown();
            assertEquals(l.getCount(), 1);
            l.countDown();
            assertEquals(l.getCount(), 0);
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
    

    /**
     * timed await returns after countDown to zero
     */
    public void testTimedAwait() {
	final CountDownLatch l = new CountDownLatch(2);

	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        threadAssertTrue(l.getCount() > 0);
			threadAssertTrue(l.await(SMALL_DELAY_MS, TimeUnit.MILLISECONDS));
		    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
		}
	    });
	t.start();
	try {
            assertEquals(l.getCount(), 2);
            Thread.sleep(SHORT_DELAY_MS);
            l.countDown();
            assertEquals(l.getCount(), 1);
            l.countDown();
            assertEquals(l.getCount(), 0);
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
    
    /**
     * await throws IE if interrupted before counted down
     */
    public void testAwait_InterruptedException() {
        final CountDownLatch l = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertTrue(l.getCount() > 0);
                        l.await();
                        threadShouldThrow();
                    } catch(InterruptedException success){}
                }
            });
	t.start();
	try {
            assertEquals(l.getCount(), 1);
            t.interrupt();
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * timed await throws IE if interrupted before counted down
     */
    public void testTimedAwait_InterruptedException() {
        final CountDownLatch l = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertTrue(l.getCount() > 0);
                        l.await(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();                        
                    } catch(InterruptedException success){}
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(l.getCount(), 1);
            t.interrupt();
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * timed await times out if not counted down before timeout
     */
    public void testAwaitTimeout() {
        final CountDownLatch l = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertTrue(l.getCount() > 0);
                        threadAssertFalse(l.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(l.getCount() > 0);
                    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
                }
            });
        t.start();
        try {
            assertEquals(l.getCount(), 1);
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }

}
