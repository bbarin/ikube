package ikube.web.service;

import ikube.IConstants;

import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Path looks like this: http://localhost:9080/ikube/service/search/json/multi
 * 
 * @author Michael couck
 * @since 21.01.12
 * @version 01.00
 */
@Component
@Path(SearcherJson.SEARCH)
@Scope(Resource.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class SearcherJson extends Searcher {

	public static final String SEARCH = "/search/json";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherJson.class);

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.SINGLE)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchSingle(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		ArrayList<HashMap<String, String>> results = searcherService.searchSingle(indexName, searchStrings, searchFields, fragment,
				firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMulti(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMulti(indexName, searchStringsArray, searchFieldsArray,
				fragment, firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_SORTED)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiSorted(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.SORT_FIELDS) final String sortFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		String[] sortFieldsArray = StringUtils.split(sortFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSorted(indexName, searchStringsArray, searchFieldsArray,
				sortFieldsArray, fragment, firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAll(indexName, searchStringsArray, fragment, firstResult,
				maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_SPATIAL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiSpacial(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults, @QueryParam(value = IConstants.DISTANCE) final int distance,
			@QueryParam(value = IConstants.LATITUDE) final String latitude, @QueryParam(value = IConstants.LONGITUDE) final String longitude) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFieldsArray = StringUtils.split(searchFields, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacial(indexName, searchStringsArray, searchFieldsArray,
				fragment, firstResult, maxResults, distance, Double.parseDouble(latitude), Double.parseDouble(longitude));
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_SPATIAL_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiSpacialAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults, @QueryParam(value = IConstants.DISTANCE) final int distance,
			@QueryParam(value = IConstants.LATITUDE) final String latitude, @QueryParam(value = IConstants.LONGITUDE) final String longitude) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacialAll(indexName, searchStringsArray, fragment,
				firstResult, maxResults, distance, Double.parseDouble(latitude), Double.parseDouble(longitude));
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_ADVANCED)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiAdvanced(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchField,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		String[] searchFields = { searchField };
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAdvanced(indexName, searchStringsArray, searchFields,
				fragment, firstResult, maxResults);
		return buildResponse(results);
	}

	/**
	 * {@inheritDoc}
	 */
	@GET
	@Override
	@Path(SearcherJson.MULTI_ADVANCED_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public Response searchMultiAdvancedAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, SEPARATOR);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAdvancedAll(indexName, searchStringsArray, fragment,
				firstResult, maxResults);
		return buildResponse(results);
	}

}