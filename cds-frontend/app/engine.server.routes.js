'use strict';

var engine = require('./engine.server.controller');

// calculate doCcount beforehand
engine.documentCount();

module.exports = function(app) {
	app.route('/api/searchresults/:queries')
	   .get(engine.search);
};