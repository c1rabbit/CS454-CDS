package link_analysis;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
	private JSONObject jsonDB;
	private JSONObject inLinksCollection;

	public LinkAnalysis() {
		tempRankList = new ArrayList<Map>();

		// set file db from
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader("sample_db.json"));
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject json = (JSONObject) obj;
		this.jsonDB = json;
		System.out.println(this.jsonDB.toJSONString());
		buildInLinksCollection();
	}

	public void insertLinkRecord(String docName, String link) {
		JSONArray collection = (JSONArray) inLinksCollection.get("db");

		boolean match = false;
		if (!collection.isEmpty()) {
			for (int i = 0; i < collection.size(); i++) {
				JSONObject doc = (JSONObject) collection.get(i);
				if (doc.get("name") == docName) {
					JSONArray links = (JSONArray) doc.get("links");
					links.add(link);
					match = true;
				}
			}
		}
		if (!match) {
			JSONObject document = new JSONObject();
			JSONArray links = new JSONArray();
			links.add(link);
			document.put("name", docName);
			document.put("links", links);
			collection.add(document);

		}
		// collection.

	}

	public void buildInLinksCollection() {
		// new collection
		inLinksCollection = new JSONObject();
		JSONArray inLinksArray = new JSONArray();

		JSONObject document = new JSONObject();
		List<String> inLinks = new LinkedList<String>();
		inLinksCollection.put("db", inLinksArray);

		insertLinkRecord("doc1", "document2");
		insertLinkRecord("doc1", "document1");
		insertLinkRecord("doc2", "document1");

		// System.out.println(inLinksCollection);

		/*
		 * inLinks.add("doc1"); inLinks.add("doc2"); document.put("name",
		 * "doc1"); document.put("links", inLinks); inLinksArray.add(document);
		 * inLinksArray.add(document);
		 */

		//
		/*
		 * for (int i = 0; i < inLinksArray.size(); i++) { JSONObject j =
		 * (JSONObject) inLinksArray.get(i); System.out.println(j.get("name"));
		 * if (j.get("name") == "doc1") { System.out.println("match"); } }
		 */

		/*
		 * JSONArray json = (JSONArray) this.jsonDB.get("db"); JSONArray newJSON
		 * = new JSONArray(); Set<String> exists = new HashSet<String>(); for
		 * (int i = 0; i < json.size(); i++) { JSONObject o = (JSONObject)
		 * json.get(i); String name = o.get("url").toString();
		 * System.out.println(o.get("url")); // add unique id to array JSONArray
		 * inLinks = (JSONArray) jsonInLinks.get("db"); for(int j =0 ;
		 * j<inLinks.size() ;j++){ JSONObject in = (JSONObject) inLinks.get(j);
		 * if (!exists.contains(in.get("url"))){
		 * exists.add(in.get("url").toString()); JSONObject j1 = new
		 * JSONObject(); j1.put(in.get("url"), new JSONArray());
		 * newJSON.add(j1); }
		 * 
		 * }
		 * 
		 * if(inLinks.get(i).containsAll("asd")) List a = new
		 * LinkedList((Collection) o.get("link")); for (int j = 0; j < a.size();
		 * j++) { String link = a.get(j).toString(); System.out.println("-" +
		 * link);
		 * 
		 * } System.out.println(newJSON.toString());
		 * 
		 * 
		 * }
		 */
		/*
		 * for (int i = 0; i < this.jsonDB.get("db"); i++) {
		 * 
		 * }
		 */
		// finalize jsonarray
		inLinksCollection.put("db", inLinksArray);

		System.out.println(inLinksCollection);

	}

	public void run() {

		// get total number of documents
		// int docCount = collection.size();
		int docCount = 0;

	}

}
