package indexer;

import java.util.LinkedList;
import java.util.List;

public class Term {
  private String term;
  private List<Location> locations;

  public Term(String term) {
    this.term = term;
    this.locations = new LinkedList<Location>();
  }

  public void addIndex(String filename, int index) {
    boolean exists = false;

    for (Location l : this.locations) {
      // update location index
      if (l.getFilename().equalsIgnoreCase(filename)) {
        l.addIndex(index);
        exists = true;
        break;
      }
    }
    // insert new location with index
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
}
