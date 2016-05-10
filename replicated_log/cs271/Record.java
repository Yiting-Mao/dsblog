package cs271;
import java.io.Serializable;

public class Record implements Serializable {
  int time;
  int id;
  String user;
  String post;
  public Record() {
    
  }
  public Record(int time, int id, String user, String post) {
    this.time = time;
    this.id = id;
    this.user = user;
    this.post = post;
  }
  public int getTime() {
    return time;
  }
  public int getId() {
    return id;
  }
  public String getUser() {
    return user;
  }
  public String getPost() {
    return post;
  }
  public void setLocalTime(int time) {
    this.time = time;
  }
  public void setNodeId(int id) {
    this.id = id;
  }
  public void setUser(String user) {
    this.user = user;
  }
  public void setPost(String post) {
    this.post = post;
  }
}
