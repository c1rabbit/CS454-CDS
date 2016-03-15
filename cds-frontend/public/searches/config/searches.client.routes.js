'use strict';

angular.module('searches').config(['$routeProvider',
	function($routeProvider) {
		$routeProvider.
		when('/', {
			templateUrl: '/searches/views/home.client.view.html',
			controller: 'SearchController'
		}).
		when('/search', {
			templateUrl: '/searches/views/search.client.view.html',
			controller: 'SearchController'
		}).
		otherwise({
			redirectTo: '/'
		});
	
	}
]); 
