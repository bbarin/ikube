package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-06-2013
 */
public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

    @Autowired
    private IDataBase dataBase;
    @Autowired
    private InternetResourceHandler internetResourceHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    public ForkJoinTask<?> handleIndexableForked(
            final IndexContext<?> indexContext,
            final IndexableInternet indexableInternet)
            throws Exception {
        IResourceProvider resourceProvider = new InternetResourceProvider(indexableInternet, dataBase);
        return getRecursiveAction(indexContext, indexableInternet, resourceProvider);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Url> handleResource(
            final IndexContext<?> indexContext,
            final IndexableInternet indexableInternet,
            final Object resource) {
        Url url = (Url) resource;
        try {
            if (url.getRawContent() != null) {
                internetResourceHandler.handleResource(indexContext, indexableInternet, new Document(), url);
            }
            return Collections.EMPTY_LIST;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (url.getParsedContent() != null) {
                url.setHash(HashUtilities.hash(url.getParsedContent()));
            }
            url.setRawContent(null);
            url.setParsedContent(null);
        }
    }


}