package cs271.raft.server;

import cs271.raft.server.State;
import cs271.raft.storage.Log;
import cs271.raft.storage.Blog;
import cs271.raft.storage.BlogEntry;
import cs271.raft.storage.PersistentStorage;
public class Server {  
  /* persistent state on all servers(update on stable storage before responding to RPCs) */
  int currentTerm; //latest term server has seen */
  String votedFor; // candidateId that received vote in current term(or null is none)
  Log log;
  Blog blog;
  
  /* volatile state on all servers */
  int commitIndex; //index of highest log entry known to be committed
  int lastApplied; //index of highest log entry applied to state machine
  
  State state;
  String ip;
  //private Configuration conf; 
  boolean alive; 
  
  public Server() {
    
  }
  public Server(Server s) {
    currentTerm = s.currentTerm;
    votedFor = s.votedFor;
    log = s.log;
    blog = s.blog;
    commitIndex = s.commitIndex;
    lastApplied = s.lastApplied;
    state = s.state;
    ip = s.ip;
   
  }
  public Server(State state, Server s) {
    currentTerm = s.currentTerm;
    votedFor = s.votedFor;
    log = s.log;
    blog = s.blog;
    commitIndex = s.commitIndex;
    lastApplied = s.lastApplied;
    ip = s.ip;
    this.state = state;
  }
  public Server(State state, String ip) {
	  currentTerm = PersistentStorage.getTerm();
	  votedFor = PersistentStorage.getVoted();
	  log = PersistentStorage.getLog();
    System.out.println("Init term: " + currentTerm);
    System.out.println("Init voted: " + votedFor);
    if (log == null) {
      log = new Log();
    } else {
      System.out.println("getting persistent log...");
      log.print();
    }
	  blog = new Blog();
	  commitIndex = -1;
	  lastApplied = -1;
    this.state = state;
    this.ip = ip;
    //conf = new Configuration();
    
  }
  public int getCurrentTerm() {
    return currentTerm;
  }

  public void setCurrentTerm(int currentTerm) {
    this.currentTerm = currentTerm;
    PersistentStorage.setTerm(currentTerm);
    /* when updated to a new term, the corresponding votedFor is set to null */
    setVotedFor(null);
  }
	
  public String getVotedFor() {
  	return votedFor;
  }
  public void setVotedFor(String votedFor) {
  	this.votedFor = votedFor;
    PersistentStorage.setVoted(votedFor);
  }
  public Log getLog() {
  	return log;
  }
  public void setLog(Log log) {
  	this.log = log;
  }

  public Blog getBlog() {
    return blog;
  }
  public void setBlog(Blog blog) {
    this.blog = blog;
  }
  public int getCommitIndex() {
  	return commitIndex;
  }
  public void setCommitIndex(int commitIndex) {
  	this.commitIndex = commitIndex;
  }
  public int getLastApplied() {
  	return lastApplied;
  }
  public void setLastApplied(int astApplied) {
  	this.lastApplied = astApplied;
  }
  
  public String getIp() {
    return this.ip;
  }
  
  public void setIp(String ip) {
    this.ip = ip;
  }
  public void commit(int newIndex) {
    for (int i = commitIndex + 1; i <= newIndex; i++) {
      BlogEntry entry = this.getLog().getEntry(i).getBlogEntry();
      if(entry == null) System.out.println("entry null");
      this.getBlog().addEntry(entry);
    }
    commitIndex = newIndex;
  }
  public boolean isAlive() {
    return alive;
  }
  public void setAlive(boolean alive) {
    this.alive = alive;
  }
}