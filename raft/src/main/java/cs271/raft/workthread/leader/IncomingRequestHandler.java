package cs271.raft.workthread.leader;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import cs271.raft.message.AppendEntryRpc;
import cs271.raft.message.ClientRequest;
import cs271.raft.message.Message;
import cs271.raft.message.MessageType;
import cs271.raft.message.RequestVoteRpc;
import cs271.raft.message.RpcReply;
import cs271.raft.message.ToClient;
import cs271.raft.server.Leader;
import cs271.raft.storage.LogEntry;
import cs271.raft.storage.Blog;
import cs271.raft.storage.BlogEntry;

/**
  * Created by Leader to handle client request. 
  * It is possible to receive appendentry/ requestvote as well.
  */
public class IncomingRequestHandler implements Runnable {
  private Socket socket;
  private Leader leader;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private boolean alive;
  public IncomingRequestHandler(Socket socket, Leader leader) {
    this.socket = socket;
    this.leader = leader;
    alive = true;
    try {
      this.socket.setSoTimeout (100000);
      in = new ObjectInputStream(socket.getInputStream());
      out = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
      alive = false;
    }
  }
  
  private void handleCR(ClientRequest request) throws Exception{
    if (request.getOp() == 'p') {
      if (request.getPost() != null) {      
        BlogEntry be = new BlogEntry(request.getUser(), request.getPost());
        LogEntry le = new LogEntry(be, leader.getCurrentTerm());
        int index = leader.getLog().addEntry(le);
        System.out.println("index:" + index + ", " +request.getUser() + " posted:" + request.getPost());
        leader.spreadWork(index);
        try {
           Thread.sleep(400);
        } catch (Exception e) {
           System.out.println(e);
        }
        if (leader.getCommitIndex() >= index) {
          ToClient toClient = new ToClient(MessageType.TOCLIENT, true, null);
          out.writeObject(toClient);
        }
      }
      
    } else if (request.getOp() == 'l') {
      System.out.println("Handling " + request.getUser() + "'s look up");
      ToClient toClient = new ToClient(MessageType.TOCLIENT, true, null);
      out.writeObject(toClient);    
      Blog blog = leader.getBlog();     
      out.reset();
      out.writeObject(blog);
      
    } else if (request.getOp() == 'c') {
      /* request.getPost() stores the newIds splited by space */
      System.out.println("Handling reconfigure:" + request.getPost());
      boolean result = leader.reconfigure(request.getPost());
      System.out.println("reconfigure " + result);
      ToClient toClient = new ToClient(MessageType.TOCLIENT, result, null);
      out.writeObject(toClient);     
      
    } else if (request.getOp() == 'q') {
      System.out.println(request.getUser() + " log out");
      alive = false;  
    }
  }
  private void handleRpc(int term) throws Exception {
    if (term > leader.getCurrentTerm()) {
      leader.turnToFollower();
    } else if (term < leader.getCurrentTerm()) {
      RpcReply reply = new RpcReply(MessageType.RPCREPLY, leader.getCurrentTerm(), false, term);
      out.writeObject(reply);
    } else {
      System.out.println("A leader should be actually dead");
    }
  }

  public void run() {
    System.out.println("START: Leader IncomingRequestHandler");
    try {
      while(alive) {
        Message message = (Message) in.readObject();
        /* when leader stops, it will set alive to false, however, this thread will be waiting to read,
         * The first message from client after leader stops will still be received
         */
        if (!alive) break;
        if (message.getType() == MessageType.CLIENTREQUEST) {
          handleCR((ClientRequest)message);
        } else if (message.getType() == MessageType.REQUESTVOTE) {
          RequestVoteRpc request= (RequestVoteRpc) message;
          handleRpc(request.getTerm());
          
        } else if (message.getType() == MessageType.APPENDENTRY) {
          AppendEntryRpc append = (AppendEntryRpc)message;
          handleRpc(append.getTerm());
        }               
      }    
    } catch (SocketTimeoutException e) {
      System.out.println("Time Out");          
    } catch (Exception e) {
       e.printStackTrace();
    } 
    
    try {
      in.close();
      out.close();
      socket.close();
    } catch (IOException e2) {
     e2.printStackTrace();
    } 
    leader.getHandlers().remove(this);
    System.out.println("END: Leader IncomingRequestHandler");
  }

  public Socket getSocket() {
    return socket;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  public ObjectInputStream getIn() {
    return in;
  }

  public void setIn(ObjectInputStream in) {
    this.in = in;
  }

  public ObjectOutputStream getOut() {
    return out;
  }

  public void setOut(ObjectOutputStream out) {
    this.out = out;
  }
  
  public void stop() {
    alive = false;
  }
  
}