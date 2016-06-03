package cs271.raft.message;

import java.io.Serializable;
public class Message implements Serializable {
  public enum MessageType {
    APPENDENTRY,
    REQUESTVOTE,
    RPCREPLY,
    CLIENTREQUEST,
    TOCLIENT;
  }
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