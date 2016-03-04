package link_analysis;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class LinkAnalysis {
	private int threads = 1;
	private JSONObject outLinksCollection;
	private JSONObject inLinksCollection;

	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection outboundLinkCollection;

	public LinkAnalysis() {
		System.out.println("connecting to db");
		String mongoURL = "mongodb://localhost:27017";
		String database = "cs454";
		String outboundLinkCollection = "outboundLinks";

		this.mongoClient = new MongoClient(new MongoClientURI(mongoURL));
		this.db = mongoClient.getDatabase(database);
		this.outboundLinkCollection = db.getCollection(outboundLinkCollection);

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

		this.outLinksCollection = new JSONObject();

		// build outlinks
		FindIterable<Document> iterable = this.outboundLinkCollection.find();
		Iterator<Document> iterator = iterable.iterator();
		JSONArray outLinksList = new JSONArray();
		while (iterator.hasNext()) {

			JSONObject object = new JSONObject();

			Document d = iterator.next();
			String filename = (String) d.get("file");
			ArrayList<String> webpageList = (ArrayList<String>) d.get("pages");
			object.put("name", filename);
			object.put("links", webpageList);
			// System.out.println("filename: " + filename);
			// for (String page : webpageList) {
			// System.out.print(page + ", ");
			// }
			// System.out.println(webpageList);
			outLinksList.add(object);
		}
		this.outLinksCollection.put("db", outLinksList);
		/*
		 * for (int i = 0; i < this.outLinksCollection.size(); i++) {
		 * this.outLinksCollection.get("db"); }
		 */
		System.out.println("out links: " + this.outLinksCollection);

		// set new collection
		inLinksCollection = new JSONObject();
		inLinksCollection.put("db", new JSONArray());
		buildInLinksCollection();
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

		System.out.println("initializing link analysis...");

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

		int iteration = 0;
		while (iteration < 3) {
			Map<String, Double> temp = new HashMap<String, Double>();

			// System.out.println("rank:\t" + rank);
			// System.out.println("tempRank:\t" + temp);

			for (int i = 0; i < inLinks.size(); i++) {
				JSONObject j = (JSONObject) inLinks.get(i);
				Object name = j.get("name");
				List<String> ilinks = (List<String>) j.get("links");

				double tempRank = 0.0;
				for (String l : ilinks) {

					int size = 0;
					// calculate the outlink rank
					for (int k = 0; k < docs.size(); k++) {
						JSONObject obj = (JSONObject) docs.get(k);

						if (obj.get("name") == l) {
							List<String> o = (List<String>) obj.get("links");
							size = o.size();

						}

					}
					// System.out.println(l + " outlink count: " + size);
					// System.out.println(l);
					try {
						double currentRank = rank.get(l) / size;
						tempRank += currentRank;
					} catch (Exception e) {

						// System.out.println(rank);
						System.err.println("can't get from rank map: " + l);
						System.err.println(name);
						System.exit(0);
					}
					// System.out.println(l + ": +" + currentRank);
				}
				// System.out.println("--rank: " + name + " " + tempRank);
				temp.put(name.toString(), tempRank);

			}

			// set temp ranking to final rank set

			rank = temp;
			System.out
					.println("completed iteration: " + iteration + " " + rank);
			/*
			 * for (String s : temp.keySet()) { System.out.println(s); }
			 */
			iteration++;
		}
		String n = "";
		double highest = 0.0;
		double total = 0.0;
		for (String s : rank.keySet()) {
			total += rank.get(s);
		}
		
		// normalize results
		Map<String, Double> temp = new HashMap<String, Double>();

		for (String s : rank.keySet()) {
			if (rank.get(s) > highest) {
				highest = rank.get(s);
				n = s;
			}
			temp.put(s, rank.get(s) / total);
		}
		System.out.println("finished normalizing results");
		rank = temp;
		//System.out.println(rank);
		
		System.out.println("size:\t" + rank.size());
		System.out.println("total raw sum:\t" + total);
		System.out.println("highest ranked:\t" + n + "\t" + rank.get(n));


		System.out.println("--Link Analysis completed");

		// record results

	}
}
