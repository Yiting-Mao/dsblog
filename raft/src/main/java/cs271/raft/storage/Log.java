package cs271.raft.storage;

import java.util.ArrayList;
import java.util.List;

import cs271.raft.storage.LogEntry;

public class Log {
  private List<LogEntry> entries;
  public Log(){
	  this.entries = new ArrayList<LogEntry>();
    entries.add(new LogEntry(null, 0));
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
  
  public int addLog(LogEntry entry) {
    entries.add(entry);
    return entries.size();
  }
  
  public void appendLog(int prevIndex, Log log) {
    int size = entries.size();
    for(int i = 0; i < log.size(); i++) {
      if (prevIndex + i < size) {
        entries.set(prevIndex + i + 1, log.getEntry(i));
      } else {
        entries.add(log.getEntry(i));
      }
    }
  }
  //last entry is size() - 1
  public int size() {
    return entries.size();
  }
  
  public int getTerm(int index) {
    return entries.get(index).getTerm();
  }
  public LogEntry getEntry(int index) { //index starts from 1
    try {
      return entries.get(index);
    } catch (IndexOutOfBoundsException e) {
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
}