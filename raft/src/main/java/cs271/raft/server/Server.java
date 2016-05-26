package cs271.raft.server;

import cs271.raft.server.State;
import cs271.raft.storage.Log;
import cs271.raft.storage.Blog;
public class Server {  
  /* persistent state on all servers(update on stable storage before responding to RPCs) */
  int currentTerm; //latest term server has seen */
  int votedFor; // candidateId that received vote in current term(or null is none)
  Log log;
  Blog blog;
  
  /* volatile state on all servers */
  int commitIndex; //index of highest log entry known to be committed
  int lastApplied; //index of highest log entry applied to state machine
  
  State state;
  String ip;
  //private Configuration conf;  
  
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
  public Server(Server s, State state) {
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
	  currentTerm = 0;
	  votedFor = -1;
	  log = new Log();
	  commitIndex = 0;
	  lastApplied = 0;
    this.state = state;
    this.ip = ip;
    //conf = new Configuration();
    
  }
  public int getCurrentTerm() {
    return currentTerm;
  }

  public void setCurrentTerm(int currentTerm) {
    this.currentTerm = currentTerm;
  }
	
  public int getVotedFor() {
  	return votedFor;
  }
  public void setVotedFor(int votedFor) {
  	this.votedFor = votedFor;
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


}