package cs271.raft.storage;

import cs271.raft.storage.BlogEntry;

public class LogEntry {
  private BlogEntry blogEntry;
  private int term;
  public LogEntry() {
    
  }
  public LogEntry(BlogEntry be, int term) {
    blogEntry = be;
    this.term = term;
  }  
  public int getTerm() {
	  return  this.term;
  }
  public void setTerm(int term) {
	  this.term = term;
  }
  public BlogEntry getBlogEntry() {
	  return this.blogEntry;
  }
  public void setBlogEntry(BlogEntry blogEntry) {
	  this.blogEntry = blogEntry;
  }
}