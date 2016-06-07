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
import cs271.raft.message.MessageType;
import cs271.raft.message.RpcReply;
import cs271.raft.storage.Log;

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
    //leader.getConnected().remove(ip);
    leader.removeToFollower(ip);
    leader.addUnconnected(ip);
  }

  public void run(){
    System.out.println("Talking to follower: " + ip);   
    while(alive) {
      boolean sent = false;
      
      /* has something to send*/
      if (!workList.isEmpty()) {
        int index = workList.get(0);
        int matchIndex = leader.getSingleMatch(ip);
        
        /* leader thinks the follower haven't got this entry before */
        if (index > matchIndex) {
          System.out.println("sending entry" + index + " -> " + ip);
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
        try {
        
          sendAppendEntry(leader.getLog().getLastIndex() + 1, null);  //IOException  
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
            } else if (term > leader.getCurrentTerm() && leader.isAlive()) {
              
              /* if leader's term is outdated, turn to a follower */
              leader.setCurrentTerm(term);
              leader.turnToFollower();
            } else {
              workList.add(0, index - 1);
            }
          }
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          //e.printStackTrace();
          System.out.println("Can't get reply, trying reconnect");
          reconnect();
          break;
        }
        timeOut.refresh();
      }            
    }
    try {
      out.close();
      in.close();
      socket.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Something wrong when trying to close the socket");
    }
    leader.removeToFollower(ip);
    System.out.println("LeaderToFollower with " + ip + " Terminates");
  }

  public void stop() {
    alive = false;
  }
  public Socket getSocket() {
    return socket;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }
}