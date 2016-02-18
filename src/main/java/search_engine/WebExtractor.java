package search_engine;

/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * WebExtractor picks up WebPath objects in the queue and extracts the contents, then write them to
 * DB or file, and then removes the WebPath object from the queue
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONObject;
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
   * //for testing purpose public static void main(String[] args) throws UnknownHostException{
   * WebExtractor extractor = new WebExtractor(); try { extractor.run(); } catch (IOException e) {
   * // TODO Auto-generated catch block e.printStackTrace(); } }
   */

  public WebExtractor() throws UnknownHostException {

    this.path = Paths.get(".").toAbsolutePath().normalize().toString() + "/data";
    this.dir = new File(path);
    this.tika = new Tika();
    tika.setMaxStringLength(100 * 1024 * 1024);

    String configLocation = "config.json";
    Util util = new Util();
    JSONObject config = util.jsonParser(configLocation);
    mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
    db = mongoClient.getDB("cs454");
    collection = db.getCollection("hw2");

    // drop collection to start fresh
    collection.drop();

  }

  public void run() throws IOException {
    System.out.println("Start Extracting..");
    visit(dir);
    mongoClient.close();
  }

  public void visit(File file) {
    // Traversal referenced from 'tutorials point - Tika'
    if (file.isDirectory()) {
      String[] children = file.list();
      for (int i = 0; i < children.length; i++) {
        visit(new File(file, children[i]));
      }
    } else if (file.isFile() && file.length() > 0) {

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

        // System.out.println(file.getParentFile().getName() + "/" + file.getName());
        String uri = "";
        uri =
            (filetype.contains("gif")) ? file.getParentFile().getName() : new String(
                (byte[]) Files.getAttribute(Paths.get(file.getPath()), "user:uri"));

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
