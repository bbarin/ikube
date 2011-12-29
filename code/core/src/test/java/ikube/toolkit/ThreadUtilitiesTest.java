package ikube.toolkit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test if for the thread utilities, which has methods to wait for threads etc.
 * 
 * @author Michael Couck
 * @since 20.03.11
 * @version 01.00
 */
public class ThreadUtilitiesTest {

	private Logger logger = Logger.getLogger(this.getClass());

	@Before
	public void before() {
		ThreadUtilities.initialize();
	}

	@After
	public void after() {
		ThreadUtilities.destroy();
	}

	@Test
	public void waitForThreads() {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 3; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep((long) (Math.random() * 10000));
					} catch (Exception e) {
						logger.error("Spit or swallow : ", e);
					}
					logger.debug("Thread exiting : " + Thread.currentThread());
				}
			});
			thread.start();
			threads.add(thread);
		}
		ThreadUtilities.waitForThreads(threads);
		// Verify that all the threads are dead
		for (Thread thread : threads) {
			assertFalse("All the threads should have died : ", thread.isAlive());
		}
		assertTrue("We just want to exit here after the threads die : ", true);
	}

	@Test
	public void waitForFutures() {
		// We just wait for this future to finish
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
		};
		Future<?> future = ThreadUtilities.submit(runnable);
		ThreadUtilities.waitForFuture(future, Integer.MAX_VALUE);
		// We must get here
		assertTrue(true);

		// We destroy this future and return from the wait method
		runnable = new Runnable() {
			public void run() {
				while (true) {
					ThreadUtilities.sleep(1000);
				}
			}
		};
		future = ThreadUtilities.submit(runnable);
		logger.info("Going into wait : " + future);
		new Thread(new Runnable() {
			public void run() {
				ThreadUtilities.sleep(3000);
				ThreadUtilities.destroy();
			}
		}).start();
		ThreadUtilities.waitForFuture(future, Integer.MAX_VALUE);
		// We must get here
		assertTrue(true);
	}

}
