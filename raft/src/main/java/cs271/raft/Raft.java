
package cs271.raft;

import java.io.IOException;

import cs271.raft.client.Client;
import cs271.raft.server.State;
import cs271.raft.server.Leader;
import cs271.raft.server.Server;
import cs271.raft.server.Follower;

public class Raft implements Runnable {
  private State state;
  private Server server;
  public Raft() {
    
  }
  public Raft(State state, Server server) {
    this.server = server;
    this.state = state;
  }
  public void run() {
    switch(state) {
      case LEADER: 
        Leader leader = (Leader) server;
        try {
          leader.start();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        break;
      case FOLLOWER:
        Follower follower = (Follower) server;
        follower.start();
        break;
    }
  }
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    if(args.length == 1) {
      char type = args[0].charAt(0);
      if (type == 'l') {
        Leader leader = new Leader();
        leader.start();
      } else if (type == 'f') {
        Follower follower = new Follower();
        follower.start();
      } else if (type == 'c') {
        Client client = new Client();
        client.interact();    
      }
    } else {
      System.out.println("use argument 'l' to start a leader, 'f' to start a follower, 'c' to start a client");
    }
  }
}
