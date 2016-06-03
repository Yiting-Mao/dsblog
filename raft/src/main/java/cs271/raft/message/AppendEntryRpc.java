package cs271.raft.message;

import java.io.Serializable;

import cs271.raft.storage.Log;
import cs271.raft.message.Message;

public class AppendEntryRpc extends Message implements Serializable{
  private int term;
  private String leaderId; //it actually stores leader ip
  private int prevLogIndex;
  private int prevLogTerm;
  private Log log;
  private int leaderCommit;
  
  public AppendEntryRpc(MessageType type, int term, String leaderId, int prevLogIndex, int prevLogTerm, Log log, int leaderCommit) {
    super(type);
	  this.term = term;
    this.leaderId = leaderId;
    this.prevLogIndex = prevLogIndex;
    this.prevLogTerm = prevLogTerm;
    this.log = log;
    this.leaderCommit = leaderCommit;
  }

  public int getTerm() {
  	return term;
  }

  public void setTerm(int term) {
  	this.term = term;
  }

  public int getLeaderCommit() {
  	return leaderCommit;
  }

  public void setLeaderCommit(int leaderCommit) {
  	this.leaderCommit = leaderCommit;
  }

  public Log getLog() {
  	return log;
  }

  public void setLog(Log log) {
  	this.log = log;
  }

  public int getPrevLogTerm() {
  	return prevLogTerm;
  }

  public void setPrevLogTerm(int prevLogTerm) {
  	this.prevLogTerm = prevLogTerm;
  }

  public int getPrevLogIndex() {
  	return prevLogIndex;
  }

  public void setPrevLogIndex(int prevLogIndex) {
  	this.prevLogIndex = prevLogIndex;
  }

  public String getLeaderId() {
  	return leaderId;
  }

  public void setLeaderId(String leaderId) {
  	this.leaderId = leaderId;
  }
}