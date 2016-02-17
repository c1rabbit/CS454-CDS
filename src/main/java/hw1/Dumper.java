package main.java.hw1;

import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.json.simple.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class Dumper {
  private static FileWriter fw;
  
  public static void main(String[] args) {
    System.out.println("Extracter Starting from JAR..");
    String path = "dump.json";
    String configLocation = "config.json";
    Util util = new Util();
    JSONObject config = util.jsonParser(configLocation);
    MongoClient mongoClient;
        
    try {
   
      fw = new FileWriter(path);
      mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
      DB db = mongoClient.getDB("cs454");
      DBCollection collection = db.getCollection("hw2");
      DBObject query = new BasicDBObject();
      DBCursor cursor = collection.find(query);

      Iterator<DBObject> iterator = cursor.iterator();
      while (iterator.hasNext()) {
        DBObject doc = iterator.next();
        writeToFile(doc);
      }
      
      fw.close();
      System.out.println("Data dumped to " + path);
      
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } 
    

  }
  
  private static void writeToFile(DBObject doc) {
    JSONObject json = new JSONObject();
    
    for (String s: doc.keySet()){
      json.put(s, doc.get(s));
    }
        
    try {
      fw.write(json.toJSONString() + "\n");
      fw.flush();
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
