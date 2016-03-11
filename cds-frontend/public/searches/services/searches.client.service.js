angular.module('searches').factory('SearchService', ['$resource', 
	function($resource) {
		return $resource('api/searchresults/:queries');
	}
]);
