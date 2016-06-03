package cs271.raft.workthread;

import java.net.Socket;
import java.net.SocketException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import cs271.raft.message.AppendEntryRpc;
import cs271.raft.message.Message;
import cs271.raft.message.Message.MessageType;
import cs271.raft.message.RpcReply;
import cs271.raft.message.ToClient;
import cs271.raft.server.Follower;
import cs271.raft.storage.Log;
import cs271.raft.storage.LogEntry;
public class FollowerWorker implements Runnable {
  private Follower follower;
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  
  public FollowerWorker() {
    
  }
  
  public FollowerWorker(Socket socket, Follower follower) {
    this.follower = follower;
    this.socket = socket;
  }
  
  private void acceptAE(AppendEntryRpc append) throws IOException {
    Log log = append.getLog();
    int prevIndex = append.getPrevLogIndex();
    if(log != null) {
      follower.getLog().appendLog(prevIndex, log);
    }      
    int lastIndex = follower.getLog().getLastIndex();
    int leaderCommit = append.getLeaderCommit();
    if (leaderCommit > follower.getCommitIndex()) {
      /*
       * TODO: Update blog
       */
      int newIndex = leaderCommit > lastIndex ? lastIndex : leaderCommit;
      follower.commit(newIndex);
    }
    RpcReply reply = new RpcReply(MessageType.RPCREPLY, follower.getCurrentTerm(), true, lastIndex);
    out.writeObject(reply);
  }
  public void run() {
    System.out.println("Starting FollowerWorker...");
    try {
      in = new ObjectInputStream(socket.getInputStream());
      out = new ObjectOutputStream(socket.getOutputStream());
      while(true) {
        Message message = (Message)in.readObject();
        if (message.getType() == MessageType.APPENDENTRY) {
          System.out.println("Processing AppendEntryRpc");
          AppendEntryRpc append = (AppendEntryRpc) message;
          int term = append.getTerm();
          if (follower.getCurrentTerm() <= term) {   
            
            /* update leaderIp info */       
            if(follower.getCurrentTerm() < term) {
              follower.setCurrentTerm(term);
              follower.setLeaderIp(append.getLeaderId());
              System.out.println("leader ip" + follower.getLeaderIp());
            } else if (follower.getLeaderIp() == null) {
              follower.setLeaderIp(append.getLeaderId());
              System.out.println("leader ip" + follower.getLeaderIp());
            }        
            
            /* send the reply */
            int prevIndex = append.getPrevLogIndex();
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
              System.exit(0);
            }
          } else { 
            /* follower's term > leader's term */
            RpcReply reply = new RpcReply(MessageType.RPCREPLY, follower.getCurrentTerm(), false, append.getPrevLogIndex() + 1);
            out.writeObject(reply);
          }          
        } else if (message.getType() == MessageType.CLIENTREQUEST) { 
          /* tell client the ip of leader */
          System.out.println("Processing ClientRequest");
          ToClient reply = new ToClient(MessageType.TOCLIENT, false, follower.getLeaderIp());
          out.writeObject(reply);
          break;
        }
      }
      socket.close();
    } catch (SocketException se) {
       se.printStackTrace();
       System.exit(0);
    } catch (IOException e) {
       e.printStackTrace();
    } catch (ClassNotFoundException cn) {
       cn.printStackTrace();
    }
  }
}