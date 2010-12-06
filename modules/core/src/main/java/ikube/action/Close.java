package ikube.action;

import ikube.model.IndexContext;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;

/**
 * This class takes the searcher and tries to close the searcher on the directory.
 * 
 * @author Michael Couck
 * @since 24.08.08
 * @version 01.00
 */
public class Close extends Action<IndexContext, Boolean> {

	@Override
	public Boolean execute(IndexContext indexContext) {
		String actionName = getClass().getName();
		if (getClusterManager().anyWorkingOnIndex(indexContext.getIndexName())) {
			logger.debug("Close : Other servers working : " + actionName);
			return Boolean.FALSE;
		}
		MultiSearcher multiSearcher = indexContext.getMultiSearcher();
		if (multiSearcher == null) {
			logger.debug("No index searcher yet in context, please build the index : ");
			return Boolean.FALSE;
		}
		boolean shouldReopen = shouldReopen(indexContext);
		if (!shouldReopen) {
			logger.debug("Shouldn't re-open : " + shouldReopen);
			return Boolean.FALSE;
		}
		try {
			getClusterManager().setWorking(indexContext.getIndexName(), actionName, Boolean.TRUE, System.currentTimeMillis());
			Searchable[] searchables = multiSearcher.getSearchables();
			if (searchables != null && searchables.length > 0) {
				for (Searchable searchable : searchables) {
					try {
						IndexSearcher indexSearcher = (IndexSearcher) searchable;
						IndexReader reader = indexSearcher.getIndexReader();
						Directory directory = reader.directory();
						if (IndexWriter.isLocked(directory)) {
							IndexWriter.unlock(directory);
						}
						reader.close();
						searchable.close();
					} catch (Exception e) {
						logger.error("Exception trying to close the searcher", e);
					}
				}
			}
			indexContext.setMultiSearcher(null);
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), null, Boolean.FALSE, 0);
		}
		return Boolean.TRUE;
	}

}