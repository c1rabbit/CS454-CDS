package search_engine;

/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * Util object conatins common methods such as JSON parsing and writing.
 */

import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Util {
  // read JSON file (config.json)
  public static JSONObject jsonParser(String filePath) {
    if (filePath == null || filePath.length() == 0)
      return null;

    JSONParser parser = new JSONParser();
    JSONObject output = new JSONObject();

    try {
      output = (JSONObject) parser.parse(new FileReader(filePath));
    } catch (Exception e) {
    }

    return output;
  }

  // generate random 20 char string (html filenames)
  // source:
  // http://stackoverflow.com/questions/20536566/creating-a-random-string-with-a-z-and-0-9-in-java
  public static String randomString() {
    String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    StringBuilder salt = new StringBuilder();
    Random rnd = new Random();
    while (salt.length() < 20) {
      int index = (int) (rnd.nextFloat() * SALTCHARS.length());
      salt.append(SALTCHARS.charAt(index));
    }
    String saltStr = salt.toString();
    return saltStr;
  }

  // delete folder
  // source: http://www.tutorialspoint.com/javaexamples/dir_delete.htm
  public static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }
    return dir.delete();
  }

  // create folder
  public static void createDir(String path) {
    try {
      File data = new File(path);
      data.mkdir();
      System.out.println("Folder created!");
    } catch (Exception e) {
      System.err.println("Failed to create folder!");
    }
  }

  // domain name stripper
  public static String domainStripper(String uri) {
    String[] array = uri.split("/");
    return array[2];
  }
  
  public static String subFolderStripper(String uri) {
    int index = uri.lastIndexOf("/");
    return uri.substring(0, index);
  }
  
  // filename stripper
  public static String filenameStripper(String uri) {
    String[] output = uri.split("/");
    return output[output.length - 1];
  }

  // indicate whether the string is a number
  public static boolean isNum(String s) {
    return s.matches("[-+]?\\d*\\.?\\d+");
  }

  // apply Porter stem. imports Stemmer class
  public static String stem(String word) {
    Stemmer stemmer = new Stemmer();
    String str = word.toLowerCase().trim();
    for (char c : str.toCharArray())
      stemmer.add(c);
    stemmer.stem();
    return stemmer.toString();
  }

  // check if a word is a stop word. imports StopWords class
  public static boolean isStopWord(String word) {
    StopWords sw = new StopWords();
    return sw.isStopWord(word);
  }

  // source taken from "Java 8 - Base64" tutorial from www.tutorialspoint.com
  public static String encode(String str) throws UnsupportedEncodingException {
    String base64encodedString = Base64.getEncoder().encodeToString(str.getBytes("utf-8"));
    return base64encodedString;
  }

  // source taken from "Java 8 - Base64" tutorial from www.tutorialspoint.com
  public static String decode(String base64encodedString) throws UnsupportedEncodingException {
    byte[] base64decodedBytes = Base64.getDecoder().decode(base64encodedString);
    String originalString = new String(base64decodedBytes, "utf-8");
    return originalString;
  }

  private static int counter = 0;

  public static int fileCounter(String dirPath) {
    File f = new File(dirPath);
    File[] files = f.listFiles();

    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        File file = files[i];

        if (file.isFile())
          counter++;
        else if (file.isDirectory())
          fileCounter(file.getAbsolutePath());
      }
    }

    return counter;
  }
  
  public static String getUri(File file) {
    String uri = "";
    
    try {
      uri = new String((byte[]) Files.getAttribute(Paths.get(file.getPath()), "user:uri"));
    } catch (Exception e) {
      System.err.println("Error extracting URI from file!");
    }
    
    return uri;
  }
}
