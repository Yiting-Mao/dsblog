package cs271;
import java.io.Serializable;

class Message implements Serializable {
  String user;
  char op;
  String message;
  public Message() {
    
  }
  public Message(String user, char op, String message) {
    this.user = user;
    this.op = op;
    this.message = message;
  }
  public String getUser() {
    return this.user;
  }
  public char getOp() {
    return this.op;
  }
  public String getMessage() {
    return this.message;
  }
  public void setUser(String user) {
    this.user = user;
  }
  public void setOp(char op) {
    this.op = op;
  }
  public void setMessage(String message) {
    this.message = message;
  }
}

