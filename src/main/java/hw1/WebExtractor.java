/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * WebExtractor picks up WebPath objects in the queue and extracts the contents, then write them to
 * DB or file, and then removes the WebPath object from the queue
 */

package hw1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.jpeg.JpegParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.SAXException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class WebExtractor {
  private String path;
  private File dir;
  private Tika tika;
  private MongoClient mongoClient;
  private DB db;
  private DBCollection collection;
  
  /*
  //for testing purpose
  public static void main(String[] args) throws UnknownHostException{
    WebExtractor extractor = new WebExtractor();
    try {
      extractor.run();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  */
  
  public WebExtractor() throws UnknownHostException {

    this.path = Paths.get(".").toAbsolutePath().normalize().toString() + "/data";
    this.dir = new File(path);
    this.tika = new Tika();
    
    String configLocation = "config.json";
    Util util = new Util();
    JSONObject config = util.jsonParser(configLocation);
    mongoClient = new MongoClient(new MongoClientURI((String) config.get("mongoURL")));
    db = mongoClient.getDB((String) config.get("database"));
    collection = db.getCollection((String) config.get("collection"));
    
    // drop collection to start fresh
    collection.drop();

  }

  public void run() throws IOException {
    System.out.println("Start Extracting..");
    visit(dir);
    mongoClient.close();    
  }

  public void visit(File file) {

    if (file.isDirectory()) {
      String[] children = file.list();
      for (int i = 0; i < children.length; i++) {
        visit(new File(file, children[i]));
      }
    } else if (file.isFile()) {

      try {
        String filetype = tika.detect(file);

        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(file);
        ParseContext context = new ParseContext();
        
        parser.parse(inputstream, handler, metadata, context);
        
        // mongodb document object
        DBObject webpage = new BasicDBObject();

        // getting the list of all meta data elements
        String[] metadataNames = metadata.names();

        for (String name : metadataNames) {
          webpage.put(name, metadata.get(name));
        }
        
        String uri = "";
        uri = (filetype.contains("image")) ? file.getParentFile().getName() : new String((byte[]) Files.getAttribute(Paths.get(file.getPath()), "user:uri"));
        
        webpage.put("uri", uri);
        System.out.println("extracting uri: " + uri + " from file: " + file.getPath());
        
        webpage.put("filepath", file.getPath());
        webpage.put("size_in_kb", file.length() / 1024);
        String content = tika.parseToString(file);
        webpage.put("content", content);
        collection.insert(webpage);

      } catch (IOException e) {
        e.printStackTrace();
      }
      
      catch (TikaException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } 
    }
  }

 
  
}
