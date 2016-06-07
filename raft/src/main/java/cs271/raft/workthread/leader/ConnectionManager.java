package cs271.raft.workthread.leader;

import java.util.List;
import java.util.Map;
import java.net.Socket;

import cs271.raft.server.Leader;
import cs271.raft.util.Configuration;
import cs271.raft.workthread.leader.LeaderToFollower;

public class ConnectionManager implements Runnable {
  private Leader leader;
  private boolean alive;
  
  public ConnectionManager(Leader leader) {
    this.leader = leader;
    alive = true;
  } 
  
  public void run() {
    /* when doing changes to this local unconnected, will the leader's field also change? */
   // Map<String, Socket> connected = leader.getConnected();
    while(alive) {
      List<String> unconnected = leader.getUnconnected();
      if(!unconnected.isEmpty()) {
        for (int i = 0; i < unconnected.size(); i++) {
          String ip = unconnected.get(i);
          //System.out.println("Connecting " + ip);
          try {          
            Socket socket = new Socket(ip, Configuration.getPORT());
           // connected.put(ip, socket);  
            unconnected.remove(i);
            i--; 
            LeaderToFollower toFollower = new LeaderToFollower(ip, socket, leader);
            new Thread(toFollower).start();
            leader.addToFollower(ip, toFollower);
            System.out.println("Connected to " + ip);
          } catch (Exception e) {
            //System.out.println("Connect to " + ip + " failed");
          } 
        }
        leader.setUnconnected(unconnected);
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {  
        //e.printStackTrace();
        System.out.println("Sleep Interrupted");
      }
    }
    System.out.println("ConnectionManager Terminates");
  }
  
  public void stop() {
    alive = false;
  }
}