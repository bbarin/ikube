package ikube;

import ikube.action.IAction;
import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class is the central class for creating indexes.
 * 
 * This class then only looks up the index contexts and executes actions on them. The index engine registers a listener with the scheduler
 * and responds to the {@link Event#TIMER} type of event. This event schedule can be configured in the configuration, as can most schedules
 * and executors.
 * 
 * Index contexts contain parameters and indexables. Indexables are objects that can be indexed, like files and databases.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexEngine implements IIndexEngine, IListener {

	private static final Logger LOGGER = Logger.getLogger(IndexEngine.class);

	@Autowired
	@SuppressWarnings("unused")
	private IClusterManager clusterManager;
	private List<IAction<IndexContext<?>, Boolean>> actions;

	public IndexEngine() {
		LOGGER.info("Index engine : " + this);
		SerializationUtilities.setTransientFields(IndexContext.class, new ArrayList<Class<?>>());
	}

	@Override
	public void handleNotification(final Event event) {
		// If this server is working on anything then return
		// if (clusterManager.getServer().getWorking()) {
		// LOGGER.debug("Server working : " + clusterManager.getServer().getAction());
		// return;
		// }

		Random random = new Random();
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		// @SuppressWarnings("rawtypes")
		// List<IndexContext> randomContexts = new ArrayList<IndexContext>(indexContexts.values());
		// Collections.shuffle(randomContexts);
		for (IndexContext<?> indexContext : indexContexts.values()) {
			if (actions == null || actions.isEmpty()) {
				LOGGER.warn("No actions configured for index engine : " + indexContext.getIndexName());
				continue;
			}
			LOGGER.debug(Logging.getString("Start working : ", indexContext.getIndexName()));
			for (IAction<IndexContext<?>, Boolean> action : actions) {
				try {
					// if (clusterManager.getServer().getWorking()) {
					// // Sleep for a random time, 0 < a < 1 seconds if the server is working
					// // to give the previous action a little time before we execute the rules
					// Thread.sleep((long) Math.abs(random.nextLong()) % 1000);
					// if (clusterManager.getServer().getWorking()) {
					// continue;
					// }
					// }
					action.execute(indexContext);
					ThreadUtilities.sleep((long) Math.abs(random.nextLong()) % 3000);
				} catch (Exception e) {
					LOGGER.error("Exception executing action : " + action, e);
				}
			}
			LOGGER.debug(Logging.getString("Finished working : ", indexContext.getIndexName()));
		}
	}

	// public void setClusterManager(IClusterManager clusterManager) {
	// this.clusterManager = clusterManager;
	// }

	public void setActions(final List<IAction<IndexContext<?>, Boolean>> actions) {
		this.actions = actions;
	}

}