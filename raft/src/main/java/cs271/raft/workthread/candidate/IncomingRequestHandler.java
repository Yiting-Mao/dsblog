package cs271.raft.workthread.candidate;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import cs271.raft.message.AppendEntryRpc;
import cs271.raft.message.Message;
import cs271.raft.message.MessageType;
import cs271.raft.message.RequestVoteRpc;
import cs271.raft.message.RpcReply;
import cs271.raft.message.ToClient;
import cs271.raft.server.Candidate;

public class IncomingRequestHandler implements Runnable{
  private Candidate candidate;
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private boolean alive;
  
  public IncomingRequestHandler(Socket socket, Candidate candidate) {
    this.candidate = candidate;
    this.socket = socket;
    try {
      this.socket.setSoTimeout (100000);
      in = new ObjectInputStream(socket.getInputStream());
      out = new ObjectOutputStream(socket.getOutputStream());
    } catch (Exception e) {
      //e.printStackTrace();
      System.out.println("Wrong when creating Stream");
      alive = false;
    }
  } 
  
  public void run() {
    System.out.println("Starting Candidate IncomingRequestHandler...");
    try {
      while(alive) {
        Message message = (Message)in.readObject();
        MessageType type = message.getType();
        if (type == MessageType.APPENDENTRY) {
          System.out.println("Processing AppendEntryRpc");
          AppendEntryRpc append = (AppendEntryRpc) message;
          int term = append.getTerm();
          if (candidate.getCurrentTerm() <= term) {  
            candidate.setCurrentTerm(term);             
            candidate.turnToFollower();  
          } else {
            RpcReply reply = new RpcReply(MessageType.RPCREPLY, candidate.getCurrentTerm(), false, append.getPrevLogIndex() + 1);
            out.writeObject(reply);
            break;
          }
        } else if (type == MessageType.CLIENTREQUEST) { 
          /* tell client the ip of leader */
          System.out.println("Processing ClientRequest");
          Thread.sleep(300);
          ToClient reply = new ToClient(MessageType.TOCLIENT, false, candidate.getIp());
          out.writeObject(reply);
          break;
        } else if (type == MessageType.REQUESTVOTE) {
          RequestVoteRpc request = (RequestVoteRpc) message;
          int term = request.getTerm();
          if (candidate.getCurrentTerm() < term) {
            candidate.setCurrentTerm(term);
            candidate.turnToFollower();
          } else {
            RpcReply reply = new RpcReply(MessageType.RPCREPLY, candidate.getCurrentTerm(), false, request.getTerm());
            out.writeObject(reply);
          }
          break;     
        }
      }
    } catch (Exception e) {
      //e.printStackTrace();
      System.out.println("The other end closes");
    }   
    try {
      out.close();
      in.close();
      socket.close();      
    } catch (Exception e) {
      //e.printStackTrace();
    }
    candidate.getRequestHandlers().remove(this);
    System.out.println("Candidate IncomingRequestHandler Terminates");
  }
  public void stop() {
    alive = false;
  }
  
}