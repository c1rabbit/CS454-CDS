package search_engine;

import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LinkCrawler implements Runnable {

	WebPath uri;
	Elements links;
	Set<String> visited;
	List<WebPath> paths;

	public LinkCrawler(WebPath uri, Elements links, Set<String> visited,
			List<WebPath> paths) {
		// add new links to the queue for crawling
		this.uri = uri;
		this.links = links;
		this.visited = visited;
		this.paths = paths;
	}

	public void run() {
		for (Element l : links) {
			if (!visited.contains(l.absUrl("href"))
					&& !l.absUrl("href").isEmpty()) {
				System.out.println((uri.getDepth() + 1) + " queued:\t"
						+ l.absUrl("href"));
				paths.add(new WebPath(l.absUrl("href"), uri.getDepth() + 1));
				visited.add(l.absUrl("href"));
			}
		}
	}

}
