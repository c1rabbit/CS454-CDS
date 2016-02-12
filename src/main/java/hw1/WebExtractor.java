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
import java.util.LinkedList;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
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

  public static void main(String[] args) throws UnknownHostException {
    System.out.println("Extracter Starting from JAR..");
    WebExtractor extractor = new WebExtractor();

    extractor.run();
  }

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

  public void run() {

    visit(dir, tika);

    mongoClient.close();
  }

  public void visit(File file, Tika tika) {


    if (file.isDirectory()) {
      String[] children = file.list();
      for (int i = 0; i < children.length; i++) {
        visit(new File(file, children[i]), tika);
      }
    } else if (file.isFile()) {

      try {
        
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

        String uri = new String(
          (byte[]) Files.getAttribute(Paths.get("./data/" + file.getName()), "user:uri"));
        webpage.put("uri", uri);

        
        webpage.put("filepath", file.getPath());
        webpage.put("size_in_kb", file.length()/1024);

        String content = tika.parseToString(file);
        webpage.put("content", content);

        collection.insert(webpage);

        System.out.println("uri: " + uri);
        System.out.println();

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();

      } catch (TikaException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
