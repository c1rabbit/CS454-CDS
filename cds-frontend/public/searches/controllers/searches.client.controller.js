'use strict';

angular.module('searches').controller('SearchController', 
	['$scope', '$routeParams', '$location', 'SearchService', 
	function($scope, $routeParams, $location, SearchService) {
		$scope.wait = false;
		$scope.search = function() {
			$scope.wait = true;
			SearchService.query({ queries: $scope.query}, function(data){
				$scope.results = data;
				$scope.wait = false;
			});
			$scope.searched_query = $scope.query;
			$location.path('search');
			$scope.query = '';
		}
	}
]);