package ikube.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class SearchTest extends ATest {

	private static Searcher SEARCHER;
	private static String INDEX_DIRECTORY_PATH = "./" + SearchTest.class.getSimpleName();
	private static String russian = "Россия";

	@BeforeClass
	public static void beforeClass() throws Exception {
		// Create the index with multiple fields
		File indexDirectory = new File(INDEX_DIRECTORY_PATH);
		FileUtilities.deleteFile(indexDirectory, 1);
		FileUtilities.getFile(INDEX_DIRECTORY_PATH, Boolean.TRUE);

		Directory directory = FSDirectory.open(indexDirectory);

		IndexWriter indexWriter = new IndexWriter(directory, IConstants.ANALYZER, true, MaxFieldLength.UNLIMITED);
		int numDocs = 50;
		for (int i = 0; i < numDocs; i++) {
			String id = Integer.toString(i * 100);
			String contents = new StringBuilder("Hello world. " + russian).append(i).toString();
			
			Document document = new Document();
			IndexManager.addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
			IndexManager.addStringField(IConstants.CONTENTS, contents, document, Store.YES, Index.ANALYZED, TermVector.YES);
			IndexManager.addStringField(IConstants.NAME, "Michael Couck." + russian, document, Store.YES, Index.ANALYZED, TermVector.YES);
			indexWriter.addDocument(document);
		}
		
		indexWriter.commit();
		indexWriter.optimize();
		indexWriter.close();

		Searchable[] searchables = new Searchable[] { new IndexSearcher(directory) };
		SEARCHER = new MultiSearcher(searchables);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (SEARCHER != null) {
			SEARCHER.close();
		}
		FileUtilities.deleteFile(new File(INDEX_DIRECTORY_PATH), 1);
	}

	private int maxResults = 10;

	public SearchTest() {
		super(SearchTest.class);
	}

	@Test
	public void searchSingle() {
		SearchSingle searchSingle = new SearchSingle(SEARCHER);
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(Boolean.TRUE);
		searchSingle.setMaxResults(maxResults);
		searchSingle.setSearchField(IConstants.CONTENTS);
		searchSingle.setSearchString(russian);
		searchSingle.setSortField(new String[] { IConstants.ID });
		ArrayList<HashMap<String, String>> results = searchSingle.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchMulti() {
		SearchMulti searchMulti = new SearchMulti(SEARCHER);
		searchMulti.setFirstResult(0);
		searchMulti.setFragment(Boolean.TRUE);
		searchMulti.setMaxResults(maxResults);
		searchMulti.setSearchField(IConstants.ID, IConstants.CONTENTS);
		searchMulti.setSearchString("id.1~", "hello");
		searchMulti.setSortField(new String[] { IConstants.ID });
		ArrayList<HashMap<String, String>> results = searchMulti.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchMultiSorted() {
		SearchMultiSorted searchMultiSorted = new SearchMultiSorted(SEARCHER);
		searchMultiSorted.setFirstResult(0);
		searchMultiSorted.setFragment(Boolean.TRUE);
		searchMultiSorted.setMaxResults(maxResults);
		searchMultiSorted.setSearchField(IConstants.ID, IConstants.CONTENTS);
		searchMultiSorted.setSearchString("id.1~", "hello");
		searchMultiSorted.setSortField(new String[] { IConstants.ID });
		ArrayList<HashMap<String, String>> results = searchMultiSorted.execute();
		assertTrue(results.size() > 1);

		// Verify that all the results are in ascending order according to the id
		String previousId = null;
		for (Map<String, String> result : results) {
			String id = result.get(IConstants.ID);
			logger.info("Previous id : " + previousId + ", id : " + id);
			if (previousId != null && id != null) {
				assertTrue(previousId.compareTo(id) < 0);
			}
			previousId = id;
		}
	}

	@Test
	public void searchMultiAll() {
		SearchMultiAll searchMultiAll = new SearchMultiAll(SEARCHER);
		searchMultiAll.setFirstResult(0);
		searchMultiAll.setFragment(Boolean.TRUE);
		searchMultiAll.setMaxResults(maxResults);
		searchMultiAll.setSearchField("content");
		searchMultiAll.setSearchString("Michael");
		searchMultiAll.setSortField("content");
		ArrayList<HashMap<String, String>> results = searchMultiAll.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void addStatistics() {
		String searchString = "michael AND couck";
		Search search = new SearchMultiAll(SEARCHER);
		search.setSearchString(searchString);

		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		search.addStatistics(results, 79, 23, null);
		Map<String, String> statistics = results.get(results.size() - 1);
		logger.info("Search strings : " + statistics.get(IConstants.SEARCH_STRINGS));
		logger.info("Corrected search strings : " + statistics.get(IConstants.CORRECTIONS));
		assertEquals("michael AND couck", statistics.get(IConstants.SEARCH_STRINGS));
		assertEquals("michael and houck", statistics.get(IConstants.CORRECTIONS));
	}

	@Test
	public void getCorrections() {
		String[] searchStrings = { "some words", "are niet corect", "AND there are AND some words WITH AND another" };
		Search search = new SearchSingle(SEARCHER);
		search.setSearchString(searchStrings);
		String[] expectedCorrectedSearchStrings = { "AND there are AND some words WITH AND another", "are net correct", "some words" };
		String[] correctedSearchStrings = search.getCorrections();
		String correctedSearchString = StringUtils.strip(Arrays.deepToString(correctedSearchStrings), IConstants.STRIP_CHARACTERS);
		logger.info("Corrected : " + correctedSearchString);
		assertEquals("Only the completely incorrect words should be replaced : ", Arrays.deepToString(expectedCorrectedSearchStrings),
				Arrays.deepToString(correctedSearchStrings));
	}

}