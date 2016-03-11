package link_analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class LinkAnalysis {
	// private int threads = 1;
	// private Map<String, Object> outLinksCollection;
	// private Map<String, Object> inLinksCollection;
	private int iteration_max;

	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> outboundLinkCollection;
	private long timestamp;

	private MongoCollection<Document> rankCollection;
	boolean debug;

	List<Page> pages;

	// List<Page> pagesIn;

	public LinkAnalysis(String mongoURL, String database,
			String outboundLinkCollection, String rankCollection,
			int iterations, boolean debug) {
		this.timestamp = System.currentTimeMillis();
		this.debug = debug;
		this.iteration_max = iterations;
		System.out.println("connecting to db");

		this.mongoClient = new MongoClient(new MongoClientURI(mongoURL));
		this.db = mongoClient.getDatabase(database);
		this.outboundLinkCollection = db.getCollection(outboundLinkCollection);

		this.rankCollection = db.getCollection(rankCollection);
		this.rankCollection.drop();

		this.pages = new LinkedList<Page>();

		// set file db from
		/*
		 * JSONParser parser = new JSONParser(); Object obj = null; try { obj =
		 * parser.parse(new FileReader("sample_db.json")); } catch (IOException
		 * | ParseException e) { e.printStackTrace(); } JSONObject json =
		 * (JSONObject) obj; this.outLinksCollection = json;
		 */

		// this.outLinksCollection = new JSONObject();

		// build outlinks
		FindIterable<Document> iterable = this.outboundLinkCollection.find();
		Iterator<Document> iterator = iterable.iterator();
		List<Object> outLinksList = new LinkedList<Object>();
		while (iterator.hasNext()) {

			Map<String, Object> object = new HashMap<String, Object>();

			Document d = iterator.next();
			// List<String> webpageList = (List<String>) d.get("pages");
			object.put("name", d.get("file").toString());
			object.put("links", d.get("pages"));
			// System.out.println("filename: " + filename);
			// for (String page : webpageList) {
			// System.out.print(page + ", ");
			// }
			// System.out.println(webpageList);
			outLinksList.add(object);

			Page page = new Page(d.get("file").toString());
			page.setOutlinks((List<String>) d.get("pages"));
			pages.add(page);
		}
		// this.outLinksCollection.put("db", outLinksList);

		// this.outLinksCollection.put("db", pages);
		/*
		 * for (int i = 0; i < this.outLinksCollection.size(); i++) {
		 * this.outLinksCollection.get("db"); }
		 */

		// if (debug) {
		// System.out.println("out links: " + this.pages);
		// } else {

		if (debug) {
			for (Page p : this.pages) {
				System.out.println(p.getFilename());
				System.out.println("Outlinks: " + p.getOutlinks().toString());
			}
		}

		System.out.println("--retrieving out-links");
		// }
		// set new collection
		// inLinksCollection = new HashMap<String, Object>();
		// inLinksCollection.put("db", new LinkedList<Object>());
		buildInLinksCollection();
		// run();
	}

	/*
	 * public void insertLinkRecord(String docName, String link) { List<Object>
	 * collection = (List<Object>) inLinksCollection.get("db");
	 * 
	 * boolean match = false; if (!collection.isEmpty()) { for (int i = 0; i <
	 * collection.size(); i++) { JSONObject doc = (JSONObject)
	 * collection.get(i); if (doc.get("name").equals(docName)) { List<String>
	 * links = (List<String>) doc.get("links"); if (!links.contains(link)) {
	 * links.add(link); } match = true; } } } if (!match) {// if new document
	 * index Map<String, Object> document = new HashMap<String, Object>();
	 * List<String> links = new LinkedList<String>(); links.add(link);
	 * document.put("name", docName); document.put("links", links);
	 * collection.add(document); }
	 * 
	 * }
	 */

	public void buildInLinksCollection() {

		// JSONArray outlinks = (JSONArray) this.outLinksCollection.get("db");

		/*
		 * for (int i = 0; i < outLinksCollection.size(); i++) { Map<String,
		 * Object> doc = (Map<String, Object>) outLinksCollection .get(i);
		 * String src = doc.get("name").toString(); List<String> links =
		 * (List<String>) doc.get("links"); for (String dest : links) {
		 * insertLinkRecord(dest, src); } }
		 */
		System.out.println("--building in-links");
		for (Page page : this.pages) {
			// System.out.println(filename);
			// System.out.println("outlinks: " + outlinks.toString());
			for (String link : page.getOutlinks()) {
				// System.out.println("checking: " + link);
				for (Page p : this.pages) {
					// System.out.println(p.getFilename());
					if (p.getFilename().equalsIgnoreCase(link)
							&& !p.getFilename().equalsIgnoreCase(
									page.getFilename())) {
						p.addInlink(page.getFilename());
						// System.out.println(page.getFilename() + "->" +
						// p.getFilename());
						break;

					}
				}
			}
		}

		if (debug) {
			for (Page p : this.pages) {
				System.out.println(p.getFilename());
				System.out.println("in-links: " + p.getInlinks().toString());
			}
		}

		System.out.println("--in-links finished");

	}

	public void run() {

		System.out.println("--initializing link analysis...");

		// get total number of documents
		// JSONArray docs = (JSONArray) outLinksCollection.get("db");

		// double docCount = docs.size();
		double docCount = this.pages.size();
		double initalRank = 1 / docCount;

		Map<String, Double> scores = new HashMap<String, Double>();

		for (Page p : pages) {
			scores.put(p.getFilename(), initalRank);
			p.setScore(initalRank);
		}
		System.out.println("inital rank set: " + initalRank);

		/*
		 * for (Page p : this.pages) { System.out.println(p.getFilename() + ": "
		 * + p.getScore()); }
		 */

		/*
		 * Map<String, Double> rank = new HashMap<String, Double>(); for (int i
		 * = 0; i < docCount; i++) { JSONObject doc = (JSONObject) docs.get(i);
		 * rank.put(doc.get("name").toString(), (double) (1 / docCount)); } if
		 * (debug) { System.out.println("initial rank:\t\t" + rank); } else {
		 * System.out.println("creating intial ranks"); }
		 * 
		 * JSONArray inLinks = (JSONArray) inLinksCollection.get("db");
		 */

		// iterate and rank through each document

		int iteration = 0;
		while (iteration < iteration_max) {
			Map<String, Double> temp = new HashMap<String, Double>();

			// Map<String, Double> rank = new HashMap<String, Double>();

			// System.out.println("rank:\t" + rank);
			// System.out.println("tempRank:\t" + temp);

			// List<Page> temp = new LinkedList<Page>();
			// temp = pages;

			for (Page p : pages) {
				double rank = 0;
				// System.out.println(p.getFilename());
				for (String link : p.getInlinks()) {
					// System.out.println("-" + link);
					rank += scores.get(link);

				}
				// System.out.println(p.getFilename() + ":\t" + rank);
				temp.put(p.getFilename(), rank);
			}

			scores = temp;

			/*
			 * for (int i = 0; i < inLinks.size(); i++) { JSONObject j =
			 * (JSONObject) inLinks.get(i); String name = (String)
			 * j.get("name"); List<String> ilinks = (List<String>)
			 * j.get("links");
			 * 
			 * double tempRank = 0.0; for (String l : ilinks) {
			 * 
			 * int size = 0; // calculate the outlink rank for (int k = 0; k <
			 * docs.size(); k++) { JSONObject obj = (JSONObject) docs.get(k);
			 * 
			 * if (obj.get("name") == l) { List<String> o = (List<String>)
			 * obj.get("links"); size = o.size();
			 * 
			 * }
			 * 
			 * } // System.out.println(l + " outlink count: " + size); //
			 * System.out.println(l); try { double currentRank = rank.get(l) /
			 * size; tempRank += currentRank; } catch (Exception e) {
			 * 
			 * // System.out.println(rank);
			 * System.err.println("can't get from rank map: " + l);
			 * System.err.println(name); System.exit(0); } //
			 * System.out.println(l + ": +" + currentRank); temp.put(l,
			 * tempRank); } // System.out.println("--rank: " + name + " " +
			 * tempRank); // temp.put(name, tempRank);
			 * 
			 * }
			 */

			// set temp ranking to final rank set

			// rank = temp;
			if (debug) {
				System.out.println("completed iteration:\t" + iteration + " "
						+ scores);
			} else {
				System.out.println("completed iteration:\t" + iteration);
			}
			/*
			 * for (String s : temp.keySet()) { System.out.println(s); }
			 */
			iteration++;
		}
		String n = "";
		double highest = 0.0;
		double total = 0.0;
		for (String s : scores.keySet()) {
			total += scores.get(s);
		}

		// normalize results
		Map<String, Double> temp = new HashMap<String, Double>();

		for (String s : scores.keySet()) {
			if (scores.get(s) > highest) {
				highest = scores.get(s);
				n = s;
			}
			temp.put(s, scores.get(s) / total);
		}
		System.out.println("--finished normalizing results");
		scores = temp;
		if (debug) {
			System.out.println("rank:\t" + scores);
		}

		System.out.println("size:\t" + scores.size());
		System.out.println("total raw sum:\t" + total);
		System.out.println("highest ranked:\t" + n + "\t" + scores.get(n));

		System.out.println("--Link Analysis completed");

		// record results
		for (Page p : pages) {
			p.setScore(scores.get(p.getFilename()));
		}/*
		 * for (String s : scores.keySet()) { addRankIndex(s, scores.get(s)); }
		 */
		System.out.println("--Results recorded");
		
		long timeElapsed = System.currentTimeMillis() - this.timestamp;
		System.out.println("Finished in: " + timeElapsed / 1000 / 60 + "min "
				+ (timeElapsed / 1000) % 60 + "sec");

	}

	public void addRankIndex(String file, double rank) {

		Document mongodoc = new Document();
		mongodoc.append("file", file);
		mongodoc.append("rank", rank);
		rankCollection.insertOne(mongodoc);

	}
}
