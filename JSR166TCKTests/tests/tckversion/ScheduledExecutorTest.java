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

public class ScheduledExecutorTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return new TestSuite(ScheduledExecutorTest.class);
    }

    // REVIEW <=> Test to ensure an executor executes a runnable, by sleeping until the executor is done
    /**
     * execute successfully executes a runnable
     */
    public void testExecute() {
	try {
            TrackedShortRunnable runnable =new TrackedShortRunnable();
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
	    p1.execute(runnable);
	    assertFalse(runnable.done);
	    Thread.sleep(SHORT_DELAY_MS);
	    try { p1.shutdown(); } catch(SecurityException ok) { return; }
	    try {
                Thread.sleep(MEDIUM_DELAY_MS);
            } catch(InterruptedException e){
                unexpectedException();
            }
	    assertTrue(runnable.done);
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
            joinPool(p1);
        }
	catch(Exception e){
            unexpectedException();
        }
        
    }


}
