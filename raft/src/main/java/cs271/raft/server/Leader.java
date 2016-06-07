package cs271.raft.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.net.Socket;
import java.net.ServerSocket;

import cs271.raft.Raft;
import cs271.raft.server.Server;
import cs271.raft.server.State;
import cs271.raft.util.Configuration;
import cs271.raft.util.Majority;
import cs271.raft.workthread.leader.LeaderToFollower;
import cs271.raft.workthread.leader.IncomingRequestHandler;
import cs271.raft.workthread.leader.ConnectionManager;
import cs271.raft.workthread.leader.ReconfigurationHandler;

public class Leader extends Server {
  /* String is the ip address, considering during configuration change,
   * different ips may be assigned to the same process id num
   * this will cause slower execution though, may change in the future.
   */
  private Map<String, Integer> nextIndex; 
  private Map<String, Integer> matchIndex;
  private Map<String, LeaderToFollower> toFollowers;
  //private Map<String, Socket> connected;
  private List<String> unconnected;
  private List<IncomingRequestHandler> handlers;
  private ConnectionManager manager;
  private ServerSocket ss;
  
  /* keep track of agreed ips for each log entry */
  //private Map<Integer, AgreeCollector> agrees; 
  public Leader() {   
  }
  public Leader(String ip) throws Exception {
    super(State.LEADER, ip);
    init();
  }
  public Leader(Server s) {
    super(State.LEADER, s);
    init();
  }
  private void init() {
    nextIndex = new HashMap<String, Integer>();
    matchIndex = new HashMap<String, Integer>();
    toFollowers = new HashMap<String, LeaderToFollower>();
    //connected = new HashMap<String, Socket>();
    unconnected = new ArrayList<String>();
    handlers = new ArrayList<IncomingRequestHandler>();
    int lastIndex = this.getLog().size() - 1;
    for (int i = 0; i < conf.getIps().size(); i++) {
      String ip = conf.getIps().get(i);
      if (this.ip.equals(ip)) continue;
      nextIndex.put(ip, lastIndex + 1);
      matchIndex.put(ip, -1);
      unconnected.add(ip);
    }
    if (conf.isInChange()) {
      for (int i = 0; i < conf.getNewIps().size(); i++) {
        String ip = conf.getNewIps().get(i);
        if (this.ip.equals(ip)) continue;
        if (!nextIndex.containsKey(ip)) {
          nextIndex.put(ip, lastIndex + 1);
          matchIndex.put(ip, -1);
          unconnected.add(ip);
        }
      }
    }
  }
  
  public void start() {
    System.out.println("--------------------------------------------------------------------------------------");
    System.out.println("Starting as a leader");
    setAlive(true);
    manager = new ConnectionManager(this);
    new Thread(manager).start();
    if (conf.isInChange()) {
      ReconfigurationHandler reconfigure = new ReconfigurationHandler(this, 1);
      new Thread(reconfigure).start();
    }
    try {
      ss = new ServerSocket(Configuration.getPORT());
      System.out.println("Leader listening:" + ss);
      while(true) {
        Socket incoming = ss.accept();
        System.out.println("Leader connecting and accepted:" + incoming);
        /* creates a new thread to due with this connection, continues accepting other socket */
        IncomingRequestHandler handler = new IncomingRequestHandler(incoming, this);
        new Thread(handler).start();
        handlers.add(handler);
      }
    } catch (Exception se) {
    } 
    System.out.println("Leader stops Listening");
  }
  
  /**
    * add log entry to worklist of every LeaderToFollower thread
    */
  public void spreadWork(int index) {
    for (Map.Entry<String, LeaderToFollower> entry : toFollowers.entrySet()) {
      entry.getValue().addWork(index);
    }
  }
  
  
  public void updateCommit() {    
    int mid = Majority.getValue(matchIndex, conf, ip);
    System.out.println("Mid: " + mid + "CommitIndex" + commitIndex);
    if (mid > commitIndex) {
      commit(mid);
      System.out.println("commitIndex:" + commitIndex);
    }
  } 
  public void stop() {
    setAlive(false);
    try {
      ss.close();
    } catch (Exception e) {
      e.printStackTrace();
    } 
    manager.stop();
    for (int i = 0; i < handlers.size(); i++) {
      handlers.get(i).stop();
    }
    for (Map.Entry<String, LeaderToFollower> entry : toFollowers.entrySet()) {
      entry.getValue().stop(); 
    }
  }
  
  public boolean reconfigure(String newIds) {
    ReconfigurationHandler handler = new ReconfigurationHandler(this, 0, newIds);
    new Thread(handler).start();
    try {
      Thread.sleep(800);
    } catch (Exception e) {
      System.out.println("Leader Reconfigure Sleep Interrupted");
    }
    System.out.println(handler.getStage());
    if (handler.getStage() >= 1) { //considers true when old and new has committed
      return true;
    } else {
      return false;
    }   
  }
  
  public void updateFollowers() {
    System.out.println("updating followers");
    for (Map.Entry<String, LeaderToFollower> entry : toFollowers.entrySet()) {
      if (!conf.contains(entry.getKey())) {
        entry.getValue().stop(); 
      }  
    }      
    unconnected = new ArrayList<String>();
    
    List<String> Ips = conf.getIps();
    int lastIndex = this.getLog().size() - 1;
    for (int i = 0; i < Ips.size(); i++) {
      String ip = Ips.get(i);
      if (this.ip.equals(ip)) continue;
      if (!toFollowers.containsKey(ip)) {
        unconnected.add(ip);
      } 
      if (!nextIndex.containsKey(ip)) {
        nextIndex.put(ip, lastIndex + 1);
      }
      if (!matchIndex.containsKey(ip)) {
        matchIndex.put(ip, -1);
      }        
    }
    
    if (conf.isInChange()) {
      List<String> newIps = conf.getNewIps();
      for (int i = 0; i < newIps.size(); i++) {
        String ip = newIps.get(i);
        if (this.ip.equals(ip)) continue;
        if (!toFollowers.containsKey(ip) && !unconnected.contains(ip)) {
          unconnected.add(ip);
        }
        if (!nextIndex.containsKey(ip)) {
          nextIndex.put(ip, lastIndex + 1);
        }
        if (!matchIndex.containsKey(ip)) {
          matchIndex.put(ip, -1);
        }    
      }
    }
    
  }
  public void turnToFollower() {
    stop();
    Raft raft = new Raft(State.FOLLOWER, this);
    new Thread(raft).start();
  }
  
  public int getSingleMatch(String ip) {
    return matchIndex.get(ip);
  }
  
  public void setSingleMatch(String ip, int index) {
    matchIndex.put(ip, index);
  }
  
  public int getSingleNext(String ip) {
    return nextIndex.get(ip);
  }
  
  public void setSingleNext(String ip, int index) {
    nextIndex.put(ip, index);
  }
  public void removeToFollower(String ip) {
    toFollowers.remove(ip);
  }
  public void addToFollower(String ip, LeaderToFollower toFollower) {
    toFollowers.put(ip, toFollower);
  }
  // public Map<String, Socket> getConnected() {
//     return connected;
//   }
//
//   public void setConnected(Map<String, Socket> connected) {
//     this.connected = connected;
//   }
  
  public List<String> getUnconnected() {
    return unconnected;
  }
  
  public void setUnconnected(List<String> unconnected) {
    this.unconnected = unconnected;
  }
  public void addUnconnected(String unconnected) {
    this.unconnected.add(unconnected);
  }
  
  public List<IncomingRequestHandler> getHandlers() {
    return handlers;
  }

}