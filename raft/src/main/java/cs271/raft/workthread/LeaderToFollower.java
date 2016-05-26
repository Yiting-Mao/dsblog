package cs271.raft.workthread;

import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import cs271.raft.util.TimeOut;
import cs271.raft.server.Leader;
import cs271.raft.message.AppendEntryRpc;
import cs271.raft.message.Message;
import cs271.raft.message.Message.MessageType;
import cs271.raft.message.RpcReply;
import cs271.raft.storage.Log;


public class LeaderToFollower implements Runnable{
  private String ip;
  private Leader leader;
  private List<Integer> workList;
  private TimeOut timeOut;
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  public LeaderToFollower(String ip, Socket socket, Leader leader) throws IOException {
    this.ip = ip;
    this.socket = socket;
    in = new ObjectInputStream(socket.getInputStream()); //IOException
    out = new ObjectOutputStream(socket.getOutputStream()); //IOException   
    this.leader = leader;
    workList = new LinkedList<Integer>();
    timeOut = new TimeOut(1);
  }
  
  public void addWork(int index) {
    workList.add(index);
  }
  private void sendAppendEntry (int index, Log log) throws IOException {
    int term = leader.getCurrentTerm();
    String leaderId = ip;
    int prevLogIndex = index - 1;
    int prevLogTerm = leader.getLog().getTerm(prevLogIndex);    
    int leaderCommit = leader.getCommitIndex();
    AppendEntryRpc append = new AppendEntryRpc(MessageType.APPENDENTRY, term, leaderId, prevLogIndex, prevLogTerm, log, leaderCommit);
    out.writeObject(append); //IOException   
  }
  
  private void updateInfo(int index) {
    leader.setSingleMatch(this.ip, index);
    leader.setSingleNext(this.ip, index + 1);
    leader.updateCommit();
  }
  public void run(){
    while(true) {
      if (!workList.isEmpty()) {
        int index = workList.get(0);
        int matchIndex = leader.getSingleMatch(ip);
        if (index > matchIndex) {
          Log log = new Log(leader.getLog().getEntries(index));
          try {
            sendAppendEntry(index, log);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          workList.remove(0);
        } else {
          workList.remove(0);
        }
      } else if (timeOut.isTimeOut()) {
        try {
          sendAppendEntry(leader.getLog().size(), null);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }   
      
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
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
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