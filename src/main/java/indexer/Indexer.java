package indexer;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static java.util.Arrays.asList;
import search_engine.Util;

public class Indexer {
  private String path;
  private File dir;
  private MongoClient mongoClient;
  private MongoDatabase db;
  private MongoCollection indexCollection;
  private MongoCollection outboundLinkCollection;
  private Map<String, HashSet<String>> termLocations;

  public Indexer(String mongoURL, String database, String indexCollection,
      String outboundLinkCollection, String path) throws UnknownHostException {
    this.path = Paths.get(".").toAbsolutePath().normalize().toString() + path;
    this.dir = new File(path);
    this.termLocations = new HashMap<>();
    this.mongoClient = new MongoClient(new MongoClientURI(mongoURL));
    this.db = mongoClient.getDatabase(database);
    this.indexCollection = db.getCollection(indexCollection);
    this.indexCollection.drop();
    this.outboundLinkCollection = db.getCollection(outboundLinkCollection);
    this.outboundLinkCollection.drop();
  }

  public static void main(String[] args) throws IOException {
    String mongoURL = "mongodb://localhost:27017";
    String database = "cs454";
    String indexCollection = "index";
    String outboundLinkCollection = "outboundLinks";
    String path = "/wiki";
    Indexer indexer =
        new Indexer(mongoURL, database, indexCollection, outboundLinkCollection, path);

    // File dir = new File("wiki");
    String url = "wiki/";
    File root = new File(url);

    long startTime = System.nanoTime();   
        
    indexer.run(root);
    indexer.closeConnection();
    long estimatedTime = System.nanoTime() - startTime;
    System.out.println("Indices created successfully.");
    System.out.println("Process took " + estimatedTime/1000000000.0);
    

  }
  
  public void closeConnection(){
    mongoClient.close();
  }
  
  public void run(File root) throws IOException {
    visit(root);
  }

  public void visit(File file) throws IOException {
    // Traversal referenced from 'tutorials point - Tika'
    if (file.isDirectory()) {
      String[] children = file.list();
      for (int i = 0; i < children.length; i++) {
        visit(new File(file, children[i]));
      }
    } else if (file.isFile() && file.length() > 0) {
      org.jsoup.nodes.Document doc = Jsoup.parse(file, "utf-8");

      // strip text out of document
      String text = doc.text();

      // create outbound link index
      makeOutboundLinkIndex(file.getName(), doc);
      System.out.println("Outbound Link Index created for: " + file.getName());

      // create index
      System.out.println("Writing Index for: " + file.getName());
      makeIndex(file.getName(), text);
      System.out.println("Finished writing index for:  " + file.getName());
    }
  }

  public void makeOutboundLinkIndex(String filename, org.jsoup.nodes.Document doc) {
    // get links
    Elements links = doc.select("a");
    Set<String> set = new HashSet<>();

    // get all outgoing links
    for (Element link : links) {
      String address = link.attr("href");
      if (address.trim().length() > 0) {
        int lastSlashIndex = address.lastIndexOf('/') + 1;
        String linkname = address.substring(lastSlashIndex);
        if (linkname.trim().length() > 0 && linkname.contains(".html")){
			try {
				set.add(java.net.URLDecoder.decode(linkname, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
      }
    }
        
    Document mongodoc = new Document();
    mongodoc.append("file", filename);
    mongodoc.append("pages", set);
    outboundLinkCollection.insertOne(mongodoc);

  }

  public void makeIndex(String filename, String text) {
    Scanner scan = new Scanner(text);
    int n = 0;
    Util util = new Util();

    while (scan.hasNext()) {
      String word = scan.next();
      // if scanned word is composed of alphabetic characters and no numeric values
      if (word.matches(".*\\w+.*") && !word.matches(".*\\d+.*")) {

        // lowercase and trim the word
        word = word.toLowerCase().trim();

        // if the word contains possessive get rid of it
        if (word.contains("'s"))
          word = word.replace("'s", "");

        // trim all special characters
        String trimmed = word.replaceAll("[^a-zA-Z-/]", "");

        Queue<String> queue = new LinkedList<>();

        // a word might contain / or - like this: tax-deductible. Then split the words
        if (trimmed.contains("/")) {
          String[] splitSlash = trimmed.split("/");
          for (String s : splitSlash) {
            queue.add(s);
          }
        }
        if (trimmed.contains("-")) {
          String[] splitSlash = trimmed.split("-");
          for (String s : splitSlash) {
            queue.add(s);
          }
        }

        // if the word doesn't contain / or -, then proceed
        if (queue.isEmpty()) {
          queue.add(trimmed);
        }

        while (!queue.isEmpty()) {
          String poll = queue.poll();

          // if the word is not a single character and
          if (poll.length() > 1 && !util.isStopWord(poll)) {
            String stemmed = util.stem(poll);

            Document termDoc = new Document();
            Document fileDoc = new Document();
            ArrayList<Integer> indexes = new ArrayList<>();

            // if index doesn't contain the word, make new
            if (!termLocations.containsKey(stemmed)) {
              // to keep track of terms
              HashSet<String> set = new HashSet<>();
              set.add(filename);
              termLocations.put(stemmed, set);

              indexes.add(n);
              fileDoc.append("filename", filename);
              fileDoc.append("index", indexes);

              termDoc.append("term", stemmed);
              termDoc.append("location", asList(fileDoc));
              indexCollection.insertOne(termDoc);

              // if index does contain the word
            } else {
              FindIterable<Document> termIterable =
                  indexCollection.find(new Document("term", stemmed));
              termDoc = termIterable.first();

              ArrayList<Document> locationDocs = (ArrayList<Document>) termDoc.get("location");

              // for keeping track
              HashSet<String> locations = termLocations.get(stemmed);

              // if same file exists
              if (locations.contains(filename)) {
                fileDoc = findFileDoc(locationDocs, filename);
                indexes = (ArrayList<Integer>) fileDoc.get("index");
                indexes.add(n);
                
              } else {
                locations.add(filename);
                termLocations.put(stemmed, locations);
                
                indexes.add(n);
                fileDoc.append("filename", filename);
                fileDoc.append("index", indexes);
                locationDocs.add(fileDoc);
              }
              indexCollection.updateOne(new Document("term", stemmed),
                  new Document("$set", new Document("location", locationDocs)));


            }

          }
          n++;
        }

        // if word ends with comma or period, it means the word after is not related, so increment
        // index
        if (word.matches(".*([.,])$"))
          n++;
      } else {
        // if not a word - ex. numbers, special characters - just increment index
        n++;
      }
    }scan.close();

  }

  public Document findFileDoc(ArrayList<Document> documents, String filename) {
    for (Document d : documents) {
      if (d.get("filename").equals(filename)) {
        return d;
      }
    }
    return null;
  }

  public static void printIndex(Map<String, Map<String, ArrayList<Integer>>> index) {
    for (String word : index.keySet()) {
      Map<String, ArrayList<Integer>> map = index.get(word);
      System.out.println(word + ": ");
      for (String file : map.keySet()) {
        ArrayList<Integer> indexes = map.get(file);
        System.out.println("at file: " + file);

        for (int i : indexes) {
          System.out.print(i + " ");
        }
        System.out.println();
      }
      System.out.println();
    }
  }


}
