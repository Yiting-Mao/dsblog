package cs271.raft.client;

import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cs271.raft.message.MessageType;
import cs271.raft.message.ToClient;
import cs271.raft.message.ClientRequest;
import cs271.raft.storage.Blog;
import cs271.raft.util.Configuration;

/**
 * Client API to interact with users, users can input their names, and post
 * supported operations are post, look up, and quit
 *
 * @author Yiting Mao
 * @since 2016-05-23
 */
public class Client {
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private BufferedReader bin;
  private String leaderIp;
  private List<String> aliveServers;
  private List<String> deadServers;
  private Configuration conf;
  private String name;
  public Client() throws IOException {
    init();
    
  }
  private void init() throws IOException {   
    aliveServers = new ArrayList<String>();
    deadServers = new ArrayList<String>();
    conf = new Configuration("");
    conf.print();
    for (int i = 0; i < conf.getIps().size(); i++) {
      String ip = conf.getIps().get(i);
      aliveServers.add(ip);
    }
    randomLeader();
    bin = new BufferedReader(new InputStreamReader(System.in)); 
    System.out.println(leaderIp);
  }
  
  private void randomLeader() {
    Random rand = new Random();    
    int num = rand.nextInt(aliveServers.size());
    leaderIp = aliveServers.get(num);
  }
  
  private void connect() {  
    try {
      if (out != null) out.close();      
      if (in != null) in.close();     
      if (socket != null) socket.close();
    } catch (Exception e) {
      System.out.println("Abnormal Close");
    }
    try {      
      socket = new Socket(leaderIp, Configuration.getPORT());   
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream()); 
      if (deadServers.contains(leaderIp)) {
        deadServers.remove(leaderIp);
        aliveServers.add(leaderIp);
      }
    } catch (Exception e) {
      System.out.println("can't connect");
      deadServers.add(leaderIp);
      aliveServers.remove(leaderIp);
      if (aliveServers.size() > deadServers.size()) {
        randomLeader();
        connect();
      } else {
        System.out.println("System isn't working, press 'q' to quit and try later");
      }     
    }
  }
  
  private void post(String post) {
    try {
      ClientRequest request = new ClientRequest(MessageType.CLIENTREQUEST, name, 'p', post);  
      out.writeObject(request);
      System.out.println("request sent");
      ToClient reply = (ToClient)in.readObject();
      System.out.println(reply.isSuccess());
      if(!reply.isSuccess()) {
        leaderIp = reply.getInfo();
        connect();
        post(post);
      }       
    } catch (Exception e) {
      connect();
      post(post);
    }
      
  } 
  
  private void lookUp() {
    try {
      ClientRequest request = new ClientRequest(MessageType.CLIENTREQUEST, name, 'l', null);
      out.writeObject(request);
      ToClient reply = (ToClient)in.readObject(); //ClassNotFoundException
      if(reply.isSuccess()) {
        Blog blog = (Blog)in.readObject();
        blog.print();
      } else {
        leaderIp = reply.getInfo();
        connect();
        lookUp();
      } 
    } catch (Exception e) {
      connect();
      lookUp();
    }      
  } 
  
  private void reconfigure(String newIds) {
    try {
      ClientRequest request = new ClientRequest(MessageType.CLIENTREQUEST, name, 'c', newIds);  
      out.writeObject(request);
      System.out.println("request sent");
      ToClient reply = (ToClient)in.readObject();
      System.out.println(reply.isSuccess());
      /**
        * TODO: update client's configuration
        */
      if(!reply.isSuccess()) {
        leaderIp = reply.getInfo();
        connect();
        reconfigure(newIds);
      }       
    } catch (Exception e) {
      connect();
      reconfigure(newIds);
    }
      
  }
  public void interact() {
   
    System.out.println("Input your name...");
    /* get inputs from user */     
    try {               
      name = bin.readLine(); //IOException
      connect();  //IOException 
    } catch (Exception e) {
      
    }
    
    while(true) {
      System.out.println("------------------------------------------------------------------------------------------------------------------");
      System.out.println("use: '<p> <content>' to post, '<l>' to look up, '<c> <id> <id>...(1-5)' to set new configuration, '<q>' to quit...");
      String input = null;
      try {
        input = bin.readLine();
      } catch (Exception e) {
        break;
      }
      char op = input.charAt(0);
      if (op == 'l') {
        lookUp();
      } else if (op == 'p') {
        input = input.substring(1).trim();
        post(input);
      } else if (op == 'c') {
        input = input.substring(1).trim();
        reconfigure(input);
      } else if (op == 'q') {
        ClientRequest request = new ClientRequest(MessageType.CLIENTREQUEST, name, 'q', null);
        try {
          out.writeObject(request);
        } catch (Exception e) {
          
        }       
        break;
      }      
    }    
    try {
      out.close();
      in.close();
      socket.close();
    } catch(Exception e) {
    }
  }
}
