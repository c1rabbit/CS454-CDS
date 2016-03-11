'use strict';

var searches = require('./searches.server.controller');

// calculate doCcount beforehand
searches.documentCount();

module.exports = function(app) {
	app.route('/api/search/:queries')
	   .get(searches.search);
};