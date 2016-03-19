'use strict';

var mongojs = require('mongojs');
var stem = require('stem-porter');
var db = mongojs('cs454', ['index', 'rankCollection']); // mongodb database, and collections
var async = require('async');

var docCount;
var queryObj = {};
var tfidfMax = 0;
var now, fiveyears;

exports.documentCount = function (){
	db.rankCollection.find(function (err, doc) {
		docCount = doc.length;
	});	
	
}

exports.prepareSearch = function(req, res){
	db.index.find({}, function(err, docs){
		var pool = [];
		docs.forEach(function(doc, index){
			pool.push(doc.term);
		})
		res.json(pool);
	})
}

exports.search = function(req, res){
	// reset tfidfMax
	tfidfMax = 0;
	now = Date.now();
	fiveyears = now - (5 * 12 * 30 * 24 * 60 * 60 * 1000);
	queryObj.queries = parseAndStemQuery(req.params.queries);

	db.index.find({term: { $in: queryObj.queries.list}}, function(err, docs){
		if (queryObj.queries.list.length == 1 || queryObj.queries.type === 'or') searchOR(docs, res);
		else buildInvertedIndex(docs, res);
	});
}

function searchOR(docs, res){
	var term, locations, filename, indices;
	var tfidf, temp;
	queryObj.rankMap = {};
	docs.forEach( function (doc, index){
		term = doc.term;
		locations = doc.location;
		locations.forEach( function (location, index){
			filename = location.filename;
			indices = location.index;
			//console.log(term + ': ' + filename + ': ' + indices.length);
			tfidf = calculateTfidf(indices.length, locations.length);
			if (filename in queryObj.rankMap)
				temp = queryObj.rankMap[filename] + tfidf;
			else
				temp = tfidf;
			queryObj.rankMap[filename] = temp;

		});
	});

	//res.json(queryObj.rankMap);
	combineLinkAnalysis(res);
}

function buildInvertedIndex(docs, res){
	var term, locations, filename, indices;
	var temp;
	var fileObj, termObj;
	queryObj.rankMap = {};
	queryObj.fileMap = {};
	
	// build inverse index
	docs.forEach( function (doc, index){
		term = doc.term;
		locations = doc.location;
			
		locations.forEach( function (location, index){
			fileObj = {};
			filename = location.filename;
			indices = location.index;
			termObj = {
				'term' : term,
				'indices' : indices,
				'locations_count' : locations.length
			};

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
	
	if (queryObj.queries.type === 'and') searchAND(res);
	else searchProx(res);
}

function searchAND(res){
	// initialize file object
	var termObj, terms, frequency;
	var fkey, fileObj, terms, termsCount, df, frequency;
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
       				tfidf += calculateTfidf(frequency, df);
       				if (tfidf > tfidfMax) tfidfMax = tfidf;
       			});
       			
       			queryObj.rankMap[fkey] = tfidf;
      		}
       	}
    }
    combineLinkAnalysis(res);
}

function searchProx(res){
	var fkey, fileObj, terms, termsCount;

	for (fkey in queryObj.fileMap){
		if (queryObj.fileMap.hasOwnProperty(fkey)) {
			fileObj = queryObj.fileMap[fkey];
			terms = fileObj.terms;
			termsCount = terms.length;

			// iterate through terms
			var termFrequency = 0;
			if (termsCount == queryObj.queries.list.length){
				terms.forEach(function(termObj, index){
					var term = termObj.term;
					var indices = termObj.indices;
					var df = termObj.locations_count;

					var weight = 0;
					// iterate through next terms
					indices.forEach(function(i){

						while (index < terms.length){
							var otherTermObj = terms[index];
							var otherIndices = otherTermObj.indices;

							// if proximity matches, count phrase frequency
							if (binaryProximitySearch(i, otherIndices) != -1){
								termFrequency++;
							}

							index++;
						}

					});
					// phrase frequency count is now done, store information
					if (termFrequency > 0){
						queryObj.rankMap[fkey] = termFrequency;
					}

				});
			}
		}
	}

	queryObj.rankMap = convertToTfidfFromFrequency(queryObj.rankMap);

	combineLinkAnalysis(res);
}

function convertToTfidfFromFrequency(rankMap){
	var df = countAttributes(rankMap);
	for (var fkey in rankMap){
		if (rankMap.hasOwnProperty(fkey)){
			rankMap[fkey] = calculateTfidf(rankMap[fkey], df);
		}
	}
	return rankMap;
}

function countAttributes(obj){
	var count = 0;
	for (var fkey in obj){
		if (obj.hasOwnProperty(fkey)) count++
	}
	return count;
}


function binaryProximitySearch(value, array){
  	var low = 0,
    	high = array.length - 1,
      	middle = Math.floor((low + high) /2 ),
      	diff = 0;
      
  	while (low <= high){

    	diff = array[middle] - value;
	    // if near
	    if ( diff === 1 | diff === -1){
      	return array[middle];
    	} else if (diff > 1){
			high = middle - 1;
	    } else if (diff < -1){
			low = middle + 1;

    	// if same (not possible. edge case)
    	} else {
	    	return -1;
	    }
	    middle = Math.floor((low + high)/2);
  	}
  	return -1;
}

function combineLinkAnalysis(res){
	// multiply by link analysis

	db.rankCollection.find({}, function (err, docs){
		var filename, linkrank, lastmodified, tfidf, rank, tfidfScore, linkScore, freshness, freshRatio;

		// for each document
		docs.forEach( function (doc, index){
			filename = doc.file;
			if (queryObj.rankMap[filename]){
				linkrank = doc.rank;
				lastmodified = doc['last-modified'];
				tfidf = queryObj.rankMap[filename];

				// calculate rank and store in rankMap
				// rank = Math.sqrt(linkrank*linkrank + tfidf*tfidf);
				tfidfScore = tfidf / tfidfMax * 0.45;

				linkScore = linkrank * 0.45;

				// freshness - if a page 5 years old, then give 0
				freshRatio = (lastmodified - fiveyears) / (now - fiveyears);
				freshness = (freshRatio > 0) ? freshRatio * 0.1 : 0;
				rank = tfidfScore + linkScore + freshness;

				var o = {
					"tfidfScore": tfidfScore,
					"linkScore": linkScore,
					"freshness": freshness,
					"rank": rank
				}
				queryObj.rankMap[filename] = o;
			}

		})
		console.log(queryObj);
		// convert rankMap to json array to sort by rank values
		var results = convertMapToArray(queryObj.rankMap);

		// sort by ranking
		results.sort(compareFile);
		
		res.json(results);
	});
}

function convertMapToArray(rankMap){
	var results = [];
	var f;
	for (f in rankMap){
		if (rankMap.hasOwnProperty(f) && rankMap[f].tfidfScore){
			var file = {
				'filename' : f,
				'tfidfScore' : rankMap[f].tfidfScore,
				'linkScore' : rankMap[f].linkScore,
				'freshness' : rankMap[f].freshness,
				'rank' : rankMap[f].rank
			}
			results.push(file);	
		}
	}
	return results;
}

function compareFile(a, b){
	return -(a.rank - b.rank);
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

function calculateTfidf (frequency, df){
	var tf = Math.log10(1 + frequency);
	var idf = Math.log10((docCount + 1) / df);
	var tfidf = tf * idf;

	if (tfidf > tfidfMax)
		tfidfMax = tfidf;

	return tfidf;
}