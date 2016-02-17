package hw1;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WebSearch {
	public WebSearch() {

	}

	public static void main(String[] args) {
		WebSearch search = new WebSearch();
		String term = "";
		if (args.length < 1) {
			args = new String[1];
			System.out.println("No search term entered.");
			return;
		} else {
			for (String s : args) {
				term += s + " ";
			}
			System.out.println("Search:\t" + term);
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
		} finally {
			System.out.println("Search Finished");
		}

	}

	private void search(JSONObject json, String term) {

		String content = json.get("uri").toString()
				+ json.get("content").toString();
		// System.out.println(json);
		// JSONArray content = (JSONArray) json.get("content");

		// System.out.println("looking for: " + term);

		if (Pattern.matches(".*" + term.toLowerCase().trim() + ".*", content
				.toString().toLowerCase())) {
			// int index = json.get("content").toString().toLowerCase()
			// .indexOf(term.toLowerCase().trim());
			// System.out.println("Match!");
			// System.out.println("URI:\t" + json.get("uri").toString());
			// System.out.println("Content:\t"
			// + json.get("content")
			// .toString()
			// .substring(
			// Math.max(0, index - 50),
			// Math.min(index + term.length() - 1,
			// content.length() - 1)));
			for (Object j : json.keySet()) {
				System.out.println(j.toString() + ":\t"
						+ json.get(j.toString()));
			}
			// System.out.println("Content:\t" + json.get("content"));
			System.out.println();
		}

	}
}
