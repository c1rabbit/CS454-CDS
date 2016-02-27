package link_analysis;

import java.util.LinkedList;
import java.util.List;

public class Document {
	String url;
	List<String> links;

	public Document(String url) {
		links = new LinkedList<String>();
		this.url = url;
	}

	public String getUrl() {
		return this.url;
	}

	public void addLink(String url) {
		this.links.add(url);
	}
}
