package cs271.raft.storage;

import java.io.Serializable;

public class BlogEntry implements Serializable{
  private String user;
  private String post;
  public BlogEntry() {
    
  }
  public BlogEntry(String user, String post) {
    this.user = user;
    this.post = post;
  }
  public String getUser() {
    return this.user;
  }
  public String getPost() {
    return this.post;
  }
}