package search_engine;

/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * WebCrawler object crawls the web with a given starting point and depth and adds all links to a
 * commonly shared queue, where the WebExtractor objects will read and extract information from the
 * links
 */

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebCrawler {
  private int depth;
  private List<WebPath> paths;
  private Set<Object> visited;
  private String downloadPath;
  private int threads;
  private long timestamp;

  public WebCrawler(URI uri, int depth, List<WebPath> paths, String downloadPath) {
    this.depth = depth;
    this.paths = Collections.synchronizedList(paths);
    this.downloadPath = downloadPath;
    paths.add(new WebPath(uri.toString(), 0));
    System.out.println("Root URI:\t" + uri.toString());
    this.visited = Collections.synchronizedSet(new HashSet<>());
    threads = 8;
    this.timestamp = System.currentTimeMillis();
  }

  public void run() {
    for (int i = 0; i < threads; i++) {
      Crawler c = new Crawler(paths, visited, downloadPath, depth);
      c.start();
      try {
        Thread.sleep(1000);
      } catch (Exception e) {
        System.err.println("Failed to sleep for 1 second!");
      }
    }
    
    long timeElapsed = System.currentTimeMillis() - this.timestamp;
    System.out.println("Finished in: " + timeElapsed / 1000 / 60 + " min " + (timeElapsed / 1000)
        % 60 + " sec");
  }
}
