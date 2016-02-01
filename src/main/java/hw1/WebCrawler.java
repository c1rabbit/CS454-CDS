package hw1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
	private static int depth;
	private List<WebPath> paths;
	private Set<String> visited;
	private String db_path = Paths.get(".").toAbsolutePath().normalize()
			.toString()
			+ "/data/db_record.txt";

	public WebCrawler(URI uri) {

		paths = new LinkedList<WebPath>();
		paths.add(new WebPath(uri.toString(), 0));
		System.out.println("Root URI:\t" + uri.toString());
		visited = new HashSet<String>();

		// clear db file
		try {
			FileWriter fw = new FileWriter(db_path, false);
			fw.close();
		} catch (IOException e) {
			File data = new File("data");
			data.mkdir();
			@SuppressWarnings("unused")
			File newfile = new File(db_path);
			// e.printStackTrace();
			System.err.println("new directory and file created");
		}

		// PrintWriter pw = new PrintWriter(Paths.get(".").toAbsolutePath()
		// .normalize().toString()
		// + "/src/data/db_record.txt");
		// pw.close();

	}

	public void run() {
		WebPath uri = paths.get(0);
		// breadth first search
		while (!paths.isEmpty()) {
			uri = paths.get(0);// reset pointer
			Document doc = new Document("temp");
			System.out.println("extracting:\t" + uri.getPath());

			try {
				doc = Jsoup.connect(uri.getPath()).get();
				// save content
				Element body = doc.body();
				// System.out.println(body.text());
				parse(body);
				System.out.println(uri.getDepth() + " parsed:\t"
						+ uri.getPath());
				// queue links
				if (uri.getDepth() < depth) {

					Elements links = doc.select("a");
					for (Element l : links) {
						if (!visited.contains(l.absUrl("href"))
								&& !l.absUrl("href").isEmpty()) {
							System.out.println((uri.getDepth() + 1)
									+ " queued:\t" + l.absUrl("href"));
							paths.add(new WebPath(l.absUrl("href"), uri
									.getDepth() + 1));
							visited.add(l.absUrl("href"));
						}
					}

				}
			} catch (Exception e) {
				System.err.println("Not a link");

			} finally {
				paths.remove(0);
				visited.add(uri.getPath());
			}
			/*
			 * if (uri.isFile() && !visited.contains(uri)) { parse(uri);
			 * visited.add(uri); System.out.println("parsed:\t" +
			 * uri.toString()); } else if (file.isDirectory() &&
			 * !visited.contains(uri)) { for (final File f : uri.listFiles()) {
			 * if (!visited.contains(f)) { if (f.isDirectory()) { paths.add(f);
			 * } else { paths.add(0, f); }
			 * 
			 * System.out.println("queued:\t" + f.toPath()); }
			 * 
			 * } }
			 */
			// paths.remove(0);
			// visited.add(uri.getPath());
		}

	}

	@SuppressWarnings("unchecked")
	public void parse(Element content) throws IOException {
		// AutoDetectParser parser = new AutoDetectParser();
		// BodyContentHandler handler = new BodyContentHandler();
		// Metadata metadata = new Metadata();
		JSONObject json = new JSONObject();

		json.put("uri", content.baseUri());
		json.put("content", content.text());
		// System.out.println(file.toString());

		// try (InputStream stream = new FileInputStream(file)) {
		// parser.parse(stream, handler, metadata);
		// json.append("Content-Type", metadata).get("Content-Type");
		// System.out.println(TikaCoreProperties.METADATA_DATE);
		// System.out.println(metadata.get(TikaCoreProperties.METADATA_DATE
		// .getName()));
		// json.append(TikaCoreProperties.TITLE.getName(),
		// metadata.get(TikaCoreProperties.TITLE));
		// String[] metadataNames = metadata.names();

		// for (String name : metadataNames) {
		// json.append(name, metadata.get(name));
		// System.out.println(name + ": " + metadata.get(name));

		// json.put("content", handler.toString());
		// System.out.print(".");
		// } catch (Exception e) {
		// e.printStackTrace();
		// } finally {
		writeJSON(json);
		// }
	}

	public void writeJSON(JSONObject json) {
		try {

			FileWriter fw = new FileWriter(db_path, true); // the true will
															// append the new
															// data
			fw.write(json.toString() + "\n");// appends the string to the file
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}

	public static void main(String[] args) throws IOException,
			URISyntaxException {
		URI uri = new URI("http://localhost/sgdancestudio.com/");
		depth = 2;
		// File directory = new File("C:/Users/ASPIRE/Desktop/test");
		WebCrawler crawler = new WebCrawler(uri);
		crawler.run();
		System.out.println("Finished Crawling");
	}

}
