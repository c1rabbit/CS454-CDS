/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * Main object is the starting point of the application. It reads parameters from the configuration
 * file and calls the components to perform crawling.
 */

package hw1;

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
    JSONObject config = util.jsonParser(configLocation);
    URI uri = new URI("localhost");
    int depth = 2;
    boolean extract = false;
    String downloadPath = "./data";

    if (args.length == 0) { // if no parameter, read from config.json
      uri = new URI((String) config.get("startingUrl"));
      depth = Integer.parseInt((String) config.get("depth"));
      String temp = (String) config.get("extract");
      if (temp.equals("true"))
        extract = true;
    } else { // if there are parameters, read from parameters
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-d")) {
          if (args[i + 1] != null && isNum(args[i + 1]) && args[i + 1].charAt(0) != '-') {
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
    String dbPath = (String) config.get("dbPath");
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
      WebExtractor extractor = new WebExtractor(dbPath, paths);
      extractor.run();
      System.out.println("Finished Extracting");
    }
  }

  private static boolean isNum(String s) {
    return s.matches("[-+]?\\d*\\.?\\d+");
  }
}