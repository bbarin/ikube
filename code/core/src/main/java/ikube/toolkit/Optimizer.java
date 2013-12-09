package ikube.toolkit;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see ikube.action.Optimizer
 * @author Michael Couck
 * @since 08.02.13
 * @version 01.00
 */
public final class Optimizer {

	static {
		Logging.configure();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Optimizer.class);

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		try {
			IndexContext<?> indexContext = new IndexContext<Indexable>();
			indexContext.setMergeFactor(10000);
			indexContext.setBufferSize(256);
			indexContext.setBufferedDocs(10000);
			indexContext.setCompoundFile(Boolean.TRUE);
			for (final String basePath : args) {
				indexContext.setIndexDirectoryPath(basePath);
				new ikube.action.Optimizer().execute(indexContext);
			}
		} catch (Exception e) {
			LOGGER.error("Usage : java -jar ikube-tools.jar ikube.toolkit.Optimizer [lucene-index-to-optimize lucene-index-to-optimize lucene-index-to-optimize...]");
			LOGGER.error(null, e);
		}
	}

}