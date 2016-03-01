package rankers;

import java.util.ArrayList;
import java.util.Iterator;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

// this is for calvin
public class LinkAnalysis {
  private MongoClient mongoClient;
  private MongoDatabase db;
  private MongoCollection outboundLinkCollection;

  public LinkAnalysis(String mongoURL, String database, String outboundLinkCollection) {
    
    this.mongoClient = new MongoClient(new MongoClientURI(mongoURL));
    this.db = mongoClient.getDatabase(database);
    this.outboundLinkCollection = db.getCollection(outboundLinkCollection);
     
  }

  public static void main(String[] args) {
    String mongoURL = "mongodb://localhost:27017";
    String database = "cs454";
    String outboundLinkCollection = "outboundLinks";
    LinkAnalysis la = new LinkAnalysis(mongoURL, database, outboundLinkCollection);
    
    la.printOutboundLinks();
  }
  
  public void printOutboundLinks(){
    
    FindIterable<Document> iterable = outboundLinkCollection.find();
    Iterator<Document> iterator = iterable.iterator();
    while (iterator.hasNext()){
      Document d = iterator.next();
      String filename = (String) d.get("file");
      ArrayList<String> webpageList = (ArrayList<String>) d.get("pages");
      System.out.println("filename: ");
      for (String page: webpageList){
        System.out.print(page + ", ");
      }
      System.out.println();
    }
    
    
  }
  
  



}
