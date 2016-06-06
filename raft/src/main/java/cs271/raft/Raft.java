
package cs271.raft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import cs271.raft.client.Client;
import cs271.raft.server.State;
import cs271.raft.util.Configuration;
import cs271.raft.server.Leader;
import cs271.raft.server.Server;
import cs271.raft.server.Candidate;
import cs271.raft.server.Follower;

/* startpoint for the program, also used for state change */
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
        Leader leader = new Leader(server);
        leader.start();     
        break;
      case CANDIDATE:
        Candidate candidate = new Candidate(server);
        candidate.start();
        break;
      case FOLLOWER:
        Follower follower = new Follower(server);
        follower.start();
        break;
    }
  }
  
  /* startpoint of the program */
  public static void main(String args[]) throws Exception {
    System.out.println("usage: '<l>/<c>/<f> <id>' to start a leader/ candidate/ follower, 'u' to start a user");
    BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
    String input = bin.readLine();
    char type = input.charAt(0); 
    String ip = null;
    if (type == 'l' || type == 'c' || type == 'f') {
      int id = -1;
      id = Integer.parseInt(input.substring(1).trim());  
      ip = Configuration.getIds().get(id);
      if (ip == null) {
        System.exit(0);
      }
    }
    switch (type) {
    case 'l':
      Leader leader = new Leader(ip);
      leader.start();
      break;
    case 'f':    
      Follower follower = new Follower(ip);
      follower.start();
      break;
    case 'c':
      Candidate candidate = new Candidate(ip);
      candidate.start();
      break; 
    case 'u':
      Client client = new Client();
      client.interact();
      break;
    default:
       System.out.println("usage: '<l> <ip>' to start a leader, '<f> <ip>' to start a follower, 'c' to start a client");     
    }
  }
}
