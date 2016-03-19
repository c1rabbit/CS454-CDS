package search_engine;

import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler extends Thread implements Runnable {
  private List<WebPath> paths;
  private Set<Object> visited;
  private String dataFolder;
  private int depth;

  public Crawler(List<WebPath> paths, Set<Object> visited, String downloadPath, int depth) {
    this.paths = paths;
    this.visited = visited;
    this.dataFolder = "./" + downloadPath + "/";
    this.depth = depth;
  }

  @Override
  public void run() {
    // breadth first search
    while (!paths.isEmpty()) {
      WebPath uri = paths.get(0);

      uri = paths.get(0); // reset pointer
      Document doc = new Document("temp");
      System.out.println("crawling:\t" + uri.getPath());

      try {
        doc = Jsoup.connect(uri.getPath()).get();

        String randomName = Util.filenameStripper(uri.getPath());
        Files.write(Paths.get(dataFolder + randomName), doc.html().getBytes());
        Files.setAttribute(Paths.get(dataFolder + randomName), "user:uri", uri.getPath()
            .getBytes());
        
        // set last modified date if any in long
        URL url = new URL(uri.getPath());
        URLConnection connection = url.openConnection();
        long modified = connection.getLastModified();
        Files.setAttribute(Paths.get(dataFolder + randomName), "basic:lastModifiedTime",
            FileTime.fromMillis(modified));

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
        if (paths.size() > 0)
          paths.remove(0);
        visited.add(uri.getPath());
      }
    }
  }
}
