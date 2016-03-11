package link_analysis;

import java.util.LinkedList;
import java.util.List;

public class Page {

	String filename;
	List<String> inlinks;
	List<String> outlinks;
	double score;

	public Page(String filename) {
		this.score = 0.0;
		this.filename = filename;
		this.inlinks = new LinkedList<String>();
		this.outlinks = new LinkedList<String>();
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

}
