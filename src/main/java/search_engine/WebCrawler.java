package search_engine;

/*
 * CS 454 - Calvin Thanh, Sam Kim, Di Shen
 * 
 * WebCrawler object crawls the web with a given starting point and depth and adds all links to a
 * commonly shared queue, where the WebExtractor objects will read and extract information from the
 * links
 */

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
	private int depth;
	private List<WebPath> paths;
	private Set<String> visited;
	private String downloadPath;
	private long timestamp;

	public WebCrawler(URI uri, int depth, List<WebPath> paths,
			String downloadPath) {
		this.depth = depth;
		this.paths = paths;
		this.downloadPath = downloadPath;
		paths.add(new WebPath(uri.toString(), 0));
		System.out.println("Root URI:\t" + uri.toString());
		visited = new HashSet<>();
		this.timestamp = System.currentTimeMillis();

	}

	public void run() {
		WebPath uri = paths.get(0);
		String oldDomainName = "";
		String dataFolder = "";

		// breadth first search
		while (!paths.isEmpty()) {
			uri = paths.get(0); // reset pointer
			Document doc = new Document("temp");
			System.out.println("crawling:\t" + uri.getPath());

			try {
				doc = Jsoup.connect(uri.getPath()).get();
				String newDomainName = Util.domainStripper(uri.getPath());

				// create a folder with domain name in data folder to hold the
				// material from that domain
				if (!newDomainName.equals(oldDomainName)) {
					oldDomainName = newDomainName;
					dataFolder = "./" + downloadPath + "/" + newDomainName;
					Util.createDir(dataFolder);
				}

				// create a random html filename
				String randomName = Util.randomString() + ".html";
				// create the html file with the filename
				Files.write(Paths.get(dataFolder + "/" + randomName), doc
						.html().getBytes());
				// set the "uri" attribute for the file just created
				Files.setAttribute(Paths.get(dataFolder + "/" + randomName),
						"user:uri", uri.getPath().getBytes());
				// set last modified date if any in long
				URL url = new URL(uri.getPath());
				URLConnection connection = url.openConnection();
				// System.out.println(connection.getLastModified());
				long modified = connection.getLastModified();
				Files.setAttribute(Paths.get(dataFolder + "/" + randomName),
						"basic:lastModifiedTime", FileTime.fromMillis(modified));

				/*
				 * download the OTHER stuff Elements images = doc.select("img");
				 * for (Element i : images) { if (!i.absUrl("src").isEmpty()) {
				 * URL website = new URL(i.absUrl("src")); ReadableByteChannel
				 * rbc = Channels.newChannel(website.openStream());
				 * FileOutputStream fos = new FileOutputStream(dataFolder + "/"
				 * + Util.filenameStripper(i.absUrl("src")));
				 * fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				 * fos.close(); Files.setAttribute( Paths.get(dataFolder + "/" +
				 * Util.filenameStripper(i.absUrl("src"))), "user:uri",
				 * uri.getPath().getBytes()); } }
				 */

				// queue links
				if (uri.getDepth() < depth) {
					// create group of <a> tag elements from the doc
					Elements links = doc.select("a");

					// add new links to the queue for crawling
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
				//e.printStackTrace();
			} finally {
				paths.remove(0);
				visited.add(uri.getPath());
			}
		}
		long timeElapsed = System.currentTimeMillis() - this.timestamp;
		System.out.println("Finished in: " + timeElapsed / 1000 / 60 + " min "
				+ (timeElapsed / 1000) % 60 + " sec");
	}
}
