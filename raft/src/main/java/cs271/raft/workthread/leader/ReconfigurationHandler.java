package cs271.raft.workthread.leader;

import cs271.raft.server.Leader;
import cs271.raft.storage.BlogEntry;
import cs271.raft.storage.LogEntry;
public class ReconfigurationHandler implements Runnable {
  private Leader leader;
  //stage is first 0, when old-and-new is commited, stage becomes 1, when new commited, becomes 2
  private int stage; 
  private String newIds;
  public ReconfigurationHandler() {    
  }
  
  /* when a new leader starts after stage 1, it doesn't need to provide the newIds */
  public ReconfigurationHandler(Leader leader, int stage) {
    this.leader = leader;
    this.stage = stage;
  }
  
  public ReconfigurationHandler(Leader leader, int stage, String newIds) {
    this.leader = leader;
    this.stage = stage;
    this.newIds = newIds;
  }
  
  private void procedure1() {   
    BlogEntry be = new BlogEntry("Reconfigure Stage One", newIds);
    LogEntry le = new LogEntry(be, leader.getCurrentTerm());
    int index = leader.getLog().addEntry(le);
    leader.getConf().changeConfiguration(newIds, index);
    System.out.println("Reconfigure Stage One index:" + index);
    leader.updateFollowers();
    leader.spreadWork(index);
    try {
       Thread.sleep(400);
    } catch (Exception e) {
       System.out.println(e);
    }
    if (leader.getCommitIndex() >= index) {
      System.out.println("Stage One Commited");
      stage = 1;
    }
  }
  
  private void procedure2() {
    
    BlogEntry be = new BlogEntry("Reconfigure Stage Two", null);
    LogEntry le = new LogEntry(be, leader.getCurrentTerm());
    int index = leader.getLog().addEntry(le);
    leader.getConf().commitConfiguration(index);
    System.out.println("Reconfigure Stage Two index:" + index);
    leader.spreadWork(index);
    try {
       Thread.sleep(400);
    } catch (Exception e) {
       System.out.println(e);
    }
    if (leader.getCommitIndex() >= index) {
      System.out.println("Stage Two Commited");
      stage = 2;
      if (leader.getConf().contains(leader.getIp())) {
        leader.updateFollowers();
      } else if (leader.isAlive()) {
        leader.turnToFollower();
      }
    }
  }
  public void run() {
    System.out.println("Starting Reconfiguration Handler");
    if (stage == 0 && newIds != null) {
      procedure1();
    }
    //when a new leader starts with conf old and new, it init a reconfiguration with stage 1, but it is possible 
    //that old and new haven't be commited, if not, wait a while
    if (stage == 1 && leader.getConf().isInChange()) {
      if (leader.getCommitIndex() < leader.getConf().getIndex()) {
        try {
           Thread.sleep(400);
        } catch (Exception e) {
           System.out.println(e);
        }
      }
      procedure2();
    }
  }
  
  public int getStage() {
    return stage;
  }
}