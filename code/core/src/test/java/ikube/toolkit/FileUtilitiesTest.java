package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class FileUtilitiesTest extends AbstractTest {

	private File file;
	private File dotFolder;
	private File indexFolderOne;
	private String[] stringPatterns;

	public FileUtilitiesTest() {
		super(FileUtilitiesTest.class);
	}

	@Before
	public void before() {
		String fileName = "file.file";
		dotFolder = new File(".");
		file = new File(dotFolder, fileName);
		String fileUtilitiesTestIndexdirectory = "fileUtilitiesTestIndexdirectory";
		indexFolderOne = FileUtilities.getFile("./" + fileUtilitiesTestIndexdirectory + "/1234567889/127.0.0.1", Boolean.TRUE);
		stringPatterns = new String[] { fileName };

		FileUtilities.deleteFile(new File("./common"), 1);
		FileUtilities.deleteFile(new File("./spring.xml"), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(file, 1);
		if (indexFolderOne != null && indexFolderOne.getParentFile().getParentFile().exists()) {
			FileUtilities.deleteFile(indexFolderOne.getParentFile().getParentFile(), 1);
		}
		FileUtilities.deleteFile(new File("./common"), 1);
		FileUtilities.deleteFile(new File("./spring.xml"), 1);
	}

	@Test
	public void findFiles() throws Exception {
		assertFalse(file.exists());
		assertTrue(file.createNewFile());
		assertTrue(file.exists());

		File[] files = FileUtilities.findFiles(dotFolder, stringPatterns);
		int initialLength = files.length;
		assertTrue(initialLength >= 1);
		assertTrue(file.delete());

		files = FileUtilities.findFiles(dotFolder, stringPatterns);
		assertEquals(initialLength - 1, files.length);
	}

	@Test
	public void findFilesRecursively() throws Exception {
		assertFalse(file.exists());
		assertTrue(file.createNewFile());
		assertTrue(file.exists());

		List<File> files = FileUtilities.findFilesRecursively(dotFolder, new ArrayList<File>(), stringPatterns);
		int initialLength = files.size();
		assertTrue(initialLength >= 1);
		files.clear();

		files = FileUtilities.findFilesRecursively(dotFolder, files, ".xml");
		initialLength = files.size();
		assertTrue(initialLength >= 1);
	}

	@Test
	public void deleteFile() throws Exception {
		assertFalse(file.exists());
		assertTrue(file.createNewFile());
		assertTrue(file.exists());

		FileUtilities.deleteFile(file, 1);
		assertFalse(file.exists());
	}

	@Test
	public void deleteFiles() throws Exception {
		assertFalse(file.exists());
		assertTrue(file.createNewFile());
		assertTrue(file.exists());

		FileUtilities.deleteFiles(dotFolder, stringPatterns);
		assertFalse(file.exists());
	}

	@Test
	public void findFile() {
		File file = FileUtilities.findFileRecursively(new File("."), "doctors.xml");
		assertNotNull(file);
	}

	@Test
	public void setContents() throws Exception {
		String data = "Michael Couck";
		File tempFile = FileUtilities.getFile("./indexes/data.dat", Boolean.FALSE);
		FileUtilities.setContents(tempFile.getAbsolutePath(), data.getBytes());
		assertTrue(tempFile.exists());
		assertTrue(tempFile.length() > 5);
	}

	@Test
	public void getContentsFromEnd() {
		// Create a file with 1024 bytes and try to read 512 bytes
		byte[] bytes = new byte[1024];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) i;
		}
		file = FileUtilities.getFile(file.getAbsolutePath(), Boolean.FALSE);
		FileUtilities.setContents(file.getAbsolutePath(), bytes);

		byte[] readBytes = FileUtilities.getContentsFromEnd(file, 512).toByteArray();
		assertTrue("There must be some bytes in the array : ", readBytes.length > 0);
		for (int i = readBytes.length - 1; i >= 0; i--) {
			assertEquals("The bytes must be the same in the ", bytes[i], readBytes[i]);
		}
		// Now read 2048 bytes from the same file
		readBytes = FileUtilities.getContentsFromEnd(file, 2048).toByteArray();
		assertTrue("There must be some bytes in the array : ", readBytes.length > 0);
		for (int i = bytes.length - 1; i >= 0; i--) {
			assertEquals("The bytes must be the same in the ", bytes[i], readBytes[i]);
		}
	}

	@Test
	public void unzip() {
		try {
			File jarFile = FileUtilities.findFileRecursively(new File("."), false, "jar.jar\\Z");
			File unzipedDirectory = FileUtilities.unzip(jarFile.getAbsolutePath(), "./deleteMe");
			assertNotNull(unzipedDirectory);
			assertTrue(unzipedDirectory.exists());
			File ruleInterceptorFile = FileUtilities.findFileRecursively(unzipedDirectory, "RuleInterceptor.java");
			assertNotNull(ruleInterceptorFile);
			assertTrue(ruleInterceptorFile.exists());
			FileUtilities.deleteFile(unzipedDirectory, 1);
		} finally {
			FileUtilities.deleteFile(new File("./deleteMe"), 1);
		}
	}

	@Test
	public void findFileRecursivelyOnlyFile() {
		File file = FileUtilities.findFileRecursively(new File("."), false, "doctors.xml");
		assertNotNull(file);
		assertTrue(file.isFile());
		assertTrue(file.exists());
		file = FileUtilities.findFileRecursively(new File("."), true, "data");
		assertNotNull(file);
		assertTrue(file.exists());
		assertTrue(file.isDirectory());
	}

}