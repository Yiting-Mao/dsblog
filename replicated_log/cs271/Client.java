package cs271;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.ArrayList;

import cs271.Message;

/**
 * Client API to interact with users, users can input their names,
 * choose the server id he/she wants to talk with,
 * supported operations are post, look up, sync, and quit
 *
 * @author Yiting Mao
 * @since 2016-04-20
 */
public class Client {
  static final String[] IPS = {"128.111.84.227", "128.111.84.250", "128.111.84.254"};
  static final int PORT = 6666;

  public static void main(String args[]) {
    
    try {           
      System.out.println("Input your name...");
      /* get inputs from user */
      BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
      String name = bin.readLine();   
      System.out.println("Choose your server number(0 to 2)...");
      int id = Integer.parseInt(bin.readLine());
      Socket s = new Socket(IPS[id], PORT);
      System.out.println("Socket:" + s);
      ObjectOutputStream toServer =  new ObjectOutputStream(s.getOutputStream());
      ObjectInputStream fromServer = new ObjectInputStream(s.getInputStream());
      
      while(true) {
        System.out.println("choose operation: input 'p' to post, 'l' to look up, 's' to sync, 'q' to quit...");
        char op = bin.readLine().charAt(0);
        if (op == 'l') {
          Message message = new Message(name, op, null);
          toServer.writeObject(message);
          System.out.println("------blog start------");
          message = (Message) fromServer.readObject();
          int num = Integer.parseInt(message.message);
          for (int i = 0; i < num; ++i) {
            message = (Message) fromServer.readObject();
            System.out.println(message.user + " said: " + message.message);
          }
           System.out.println("------blog end------");
        } else if (op == 'p') {
          System.out.println("input message...");
          String input = bin.readLine();
          Message message = new Message(name, op, input);
          toServer.writeObject(message);
        } else if (op == 'q') {
          Message message = new Message(name, op, null);
          toServer.writeObject(message);
          break;
        } else if (op == 's') {  
          while(true) {
            System.out.println("input datacenter id(0 to 2)...");
            String input = bin.readLine();
            id = Integer.parseInt(input);
            if(id < 0 || id > 2) {
              System.out.println("wrong input");
              continue;
            }
            Message message = new Message(name, op, input);
            toServer.writeObject(message);
            break;
          }  
        } else {
          System.out.println("wrong input");
        }
      }
      s.close();
    } catch(IOException e) {
      System.err.println(e.getMessage());
    } catch (ClassNotFoundException cn) {
       cn.printStackTrace();
    }
  }
}
