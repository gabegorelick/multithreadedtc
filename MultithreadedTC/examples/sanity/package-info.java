/**
 * The sanity package contains acceptance tests that test all the functionality in
 * MultithreadedTC. These tests should be run whenever changes are made to 
 * MultithreadedTC.
 * <p>
 * 
 * Basic tests - Thread order and blocking based tests, waitForTick delay, 
 * <p>
 * 
 * General TestFramework tests - getting/setting parameters, run many times, guarantee that threads are started in random order, interleaving from mayyield
 * <p>
 * 
 * Test TestFramework.buildTestSuite (a class with many kinds of inner classes, all detected and run)
 * <p>
 * 
 * Customized wait - test customized waiting functions
 * <p>
 * 
 * Timing issues - test freezeclock, tests with timing
 * <p>
 * 
 * Error detection tests - tests all the error states detected by framework, throw exception in thread
 * 
 * @author William Pugh
 * @author Nathaniel Ayewah
 * @since 1.0
 */
package sanity;

