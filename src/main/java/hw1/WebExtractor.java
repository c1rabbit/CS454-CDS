/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * WebExtractor picks up WebPath objects in the queue and extracts the contents, then write them to
 * DB or file, and then removes the WebPath object from the queue
 */

package hw1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebExtractor {
  private String dbPath;
  private LinkedList<WebPath> paths;

  public WebExtractor(String dbPath, LinkedList<WebPath> paths) {
    this.dbPath = dbPath;
    this.paths = paths;

    // initializes data file
    try {
      FileWriter fw = new FileWriter(dbPath, false);
      fw.close();
    } catch (IOException e) {
      File data = new File("data");
      data.mkdir();
      @SuppressWarnings("unused")
      File newfile = new File(dbPath);
      // e.printStackTrace();
      System.err.println("new directory and file created");
    }
  }

  public void run() {
    try {
      while (paths.size() > 0) {
        WebPath uri = paths.get(0);
        Document doc = Jsoup.connect(uri.getPath()).get();
        // save content
        Element body = doc.body();
        parse(body);
        System.out.println(uri.getDepth() + " extracted:\t" + uri.getPath());
        paths.remove(0);
      }
    } catch (Exception e) {
      System.err.println("Not a link");
    }
  }

  // creates JSON object for each crawled document
  @SuppressWarnings("unchecked")
  private void parse(Element content) throws IOException {
    JSONObject json = new JSONObject();

    json.put("uri", content.baseUri());
    json.put("content", content.text());
    writeJSON(json);
  }

  private void writeJSON(JSONObject json) {
    try {
      FileWriter fw = new FileWriter(dbPath, true); // the true will
      // append the new data
      fw.write(json.toString() + "\n");// appends the string to the file
      fw.close();
    } catch (IOException ioe) {
      System.err.println("IOException: " + ioe.getMessage());
    }
  }
}
