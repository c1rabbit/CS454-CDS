/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * Util object conatins common methods such as JSON parsing and writing.
 */

package hw1;

import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Util {
  // read JSON file
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
}