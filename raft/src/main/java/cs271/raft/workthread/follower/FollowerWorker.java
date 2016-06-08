package cs271.raft.workthread.follower;

import java.net.Socket;
import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import cs271.raft.message.AppendEntryRpc;
import cs271.raft.message.Message;
import cs271.raft.message.MessageType;
import cs271.raft.message.RequestVoteRpc;
import cs271.raft.message.RpcReply;
import cs271.raft.message.ToClient;
import cs271.raft.server.Follower;
import cs271.raft.storage.BlogEntry;
import cs271.raft.storage.Log;
import cs271.raft.storage.LogEntry;
import cs271.raft.util.TimeOut;

/** It's for followers to due with appendentry, requestvote, and client request 
 */
public class FollowerWorker implements Runnable {
  private Follower follower;
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private String leaderIp;
  private boolean alive;
  
  public FollowerWorker() {
    
  }
  
  public FollowerWorker(Socket socket, Follower follower) {
    this.follower = follower;
    this.socket = socket;
    try {
      in = new ObjectInputStream(socket.getInputStream());
      out = new ObjectOutputStream(socket.getOutputStream());
      alive = true;
    } catch (Exception e) {
      //e.printStackTrace();
    }
  }
  
  private void acceptAE(AppendEntryRpc append) throws IOException { 
   // System.out.println("Accepting AE");         
    Log log = append.getLog();
    int prevIndex = append.getPrevLogIndex();
    if(log != null) {
      follower.getLog().appendLog(prevIndex, log); 
      List<LogEntry> entries = log.getEntries(0);
      for (int i = 0; i < entries.size(); i++) {
        BlogEntry be = entries.get(i).getBlogEntry();
        if (be.getUser().equals("Reconfigure Stage One")) {
          follower.getConf().changeConfiguration(be.getPost(), prevIndex + i + 1);
        } else if (be.getUser().equals("Reconfigure Stage Two")) {
          follower.getConf().commitConfiguration(prevIndex + i + 1);
        }
      }
    }      
    int lastIndex = follower.getLog().getLastIndex();
    int leaderCommit = append.getLeaderCommit();
   // System.out.println("LeaderCommit " + leaderCommit + "lastIndex " + lastIndex);
    if (leaderCommit > follower.getCommitIndex()) {
      int newIndex = leaderCommit > lastIndex ? lastIndex : leaderCommit;
      follower.commit(newIndex);
    }
    RpcReply reply = new RpcReply(MessageType.RPCREPLY, follower.getCurrentTerm(), true, lastIndex);
    out.writeObject(reply);
  }
  private boolean grantVote(RequestVoteRpc request) {
    int term = request.getTerm();
    String candidateIp = request.getCandidateIp();
    int lastLogIndex = request.getLastLogIndex();
    int lastLogTerm = request.getLastLogTerm();
    int myLastIndex = follower.getLog().getLastIndex();
    int myLastTerm = follower.getLog().getTerm(myLastIndex);
    int currentTerm = follower.getCurrentTerm();
    if (term < currentTerm) {
      return false;
    } else if (term == currentTerm) {
      if ((follower.getVotedFor() == null || follower.getVotedFor().equals(candidateIp))
      && (myLastTerm < lastLogTerm || myLastTerm == lastLogTerm && myLastIndex <= lastLogIndex)) {
        return true;
      } else {
        return false;
      }
    } else {
      follower.setCurrentTerm(term);
      if (myLastTerm < lastLogTerm || myLastTerm == lastLogTerm && myLastIndex <= lastLogIndex) {
        follower.setVotedFor(candidateIp);
        return true;
      } else {
        return false;
      }
    }
  }
  
  /* when follower's term <= leader's, handleAE */
  private void handleAE(AppendEntryRpc append) throws Exception {
    //System.out.println("HandleAE"); 
    int term = append.getTerm();      
      /* update leaderIp info */   
    leaderIp = append.getLeaderId();    
    if(follower.getCurrentTerm() < term ) {
      follower.setCurrentTerm(term);
    }       
    if (!leaderIp.equals(follower.getLeaderIp())) {
      follower.setLeaderIp(leaderIp);
      System.out.println("leader ip: " + leaderIp);
    }
    /* send the reply */
    int prevIndex = append.getPrevLogIndex();
    //System.out.println("append's prevIndex " + prevIndex);
    if (prevIndex >= 0) {
      LogEntry entry = follower.getLog().getEntry(prevIndex);
      if (entry != null && entry.getTerm() == append.getPrevLogTerm()) {
        acceptAE(append);
      } else {
        RpcReply reply = new RpcReply(MessageType.RPCREPLY, follower.getCurrentTerm(), false, append.getPrevLogIndex() + 1);
        out.writeObject(reply);
      }
    } else if (prevIndex == -1) {
      acceptAE(append);
    } else if (prevIndex == -2) {
      RpcReply reply = new RpcReply(MessageType.RPCREPLY, follower.getCurrentTerm(), true, -1);
      out.writeObject(reply);
    } else {
      System.out.println("prevIndex shouldn't < -2, something wrong");
    }
  }
  
  public void run() {
    System.out.println("Starting FollowerWorker...");
    try {
      while(alive) {
        Message message = (Message)in.readObject();
        MessageType type = message.getType();
        if (type == MessageType.APPENDENTRY) {
          //System.out.println("Processing AppendEntryRpc");
          AppendEntryRpc append = (AppendEntryRpc) message;
          int term = append.getTerm();
          //System.out.println("CurrentTerm:" + follower.getCurrentTerm() + "RequestTerm:" + append.getTerm());
          if (follower.getCurrentTerm() <= term) {   
            follower.getManager().reset();             
            handleAE(append);     
          } else {
            RpcReply reply = new RpcReply(MessageType.RPCREPLY, follower.getCurrentTerm(), false, append.getPrevLogIndex() + 1);
            out.writeObject(reply);
            break;
          }
        } else if (type == MessageType.CLIENTREQUEST) { 
          /* tell client the ip of leader */
          System.out.println("Processing ClientRequest");
          ToClient reply = new ToClient(MessageType.TOCLIENT, false, follower.getLeaderIp());
          out.writeObject(reply);
          break;
        } else if (type == MessageType.REQUESTVOTE) {
          RequestVoteRpc request = (RequestVoteRpc) message;
          boolean granted = grantVote(request);
          if (granted) follower.getManager().reset();
          RpcReply reply = new RpcReply(MessageType.RPCREPLY, follower.getCurrentTerm(), granted, request.getTerm());
          out.writeObject(reply);
          System.out.println("voted:" + granted);
          break;     
        }
      }
    } catch (Exception e) {
      System.out.println("IOException");
    }   
    try {
      out.close();
      in.close();
      socket.close();     
    } catch (Exception e) {
      //e.printStackTrace();
    }
    follower.getWorkers().remove(this);
    System.out.println("FollowerWorker Terminates");
  }
  
  public void stop() {
    alive = false;
  }
  
}