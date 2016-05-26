package cs271;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import cs271.DataCenter;
import cs271.Message;
import cs271.SyncDC;

/**
 * This class dues with client request and other server's sync request. 
 * It is established by the main function of DataCenter.
 *
 * @author Yiting Mao
 * @since 2016-04-20
 */
public class Worker implements Runnable {
  Socket socket;
  DataCenter dc;
  ObjectInputStream inStream = null;
  ObjectOutputStream outStream = null;
  public Worker(Socket socket, DataCenter dc) {
    this.socket = socket;
    try {
      this.socket.setSoTimeout (9000);
    } catch (SocketException se) {
      System.err.println ("Unable to set socket option SO_TIMEOUT");
    }
    this.dc = dc;
  }
  
  /* update log, blog, time, and timetable based on message from client */
  private synchronized void addBlog(Message m) {
    dc.addBlog(m);
    int time = dc.getTime();
    int id = dc.getId();
    Record r = new Record(time, id, m.getUser(), m.getMessage());
    dc.addLog(r);
    dc.setTime(time + 1);
    dc.addLocalEntry();
  }
  /* establish a new socket and thread to receive sync data from other servers */
  private void getData(Message message) {
    int num = Integer.parseInt(message.message);
    if (num == dc.id) return;
    try {
      Socket s = new Socket(dc.IPS[num], dc.PORT);
      SyncDC sync = new SyncDC(s, dc);
      Thread t = new Thread(sync);
      t.start();
    } catch (SocketException se) {
      se.printStackTrace();
      System.exit(0);
    } catch (UnknownHostException se) {
      se.printStackTrace();
      System.exit(0);
    } catch (IOException e) {
       e.printStackTrace();
    } 
  }
  
  /* send log and time table to other servers */
  private void sendData(Message message) {
    int num = Integer.parseInt(message.message);
    List<Record> log = new ArrayList<Record>();
    Iterator<Record> it = dc.log.iterator();
    while (it.hasNext()) {
      Record record = it.next();
      int id = record.getId();
      int time = record.getTime();
      if(dc.table[num][id] < time) {
        log.add(record);
      }
    }    
    TransData td = new TransData(dc.id, log, dc.table);
    try {
      outStream.writeObject(td);
    } catch (IOException e) {
       e.printStackTrace();
    }    
  }
  
  public void run() {
    try {
        System.out.println("Connected");
        inStream = new ObjectInputStream(socket.getInputStream());
        outStream =  new ObjectOutputStream(socket.getOutputStream());
        while(true) {
          Message message = (Message) inStream.readObject();
          System.out.println("received");
          //client input quit
          if(message.op == 'q') {
            System.out.println(message.user + " log out");
            break;
          } else if(message.op == 'p') {
            if(message.message != null) {
              System.out.println(message.user + " said: " + message.message);
              addBlog(message);
              dc.printTable();
              dc.printLog();  
            }      
          } else if(message.op == 'l') {
            int num = dc.blog.size();
            Message m = new Message(null, 'l' , Integer.toString(num));
            outStream.writeObject(m);
            for (int i = 0; i < num; i++) {
              outStream.writeObject(dc.blog.get(i));
            } 
          } else if (message.op == 's') {
            getData(message);
          } else if (message.op == 'r') {
            sendData(message);
            break;
          }
        }
        socket.close();
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
}
