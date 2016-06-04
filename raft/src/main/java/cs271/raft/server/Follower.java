package cs271.raft.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import cs271.raft.server.Server;
import cs271.raft.server.State;
import cs271.raft.util.Configuration;
import cs271.raft.workthread.follower.FollowerWorker;
public class Follower extends Server {
  private FollowerWorker fromLeader;
  private String leaderIp;
  private ServerSocket ss;
  public Follower(){
    
  }
  public Follower(String ip) {
    super(State.FOLLOWER, ip);
  }
  public Follower(Server s) {
    super(State.FOLLOWER, s);
  }
  public void start() {
    System.out.println("Starting as a follower");
    try {
      ss = new ServerSocket(Configuration.getPORT());
      System.out.println("System listening:" + ss);
      while(true) {
        Socket incoming = ss.accept();
        System.out.println("System connecting and accepted:" + incoming);
        /* creates a new thread to due with this connection, continues accepting other socket */
        FollowerWorker worker = new FollowerWorker(incoming, this);
        Thread t = new Thread(worker);
        t.start();
      }
    } catch (SocketException se) {
      se.printStackTrace();
      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public String getLeaderIp() {
    return leaderIp;
  }
  public void setLeaderIp(String ip) {
    this.leaderIp = ip;
  }
  public FollowerWorker getFromLeader() {
    return fromLeader;
  }
  public void setFromLeader(FollowerWorker fromLeader) {
    this.fromLeader = fromLeader;
  }
}