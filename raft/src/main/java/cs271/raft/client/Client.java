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
  public Client() throws IOException {
    init();
  }
  private void init() throws IOException {    
    Random rand = new Random();    
    int num = rand.nextInt(Configuration.getSize());
    leaderIp = Configuration.getIps().get(num);
    bin = new BufferedReader(new InputStreamReader(System.in));
    connect();  //IOException
    System.out.println("Input your name...");
    /* get inputs from user */    
    name = bin.readLine(); //IOException
 
  }
  
  private void connect() throws IOException {  
    socket = new Socket(leaderIp, Configuration.getPORT()); 
    in = new ObjectInputStream(socket.getInputStream());
    out = new ObjectOutputStream(socket.getOutputStream());
  }
  
  private void post(String post) throws IOException, ClassNotFoundException {
    ClientRequest request = new ClientRequest(MessageType.CLIENTREQUEST, name, 'p', post);  
    out.writeObject(request);
    ToClient reply = (ToClient)in.readObject();
    if(!reply.isSuccess()) {
      leaderIp = reply.getInfo();
      connect();
      post(post);
    }         
  } 
  
  private void lookUp() throws IOException, ClassNotFoundException {
    ClientRequest request = new ClientRequest(MessageType.CLIENTREQUEST, name, 'l', null);
    out.writeObject(request);
    ToClient reply = (ToClient)in.readObject(); //ClassNotFoundException
    if(reply.isSuccess()) {
      Blog blog = (Blog)in.readObject();
      System.out.println("------blog starts------");
      blog.print();
      System.out.println("------blog ends------");
    } else {
      leaderIp = reply.getInfo();
      connect();
      lookUp();
    }   
  } 
  public void interact() throws ClassNotFoundException {
    try {                  
      System.out.println("use operation:");
      System.out.println("'<p> <content>' to post");
      System.out.println("'<l>' to look up");
      System.out.println("'<q>' to quit");
      while(true) {
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
