package tfidf_analysis;

public class Page implements Comparable<Page> {
  private String name;
  private double rank;
  
  public Page(String name, double rank) {
    this.name = name;
    this.rank = rank;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getRank() {
    return rank;
  }

  public void setRank(double rank) {
    this.rank = rank;
  }
  
  public int compareTo(Page p) {
    double otherRank = p.getRank();
    
    if (this.rank > otherRank)
      return -1;
    else if (this.rank < otherRank)
      return 1;
    
    return 0;
  }
}
