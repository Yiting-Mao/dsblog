package cs271;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import cs271.Record;
import cs271.Message;
import cs271.Worker;

/** 
 * DataCenter keeps track of a log, which is a list of Records, a blog, which is a list of Messages, and a time table.
 * It also increases its local time whenever a new blog is posted.
 *
 * @author Yiting Mao
 * @since 2016-04-20
 */
public class DataCenter {
  static final String[] IPS = {"128.111.84.227", "128.111.84.250", "128.111.84.254"};
  static final int PORT = 6666;
  static final int DCNUM = 3;
  int id;
  int time;
  int[][] table;
  List<Record> log;
  List<Message> blog;
  
  public DataCenter(int id) {
    this.id = id;
    time = 1;
    table = new int[DCNUM][DCNUM];
    log = new ArrayList<Record>();
    blog = new ArrayList<Message>();
  }

  public int getId() {
    return this.id;
  }
  public int getTime() {
    return time;
  }
  public int[][] getTable() {
    return table;
  }
  public void printTable() {
    for (int i = 0; i < DCNUM; ++i) {
      for (int j = 0; j < DCNUM; ++j) {
        System.out.print(table[i][j] + " ");
      }
      System.out.println();
    }
  }
  public void addLocalEntry() {
    table[id][id]++;
  }
  public List<Record> getLog() {
    return log;
  }
  public void printLog() {
    for (int i = 0; i < log.size(); ++i) {
      System.out.println(log.get(i).time + " " + log.get(i).id + " " + log.get(i).user + " " + log.get(i).post);
    }
  }
  public void setId(int id) {
    this.id = id;
  }
  public void setTime(int time) {
    this.time = time;
  }
  public void addLog(Record r) {
    log.add(r);  
  }
  public void addBlog(Message m) {
    blog.add(m);
  }
  /**
   * Creates a new DataCenter object when the program starts, the data center id can be specified.
   * Opens a ServerSocket and accepts socket connections through a while loop.
   * When a new socket connection is established, open a new Worker thread to due with the requests.
   */
  public static void main(String[] args) {
    BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
    int id = -1;
    while(true) {
      System.out.println("specify data center id(0 to 2)");
      try{
        id = Integer.parseInt(bin.readLine());
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(0);
      }      
      if (id < 0 || id > 2) {
        System.out.println("wrong input");
      } else {
        break;
      }
    } 
    DataCenter dc = new DataCenter(id);   
    try {
      ServerSocket ss = new ServerSocket(PORT);
      System.out.println("System start:" + ss);
      while(true) {
        Socket incoming = ss.accept();
        System.out.println("System connecting and accepted:" + incoming);
        Worker worker = new Worker(incoming, dc);
        Thread t = new Thread(worker);
        t.start();
      }
    } catch (SocketException se) {
      se.printStackTrace();
      System.exit(0);
    } catch (IOException e) {
        e.printStackTrace();
    } 
  }
}


