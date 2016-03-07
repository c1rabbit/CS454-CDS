package search_engine;

/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * Main object is the starting point of the application. It reads parameters from the configuration
 * file and calls the components to perform crawling.
 */

import indexer.Indexer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.json.simple.JSONObject;

public class Main {
  public static void main(String[] args) throws IOException, URISyntaxException {
    // parameters necessary to run the program
    Util util = new Util();
    String configLocation = "config.json";
    String downloadPath = "./data";
    String mongoURL = "mongodb://localhost:27017";
    String database = "cs454";
    String indexCollection = "index";
    String outboundLinkCollection = "outboundLinks";
    String localSampleDataPath = "wiki/en/articles/c/h/i";

    // legacy parameters
    int depth = 2;
    String baseCollection = "hw2";
    URI uri = new URI("http://samskim.com/");

    // component booleans
    boolean crawl = false;
    boolean extract = false;

    // new parameter handling (always read from config.json)
    try {
      JSONObject config = util.jsonParser(configLocation);
      downloadPath = (String) config.get("downloadPath");
      mongoURL = (String) config.get("mongoURL");
      database = (String) config.get("database");
      indexCollection = (String) config.get("indexCollection");
      outboundLinkCollection = (String) config.get("outboundLinkCollection");
      localSampleDataPath = (String) config.get("localSampleDataPath");

      // legacy parameters
      depth = Integer.parseInt((String) config.get("depth"));
      baseCollection = (String) config.get("baseCollection");
      uri = new URI((String) config.get("uri"));

      // component booleans
      crawl = (boolean) config.get("crawl");
      extract = (boolean) config.get("extract");
    } catch (Exception e) {
      System.err.println("Unable to locate config.json or parse parameters!");
    }

    // hw 2 parameter handling, overwrite config.json
    if (args.length > 0) { // if there are parameters, read from parameters
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

    // run crawler if desired
    if (crawl) {
      // creating URI queue for crawling
      LinkedList<WebPath> paths = new LinkedList<>();

      if (util.deleteDir(new File(downloadPath)))
        System.out.println("Folder deleted!");

      util.createDir(downloadPath);
      WebCrawler crawler = new WebCrawler(uri, depth, paths, downloadPath);
      crawler.run();
      System.out.println("Finished Crawling");
    }

    // run extractor if desired
    if (extract) {
      WebExtractor extractor = new WebExtractor(mongoURL, database, baseCollection);
      extractor.run();
      System.out.println("Finished Extracting");
    }

    // launch indexer
    Indexer indexer =
        new Indexer(mongoURL, database, indexCollection, outboundLinkCollection, localSampleDataPath);
    indexer.run();
  }
}
