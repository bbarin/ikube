package ikube.index.content;

import ikube.model.IndexableInternet;
import ikube.toolkit.FileUtilities;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 23.11.10
 * @version 01.00
 */
public class InternetContentProvider implements IContentProvider<IndexableInternet> {

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void getContent(IndexableInternet indexable, OutputStream outputStream) {
		try {
			InputStream inputStream = indexable.getCurrentInputStream();
			FileUtilities.getContents(inputStream, outputStream, Integer.MAX_VALUE);
		} catch (Exception e) {
			logger.error("Exception accessing url : " + indexable, e);
		}
	}

}