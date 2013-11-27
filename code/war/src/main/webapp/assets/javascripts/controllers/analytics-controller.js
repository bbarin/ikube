/**
 * @author Michael Couck
 * @since 24-11-2013
 * 
 * @param $scope
 * @param $http
 * @param $injector
 * @param $timeout
 */
module.controller('AnalyticsController', function($http, $scope, $injector, $timeout) {
	
	$scope.status = 200;
	$scope.analysis = {};
	$scope.analyzer;
	$scope.analyzers;
	
	$scope.doAnalysis = function() {
		var url = getServiceUrl('/ikube/service/analyzer/analyze');
		var promise = $http.post(url, $scope.analysis);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.analysis = data;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
		$scope.waaaaaiiiiitttt = function() {
			return $timeout(function() {
				// Bla...
			}, 100);
		};
		return $scope.waaaaaiiiiitttt();
	};
	
	$scope.doTrain = function() {
		var url = getServiceUrl('/ikube/service/analyzer/analyze');
		var promise = $http.post(url, $scope.analysis);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	
	$scope.doAnalyzers = function() {
		var url = getServiceUrl('/ikube/service/monitor/analyzers');
		var promise = $http.get(url);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.analyzers = data;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	};
	$scope.doAnalyzers();
	
	//a simple model to bind to and send to the server
    $scope.model = {
        name: "",
        comments: ""
    };
    
    //an array of files selected
    $scope.files = [];
    
    //listen for the file selected event
    $scope.$on("fileSelected", function (event, args) {
        $scope.$apply(function () {            
            //add the file object to the scope's files collection
            $scope.files.push(args.file);
        });
    });
    
    //the save method
    $scope.save = function() {
    	// "/Api/PostStuff"
    	var url = getServiceUrl('/ikube/service/analyzer/analyze');
        $http({
            method: 'POST',
            url: url,
            //IMPORTANT!!! You might think this should be set to 'multipart/form-data' 
            // but this is not true because when we are sending up files the request 
            // needs to include a 'boundary' parameter which identifies the boundary 
            // name between parts in this multi-part request and setting the Content-type 
            // manually will not set this boundary parameter. For whatever reason, 
            // setting the Content-type to 'false' will force the request to automatically
            // populate the headers properly including the boundary parameter.
            headers: { 'Content-Type': false },
            //This method will allow us to change how the data is sent up to the server
            // for which we'll need to encapsulate the model data in 'FormData'
            transformRequest: function (data) {
                var formData = new FormData();
                //need to convert our json object to a string version of json otherwise
                // the browser will do a 'toString()' on the object which will result 
                // in the value '[Object object]' on the server.
                formData.append("model", angular.toJson(data.model));
                //now add all of the assigned files
                for (var i = 0; i < data.files; i++) {
                    //add each file to the form data and iteratively name them
                    formData.append("file" + i, data.files[i]);
                }
                return formData;
            },
            //Create an object that contains the model and files which will be transformed
            // in the above transformRequest method
            data: { model: $scope.model, files: $scope.files }
        }).
        success(function (data, status, headers, config) {
            alert("success!");
        }).
        error(function (data, status, headers, config) {
            alert("failed!");
        });
    };
	
});