<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<script type="text/javascript">

	// The controller that does the search
	module.controller('SearcherController', function($http, $scope) {
		
		// The model data that we bind to in the form
		$scope.allWords = 'find me';
		$scope.exactPhrase = '';
		$scope.oneOrMore = '';
		$scope.noneOfTheseWords = '';
		$scope.pageBlock = 10; // Only results per page
		$scope.endResult = 0;

		$scope.statistics = {};
		$scope.pagination = []

		// This function concatenates the search strings for all the predicate
		// data into a semi colon separated string that can be used in the advanced
		// search
		$scope.doSearchStrings = function() {
			var searchStrings = [];
			searchStrings.push($scope.allWords);
			searchStrings.push(';');
			searchStrings.push($scope.exactPhrase);
			searchStrings.push(';');
			searchStrings.push($scope.oneOrMore);
			searchStrings.push(';');
			searchStrings.push($scope.noneOfTheseWords);
			return searchStrings.join('');
		};
		
		// The form parameters we send to the server
		$scope.searchParameters = { 
			indexName : 'desktop', // The default is the desktop index
			searchStrings : $scope.doSearchStrings(),
			fragment : true,
			firstResult : 0,
			maxResults : $scope.pageBlock
		};
		
		// The configuration for the request to the server for the results
		$scope.config = { params : $scope.searchParameters };
		
		// Go to the web service for the results
		$scope.doSearch = function() {
			// Advanced search
			$scope.url = getServiceUrl('/ikube/service/search/json/multi/advanced/all');
			$scope.searchParameters['searchStrings'] = $scope.doSearchStrings();
			delete $scope.searchParameters['distance'];
			delete $scope.searchParameters['latitude'];
			delete $scope.searchParameters['longitude'];
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				// Pop the statistics Json off the array
				$scope.data = data;
				$scope.status = status;
				$scope.statistics = $scope.data.pop();
				$scope.doPagination($scope.data);
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
		};
		
		// Sets the first result based on the pagination page requested
		$scope.doFirstResult = function(firstResult) {
			$scope.searchParameters.firstResult = firstResult;
		}
		
		// Creates the Json pagination array for the next pages in the search
		$scope.doPagination = function(data) {
			$scope.pagination = [];
			var total = $scope.statistics.total;
			// Exception or no results
			if (total == null || total == 0) {
				$scope.searchParameters.firstResult = 0;
				$scope.endResult = 0;
				return;
			}
			// We just started a search and got the first results
			var pages = total / $scope.pageBlock;
			// Create one 'page' for each block of results
			for (var i = 0; i < pages && i < $scope.pageBlock; i++) {
				var firstResult = i * $scope.pageBlock;
				var active = firstResult == $scope.searchParameters.firstResult ? 'black' : 'blue';
				$scope.pagination[i] = { page : i, firstResult : firstResult, active : active };
			};
			// Find the 'to' result being displayed
			var modulo = total % $scope.pageBlock;
			$scope.endResult = $scope.searchParameters.firstResult + modulo == total ? total : $scope.searchParameters.firstResult + $scope.pageBlock;
		}
	});
	
</script>

<table ng-app="ikube" ng-controller="SearcherController" width="100%">
	<form ng-submit="doSearch()">
	<tr>
		<td>Collection : </td>
		<td>
			<select ng-controller="IndexesController" ng-model="searchParameters.indexName">
				<option ng-repeat="index in indexes" value="{{index}}">{{index}}</option>
			</select>
		</td>
	</tr>
	<tr>
		<td>All of these words:</td>
		<td><input id="allWords" name="allWords" ng-model="allWords"></td>
	</tr>
	<tr>
		<td>This exact word or phrase:</td>
		<td><input id="exactPhrase" name="exactPhrase" ng-model="exactPhrase"></td>
	</tr>
	<tr>
		<td>One or more of these words:</td>
		<td><input id="oneOrMore" name="oneOrMore" ng-model="oneOrMore"></td>
	</tr>
	<tr>
		<td>None of these words:</td>
		<td><input id="noneOfTheseWords" name="noneOfTheseWords" ng-model="noneOfTheseWords"></td>
	</tr>
	
	<tr>
		<td colspan="2">
			<button>Go!</button>
		</td>
	</tr>
	</form>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2">
			Showing results '{{searchParameters.firstResult}} 
			to {{endResult}} 
			of {{statistics.total}}' 
			for search '{{searchParameters.searchStrings}}', 
			corrections : {{statistics.corrections}}, 
			duration : {{statistics.duration}}</td>
	</tr>
	
	<tr>
		<td colspan="2" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="
					doFirstResult(page.firstResult);
					doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr ng-repeat="datum in data">
		<td colspan="2">
			<span ng-hide="!datum.id"><b>Identifier</b> : {{datum.id}}<br></span> 
			<b>Score</b> : {{datum.score}}<br>
			<b>Fragment</b> : <span ng-bind-html-unsafe="datum.fragment"></span><br>
			<span ng-hide="!datum.latitude"><b>Latitude</b> : {{datum.latitude}}<br></span>
			<span ng-hide="!datum.longitude"><b>Longitude</b> : {{datum.longitude}}<br></span>
			<span ng-hide="!datum.distance"><b>Distance</b> : {{datum.distance}}<br></span>
			<span ng-hide="!datum.path"><b>Path</b> : {{datum.path}}<br></span>
			<br>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" 
					ng-click="doFirstResult(page.firstResult);doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
</table>