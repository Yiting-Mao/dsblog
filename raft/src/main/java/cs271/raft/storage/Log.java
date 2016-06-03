package cs271.raft.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cs271.raft.storage.LogEntry;
import cs271.raft.storage.PersistentStorage;

public class Log implements Serializable{
  private List<LogEntry> entries;
  public Log(){
	  this.entries = new ArrayList<LogEntry>();
  }
  public Log(List<LogEntry> entries) {
    this.entries = entries;
  }
  public List<LogEntry> getLog() {
	  return this.entries;
  }
  
  public void setLog(List<LogEntry> entries) {
	  this.entries = entries;
  }
  
  public int addEntry(LogEntry entry) {
    entries.add(entry);
    System.out.println("Update Persistent Log");
    PersistentStorage.setLog(this);
    return entries.size() - 1;
  }
  
  public void appendLog(int prevIndex, Log log) {
    int last = entries.size() - 1;
    if (prevIndex > last) {
      System.out.println("prevIndex can't match, shouldn't append");
      System.exit(0);
    }
    for (int i = 0; i < log.size(); i++) {
      if (prevIndex + i + 1 <= last) {
        entries.set(prevIndex + i + 1, log.getEntry(i));
      } else {
        entries.add(log.getEntry(i));
      }
    }
    /*is it possible that follower's original log is longer than leaders? */
    if (prevIndex + log.size() < last) {
      delete(prevIndex + log.size() + 1);
    }
    System.out.println("Update Persistent Log");
    PersistentStorage.setLog(this);
  }
  //last entry is size() - 1
  public int size() {
    return entries.size();
  }
  
  public int getLastIndex() {
    return entries.size() - 1;
  }
  
  public int getTerm(int index) {
    if(index < 0) return 0;
    return entries.get(index).getTerm();
  }
  public LogEntry getEntry(int index) { 
    try {
      return entries.get(index);
    } catch (IndexOutOfBoundsException e) {
      System.out.println("Log index out of bound");
      return null;
    }
    
  }
  public List<LogEntry> getEntries(int index) {
    List<LogEntry> tmp_entries = new ArrayList<LogEntry>();
    for (int i = index; i < entries.size(); i++) {
      tmp_entries.add(entries.get(i));
    }
    return tmp_entries;
  }
  public void delete(int index) {
    for(int i = getLastIndex(); i >= index; i--) {
      entries.remove(i);
    }
  }
  public void print() {
    for (int i = 0; i < entries.size(); i++) {
      System.out.println(entries.get(i).getTerm() + ":" + entries.get(i).getBlogEntry().getUser() + ":" + entries.get(i).getBlogEntry().getPost());
    }
  }
}