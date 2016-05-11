package cs271;
import java.io.Serializable;

/**
 * The structure of each entry in the log
 *
 * @author Yiting Mao
 * @since 2016-04-20
 */
public class Record implements Serializable {
  /* local time of server when the event takes place */
  int time;
  /* id of server at which the event takes place */
  int id;
  String user;  
  /* The content of the post */
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
