package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.scheduling.Schedule;
import ikube.toolkit.ThreadUtilities;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Michael Couck
 * @since 28.03.13
 * @version 01.00
 */
public class MemorySchedule extends Schedule {

	@Value("${max.memory}")
	private String maxMemory = "2000";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		if (Runtime.getRuntime().totalMemory() / IConstants.MILLION > Integer.parseInt(maxMemory)) {
			ThreadUtilities.destroy();
		}
	}

}