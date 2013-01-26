package ikube.index.handler.filesystem;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import ikube.ATest;
import ikube.index.handler.ResourceBaseHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemLog;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import mockit.Deencapsulation;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class IndexableFilesystemLogHandlerTest extends ATest {

	private IndexableFilesystemLogHandler indexableFilesystemLogHandler;

	public IndexableFilesystemLogHandlerTest() {
		super(IndexableFilesystemLogHandlerTest.class);
	}

	@Before
	public void before() {
		new ThreadUtilities().initialize();
		indexableFilesystemLogHandler = new IndexableFilesystemLogHandler();
	}

	@After
	public void after() {
		new ThreadUtilities().destroy();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void handle() throws Exception {
		IndexableFileSystemLog indexableFileSystemLog = new IndexableFileSystemLog();
		File logDirectory = FileUtilities.findFileRecursively(new File("."), true, "logs");
		indexableFileSystemLog.setPath(logDirectory.getAbsolutePath());
		indexableFileSystemLog.setFileFieldName("fileName");
		indexableFileSystemLog.setPathFieldName("filePath");
		indexableFileSystemLog.setLineFieldName("lineNumber");
		indexableFileSystemLog.setContentFieldName("lineContents");

		ResourceBaseHandler<IndexableFileSystemLog> resourceBaseHandler = Mockito.mock(ResourceBaseHandler.class);
		Deencapsulation.setField(indexableFilesystemLogHandler, "resourceHandler", resourceBaseHandler);

		List<Future<?>> futures = indexableFilesystemLogHandler.handleIndexable(indexContext, indexableFileSystemLog);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		verify(resourceBaseHandler, atLeastOnce()).handleResource(any(IndexContext.class), any(IndexableFileSystemLog.class),
				any(Document.class), any(Object.class));
	}

}