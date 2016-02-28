package rankers;

import java.util.ArrayList;
import java.util.Iterator;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class TfIdf {
  private MongoClient mongoClient;
  private MongoDatabase db;
  private MongoCollection index;

  // for Di
  public TfIdf(String mongoURL, String database, String index) {
    
    this.mongoClient = new MongoClient(new MongoClientURI(mongoURL));
    this.db = mongoClient.getDatabase(database);
    this.index = db.getCollection(index);
     
  }

  public static void main(String[] args) {
    String mongoURL = "mongodb://localhost:27017";
    String database = "cs454";
    String index = "index";
    TfIdf tfidf = new TfIdf(mongoURL, database, index);
    
    tfidf.printIndex();
  }
  
  public void printIndex(){
    FindIterable<Document> iterable = index.find();
    Iterator<Document> termIterator = iterable.iterator();
    while (termIterator.hasNext()){
      Document termDoc = termIterator.next();
      String term = termDoc.getString("term");
      
      System.out.println("term: " + term);
      ArrayList<Document> locationDocs = (ArrayList<Document>) termDoc.get("location");
      for (Document locationDoc: locationDocs){
        String filename = locationDoc.getString("filename");
        ArrayList<Integer> indices = (ArrayList<Integer>) locationDoc.get("index");
        System.out.println("filename: " + filename);
        for (Integer n : indices) {
          System.out.print(n + " ");
        }
        System.out.println();
      }     
      System.out.println();
    }
  }

}
