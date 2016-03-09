var myApp = angular.module('myApp', []);
myApp.controller('AppCtrl', ['$scope', '$http', function ($scope, $http) {
	
	$scope.search = function () {
		console.log('Search query is: ' + $scope.searchquery);
		
		if ($scope.searchquery) {
			$http.get('/searchresults/' + $scope.searchquery).success(function (response) {
				console.log('Response is: ' + response.location[0].filename);
			});
			
			$scope.searchquery = '';
		}
	};
}]);