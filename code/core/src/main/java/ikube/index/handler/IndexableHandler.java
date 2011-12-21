package ikube.index.handler;

import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for the handlers that contains access to common functionality like the threads etc.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public abstract class IndexableHandler<T extends Indexable<?>> implements IHandler<T> {

	protected Logger logger = Logger.getLogger(this.getClass());

	/** Access to the database. */
	@Autowired
	protected IDataBase dataBase;
	@Autowired
	protected IDelegate delegate;
	/** The number of threads that this handler will spawn. */
	private int threads;
	/** The class that this handler can handle. */
	private Class<T> indexableClass;

	public int getThreads() {
		return threads;
	}

	public void setThreads(final int threads) {
		this.threads = threads;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> getIndexableClass() {
		return indexableClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIndexableClass(final Class<T> indexableClass) {
		this.indexableClass = indexableClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDocument(final IndexContext<?> indexContext, final Document document) throws Exception {
		delegate.delegate(indexContext, document);
	}

}