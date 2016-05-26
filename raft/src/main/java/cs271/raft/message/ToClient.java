package cs271.raft.message;

import java.io.Serializable;
import cs271.raft.message.Message;

public class ToClient extends Message implements Serializable{
  /**
   * 
   */
  private static final long serialVersionUID = 6127652900541499949L;
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