package link_analysis;

import java.util.LinkedList;
import java.util.List;

public class Page {

	private String filename;
	private List<String> inlinks;
	private List<String> outlinks;
	private double score;
	private long lastModified;

	public Page(String filename) {
		this.score = 0.0;
		this.filename = filename;
		this.inlinks = new LinkedList<String>();
		this.outlinks = new LinkedList<String>();
		this.lastModified = new Long(0);
	}

	public void addInlink(String link) {
		this.inlinks.add(link);
	}

	public void setOutlinks(List<String> outlinks) {
		this.outlinks = outlinks;
	}

	public List<String> getOutlinks() {
		return this.outlinks;
	}

	public List<String> getInlinks() {
		return this.inlinks;
	}

	public String getFilename() {
		return this.filename;
	}

	public double getScore() {
		return this.score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public long getLastModified() {
		return this.lastModified;
	}

}
