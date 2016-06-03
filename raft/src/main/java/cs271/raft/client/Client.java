package cs271.raft.client;

import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import cs271.raft.message.Message.MessageType;
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
  private String name;
  private Blog blog;
  public Client() throws IOException {
    init();
  }
  private void init() throws IOException {    
    Random rand = new Random();    
    int num = rand.nextInt(Configuration.getSize());
    leaderIp = Configuration.getIps().get(num);
    //leaderIp = "128.111.84.202";
    bin = new BufferedReader(new InputStreamReader(System.in)); 
    blog = null;
  }
  
  private void connect() throws IOException {  
    socket = new Socket(leaderIp, Configuration.getPORT()); 
    out = new ObjectOutputStream(socket.getOutputStream());
    in = new ObjectInputStream(socket.getInputStream());   
  }
  
  private void post(String post) throws IOException, ClassNotFoundException {
    ClientRequest request = new ClientRequest(MessageType.CLIENTREQUEST, name, 'p', post);  
    out.writeObject(request);
    System.out.println("request sent");
    ToClient reply = (ToClient)in.readObject();
    System.out.println(reply.isSuccess());
    if(!reply.isSuccess()) {
      leaderIp = reply.getInfo();
      socket.close();
      connect();
      post(post);
    }         
  } 
  
  private void lookUp() throws IOException, ClassNotFoundException {
    blog = null;
    ClientRequest request = new ClientRequest(MessageType.CLIENTREQUEST, name, 'l', null);
    out.writeObject(request);
    ToClient reply = (ToClient)in.readObject(); //ClassNotFoundException
    if(reply.isSuccess()) {
      blog = (Blog)in.readObject();
      blog.print();
    } else {
      leaderIp = reply.getInfo();
      socket.close();
      connect();
      lookUp();
    }   
  } 
  public void interact() throws ClassNotFoundException {
   
    System.out.println("Input your name...");
    /* get inputs from user */     
    try {               
      name = bin.readLine(); //IOException
      connect();  //IOException    
      while(true) {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("use: '<p> <content>' to post, '<l>' to look up, '<q>' to quit...");
        String input = bin.readLine();
        char op = input.charAt(0);
        if (op == 'l') {
          lookUp();
        } else if (op == 'p') {
          input = input.substring(1).trim();
          post(input);
        } else if (op == 'q') {
          ClientRequest request = new ClientRequest(MessageType.CLIENTREQUEST, name, 'q', null);
          out.writeObject(request);
          break;
        } 
      }
      socket.close();
    } catch(IOException e) {
      System.err.println(e.getMessage());
    }
  }
}
