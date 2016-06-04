package cs271.raft.message;

import java.io.Serializable;
import cs271.raft.message.Message;

public class RequestVoteRpc extends Message implements Serializable{
  private int term;
  private String candidateIp;
  private int lastLogIndex;
  private int lastLogTerm;
  public RequestVoteRpc(MessageType type, int term, String ip, int index, int lastTerm) {
    super(type);
    this.term = term;
    this.candidateIp = ip;
    this.lastLogIndex = index;
    this.lastLogTerm = lastTerm;
  }
  public int getTerm() {
  	return term;
  }
  public void setTerm(int term) {
  	this.term = term;
  }
  public String getCandidateIp() {
  	return candidateIp;
  }
  public void setCandidateIp(String candidateIp) {
  	this.candidateIp = candidateIp;
  }
  public int getLastLogIndex() {
  	return lastLogIndex;
  }
  public void setLastLogIndex(int lastLogIndex) {
  	this.lastLogIndex = lastLogIndex;
  }
  public int getLastLogTerm() {
  	return lastLogTerm;
  }
  public void setLastLogTerm(int lastLogTerm) {
  	this.lastLogTerm = lastLogTerm;
  }
}