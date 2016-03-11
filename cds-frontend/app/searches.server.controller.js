'use strict';

var mongojs = require('mongojs');
var stem = require('stem-porter');
var db = mongojs('cs454', ['index', 'rankCollection']); // mongodb database, and collections
var async = require('async');

var docCount;
var queryObj = {};

exports.documentCount = function (){
	db.rankCollection.find(function (err, doc) {
		docCount = doc.length;
	});	
}

exports.search = function(req, res){
	queryObj.queries = parseAndStemQuery(req.params.queries);
	queryObj.calculateIdf = queryObj.queries.list.length > 1;

	//console.log(queryObj);

	db.index.find({term: { $in: queryObj.queries.list}}, function(err, docs){
		if (queryObj.queries.list.length == 1) searchOR(res, queryObj, docs);
		else if (queryObj.queries.type === 'or') searchOR(res, queryObj, docs);
		else if (queryObj.queries.type === 'and') searchAND(res, queryObj, docs);
		else searchProx(res, queryObj, docs);
	});
}

function compareFile(a, b){
	return -(a.weight - b.weight);
}

function documentCount (callback) {
	db.rankCollection.find(function (err, doc) {
		callback(null, doc.length);
	});
}

function parseAndStemQuery (input) {
	var queries = {};
	var list = input.split(' ');
	queries.list = [];
	var query;
	for (var i = 0; i < list.length; i++){
		query = stem(list[i].toLowerCase());
		if (query == 'and' || query == 'or') {
			queries.type = query;
		} else {
			queries.list.push(query);
		}
	}
	return queries;
}

function searchOR(res, queryObj, docs){
	var term, locations, filename, indices;
	var tfidf, temp;
	queryObj.rankMap = {};
	docs.forEach( function (doc, index){
		term = doc.term;
		locations = doc.location;
			//console.log('term ' + term + ' has ' + locations.length + ' locations.');
			//console.log(doc);
			
			locations.forEach( function (location, index){
				filename = location.filename;
				indices = location.index;
				//console.log(term + ': ' + filename + ': ' + indices.length);
				tfidf = calculateTfidf(indices.length, queryObj.calculateIdf, locations.length);
				if (filename in queryObj.rankMap)
					temp = queryObj.rankMap[filename] + tfidf;
				else
					temp = tfidf;
				queryObj.rankMap[filename] = temp;
			});
		});

	//res.json(queryObj.rankMap);
	combineLinkAnalysis(res, queryObj);
}

function searchProx(){

}

function calculateTfidf (frequency, calculateIdf, df){
	var tf = Math.log10(1 + frequency);
	var idf = calculateIdf ? Math.log10((docCount + 1) / df) : 1;
	return tf * idf;
}

function searchAND(res, queryObj, docs){
	var term, locations, filename, indices;
	var temp;
	var fileObj, termObj, terms, frequency;
	queryObj.rankMap = {};
	queryObj.fileMap = {};
	
	docs.forEach( function (doc, index){
		term = doc.term;
		locations = doc.location;
		//console.log('term ' + term + ' has ' + locations.length + ' locations.');
			
		locations.forEach( function (location, index){
			fileObj = {};
			filename = location.filename;
			indices = location.index;
			termObj = {
				'term' : term,
				'indices' : indices,
				'locations_count' : locations.length
			};
			//console.log(term + ': ' + filename + ': ' + indices.length);
			if (queryObj.fileMap[filename]){
				fileObj = queryObj.fileMap[filename];
				fileObj.terms.push(termObj);
				fileObj.tot_frequency += indices.length;
			}else{
				fileObj = {
					terms : [termObj],
					tot_frequency : indices.length
				}
				queryObj.fileMap[filename] = fileObj;
			}
		});

	});
	//console.log(queryObj.fileMap);

	// initialize file object
	fileObj = {};
	var fkey, terms, termsCount, df, frequency;
	var tfidf = 0;
	// for each file
	for (fkey in queryObj.fileMap){
		if (queryObj.fileMap.hasOwnProperty(fkey)) {
			fileObj = queryObj.fileMap[fkey];
			terms = fileObj.terms;
			termsCount = terms.length;

			// contains both, then calculate tfidf
       		if (termsCount == queryObj.queries.list.length){

       			terms.forEach( function(termObj,index){
       				df = termObj.locations_count;
       				frequency = termObj.indices.length;
       				tfidf += calculateTfidf(frequency, true, df);
       			});
       			
       			queryObj.rankMap[fkey] = tfidf;
      		}
       	}
    }

    //res.json(queryObj.rankMap);

	combineLinkAnalysis(res, queryObj);
}

function combineLinkAnalysis(res, queryObj){
	// multiply by link analysis
	var results = [];
	db.rankCollection.find({}, function (err, docs){
		var filename, linkrank, tfidf, rank;
		docs.forEach( function (doc, index){
			filename = doc.file;
			linkrank = doc.rank;
			tfidf = queryObj.rankMap[filename];
			rank = Math.sqrt(linkrank*linkrank + tfidf*tfidf);
			queryObj.rankMap[filename] = rank;
		})
		
		var f;
		for (f in queryObj.rankMap){
			if (queryObj.rankMap.hasOwnProperty(f) && queryObj.rankMap[f]){
				var file = {
					'filename' : f,
					'weight' : queryObj.rankMap[f]
				}
				results.push(file);	
			}
		}

		results.sort(compareFile);
		
		res.json(results);
	});
}