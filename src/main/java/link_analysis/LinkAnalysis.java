package link_analysis;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LinkAnalysis {
	private int threads = 1;
	private List<Link> link;
	private Map<String, Double> tempRank;
	private List<Map> tempRankList;
	private JSONObject outLinksCollection;
	private JSONObject inLinksCollection;

	public LinkAnalysis() {
		tempRankList = new ArrayList<Map>();

		// set file db from
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader("sample_db.json"));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject json = (JSONObject) obj;
		this.outLinksCollection = json;
		JSONArray outdocs = (JSONArray) json.get("db");
		for (int i = 0; i < this.outLinksCollection.size(); i++) {
			this.outLinksCollection.get("db");
		}
		System.out.println("out links: " + this.outLinksCollection);

		// set new collection
		inLinksCollection = new JSONObject();
		inLinksCollection.put("db", new JSONArray());

		run();
	}

	public void insertLinkRecord(String docName, String link) {
		JSONArray collection = (JSONArray) inLinksCollection.get("db");

		boolean match = false;
		if (!collection.isEmpty()) {
			for (int i = 0; i < collection.size(); i++) {
				JSONObject doc = (JSONObject) collection.get(i);
				if (doc.get("name").equals(docName)) {
					JSONArray links = (JSONArray) doc.get("links");
					if (!links.contains(link)) {
						links.add(link);
					}
					match = true;
				}
			}
		}
		if (!match) {// if new document index
			JSONObject document = new JSONObject();
			JSONArray links = new JSONArray();
			links.add(link);
			document.put("name", docName);
			document.put("links", links);
			collection.add(document);
		}

	}

	public void buildInLinksCollection() {

		JSONArray outlinks = (JSONArray) this.outLinksCollection.get("db");
		for (int i = 0; i < outlinks.size(); i++) {
			JSONObject doc = (JSONObject) outlinks.get(i);
			String src = (String) doc.get("name");
			List<String> links = (List<String>) doc.get("links");
			for (String dest : links) {
				insertLinkRecord(dest, src);
			}
		}

		// finalize jsonarray
		System.out.println("in links: " + inLinksCollection);

	}

	public void run() {
		buildInLinksCollection();

		// get total number of documents
		JSONArray docs = (JSONArray) outLinksCollection.get("db");

		double docCount = docs.size();
		Map<String, Double> rank = new HashMap<String, Double>();
		for (int i = 0; i < docCount; i++) {
			JSONObject doc = (JSONObject) docs.get(i);
			rank.put(doc.get("name").toString(), (double) (1 / docCount));
		}
		System.out.println("initial rank:\t" + rank);

		JSONArray inLinks = (JSONArray) inLinksCollection.get("db");

		// iterate and rank through each document
		Map<String, Double> temp = new HashMap<String, Double>();
		for (String key : rank.keySet()) {
			temp.put(key, 0.0);
		}
		System.out.println("tempRank:\t" + temp);

		int iteration = 0;
		while (iteration < 3) {

			for (int i = 0; i < inLinks.size(); i++) {
				JSONObject j = (JSONObject) inLinks.get(i);
				String name = (String) j.get("name");
				List<String> ilinks = (List<String>) j.get("links");

				double tempRank = 0.0;
				for (String l : ilinks) {

					int size = 0;
					// calculate the outlink rank
					for (int k = 0; k < docs.size(); k++) {
						JSONObject obj = (JSONObject) docs.get(k);

						if (obj.get("name").equals(l)) {
							List<String> o = (List<String>) obj.get("links");
							size = o.size();

						}

					}
					System.out.println(l + " outlink count: " + size);
					double currentRank = rank.get(name) / size;
					tempRank += currentRank;
					System.out.println(l + ": +" + currentRank);
				}
				System.out.println("--rank: " + name + " " + tempRank);
				temp.put(name, tempRank);

			}

			// set temp ranking to final rank set
			rank = temp;
			System.out.println("iteration: " + iteration + " " + rank);
			iteration++;
		}

		/*
		 * for (String key : rank.keySet()) { // get rank per key(document)
		 * double value = 0.0; // System.out.println(docs);
		 * 
		 * temp.put(key, value);
		 * 
		 * }
		 */

		/*
		 * for (int i = 0; i < 4; i++) {
		 * 
		 * for (int j = 0; j < docCount; j++) { JSONObject document =
		 * (JSONObject) docs.get(j); List<String> links = (LinkedList<String>)
		 * document.get("link"); for (String link : links) {
		 * temp.put(document.get("name").toString(),
		 * temp.get(document.get("name"))); } } for (String document :
		 * rank.keySet()) {
		 * 
		 * }
		 * 
		 * }
		 * 
		 * for (String key : rank.keySet()) {
		 * 
		 * double rankValue = rank.get(key);
		 * 
		 * }
		 */

	}
}
