package ikube.action.rule;

import ikube.toolkit.Logging;

import java.io.File;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class DirectoryExistsAndIsLocked extends ARule<File> {

	public boolean evaluate(final File indexDirectory) {
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexDirectory);
			boolean exists = IndexReader.indexExists(directory);
			boolean locked = IndexWriter.isLocked(directory);
			logger.info(Logging.getString("Server index directory : ", indexDirectory, "exists : ", exists, "locked : ", locked));
			if (exists && locked) {
				return Boolean.TRUE;
			} else {
				logger.info("Locked directory : " + directory);
			}
		} catch (Exception e) {
			logger.error("Exception checking the directories : ", e);
		} finally {
			try {
				if (directory != null) {
					directory.close();
				}
			} catch (Exception e) {
				logger.error("Exception closing the directory : " + directory, e);
			}
		}
		return Boolean.FALSE;
	}

}
