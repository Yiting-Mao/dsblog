package cs271.raft.message;

import java.io.Serializable;
import cs271.raft.message.Message;

class RequestVoteRpc extends Message implements Serializable{
  /**
	 * 
	 */
  private static final long serialVersionUID = -5970246788866602925L;
  private int term;
  private int candidateId;
  private int lastLogIndex;
  private int lastLogTerm;
  public RequestVoteRpc(MessageType type, int term, int id, int index, int lastTerm) {
    super(type);
    this.term = term;
    this.candidateId = id;
    this.lastLogIndex = index;
    this.lastLogTerm = lastTerm;
  }
  public int getTerm() {
  	return term;
  }
  public void setTerm(int term) {
  	this.term = term;
  }
  public int getCandidateId() {
  	return candidateId;
  }
  public void setCandidateId(int candidateId) {
  	this.candidateId = candidateId;
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