package cs271.raft.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.io.IOException;
import java.net.ServerSocket;


import cs271.raft.server.Server;
import cs271.raft.server.State;
import cs271.raft.util.Configuration;
import cs271.raft.workthread.LeaderToFollower;
import cs271.raft.workthread.ClientRequestHandler;

public class Leader extends Server {
  /* String is the ip address, considering during configuration change,
   * different ips may be assigned to the same process id num
   * this will cause slower execution though, may change in the future.
   */
  private Map<String, Integer> nextIndex; 
  private Map<String, Integer> matchIndex;
  private Map<String, LeaderToFollower> toFollowers;
  private ServerSocket ss;
  
  /* keep track of agreed ips for each log entry */
  //private Map<Integer, AgreeCollector> agrees; 
  public Leader() {   
  }
  public Leader(String ip) {
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
    int lastIndex = this.getLog().size() - 1;
    for (int i = 0; i < Configuration.getIps().size(); i++) {
      String ip = Configuration.getIps().get(i);
      if (this.ip.equals(ip)) continue;
      nextIndex.put(ip, lastIndex + 1);
      matchIndex.put(ip, -1);
    }
    if (Configuration.isInChange()) {
      for (int i = 0; i < Configuration.getNewIps().size(); i++) {
        String ip = Configuration.getNewIps().get(i);
        if (this.ip.equals(ip)) continue;
        if (!nextIndex.containsKey(ip)) {
          nextIndex.put(ip, lastIndex + 1);
          matchIndex.put(ip, -1);
        }
      }
    }
  }
  
  public void start() {
    System.out.println("Starting as a leader");
    for (int i = 0; i < Configuration.getIps().size(); i++) {
      String ip = Configuration.getIps().get(i);
      if (this.ip.equals(ip)) continue;
      try {
        Socket socket = new Socket(ip, Configuration.getPORT());
        LeaderToFollower toFollower = new LeaderToFollower(ip, socket, this);
        Thread t = new Thread(toFollower);
        t.start();       
        toFollowers.put(ip, toFollower);
        System.out.println("Connected to follower" + ip);

      } catch (Exception e) {
        System.out.println("Can't connect");
        e.printStackTrace();
      }
      
    }
    if (Configuration.isInChange()) {
      for (int i = 0; i < Configuration.getNewIps().size(); i++) {
        String ip = Configuration.getNewIps().get(i);
        if (this.ip.equals(ip)) continue;
        if (!nextIndex.containsKey(ip)) {          
          try {
            Socket socket = new Socket(ip, Configuration.getPORT());
            LeaderToFollower toFollower = new LeaderToFollower(ip, socket, this);
            Thread t = new Thread(toFollower);
            t.start();
            System.out.println("Connected to follower" + ip);
            toFollowers.put(ip, toFollower);
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } 
          
        }
      }
    }
    try {
      ss = new ServerSocket(Configuration.getPORT());
      System.out.println("System listening:" + ss);
      while(true) {
        Socket incoming = ss.accept();
        System.out.println("System connecting and accepted:" + incoming);
        /* creates a new thread to due with this connection, continues accepting other socket */
        ClientRequestHandler handler = new ClientRequestHandler(incoming, this);
        Thread t = new Thread(handler);
        t.start();
      }
    } catch (Exception se) {
      se.printStackTrace();
      System.exit(0);
    } 
  }
  
  /**
    * add log entry to worklist of every LeaderToFollower thread
    */
  public void spreadWork(int index) {
    for (Map.Entry<String, LeaderToFollower> entry : toFollowers.entrySet()) {
      entry.getValue().addWork(index);
    }
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
  
  public void addToFollower(String ip, LeaderToFollower toFollower) {
    toFollowers.put(ip, toFollower);
  }
  
  public void updateCommit() {
    List<Integer> tmp = new ArrayList<Integer>();
    for (Map.Entry<String, Integer> entry : matchIndex.entrySet()) {
      tmp.add(entry.getValue()); 
    }
    Collections.sort(tmp);
    int mid = tmp.get(tmp.size() / 2);
    if (mid > commitIndex) {
      /*
       * TODO: apply to state machine
       */
      System.out.println("mid = " + mid);
      System.out.println("commitIndex = " + commitIndex);
     // this.getLog().print();
      commit(mid);
    }
  } 

}