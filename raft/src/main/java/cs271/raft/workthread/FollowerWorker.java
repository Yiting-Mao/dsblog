package cs271.raft.workthread;

import java.net.Socket;
import java.net.SocketException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
    try {
      in = new ObjectInputStream(socket.getInputStream());
      out = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void run() {
    try {
      while(true) {
        Message message = (Message)in.readObject();
        if (message.getType() == MessageType.APPENDENTRY) {
          AppendEntryRpc append = (AppendEntryRpc) message;
          int term = append.getTerm();
          if (follower.getCurrentTerm() <= term) {
            if(follower.getCurrentTerm() < term) {
              follower.setCurrentTerm(term);
              follower.setLeaderIp(socket.getRemoteSocketAddress().toString());
            }        
            Log log = append.getLog();
            int prevIndex = append.getPrevLogIndex();
            LogEntry entry = follower.getLog().getEntry(prevIndex);
            //replicate successfully
            if (entry != null && entry.getTerm() == append.getPrevLogTerm()) {
              
              follower.getLog().appendLog(prevIndex, log);
              int lastIndex = append.getPrevLogIndex() + log.size();
              int leaderCommit = append.getLeaderCommit();
              if (leaderCommit > follower.getCommitIndex()) {
                /*
                 * TODO: Update blog
                 */
                  follower.setCommitIndex(leaderCommit > lastIndex ? lastIndex : leaderCommit);
              }
              /*
               * TODO: store in file
               */
              RpcReply reply = new RpcReply(MessageType.RPCREPLY, follower.getCurrentTerm(), true, lastIndex);
              out.writeObject(reply);
            } else {
              RpcReply reply = new RpcReply(MessageType.RPCREPLY, follower.getCurrentTerm(), false, append.getPrevLogIndex() + 1);
              out.writeObject(reply);
            }            
          } else {
            RpcReply reply = new RpcReply(MessageType.RPCREPLY, follower.getCurrentTerm(), false, append.getPrevLogIndex() + 1);
            out.writeObject(reply);
          }          
        } else if (message.getType() == MessageType.CLIENTREQUEST) { //tell client the ip of leader
          ToClient reply = new ToClient(MessageType.TOCLIENT, false, follower.getLeaderIp());
          out.writeObject(reply);
        }
      }
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