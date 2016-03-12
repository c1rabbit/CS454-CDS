/*var _ = require('underscore');

var docs = [
{ 
  term: 'school',
  location:
  [ 
  { filename: 'Chief_Atahm_School_221b.html', index: 1 },
  { filename: 'Children\'s_Discovery_Museum_of_San_Jose_e02a.html', index: 1 },
  { filename: 'Chittarkottai.html', index: 1 },
  { filename: 'Chittoor.html', index: 14 } 
  ] 
},
{ 
  term: 'histori',
  location:
  [ 
  { filename: 'Children\'s_Discovery_Museum_of_San_Jose_e02a.html', index: 2 },
  { filename: 'Children\'s_street_culture.html', index: 2 },
  { filename: 'Chinook_Regional_Hospital_e1f8.html', index: 4 },
  { filename: 'Chittoor.html', index: 1 } 
  ] 
},
{ 
  term: 'hospital',
  location:
  [ 
  { filename: 'Children\'s_street_culture.html', index: 2 },
  { filename: 'Chittoor.html', index: 1 } 
  ] 
}
]

var locations = [];

docs.forEach(function(doc, index){
  locations.push(doc.location);
})

function intersectionObjects2(a, b, areEqualFunction) {
    var results = [];

    for(var i = 0; i < a.length; i++) {
        var aElement = a[i];
        var existsInB = _.any(b, function(bElement) { 
          return areEqualFunction(bElement, aElement); 
        });

        if(existsInB) {
            results.push(aElement);
        }
    }

    return results;
}

function intersectionObjects() {
    var arrays = arguments[0];
    var results = arrays[0];
    
    var arrayCount = arguments.length-1;
    var areEqualFunction = arguments[1];

    for(var i = 1; i < arrays.length ; i++) {
        var array = arrays[i];
        results = intersectionObjects2(results, array, areEqualFunction);
        if(results.length === 0) break;
    }

    return results;
}

var result = intersectionObjects(locations, function(item1, item2){
  return item1.filename === item2.filename;
})

docs.forEach(function(doc){
  console.log(doc.location);
})
console.log('-----');

console.log(result);


*/

var fileMap = {};

var filename = 'Abcd';

if (fileMap[filename]) {
  console.log('hasit');
}else{
  console.log('doesnt');
}

var term = 'free';

fileMap[filename] = {
  terms : [ term ],
  frequency: 4
}

if (fileMap[filename]){
  console.log(fileMap[filename]);
}