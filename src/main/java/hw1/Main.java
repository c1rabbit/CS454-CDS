/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * Main object is the starting point of the application. It reads parameters from the configuration
 * file and calls the components to perform crawling.
 */

package hw1;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.json.simple.JSONObject;

public class Main {
  public static void main(String[] args) throws IOException, URISyntaxException {
    Util util = new Util();
    String configLocation = "C://svn//classes//2016winter//cs454//CS454-CDS//config.json";
    JSONObject config = util.jsonParser(configLocation);

    URI uri = new URI((String) config.get("startingUrl"));
    int depth = Integer.parseInt((String) config.get("depth"));
    String dbPath = (String) config.get("dbPath");
    LinkedList<WebPath> paths = new LinkedList<>();

    WebCrawler crawler = new WebCrawler(uri, depth, paths);
    crawler.run();
    WebExtractor extractor = new WebExtractor(dbPath, paths);
    extractor.run();
    System.out.println("Finished Crawling");
  }
}
