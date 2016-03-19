package indexer;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import search_engine.Util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Indexer {
  private File root;
  private MongoClient mongoClient;
  private MongoDatabase db;
  private MongoCollection<Document> indexCollection;
  private MongoCollection<Document> outboundLinkCollection;
  private Map<String, Term> terms; // memory
  private long timestamp;

  public Indexer(String mongoURL, String database, String indexCollection,
      String outboundLinkCollection, String path) throws UnknownHostException {
    this.terms = new HashMap<String, Term>();
    this.timestamp = System.currentTimeMillis();
    this.root = new File(path);
    new HashMap<>();
    this.mongoClient = new MongoClient(new MongoClientURI(mongoURL));
    this.db = mongoClient.getDatabase(database);
    this.indexCollection = db.getCollection(indexCollection);
    this.indexCollection.drop();
    this.outboundLinkCollection = db.getCollection(outboundLinkCollection);
    this.outboundLinkCollection.drop();
  }

  public void closeConnection() {
    mongoClient.close();
    System.out.println("Indices created successfully.");
  }

  public void run() throws IOException {
    visit(root);
    bulkWrite();
    long timeElapsed = System.currentTimeMillis() - this.timestamp;
    System.out.println("Finished in: " + timeElapsed / 1000 / 60 + " min " + (timeElapsed / 1000)
        % 60 + " sec");

    closeConnection();
  }

  public void bulkWrite() {
    System.out.println("Ready to Bulk Write");
    List<Document> docs = new LinkedList<Document>();

    for (String t : terms.keySet()) {
      Document termDoc = new Document();
      termDoc.append("term", t);
      List<Document> locations = new LinkedList<Document>();

      for (Location l : terms.get(t).getLocations()) {
        Document locationDoc = new Document();
        locationDoc.append("filename", l.getFilename());
        locationDoc.append("index", l.getIndcies());
        locations.add(locationDoc);
      }
      termDoc.append("location", locations);
      docs.add(termDoc);
    }

    System.out.println(docs.size());
    this.indexCollection.insertMany(docs);
    System.out.println("Finished writing to db");

  }

  public void visit(File file) throws IOException {
    // Traversal referenced from 'tutorials point - Tika'
    if (file.isDirectory()) {
      String[] children = file.list();

      for (String c : children) {
        visit(new File(file, c));
      }

    } else if (file.isFile() && file.length() > 0) {
      org.jsoup.nodes.Document doc = Jsoup.parse(file, "utf-8");

      // strip text out of document
      String text = doc.text();

      // create outbound link index
      makeOutboundLinkIndex(file, doc);

      // create index
      makeIndex(file, text);

      System.out.println("Finished:\t" + file.getName());

    }
  }

  public void makeOutboundLinkIndex(File file, org.jsoup.nodes.Document doc) {
    // get links
    Elements links = doc.select("a");
    Set<String> set = new HashSet<>();

    // get all outgoing links
    for (Element link : links) {
      // COMMENTING OUT METHOD SPECIFICALLY FOR FORMATTING HTML FILENAMES FOR HW3/4
      // String address = link.attr("href");
      // if (address.trim().length() > 0) {
      // int lastSlashIndex = address.lastIndexOf('/') + 1;
      // String linkname = address.substring(lastSlashIndex);
      // if (linkname.trim().length() > 0 && linkname.contains(".html")) {
      // try {
      // set.add(java.net.URLDecoder.decode(linkname, "UTF-8"));
      // } catch (UnsupportedEncodingException e) {
      // e.printStackTrace();
      // }
      // }
      // }
      try {
        String absUrl = java.net.URLDecoder.decode(link.absUrl("href"), "UTF-8");
        
        if (absUrl != null && !absUrl.equals("")){
          
        }else{
          String domain = new String((byte[]) Files.getAttribute(Paths.get(file.getPath()), "user:uri"));
          absUrl = Util.subFolderStripper(domain) + "/" + link.attr("href");
        }
        set.add(absUrl);
          
      } catch (Exception e) {
        System.err.println("Failed to read link: " + link.absUrl("href"));
      }
    }

    Document mongodoc = new Document();
    mongodoc.append("file", Util.getUri(file));
    mongodoc.append("local", file.getName());
    mongodoc.append("pages", set);
    mongodoc.append("last-modified", file.lastModified());
    outboundLinkCollection.insertOne(mongodoc);
  }

  public void makeIndex(File file, String text) {
    Scanner scan = new Scanner(text);
    int n = 0;

    Queue<String> queue = new LinkedList<String>();
    // queue words in doc
    while (scan.hasNext()) {
      String word = scan.next();
      // if scanned word is composed of alphabetic characters and no
      // numeric values
      if (word.matches(".*\\w+.*") && !word.matches(".*\\d+.*")) {

        // lowercase and trim the word
        word = word.toLowerCase().trim();

        // if the word contains possessive get rid of it
        if (word.contains("'s"))
          word = word.replace("'s", "");

        // trim all special characters
        String trimmed = word.replaceAll("[^a-zA-Z-/]", "");

        // a word might contain / or - like this: tax-deductible. Then
        // split the words
        if (trimmed.contains("/") || trimmed.contains("-")) {
          String[] splitSlash = trimmed.split("/,-");
          for (String s : splitSlash) {
            queue.add(s);
          }
        } else { // if the word doesn't contain / or -, then proceed\
          queue.add(trimmed);
        }
      }
    }

    scan.close();

    // add locations to terms
    String fileUri = Util.getUri(file);
    
    while (!queue.isEmpty()) {
      String word = queue.poll();

      // if the word is not a single character and
      if (word.length() > 1 && !Util.isStopWord(word)) {
        String stemmed = Util.stem(word);

        if (!this.terms.containsKey(stemmed)) {
          Term term = new Term(stemmed);
          term.addIndex(fileUri, n);
          this.terms.put(stemmed, term);
        } else {
          this.terms.get(stemmed).addIndex(fileUri, n);
        }
      }
      n++;
    }
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
