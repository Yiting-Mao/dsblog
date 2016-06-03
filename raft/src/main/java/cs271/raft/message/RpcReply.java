package cs271.raft.message;

import java.io.Serializable;
import cs271.raft.message.Message;

public class RpcReply extends Message implements Serializable{
  private int term;
  private boolean success;
  private int index;
  public RpcReply (MessageType type, int term, boolean success, int index) {
    super(type);
    this.term = term;
    this.success = success;
    this.index = index;
  }
  public int getTerm() {
  	return term;
  }
  public void setTerm(int term) {
  	this.term = term;
  }
  public boolean isSuccess() {
  	return success;
  }
  public void setSuccess(boolean success) {
  	this.success = success;
  }
  public int getIndex() {
    return index;
  }
  public void setIndex(int index) {
    this.index = index;
  }
}