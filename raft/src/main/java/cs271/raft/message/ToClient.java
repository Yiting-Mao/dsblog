package cs271.raft.message;

import java.io.Serializable;
import cs271.raft.message.Message;

public class ToClient extends Message implements Serializable{
  private boolean success;
  private String info;
  public ToClient (MessageType type, boolean success, String info) {
    super(type);
    this.success = success;
    this.info = info;
  }
  public boolean isSuccess() {
  	return success;
  }
  public void setSuccess(boolean success) {
  	this.success = success;
  }
  public String getInfo() {
    return this.info;
  }
}