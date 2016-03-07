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
  @SuppressWarnings("rawtypes")
  private MongoCollection index;
  private List<Page> rankedPages = new ArrayList<>();

  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("Please enter search term!");
      System.exit(0);
    }

    String mongoURL = "mongodb://localhost:27017";
    String database = "cs454";
    String index = "index";
    Tfidf tfidf = new Tfidf(mongoURL, database, index);

    tfidf.rank(args[0]);
  }

  // constructor
  public Tfidf(String mongoURL, String database, String indexCollection) {
    this.mongoClient = new MongoClient(new MongoClientURI(mongoURL));
    this.db = mongoClient.getDatabase(database);
    this.index = db.getCollection(indexCollection);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void rank(String query) {
    if (query == null)
      return;

    double docCount = Util.fileCounter("wiki/en/articles/c/h");
    String[] queries = query.split(" ");
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

    // move map elements to rankedPages list
    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Double> pair = (Map.Entry<String, Double>) it.next();
      rankedPages.add(new Page(pair.getKey(), pair.getValue()));
      it.remove();
    }

    // sort the list
    Collections.sort(rankedPages);

    for (Page p : rankedPages)
      System.out.println(p.getName() + ": " + p.getRank());
  }
}
