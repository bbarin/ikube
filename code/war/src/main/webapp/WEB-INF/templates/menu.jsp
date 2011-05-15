<%@ taglib prefix="search" uri="http://ikube/search" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
	window.onload = function() {
		document.ikubeSearchForm.searchStrings.focus();
	}
</script>

<div id="sidebar" class="menu">
	<ul>
		<li id="search">
			<form name="ikubeSearchForm" id="ikubeSearchForm" action="<c:url value="/results.html"/>">
				<fieldset>
					<input type="hidden" name="indexName" value="ikube">
					<input type="hidden" name="fragment" value="true">
					<input type="hidden" name="searchFields" value="content">
					<!-- TODO change this to a Spring controller that goes to the web service -->
					<input type="hidden" name="searchUrl" value="http://localhost:9000/ikube/SearchServlet">
					<input type="text" name="searchStrings" id="search-text"
						value="<c:out value='${param.searchStrings}' />" />
					<input type="submit" id="search-submit" value="Go" />
				</fieldset>
			</form>
			<search:spellingTag>
				<script type="text/javascript">
					function submitIkubeSearchForm(searchStrings) {
						document.ikubeSearchFormchStrings.value = searchStrings;
						document.ikubeSearchForm.submit();
					}
				</script>
				Did you mean : <a href="JavaScript:submitIkubeSearchForm('<search:spellingWriterTag />')"><search:spellingWriterTag /></a>
			</search:spellingTag>
		</li>
		<li>
			<h2>Navigation</h2>
			<ul>
				<li><a href="<c:url value="/index.html"/>">Home</a></li>
				<li><a href="<c:url value="/admin/servers.html"/>">Monitoring</a></li>
				<li><a href="<c:url value="/admin/admin.html"/>">Administration</a></li>
				<li><a href="<c:url value="/documentation/index.html"/>">Documentation</a></li>
			</ul>
		</li>
		<li>
			<h2>libraries used</h2>
			<ul>
				<li><a href="http://lucene.apache.org/">Lucene</a></li>
				<li><a href="http://pdfbox.apache.org/">PdfBox</a></li>
				<li><a href="http://www.neodatis.org/">Neodatis</a></li>
				<li><a href="http://sourceforge.net/projects/c3p0/">C3p0</a></li>
				<li><a href="http://www.springsource.org">Spring</a></li>
				<li><a href="http://www.hazelcast.com/">Hazelcast</a></li>
				<li><a href="http://hudson-ci.org/">Hudson CI</a></li>
				<li><a href="http://www.h2database.com/html/main.html">H2 database</a></li>
			</ul>
		</li>
		<li>
			<h2>Similar products</h2>
			<ul>
				<li><a href="http://en.wikipedia.org/wiki/Apache_Solr">Solr</a></li>
				<li><a href="http://en.wikipedia.org/wiki/ISYS_Search_Software">Isys</a></li>
				<li><a href="http://en.wikipedia.org/wiki/Autonomy_Corporation">Autonomy</a></li>
				<li><a href="http://en.wikipedia.org/wiki/Google_Search_Appliance">Google Search Appliance</a></li>
				<li><a href="http://www.searchtools.com/info/database-search.html">Database search tools</a></li>
			</ul>
		</li>
		<li>
			<h2>Interesting Java Links</h2>
			<ul>
				<li><a href="http://jenkins-ci.org/">Jenkins CI</a></li>
				<li><a href="http://www.springbyexample.org">Spring by Example</a></li>
				<li><a href="http://en.wikipedia.org/wiki/Software_package_metrics">Code Metrics</a></li>
				<li><a href="http://en.wikipedia.org/wiki/PageRank">Page Rank</a></li>
			</ul>
		</li>
	</ul>
</div>