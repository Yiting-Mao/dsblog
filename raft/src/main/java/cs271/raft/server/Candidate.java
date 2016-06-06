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
import cs271.raft.workthread.candidate.IncomingRequestHandler;
import cs271.raft.workthread.candidate.RequestVoteManager;
import cs271.raft.workthread.candidate.RequestVoteSender;

public class Candidate extends Server {
  private Map<String, Integer> agreedTerm;
  private Map<String, Socket> connected;
  private Map<String, RequestVoteSender> senders;
  private List<String> unconnected;
  private List<IncomingRequestHandler> handlers;
  private RequestVoteManager manager;
  private ServerSocket ss;
  
  public Candidate() {
    
  }  
  
  public Candidate(String ip) throws Exception {
    super(State.CANDIDATE, ip);
    init();
  }
  public Candidate(Server s) {
    super(State.CANDIDATE, s);
    init();
  }
  
  private void init() {
    agreedTerm = new HashMap<String, Integer>();
    connected = new HashMap<String, Socket>();
    senders = new HashMap<String, RequestVoteSender>();
    unconnected = new ArrayList<String>();
    handlers = new ArrayList<IncomingRequestHandler>();
    
    /* set all servers' agreedTerm to  -1 */
    for (int i = 0; i < conf.getIps().size(); i++) {
      String ip = conf.getIps().get(i);
      if (this.ip.equals(ip)) continue;
      agreedTerm.put(ip, -1);
      unconnected.add(ip);
    }
    if (conf.isInChange()) {
      for (int i = 0; i < conf.getNewIps().size(); i++) {
        String ip = conf.getNewIps().get(i);
        if (this.ip.equals(ip)) continue;
        if (!agreedTerm.containsKey(ip)) {
          agreedTerm.put(ip, -1);
          unconnected.add(ip);
        }
      }
    }
  }
  public void start() {
    System.out.println("--------------------------------------------------------------------------------------");
    System.out.println("Starting as a candidate");
    setAlive(true);
    manager = new RequestVoteManager(this);
    new Thread(manager).start();
    
    try {
      ss = new ServerSocket(Configuration.getPORT());
      System.out.println("Candidate listening:" + ss);
      while(true) {
        Socket incoming = ss.accept();
        System.out.println("Candidate connecting and accepted:" + incoming);
        /* creates a new thread to due with this connection, continues accepting other socket */
        IncomingRequestHandler handler= new IncomingRequestHandler(incoming, this);
        new Thread(handler).start();
        handlers.add(handler);
      }
    } catch (Exception se) {
    } 
    System.out.println("Candidate Stops listening");
  }
  public List<IncomingRequestHandler> getRequestHandlers() {
    return handlers;
  }
  public Map<String, Socket> getConnected() {
    return connected;
  }

  public void setConnected(Map<String, Socket> connected) {
    this.connected = connected;
  }

  public Map<String, RequestVoteSender> getSenders() {
    return senders;
  }

  public void setSenders(Map<String, RequestVoteSender> senders) {
    this.senders = senders;
  }
  
  public List<String> getUnconnected() {
    return unconnected;
  }
  
  public void setUnconnected(List<String> unconnected) {
    this.unconnected = unconnected;
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
  
  public void stop() {
    setAlive(false);
    try {
      ss.close();
    } catch (Exception e) {
      e.printStackTrace();
    }   
    manager.stop();
  }
  
  public void turnToLeader() {
    stop();
    Raft raft = new Raft(State.LEADER, this);
    Thread t = new Thread(raft);
    t.start();
  }
  
  public void turnToFollower() {
    stop();
    Raft raft = new Raft(State.FOLLOWER, this);
    Thread t = new Thread(raft);
    t.start();
  }
}
