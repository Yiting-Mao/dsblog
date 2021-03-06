package cs271.raft.workthread.candidate;

import cs271.raft.message.RequestVoteRpc;
import cs271.raft.message.RpcReply;
import cs271.raft.server.Candidate;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import cs271.raft.message.MessageType;

/**
 * sends requestVote and get reply from followers,
 * if got the majority, turn to leader
 * or found a higher term, turn to follower
 */
public class RequestVoteSender implements Runnable{
  private String ip;
  private Socket socket;
  private Candidate candidate;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  
  public RequestVoteSender() {
    
  }
  public RequestVoteSender(String ip, Socket socket, Candidate candidate) {
    this.ip = ip;
    this.socket = socket;
    this.candidate = candidate;
    try {
      out = new ObjectOutputStream(socket.getOutputStream()); //IOException   
      in = new ObjectInputStream(socket.getInputStream()); //IOException
    } catch (Exception e) {
      //e.printStackTrace();
    }
     
  }
  
  public void run() {
    System.out.println("Sending requestvote to " + ip);
    int term = candidate.getCurrentTerm();
    String candidateIp = candidate.getIp();
    int lastLogIndex = candidate.getLog().getLastIndex();
    int lastLogTerm = candidate.getLog().getTerm(lastLogIndex);
    RequestVoteRpc request = new RequestVoteRpc(MessageType.REQUESTVOTE, term, candidateIp, lastLogIndex, lastLogTerm);
  
    try {
      out.writeObject(request);
      RpcReply reply = (RpcReply)in.readObject();
      if (reply.isSuccess()) {
        candidate.updateAgreedTerm(ip, reply.getIndex());
        if (candidate.hasMajority() && candidate.isAlive()) {
          System.out.println("Has votes from majority");
          candidate.turnToLeader();
        }
      } else if (reply.getTerm() > term && candidate.isAlive()) {
        candidate.setCurrentTerm(reply.getTerm());
        candidate.turnToFollower();
      }
      in.close();
      out.close(); 
      socket.close();  
    } catch (Exception e) {
      //e.printStackTrace();
      System.out.println("Connection Broken");
      candidate.getConnected().remove(ip);
      candidate.getUnconnected().add(ip);
    }  
    System.out.println("RequestVoteSender Terminates");
  }
}