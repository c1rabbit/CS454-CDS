'use strict';

var searches = require('./searches.server.controller');

// calculate doCcount beforehand
searches.documentCount();

module.exports = function(app) {
	app.route('/api/searchresults/:queries')
	   .get(searches.search);
};