package ikube.index.handler.filesystem;

import static org.junit.Assert.assertTrue;
import ikube.Integration;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.junit.Before;
import org.junit.Test;

public class IndexableFilesystemHandlerIntegration extends Integration {

	private IndexContext<?> desktop;
	private IndexableFileSystem desktopFolder;
	private IndexableFilesystemHandler indexableFilesystemHandler;

	@Before
	public void before() {
		desktop = ApplicationContextManager.getBean("desktop");
		desktopFolder = ApplicationContextManager.getBean("desktopFolder");
		indexableFilesystemHandler = ApplicationContextManager.getBean(IndexableFilesystemHandler.class);
		delete(ApplicationContextManager.getBean(IDataBase.class), ikube.model.File.class);
		FileUtilities.deleteFile(new File(desktop.getIndexDirectoryPath()), 1);
	}

	@Test
	public void handle() throws Exception {
		Directory directory = null;
		try {
			File dataIndexFolder = FileUtilities.findFileRecursively(new File("."), "data");
			String dataIndexFolderPath = FileUtilities.cleanFilePath(dataIndexFolder.getAbsolutePath());
			desktopFolder.setPath(dataIndexFolderPath);
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexWriter indexWriter = IndexManager.openIndexWriter(desktop, System.currentTimeMillis(), ip);
			desktop.setIndexWriters(indexWriter);
			List<Future<?>> threads = indexableFilesystemHandler.handle(desktop, desktopFolder);
			ThreadUtilities.waitForFutures(threads, Integer.MAX_VALUE);

			// Verify that there are some documents in the index
			assertTrue("There should be at least one document in the index : ", desktop.getIndexWriters()[0].numDocs() > 0);
		} finally {
			IndexManager.closeIndexWriter(desktop);
			if (directory != null) {
				directory.close();
			}
		}
	}

	@Test
	public void interrupt() throws Exception {
		desktopFolder.setPath("/");
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(desktop, System.currentTimeMillis(), ip);
		desktop.setIndexWriters(indexWriter);

		ThreadUtilities.submit(new Runnable() {
			public void run() {
				ThreadUtilities.sleep(15000);
				ThreadUtilities.destroy(desktop.getIndexName());
			}
		});

		List<Future<?>> futures = indexableFilesystemHandler.handle(desktop, desktopFolder);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		// If we don't get here then the test failed :)
	}

}