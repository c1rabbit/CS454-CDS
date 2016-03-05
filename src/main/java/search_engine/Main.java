package search_engine;

/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * Main object is the starting point of the application. It reads parameters from the configuration
 * file and calls the components to perform crawling.
 */

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.json.simple.JSONObject;

public class Main {
  public static void main(String[] args) throws IOException, URISyntaxException {
    Util util = new Util();
    String configLocation = "config.json";
    URI uri = new URI("localhost");
    int depth = 2;
    boolean extract = false;
    String downloadPath = "./data";
    String mongoURL = "mongodb://localhost:27017";
    String database = "cs454";
    String collectionName = "hw2";

    if (args.length == 0) { // if no parameter, read from config.json
      try {
        JSONObject config = util.jsonParser(configLocation);
        mongoURL = (String) config.get("mongoURL");
        database = (String) config.get("database");
        collectionName = (String) config.get("collectionName");
        // variables from either config file or command parameters
        uri = new URI((String) config.get("startingUrl"));
        depth = Integer.parseInt((String) config.get("depth"));
        String temp = (String) config.get("extract");
        if (temp.equals("true"))
          extract = true;
      } catch (Exception e) {
        System.err.println("Config.json not found!");
      }
    } else { // if there are parameters, read from parameters
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-d")) {
          if (args[i + 1] != null && util.isNum(args[i + 1]) && args[i + 1].charAt(0) != '-') {
            depth = Integer.parseInt(args[i + 1]);
            i++;
          }
        } else if (args[i].equals("-u")) {
          if (args[i + 1] != null && args[i + 1].charAt(0) != '-') {
            uri = new URI(args[i + 1]);
            i++;
          }
        } else if (args[i].equals("-e")) {
          extract = true;
        }
      }
    }

    // creating URI queue for crawling
    LinkedList<WebPath> paths = new LinkedList<>();

    // run crawler
    if (util.deleteDir(new File(downloadPath)))
      System.out.println("Folder deleted!");
    util.createDir(downloadPath);
    WebCrawler crawler = new WebCrawler(uri, depth, paths, downloadPath);
    crawler.run();
    System.out.println("Finished Crawling");

    // run extractor if desired
    if (extract) {
     // WebExtractor extractor = new WebExtractor(mongoURL, database, collectionName);
    //  extractor.run();
      System.out.println("Finished Extracting");
    }
  }
}
