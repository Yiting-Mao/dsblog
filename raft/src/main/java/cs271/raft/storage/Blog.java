package cs271.raft.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cs271.raft.storage.BlogEntry;
public class Blog implements Serializable {
  private List<BlogEntry> blog;
  public Blog() {
    blog = new ArrayList<BlogEntry>();
  }
  public void addEntry(BlogEntry entry) {
    blog.add(entry);
  }
  public void print() {
    for (int i = 0; i < blog.size(); i++) {
      System.out.println(blog.get(i).getUser() + ": " + blog.get(i).getPost());
    }
  }
}