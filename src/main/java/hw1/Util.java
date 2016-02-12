/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * Util object conatins common methods such as JSON parsing and writing.
 */

package hw1;

import java.io.File;
import java.io.FileReader;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Util {
  // read JSON file (config.json)
  public JSONObject jsonParser(String filePath) {
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
  public String randomString() {
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

  // delete and recreate data download folder
  public void dataFolderReset(String path) {
    try {
      File data = new File(path);
      data.delete();
      data.mkdir();
      System.out.println("Download folder created!");
    } catch (Exception e) {
      System.err.println("Folder not found!");
    }
  }
}
