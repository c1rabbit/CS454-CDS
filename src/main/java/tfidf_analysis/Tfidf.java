package tfidf_analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import search_engine.Util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Tfidf {
  private MongoClient mongoClient;
  private MongoDatabase db;
  private String localSampleDataPath;
  @SuppressWarnings("rawtypes")
  private MongoCollection index;
  @SuppressWarnings("rawtypes")
  private MongoCollection rank;
  private List<Page> rankedPages = new ArrayList<>();
  double tfidfRatio;
  double linkRatio;

  // constructor
  public Tfidf(String mongoURL, String database, String localSampleDataPath, String indexCollection, String rankCollection,
      double tfidfRatio, double linkRatio) {
    this.mongoClient = new MongoClient(new MongoClientURI(mongoURL));
    this.db = mongoClient.getDatabase(database);
    this.localSampleDataPath = localSampleDataPath;
    this.index = db.getCollection(indexCollection);
    this.rank = db.getCollection(rankCollection);
    this.tfidfRatio = tfidfRatio;
    this.linkRatio = linkRatio;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void rank(String query) {
    if (query == null)
      return;

    double docCount = Util.fileCounter(localSampleDataPath);
    String[] queries = prepareQueries(query);
    boolean calculateIdf = queries.length > 1;
    String filename = "";
    double tf = 1;
    double idf = 1;
    double weight = 0;
    double temp = 0;
    Map<String, Double> map = new HashMap<>();

    for (String s : queries) {
      // get the object with the term
      FindIterable<Document> iterable = index.find(new Document("term", s));
      Document item = iterable.first();

      // calculate weight from term object
      if (item != null) {
        ArrayList<Document> locations = (ArrayList<Document>) item.get("location");

        for (Document d : locations) {
          filename = d.getString("filename");
          ArrayList<Integer> termIndexes = (ArrayList<Integer>) d.get("index");
          tf = Math.log10(1 + termIndexes.size()); // term frequency
          if (calculateIdf)
            idf = Math.log10(docCount / locations.size()); // inverse document frequency
          if (idf == 0) // if the term is in every single document, make idf ineffective
            idf = 1;
          weight = tf * idf;

          if (map.containsKey(filename))
            temp = map.get(filename) + weight;
          else
            temp = weight;

          map.put(filename, temp);
        }
      }
    }

    // exit program if there's no search result
    if (map.size() == 0) {
      System.out.println("No record found!");
      System.exit(0);
    }

    // incorporate link analysis weight and move ranked pages to rankedPages list
    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Double> pair = (Map.Entry<String, Double>) it.next();

      // incorporate link analysis weight
      // get object with filename
      FindIterable<Document> iterable = rank.find(new Document("file", pair.getKey()));
      Document item = iterable.first();
      // add link analysis ranking to tfidf ranking
      double linkRank = (double) item.get("rank");

      pair.setValue((pair.getValue() * tfidfRatio) + (linkRank * linkRatio));

      rankedPages.add(new Page(pair.getKey(), pair.getValue()));
      it.remove();
    }

    // sort the list
    Collections.sort(rankedPages);

    for (Page p : rankedPages)
      System.out.println(p.getName() + ": " + p.getRank());
  }
  
  private String[] prepareQueries(String input) {
	  if (input.length() == 0)
		  return null;
	  
	  String[] queryArray = input.split(" ");
	  
	  for (int i = 0; i < queryArray.length; i++) {
		  queryArray[i] = Util.stem(queryArray[i].toLowerCase());
	  }
	  
	  return queryArray;
  }
}
