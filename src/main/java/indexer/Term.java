package indexer;

import java.util.LinkedList;
import java.util.List;

public class Term {
	private String term;
	private List<Location> locations;

	public Term(String term) {
		this.term = term;
		this.locations = new LinkedList<Location>();

		/*
		 * Map<String, Object> data = new HashMap<String, Object>();
		 * data.put("filename", filename); data.put("index", new
		 * LinkedList<Integer>().add(index)); locations.add(data);
		 */

	}

	public void addIndex(String filename, int index) {
		boolean exists = false;

		for (Location l : this.locations) {
			// update field
			if (l.getFilename().equalsIgnoreCase(filename)) {
				l.addIndex(index);
				exists = true;
				break;
			}
		}
		// insert new field
		if (!exists) {
			this.locations.add(new Location(filename, index));
		}
	}

	public String getTerm() {
		return this.term;
	}

	public List<Location> getLocations() {
		return this.locations;
	}
	/*
	 * public void setLocation(String filename, int index) {
	 * this.locations.add(addLocation(filename, index));
	 * 
	 * for (Map<String, Object> location : this.locations) { if
	 * (location.get("filename").equals(filename)) { List<Integer> indexes =
	 * (List<Integer>) location.get("index"); } else { // add new } }
	 * 
	 * if (this.locations.get("locations").get("filename").equals(filename)) {
	 * 
	 * }
	 * 
	 * }
	 * 
	 * public Map<String, Object> addLocation(String filename, int index) {
	 * Map<String, Object> location = new HashMap<String, Object>();
	 * location.put("filename", filename); location.put("index", index); return
	 * location; }
	 */

}
