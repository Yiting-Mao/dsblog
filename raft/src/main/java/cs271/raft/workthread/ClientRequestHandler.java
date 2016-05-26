package cs271.raft.workthread;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import cs271.raft.message.ClientRequest;
import cs271.raft.message.Message;
import cs271.raft.message.Message.MessageType;
import cs271.raft.message.ToClient;
import cs271.raft.server.Leader;
import cs271.raft.server.Follower;
import cs271.raft.storage.Log;
import cs271.raft.storage.LogEntry;
import cs271.raft.storage.Blog;
import cs271.raft.storage.BlogEntry;

public class ClientRequestHandler implements Runnable {
  private Socket socket;
  private Leader leader;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  public ClientRequestHandler(Socket socket, Leader leader) {
    this.socket = socket;
    this.leader = leader;
    try {
      this.socket.setSoTimeout (10000);
      in = new ObjectInputStream(socket.getInputStream());
      out = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void run() {
    System.out.println("handling requests from client.");
    try {
        while(true) {
          Message message = (Message) in.readObject();
          if (message.getType() == MessageType.CLIENTREQUEST) {
            ClientRequest request = (ClientRequest) message;
            if (request.getOp() == 'p') {
              if (request.getPost() != null) {
                BlogEntry be = new BlogEntry(request.getUser(), request.getPost());
                LogEntry le = new LogEntry(be, leader.getCurrentTerm());
                int index = leader.getLog().addLog(le);
                leader.spreadWork(index);
                try {
                   Thread.sleep(200);
                } catch (Exception e) {
                   System.out.println(e);
                }
                if (leader.getCommitIndex() >= index) {
                  ToClient toClient = new ToClient(MessageType.TOCLIENT, true, null);
                  out.writeObject(toClient);
              }
            }
          } else if (request.getOp() == 'l') {
            ToClient toClient = new ToClient(MessageType.TOCLIENT, true, null);
            out.writeObject(toClient);
            Blog blog = leader.getBlog();
            out.writeObject(blog);
          } else if (request.getOp() == 'q') {
            System.out.println(request.getUser() + " log out");
            break;
          }
        } 
        socket.close();
      }
    } catch (SocketTimeoutException e) {
      System.out.println("Time Out");
      try {
        socket.close();
      } catch (IOException e2) {
       e2.printStackTrace();
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
  
}