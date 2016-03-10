'use strict';

var express = require('express'),
	morgan = require('morgan'),
	bodyParser = require('body-parser'),
	flash = require('connect-flash');
	
module.exports = function() {
	var app = express();

	app.use(morgan('dev'));

	app.use(bodyParser.urlencoded({
		extended: true
	}));
	app.use(bodyParser.json());
	
	app.use(flash());

	require('./app/engine.server.routes.js')(app);
	
	app.use(express.static('./public'));

	return app;
};