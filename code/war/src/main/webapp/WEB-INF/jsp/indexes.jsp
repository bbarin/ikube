<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table ng-controller="IndexContextsController" class="table table-condensed" style="margin-top: 55px;">
	<tr>
		<th colspan="9"><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Local server indexes</th>
	</tr>
	<tr>
		<th>
			<img src="<c:url value="/images/icons/index.gif" />">
			&nbsp;Name
			<button class="btn btn-mini btn-link" ng-click="sort('name')"><i class="icon-sort"></i></button>
		</th>
		<th>
			<img src="<c:url value="/images/icons/open.gif" />">
			&nbsp;Open</a>
			<button class="btn btn-mini btn-link" ng-click="sort('open')"><i class="icon-sort"></i></button>
		</th>
		<th>
			<img src="<c:url value="/images/icons/link_obj.gif" />">
			&nbsp;Documents</a>
			<button class="btn btn-mini btn-link" ng-click="sort('numDocsForSearchers')"><i class="icon-sort"></i></button>
		</th>
		<th>
			<img src="<c:url value="/images/icons/repository.gif" />">
			&nbsp;Size</a>
			<button class="btn btn-mini btn-link" ng-click="sort('snapshot.indexSize')"><i class="icon-sort"></i></button>
		</th>
		<%-- <th><img src="<c:url value="/images/icons/register_view.gif" />">&nbsp;<a href="#" ng-click="sortIndexContexts('maxAge')">Max age</a></th> --%>
		<th>
			<img src="<c:url value="/images/icons/refresh.gif" />">
			&nbsp;Index timestamp</a>
			<button class="btn btn-mini btn-link" ng-click="sort('snapshot.latestIndexTimestamp')"><i class="icon-sort"></i></button>
		</th>
		<th>
			<img src="<c:url value="/images/icons/jar_l_obj.gif" />">
			&nbsp;Path</a>
			<button class="btn btn-mini btn-link" ng-click="sort('indexDirectoryPath')"><i class="icon-sort"></i></button>
		</th>
		<%-- <th><img src="<c:url value="/images/icons/progress_task.gif" />">&nbsp;<a href="#" ng-click="sortIndexContexts('throttle')">Throttle</a></th> --%>
		<th><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Function</th>
	</tr>
	<tr ng-repeat="indexContext in indexContexts | orderBy:orderProp:direction" ng-class="{ 'well' : indexContext.indexing == true }">
		<td>{{indexContext.name}}</td>
		<td>{{indexContext.open}}</td>
		<td>{{indexContext.numDocsForSearchers / 1000000}}</td>
		<td>{{indexContext.snapshot.indexSize / 1000000}}</td>
		<!-- <td>{{indexContext.maxAge}}</td> -->
		<td>{{indexContext.snapshot.latestIndexTimestamp}}</td>
		<td>{{indexContext.indexDirectoryPath}}</td>
		<!-- <td">{{indexContext.throttle}}</td> -->
		<td nowrap="nowrap">
			<div class="btn-group">
				<button class="btn btn-mini btn-success" ng-click="startIndexing(indexContext.name);">Start</button>
				<button class="btn btn-mini btn-warning" ng-click="terminateIndexing(indexContext.name);">Stop</button>
				<button class="btn btn-mini btn-danger" ng-click="deleteIndex(indexContext.name);">Delete</button>
			</div>
		</td>
	</tr>
</table>