package link_analysis;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
		System.out.println(this.outLinksCollection.toJSONString());

		// set new collection
		inLinksCollection = new JSONObject();
		inLinksCollection.put("db", new JSONArray());

		buildInLinksCollection();
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
			List<String> links = (List<String>) doc.get("link");
			for (String dest : links) {
				insertLinkRecord(dest, src);
			}
		}

		// finalize jsonarray

		System.out.println(inLinksCollection);

	}

	public void run() {

		// get total number of documents
		// int docCount = collection.size();
		int docCount = 0;

	}

}
