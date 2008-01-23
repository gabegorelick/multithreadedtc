package mtcversion;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */


import junit.framework.*;
import java.util.concurrent.*;
import java.security.*;

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

public class ExecutorsTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());  
    }
    public static Test suite() {
        return TestFramework.buildTestSuite(ExecutorsTest.class);
    }

    /**
     * ThreadPoolExecutor using privilegedThreadFactory has
     * specified group, priority, daemon status, name,
     * access control context and context class loader
     * 
	 * TUnit Version: thread1 waits for ExecutorService to finish
	 * by waiting for tick 2 instead of Thread.sleep
	 */
	class TUnitTestPrivilegedThreadFactory extends MultithreadedTest {

    	public void thread1() {
            Policy savedPolicy = null;
            try {
                savedPolicy = Policy.getPolicy();
                AdjustablePolicy policy = new AdjustablePolicy();
                policy.addPermission(new RuntimePermission("getContextClassLoader"));
                policy.addPermission(new RuntimePermission("setContextClassLoader"));
                Policy.setPolicy(policy);
            } catch (AccessControlException ok) {
                return;
            }
            final ThreadGroup egroup = Thread.currentThread().getThreadGroup();
            final ClassLoader thisccl = Thread.currentThread().getContextClassLoader();
            final AccessControlContext thisacc = AccessController.getContext();
            Runnable r = new Runnable() {
                    public void run() {
    		    try {
    		    	
    			Thread current = Thread.currentThread();
    			assertTrue(!current.isDaemon());
    			assertTrue(current.getPriority() <= Thread.NORM_PRIORITY);
    			ThreadGroup g = current.getThreadGroup();
    			SecurityManager s = System.getSecurityManager();
    			if (s != null)
    			    assertTrue(g == s.getThreadGroup());
    			else
    			    assertTrue(g == egroup);
    			String name = current.getName();
    			assertTrue(name.endsWith("thread-1"));
    			assertTrue(thisccl == current.getContextClassLoader());
    			assertTrue(thisacc.equals(AccessController.getContext()));
    			
    			waitForTick(1);
    			
    		    } catch(SecurityException ok) {
    			// Also pass if not allowed to change settings
    		    }
                    }
                };
            ExecutorService e = Executors.newSingleThreadExecutor(Executors.privilegedThreadFactory());
            
            Policy.setPolicy(savedPolicy);
            e.execute(r);
            try {
                e.shutdown();
            } catch(SecurityException ok) {
            }
            try {
                //Thread.sleep(SHORT_DELAY_MS);
            	waitForTick(2);
            } catch (Exception ex) {
                unexpectedException();
            } finally {
                joinPool(e);
            }
    	}    	
    }    
	// TUNIT Unclassified
}
