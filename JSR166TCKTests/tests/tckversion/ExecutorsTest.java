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
import java.security.*;

public class ExecutorsTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());  
    }
    public static Test suite() {
        return new TestSuite(ExecutorsTest.class);
    }

    /**
     * ThreadPoolExecutor using privilegedThreadFactory has
     * specified group, priority, daemon status, name,
     * access control context and context class loader
     */
    public void testPrivilegedThreadFactory() {
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
			threadAssertTrue(!current.isDaemon());
			threadAssertTrue(current.getPriority() <= Thread.NORM_PRIORITY);
			ThreadGroup g = current.getThreadGroup();
			SecurityManager s = System.getSecurityManager();
			if (s != null)
			    threadAssertTrue(g == s.getThreadGroup());
			else
			    threadAssertTrue(g == egroup);
			String name = current.getName();
			threadAssertTrue(name.endsWith("thread-1"));
			threadAssertTrue(thisccl == current.getContextClassLoader());
			threadAssertTrue(thisacc.equals(AccessController.getContext()));
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
            Thread.sleep(SHORT_DELAY_MS);
        } catch (Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }

    }
        
}
