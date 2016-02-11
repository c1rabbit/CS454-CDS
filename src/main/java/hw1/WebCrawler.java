/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * WebCrawler object crawls the web with a given starting point and depth and adds all links to a
 * commonly shared queue, where the WebExtractor objects will read and extract information from the
 * links
 */

package hw1;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
  private int depth;
  private List<WebPath> paths;
  private Set<String> visited;

  public WebCrawler(URI uri, int depth, List<WebPath> paths) {
    this.depth = depth;
    this.paths = paths;
    paths.add(new WebPath(uri.toString(), 0));
    System.out.println("Root URI:\t" + uri.toString());
    visited = new HashSet<>();
  }

  public void run() {
    int index = 0;
    WebPath uri = paths.get(0);
    
    // breadth first search
    while (index < paths.size()) {
      uri = paths.get(0); // reset pointer
      Document doc = new Document("temp");
      System.out.println("crawling:\t" + uri.getPath());

      try {
        doc = Jsoup.connect(uri.getPath()).get();

        // queue links
        if (uri.getDepth() < depth) {
          // create group of <a> tag elements from the doc
          Elements links = doc.select("a");

          // add new links to the queue for crawling
          for (Element l : links) {
            if (!visited.contains(l.absUrl("href")) && !l.absUrl("href").isEmpty()) {
              System.out.println((uri.getDepth() + 1) + " queued:\t" + l.absUrl("href"));
              paths.add(new WebPath(l.absUrl("href"), uri.getDepth() + 1));
              visited.add(l.absUrl("href"));
            }
          }
        }
      } catch (Exception e) {
        System.err.println("Not a link");
      } finally {
        visited.add(uri.getPath());
        index++;
      }
    }
  }
}