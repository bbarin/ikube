package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.File;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Url;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class resets the data in the cluster. It is imperative that nothing gets reset if there are any servers working of course. The urls
 * that are published into the cluster during the indexing need to be deleted. This deletion action will delete them not only from this
 * server's map but from all the servers' maps.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Reset extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean executeInternal(final IndexContext<?> indexContext) {
		ikube.model.Action action = null;
		try {
			action = start(indexContext.getIndexName(), "");
			if (action == null) {
				return Boolean.FALSE;
			}
			delete(dataBase, indexContext);
			return Boolean.TRUE;
		} finally {
			stop(action);
			notifyAll();
		}
	}

	protected synchronized void delete(final IDataBase dataBase, final Indexable<?> indexable) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.NAME, indexable.getName());
		delete(dataBase, Url.class, Url.SELECT_FROM_URL_BY_NAME, parameters);
		delete(dataBase, File.class, File.SELECT_FROM_FILE_BY_NAME, parameters);
		if (indexable.getChildren() != null) {
			for (Indexable<?> child : indexable.getChildren()) {
				delete(dataBase, child);
			}
		}
	}

	protected synchronized void delete(final IDataBase dataBase, final Class<?> klass, final String sql,
			final Map<String, Object> parameters) {
		try {
			List<?> list = dataBase.find(klass, sql, parameters, 0, IConstants.RESET_DELETE_BATCH_SIZE);
			do {
				if (list.size() > 0) {
					logger.info("Removing size {} : classes {}", new Object[] { list.size(), klass });
				}
				dataBase.removeBatch(list);
				list = dataBase.find(klass, sql, parameters, 0, IConstants.RESET_DELETE_BATCH_SIZE);
			} while (list.size() > 0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}