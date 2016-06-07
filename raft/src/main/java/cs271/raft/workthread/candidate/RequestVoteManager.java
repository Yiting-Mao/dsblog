package cs271.raft.workthread.candidate;

import java.util.HashMap;
import java.util.List;
import java.net.Socket;
import java.util.Map;

import cs271.raft.server.Candidate;

import cs271.raft.util.Configuration;
import cs271.raft.util.TimeOut;
import cs271.raft.workthread.candidate.RequestVoteSender;


public class RequestVoteManager implements Runnable{
  private Candidate candidate;
  private TimeOut timeOut;
  private boolean alive;
  public RequestVoteManager() {
   
  }
  
  public RequestVoteManager(Candidate c) {
    this.candidate = c;
    alive = true;
    timeOut = new TimeOut();
  }
  public void run() {
    System.out.println("Starting RequestVoteManager");
    Map<String, Socket> connected = candidate.getConnected();
    Map<String, RequestVoteSender> senders = candidate.getSenders();
    List<String> unconnected = candidate.getUnconnected();
    while(alive) {
      for (int i = 0; i < unconnected.size(); i++) {
        String ip = unconnected.get(i);
        try {          
          Socket socket = new Socket(ip, Configuration.getPORT());
          connected.put(ip, socket);  
          unconnected.remove(i);
          i--; 
        } catch (Exception e) {
          System.out.println("Connect to " + ip + " failed");
        } 
      }
      timeOut.randomTimeOut();     
      candidate.setCurrentTerm(candidate.getCurrentTerm() + 1);
      candidate.setVotedFor(candidate.getIp());
      senders = new HashMap<String, RequestVoteSender>();
      for (Map.Entry<String, Socket> entry : connected.entrySet()) {
        String ip = entry.getKey();
        Socket s = entry.getValue();
        RequestVoteSender sender = new RequestVoteSender(ip, s, candidate);
        new Thread(sender).start();
        senders.put(ip, sender);
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException e1) {  
        System.out.println("Sleep Interrupted");
      }   
      int remain = timeOut.getRemain();
      if (remain > 0) {
        try {
          Thread.sleep(remain);
        } catch (InterruptedException e1) {  
          System.out.println("Sleep Interrupted");
        }
      }            
    } 
    System.out.println("RequestVoteManager Terminates"); 
  }
  public void stop() {
    alive = false;
  }
}