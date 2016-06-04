package cs271.raft.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs271.raft.Raft;
import cs271.raft.server.Server;
import cs271.raft.server.State;
import cs271.raft.util.Configuration;
import cs271.raft.workthread.RequestVoteManager;
import cs271.raft.workthread.RequestVoteSender;

public class Candidate extends Server {
  private Map<String, Integer> agreedTerm;
  private Map<String, Socket> connectedServers;
  private Map<String, RequestVoteSender> senders;
  private List<String> unconnectedServers;
  private RequestVoteManager manager;
  private ServerSocket ss;
  
  public Candidate() {
    
  }  
  
  public Candidate(String ip) {
    super(State.CANDIDATE, ip);
    init();
  }
  public Candidate(Server s) {
    super(State.CANDIDATE, s);
    init();
  }
  
  private void init() {
    agreedTerm = new HashMap<String, Integer>();
    connectedServers = new HashMap<String, Socket>();
    senders = new HashMap<String, RequestVoteSender>();
    unconnectedServers = new ArrayList<String>();
    
    /* set all servers' agreedTerm to  -1 */
    for (int i = 0; i < Configuration.getIps().size(); i++) {
      String ip = Configuration.getIps().get(i);
      if (this.ip.equals(ip)) continue;
      agreedTerm.put(ip, -1);
      unconnectedServers.add(ip);
    }
    if (Configuration.isInChange()) {
      for (int i = 0; i < Configuration.getNewIps().size(); i++) {
        String ip = Configuration.getNewIps().get(i);
        if (this.ip.equals(ip)) continue;
        if (!agreedTerm.containsKey(ip)) {
          agreedTerm.put(ip, -1);
          unconnectedServers.add(ip);
        }
      }
    }
  }
  public void start() {
    System.out.println("Starting as a candidate");
    manager = new RequestVoteManager(this);
    Thread t = new Thread(manager);
    t.start();
    
    try {
      ss = new ServerSocket(Configuration.getPORT());
      System.out.println("System listening:" + ss);
      while(true) {
        Socket incoming = ss.accept();
        System.out.println("System connecting and accepted:" + incoming);
        /* creates a new thread to due with this connection, continues accepting other socket */
      
      }
    } catch (Exception se) {
      se.printStackTrace();
    } 
    System.out.println("Stop listening");
  }

  public Map<String, Socket> getConnectedServers() {
    return connectedServers;
  }

  public void setConnectedServers(Map<String, Socket> connectedServers) {
    this.connectedServers = connectedServers;
  }

  public Map<String, RequestVoteSender> getSenders() {
    return senders;
  }

  public void setSenders(Map<String, RequestVoteSender> senders) {
    this.senders = senders;
  }
  
  public List<String> getUnconnectedServers() {
    return unconnectedServers;
  }
  
  public void setUnconnectedServers(List<String> unconnected) {
    this.unconnectedServers = unconnected;
  }
  
  public void updateAgreedTerm(String ip, int term) {
    agreedTerm.put(ip, term);
  }
  public boolean hasMajority() {
    List<Integer> tmp = new ArrayList<Integer>();
    for (Map.Entry<String, Integer> entry : agreedTerm.entrySet()) {
      tmp.add(entry.getValue()); 
    }
    Collections.sort(tmp);
    int mid = tmp.get(tmp.size() / 2);
    if (mid == this.getCurrentTerm()) {
      return true;
    } else {
      return false;
    }
  }  
  public void turnToLeader() {
    try {
      ss.close();
    } catch (Exception e) {
      e.printStackTrace();
    }   
    manager.stop();
    Raft raft = new Raft(State.LEADER, this);
    Thread t = new Thread(raft);
    t.start();
  }
  
  public void turnToFollower() {
    try {
      ss.close();
    } catch (Exception e) {
      e.printStackTrace();
    }   
    manager.stop();
    Raft raft = new Raft(State.FOLLOWER, this);
    Thread t = new Thread(raft);
    t.start();
  }
}
