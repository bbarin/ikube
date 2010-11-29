package ikube.action;

import ikube.ATest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexThreadTest extends ATest {

	private Index index = new Index();

	@Test
	public void waitForThreads() {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 3; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep((long) (Math.random() * 10000));
					} catch (Exception e) {
						logger.error("", e);
					}
					logger.debug("Thread exiting : " + Thread.currentThread());
				}
			});
			thread.start();
			threads.add(thread);
		}
		index.waitForThreads(threads);
	}

}
