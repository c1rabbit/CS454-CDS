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

import link_analysis.LinkAnalysis;

import org.json.simple.JSONObject;

import tfidf_analysis.Tfidf;

public class Main {
  public static void main(String[] args) throws IOException, URISyntaxException {
    // parameters necessary to run the program
    String configLocation = "config.json";
    String downloadPath = "./data";
    String mongoURL = "mongodb://localhost:27017";
    String database = "cs454";
    String indexCollection = "index";
    String outboundLinkCollection = "outboundLinks";
    String localSampleDataPath = "wiki/en/articles/c/h/i";
    String rankCollection = "rankCollection";
    int iterations = 3;
    double tfidfRatio = 1.0;
    double linkRatio = 1.5;

    // legacy parameters
    int depth = 2;
    // String baseCollection = "hw2";
    URI uri = new URI("http://samskim.com/");

    // component booleans
    boolean runCrawl = false;
    boolean runExtract = false;
    boolean runIndex = false;
    boolean runDebugMode = false;
    boolean runLinkAnalysis = false;
    boolean runTfidf = false;

    // new parameter handling (always read from config.json)
    try {
      JSONObject config = Util.jsonParser(configLocation);
      downloadPath = (String) config.get("downloadPath");
      mongoURL = (String) config.get("mongoURL");
      database = (String) config.get("database");
      indexCollection = (String) config.get("indexCollection");
      outboundLinkCollection = (String) config.get("outboundLinkCollection");
      localSampleDataPath = (String) config.get("localSampleDataPath");
      rankCollection = (String) config.get("rankCollection");
      iterations = Integer.parseInt(config.get("iterations").toString());
      tfidfRatio = Double.parseDouble(config.get("tfidfRatio").toString());
      linkRatio = Double.parseDouble(config.get("linkRatio").toString());

      // legacy parameters
      depth = Integer.parseInt((String) config.get("depth"));
      // baseCollection = (String) config.get("baseCollection");
      uri = new URI((String) config.get("uri"));

      // component booleans
      runCrawl = (boolean) config.get("runCrawl");
      runExtract = (boolean) config.get("runExtract");
      runIndex = (boolean) config.get("runIndex");
      runDebugMode = (boolean) config.get("runDebugMode");
      runLinkAnalysis = (boolean) config.get("runLinkAnalysis");
      runTfidf = (boolean) config.get("runTfidf");
    } catch (Exception e) {
      System.err.println("Unable to locate config.json or parse parameters!");
      // e.printStackTrace();
    }

    // hw 2 parameter handling, overwrite config.json
    // if (args.length > 0) { // if there are parameters, read from parameters
    // for (int i = 0; i < args.length; i++) {
    // if (args[i].equals("-d")) {
    // if (args[i + 1] != null && Util.isNum(args[i + 1]) && args[i + 1].charAt(0) != '-') {
    // depth = Integer.parseInt(args[i + 1]);
    // i++;
    // }
    // } else if (args[i].equals("-u")) {
    // if (args[i + 1] != null && args[i + 1].charAt(0) != '-') {
    // uri = new URI(args[i + 1]);
    // i++;
    // }
    // } else if (args[i].equals("-e")) {
    // extract = true;
    // }
    // }
    // }

    // run crawler if desired
    if (runCrawl) {
      // creating URI queue for crawling
      LinkedList<WebPath> paths = new LinkedList<>();

      if (Util.deleteDir(new File(downloadPath)))
        System.out.println("Folder deleted!");

      Util.createDir(downloadPath);
      WebCrawler crawler = new WebCrawler(uri, depth, paths, downloadPath);
      crawler.run();
      System.out.println("Finished Crawling");
    }

    // run extractor if desired
    if (runExtract) {
      // WebExtractor extractor = new WebExtractor(mongoURL, database, baseCollection);
      // extractor.run();
      System.out.println("Finished Extracting");
    }

    // run indexer if desired
    if (runIndex) {
      Indexer indexer =
          new Indexer(mongoURL, database, indexCollection, outboundLinkCollection,
              localSampleDataPath);
      indexer.run();
    }

    // run link analysis if desired
    if (runLinkAnalysis) {
      LinkAnalysis linkAnalysis =
          new LinkAnalysis(mongoURL, database, outboundLinkCollection, rankCollection, iterations,
              runDebugMode);
      linkAnalysis.run();
    }

    // run tfidf if desired
    if (runTfidf) {
      if (args.length == 0) {
        System.err.println("Please enter search terms!");
        System.exit(0);
      }
      Tfidf tfidf =
          new Tfidf(mongoURL, database, localSampleDataPath, indexCollection, rankCollection, tfidfRatio, linkRatio);
      tfidf.rank(args[0]);
    }
  }
}
