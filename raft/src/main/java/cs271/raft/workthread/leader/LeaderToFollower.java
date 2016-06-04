package cs271.raft.workthread.leader;

import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ConnectException;

import cs271.raft.util.TimeOut;
import cs271.raft.server.Leader;
import cs271.raft.message.AppendEntryRpc;
import cs271.raft.message.Message;
import cs271.raft.message.Message.MessageType;
import cs271.raft.message.RpcReply;
import cs271.raft.storage.Log;
import cs271.raft.util.Configuration;

/* This is for leader to send append entry/ heartbeat and due with the replies */
public class LeaderToFollower implements Runnable{
  private String ip;
  private Leader leader;
  private List<Integer> workList;
  private TimeOut timeOut;
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private boolean alive;
  public LeaderToFollower(String ip, Socket socket, Leader leader) throws IOException {
    this.ip = ip;
    this.socket = socket;   
    out = new ObjectOutputStream(socket.getOutputStream()); //IOException   
    in = new ObjectInputStream(socket.getInputStream()); //IOException
    this.leader = leader;
    workList = new LinkedList<Integer>();
    timeOut = new TimeOut(1);
    alive = true;
  }
  
  public void addWork(int index) {
    workList.add(index);
  }
  private void sendAppendEntry (int index, Log log) throws IOException{
    int term = leader.getCurrentTerm();
    String leaderId = leader.getIp();
    int prevLogIndex = index - 1;
    int prevLogTerm = leader.getLog().getTerm(prevLogIndex);    
    int leaderCommit = leader.getCommitIndex();
    AppendEntryRpc append = new AppendEntryRpc(MessageType.APPENDENTRY, term, leaderId, prevLogIndex, prevLogTerm, log, leaderCommit);
    out.writeObject(append); //IOException     
  }
  
  private void updateInfo(int index) {
    if (index >= 0) {
      leader.setSingleMatch(this.ip, index);
      /* by this way, nextIndex is useless */
      leader.setSingleNext(this.ip, index + 1); 
      leader.updateCommit();
    }    
  }
  
  private void reconnect() {
    try {
      socket.close();
      while(true) {
        try {
          socket = new Socket(ip, Configuration.getPORT());
          break;   
        } catch (ConnectException e) {
          System.out.println("Reconnect failed");
          try {
            Thread.sleep(800);
          } catch (InterruptedException e1) {  
            System.out.println("Sleep Interrupted");
          }
        } catch (UnknownHostException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } 
      }
      LeaderToFollower toFollower = new LeaderToFollower(ip, socket, leader);
      Thread t = new Thread(toFollower);
      t.start(); 
      leader.addToFollower(ip, toFollower);
    } catch (IOException e) {
      System.out.println("Exception while closing socket");
    }       
  }

  public void run(){
    System.out.println("Starting LeaderToFollower");   
    while(alive) {
      boolean sent = false;
      
      /* has something to send*/
      if (!workList.isEmpty()) {
        System.out.println("Sending AppendEntry");
        int index = workList.get(0);
        int matchIndex = leader.getSingleMatch(ip);
        
        /* leader thinks the follower haven't got this entry before */
        if (index > matchIndex) {
          System.out.println("sending entry" + index);
          Log log = new Log(leader.getLog().getEntries(index));
          try {
            sendAppendEntry(index, log); //IOException  
          } catch (IOException e) {
            System.out.println("Can't send AppendEntry");
            reconnect(); //reconnect creates a new thread, this thread ends
            break;
          }        
          workList.remove(0);
          sent = true;
        } else {
          workList.remove(0);
        }
        
        /* have nothing specific to send but reach a timeout */
      } else if (timeOut.isTimeOut()) {
        System.out.println("Sending Heartbeat");
        try {
          sendAppendEntry(leader.getLog().getLastIndex(), null);  //IOException  
        } catch (IOException e) {
          System.out.println("Can't send AppendEntry");
          reconnect();
          break;
        }     
        sent = true;
      }   
      
      /* due with the reply */
      if (sent) {
        try {
          Message message = (Message)in.readObject();
          if (message.getType() == MessageType.RPCREPLY) {
            RpcReply reply = (RpcReply) message;
            int term = reply.getTerm();
            boolean success = reply.isSuccess();
            int index = reply.getIndex();
            if (success) {
              updateInfo(index);
            } else if (term > leader.getCurrentTerm()) {
              /**
                * TODO: turn to follower
                */
            } else {
              workList.add(0, index - 1);
            }
          }
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          System.out.println("Can't get reply, trying reconnect");
          reconnect();
          break;
        }
        timeOut.refresh();
      }            
    }
  }

  public Socket getSocket() {
    return socket;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }
}