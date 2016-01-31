package hw0;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Search {
	public Search() {

	}

	public static void main(String[] args) {
		Search search = new Search();
		String term = "";
		if (args.length < 1) {
			args = new String[1];
			System.out.println("No search term entered.");
			return;
		} else {
			for (String s : args) {
				term += s + " ";
			}
		}
		String filepath = Paths.get(".").toAbsolutePath().normalize()
				.toString()
				+ "/data/db_record.txt";
		// System.out.println(filepath);
		JSONParser parser = new JSONParser();

		try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {

			for (String line; (line = br.readLine()) != null;) {
				// System.out.println(line);
				JSONObject jsonObject = (JSONObject) parser.parse(line);
				// System.out.println(jsonObject.toJSONString());
				search.search(jsonObject, term);

				// System.out.println(filename.toString());
			}

		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

	}

	private void search(JSONObject json, String term) {

		String content = json.get("filename").toString()
				+ json.get("content").toString();
		// System.out.println(json);
		// JSONArray content = (JSONArray) json.get("content");

		// System.out.println("looking for: " + term);

		if (Pattern.matches(".*" + term.toLowerCase().trim() + ".*", content
				.toString().toLowerCase())) {
			System.out.println("Match!");
			System.out.println("Filename:\t" + json.get("filename").toString());
			System.out.println("Directory:\t"
					+ json.get("directory").toString());
			System.out.println();
		}

	}
}
