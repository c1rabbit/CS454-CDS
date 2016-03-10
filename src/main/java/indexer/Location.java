package indexer;

import java.util.LinkedList;
import java.util.List;

public class Location {
	private String filename;
	private List<Integer> index;

	public Location(String filename, int index) {
		this.filename = filename;
		this.index = new LinkedList<Integer>();
		addIndex(index);
	}

	public void addIndex(int index) {
		if (!this.index.contains(index)) {
			this.index.add(index);
		} else {
			System.err.println("index already exists");
		}
	}

	public String getFilename() {
		return this.filename;
	}

	public List<Integer> getIndcies() {
		return this.index;
	}
}
