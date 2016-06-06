package cs271.raft.message;

import java.io.Serializable;
import cs271.raft.message.Message;
public class Message implements Serializable {
  private MessageType type;
  public Message() {
	  
  }
  public Message(MessageType type) {
    this.type = type;
  }
  public MessageType getType() {
  	return type;
  }
  public void setType(MessageType type) {
  	this.type = type;
  }
}