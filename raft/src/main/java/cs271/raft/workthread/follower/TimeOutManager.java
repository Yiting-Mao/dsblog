package cs271.raft.workthread.follower;

import cs271.raft.server.Follower;
import cs271.raft.util.TimeOut;
import cs271.raft.util.Configuration;
public class TimeOutManager implements Runnable {
  private Follower follower;
  private TimeOut timeOut;
  private boolean alive;
  public TimeOutManager (Follower follower) {
    this.follower = follower;   
    alive = true;
  }
  
  public void run() {
    timeOut = new TimeOut();
    while(alive) {
      if (timeOut.isTimeOut()) {
        System.out.println("Follower TimeOut");
        if (follower.getConf().contains(follower.getIp()) && follower.isAlive()) {
          follower.turnToCandidate();
        } else {
          timeOut.refresh();
        }
      } else {
        try {
          Thread.sleep(timeOut.getRemain());
        } catch (Exception e) {
          System.out.println("TimeOutManager Sleep Interrupted");
        }
      }      
    }
  }
  
  public void stop() {
    alive = false;
  }
  
  public void reset() {
    timeOut.refresh();
  }
}