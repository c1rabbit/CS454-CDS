var express = require('express');
var app = express();
var mongojs = require('mongojs');
var stem = require('stem-porter');
var db = mongojs('cs454', ['index', 'rankCollection']); // mongodb database, and collections
var bodyParser = require('body-parser');
var docCount;

app.use(express.static(__dirname + "/public"));
app.use(bodyParser.json());

var documentCount = function () {
	db.rankCollection.find(function (err, doc) {
			docCount = doc.length;
		});
}

documentCount();

app.get('/searchresults/:queries', function (req, res) {
	var queries = parseAndStemQuery(req.params.queries);
	var calculateIdf = queries.length > 1;
	var tf, weight, temp;
	var idf = 1;
	var rankMap = {};

	for (var i = 0; i < queries.length; i++) {
		// get object with the term
		db.index.findOne({term: queries[i]}, function (err, doc) {
			var locations = doc.location;

			for (var i = 0; i < locations.length; i++) {
				var filename = locations[i].filename;
				var indexes = locations[i].index;
				tf = Math.log10(1 + indexes.length); // term frequency

				if (calculateIdf === true) {
					idf = Math.log10(docCount / locations.length);

					if (idf === 0)
						idf = 1;
				}

				weight = tf * idf;

				if (filename in rankMap)
					temp = rankMap[filename] + weight;
				else
					temp = weight;

				rankMap[filename] = temp;

//				console.log(filename + ': ' + temp)
			}
		});
	}

	console.log(rankMap);
});

var parseAndStemQuery = function (input) {
	var queryList = input.split(' ');

	for (var i = 0; i < queryList.length; i++)
		queryList[i] = stem(queryList[i].toLowerCase());

	return queryList;
}

app.listen(3000, function () {
	console.log('Server is running...');
});