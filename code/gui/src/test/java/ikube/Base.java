package ikube;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.UriUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;

public abstract class Base {

	protected Logger logger = Logger.getLogger(this.getClass());

	protected static String LOCALHOST = "localhost";
	/** This client({@link HttpClient}) is for the web services. */
	protected static HttpClient HTTP_CLIENT = new HttpClient();
	protected static int SERVER_PORT = 9080;
	protected static String REST_USER_NAME = "user";
	protected static String REST_PASSWORD = "user";

	/**
	 * This method will build an array of name value pairs that can be used in the HttpClient to parameterize the request to resources and
	 * pages in fact.
	 * 
	 * @param names the names of the parameters
	 * @param values the values to be assigned to the parameters
	 * @return the array of name value pairs for the request
	 */
	protected static NameValuePair[] getNameValuePairs(String[] names, String[] values) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (int i = 0; i < names.length && i < values.length; i++) {
			NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
			nameValuePairs.add(nameValuePair);
		}
		return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
	}

	/**
	 * This method creates an index using the index path in the context, the time and the ip and returns the latest index directory, i.e.
	 * the index that has just been created. Note that if there are still cascading mocks from JMockit, the index writer sill not create the
	 * index! So you have to tear down all mocks prior to using this method.
	 * 
	 * @param indexContext the index context to use for the path to the index
	 * @param strings the data that must be in the index
	 * @return the latest index directory, i.e. the one that has just been created
	 */
	protected File createIndex(IndexContext<?> indexContext, String... strings) {
		IndexWriter indexWriter = null;
		String ip = null;
		try {
			ip = UriUtilities.getIp();
			indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
			Document document = new Document();
			IndexManager.addStringField(IConstants.CONTENTS, "Michael Couck", document, Store.YES, Field.Index.ANALYZED, TermVector.YES);
			indexWriter.addDocument(document);
			for (String string : strings) {
				document = new Document();
				IndexManager.addStringField(IConstants.CONTENTS, string, document, Store.YES, Field.Index.ANALYZED, TermVector.YES);
				indexWriter.addDocument(document);
			}
		} catch (Exception e) {
			logger.error("Exception creating the index : ", e);
		} finally {
			IndexManager.closeIndexWriter(indexWriter);
		}
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		// File serverIndexDirectory = new File(latestIndexDirectory, ip);
		// logger.info("Created index in : " + serverIndexDirectory.getAbsolutePath());
		return latestIndexDirectory;
	}

}