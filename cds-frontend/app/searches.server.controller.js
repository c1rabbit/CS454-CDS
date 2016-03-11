'use strict';

var mongojs = require('mongojs');
var stem = require('stem-porter');
var db = mongojs('cs454', ['index', 'rankCollection']); // mongodb database, and collections
var async = require('async');

var docCount;

exports.documentCount = function (){
	db.rankCollection.find(function (err, doc) {
		docCount = doc.length;
	});	
}

exports.search = function(req, res){
	var queries = parseAndStemQuery(req.params.queries);
	var tf, weight, temp;
	var calculateIdf = queries.length > 1;
	var idf;
	var rankMap = {};
	
	db.index.find({term: { $in: queries}}, function(err, docs){
		docs.forEach( function (doc, index){
			var term = doc.term;
			var locations = doc.location;
			//console.log('term ' + term + ' has ' + locations.length + ' locations.');

			locations.forEach( function (location, index){
				var filename = location.filename;
				var indices = location.index;
				//console.log(term + ': ' + filename + ': ' + indices.length);

				tf = Math.log10(1 + indices.length);

				idf = Math.log10((docCount + 1) / locations.length);

				weight = tf * idf;

				if (filename in rankMap)
					temp = rankMap[filename] + weight;
				else
					temp = weight;

				rankMap[filename] = temp;

			});
			
		});

		//console.log(rankMap);

		db.rankCollection.find({}, function (err, docs){
			docs.forEach( function (doc, index){
				var filename = doc.file;
				var linkrank = doc.rank;
				var tfidf = rankMap[filename];
				var rank = Math.sqrt(linkrank*linkrank + tfidf*tfidf);
				rankMap[filename] = rank;
			})
			console.log(rankMap);
		});


	});
}

var documentCount = function (callback) {
	db.rankCollection.find(function (err, doc) {
		callback(null, doc.length);
	});
}

var parseAndStemQuery = function (input) {
	var queryList = input.split(' ');

	for (var i = 0; i < queryList.length; i++)
		queryList[i] = stem(queryList[i].toLowerCase());

	return queryList;
}