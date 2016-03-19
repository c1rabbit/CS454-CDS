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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebCrawler {
  private int depth;
  private List<WebPath> paths;
  private Set<Object> visited;
  private String downloadPath;
  private int threads;
  private long timestamp;
  private int waitTime;

  public WebCrawler(URI uri, int depth, List<WebPath> paths, String downloadPath, int threads, int waitTime) {
    this.depth = depth;
    this.paths = Collections.synchronizedList(paths);
    this.downloadPath = downloadPath;
    paths.add(new WebPath(uri.toString(), 0));
    System.out.println("Root URI:\t" + uri.toString());
    this.visited = Collections.synchronizedSet(new HashSet<>());
    this.threads = threads;
    this.waitTime = waitTime;
    this.timestamp = System.currentTimeMillis();
  }

  public void run() {
    boolean finished = false;

    ExecutorService es = Executors.newCachedThreadPool();
    for (int i = 0; i < threads; i++)
      es.execute(new Crawler(paths, visited, downloadPath, depth));
    es.shutdown();
    try {
      finished = es.awaitTermination(waitTime, TimeUnit.MINUTES);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (finished) {
      long timeElapsed = System.currentTimeMillis() - this.timestamp;
      System.out.println("Finished in: " + timeElapsed / 1000 / 60 + " min " + (timeElapsed / 1000)
          % 60 + " sec");
    }
  }
}
