package ikube.search;

import ikube.IConstants;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.NumericUtils;

/**
 * @see Search
 * @author Michael Couck
 * @since 10.01.2012
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchComplex extends SearchSpatial {

	public SearchComplex(final Searcher searcher) {
		this(searcher, IConstants.ANALYZER);
	}

	public SearchComplex(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(final Query query) throws IOException {
		if (coordinate != null) {
			return super.search(query);
		}
		return searcher.search(query, firstResult + maxResults);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query getQuery() throws ParseException {
		BooleanQuery booleanQuery = new BooleanQuery();
		for (int i = 0; i < searchStrings.length; i++) {
			final String typeField = typeFields[i];
			final String searchField = searchFields[i];
			final String searchString = searchStrings[i];
			if (StringUtils.isEmpty(searchString)) {
				// Just ignore the empty strings
				continue;
			}
			Query query = null;
			if (TypeField.STRING.fieldType().equals(typeField)) {
				query = getQueryParser(searchField).parse(searchString);
			} else if (TypeField.NUMERIC.fieldType().equals(typeField)) {
				Double numeric = Double.parseDouble(searchString);
				query = new TermQuery(new Term(searchField, NumericUtils.doubleToPrefixCoded(numeric)));
			} else if (TypeField.RANGE.fieldType().equals(typeField)) {
				String[] values = StringUtils.split(searchString, '-');
				double min = Double.parseDouble(values[0]);
				double max = Double.parseDouble(values[1]);
				query = NumericRangeQuery.newDoubleRange(searchField, min, max, Boolean.TRUE, Boolean.TRUE);
			}
			booleanQuery.add(query, BooleanClause.Occur.MUST);
		}
		return booleanQuery;
	}

}