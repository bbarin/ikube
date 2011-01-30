package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.lucene.search.Searchable;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ActionTest extends BaseTest {

	private Action action = new Action() {
		@Override
		public Boolean execute(IndexContext e) {
			return Boolean.FALSE;
		}
	};

	@Test
	public void indexCurrent() throws Exception {
		long newMaxAge = 1000;
		long maxAge = indexContext.getMaxAge();
		indexContext.setMaxAge(newMaxAge);
		Thread.sleep(newMaxAge);

		boolean indexCurrent = action.isIndexCurrent(indexContext);
		assertFalse(indexCurrent);

		StringBuilder builder = new StringBuilder();
		builder.append(indexContext.getIndexDirectoryPath());
		builder.append(File.separator);
		builder.append(indexContext.getIndexName());
		builder.append(File.separator);
		builder.append(System.currentTimeMillis());
		builder.append(File.separator);
		builder.append(IP);
		File serverIndexDirectory = FileUtilities.getFile(builder.toString(), Boolean.TRUE);

		indexContext.setMaxAge(maxAge);

		indexCurrent = action.isIndexCurrent(indexContext);
		assertTrue(indexCurrent);

		FileUtilities.deleteFile(serverIndexDirectory, 1);
		assertFalse(serverIndexDirectory.exists());
	}

	@Test
	public void shoudReopen() throws Exception {
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
		boolean shouldReopen = action.shouldReopen(indexContext);
		// Searcher null in the context
		assertTrue(shouldReopen /* && !baseIndexDirectory.exists() */);

		indexContext.getIndex().setMultiSearcher(MULTI_SEARCHER);

		// No SEARCHABLES in the searcher
		when(MULTI_SEARCHER.getSearchables()).thenReturn(new Searchable[0]);
		shouldReopen = action.shouldReopen(indexContext);
		assertTrue(shouldReopen);

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File serverIndexDirectory = createIndex(new File(serverIndexDirectoryPath));

		when(FS_DIRECTORY.getFile()).thenReturn(new File(serverIndexDirectory.getAbsolutePath()));
		when(MULTI_SEARCHER.getSearchables()).thenReturn(SEARCHABLES);

		// All the directories are in the searcher
		shouldReopen = action.shouldReopen(indexContext);
		assertFalse(shouldReopen);

		// Create a new server index directory
		File anotherServerIndexDirectory = createIndex(new File(serverIndexDirectoryPath.replace(IP, "127.0.0.2")));

		shouldReopen = action.shouldReopen(indexContext);
		assertTrue(shouldReopen);

		indexContext.getIndex().setMultiSearcher(null);
		FileUtilities.deleteFile(serverIndexDirectory, 1);
		FileUtilities.deleteFile(anotherServerIndexDirectory, 1);

		assertFalse(serverIndexDirectory.exists());
		assertFalse(anotherServerIndexDirectory.exists());
	}

}