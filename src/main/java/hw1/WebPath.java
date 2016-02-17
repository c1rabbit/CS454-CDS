package hw1;


public class WebPath {
  private int depth;
  private String path;

  public WebPath(String path, int depth) {
    this.path = path;
    this.depth = depth;
  }

  public String getPath() {
    return this.path;
  }

  public int getDepth() {
    return this.depth;
  }
}