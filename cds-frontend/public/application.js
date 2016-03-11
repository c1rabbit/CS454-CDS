'use strict';

var mainApplicationModuleName = 'searchEngine';

// Create the main application
var mainApplicationModule = angular.module(mainApplicationModuleName, 
    ['ngResource', 'ngRoute', 'searches']);

angular.element(document).ready(function() {
    angular.bootstrap(document, [mainApplicationModuleName]);
});