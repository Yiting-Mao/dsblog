package cs271.raft.message;

import java.io.Serializable;
import cs271.raft.message.Message;

public class ClientRequest extends Message implements Serializable {
  private static final long serialVersionUID = -5931871829935756704L;
  private String user;
  private char op;
  private String post;
  public ClientRequest(MessageType type, String user, char op, String post) {
	  super(type);
	  this.user = user;
	  this.op = op;
	  this.post = post;
  }
  public String getUser() {
  	return user;
  }
  public void setUser(String user) {
  	this.user = user;
  }
  public char getOp() {
  	return op;
  }
  public void setOp(char op) {
  	this.op = op;
  }
  public String getPost() {
  	return post;
  }
  public void setPost(String post) {
  	this.post = post;
  }

	
}
