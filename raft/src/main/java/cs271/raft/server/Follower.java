package cs271.raft.server;

import java.util.ArrayList;
import java.util.List;
import java.net.ServerSocket;
import java.net.Socket;
import cs271.raft.Raft;
import cs271.raft.server.Server;
import cs271.raft.server.State;
import cs271.raft.util.Configuration;
import cs271.raft.workthread.follower.FollowerWorker;
public class Follower extends Server {
  private FollowerWorker fromLeader;
  private String leaderIp;
  private List<FollowerWorker> workers;
  private ServerSocket ss;
  public Follower(){
    
  }
  public Follower(String ip) {
    super(State.FOLLOWER, ip);
    workers = new ArrayList<FollowerWorker>();
  }
  public Follower(Server s) {
    super(State.FOLLOWER, s);
    workers = new ArrayList<FollowerWorker>();
  }
  public void start() {
    System.out.println("Starting as a follower");
    setAlive(true);
    try {
      ss = new ServerSocket(Configuration.getPORT());
      System.out.println("System listening:" + ss);
      while(true) {
        Socket incoming = ss.accept();
        System.out.println("System connecting and accepted:" + incoming);
        /* creates a new thread to due with this connection, continues accepting other socket */
        FollowerWorker worker = new FollowerWorker(incoming, this);
        new Thread(worker).start();
        workers.add(worker);
      }
    } catch (Exception e) {
    } 
    System.out.println("Follower Stops Listening");
  }
  public void stop() {
    setAlive(false);
    try {
      ss.close();
    } catch (Exception e) {
      e.printStackTrace();
    } 
    for (int i = 0; i < workers.size(); i++) {
      workers.get(i).stop();
    }
  }
  public void turnToCandidate() {
    stop();
    Raft raft = new Raft(State.CANDIDATE, this);
    new Thread(raft).start();
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
  public List<FollowerWorker> getWorkers() {
    return this.workers;
  }
}