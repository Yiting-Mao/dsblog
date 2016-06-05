package cs271.raft.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import cs271.raft.storage.Log;

public class PersistentStorage {
  private static File log_f;
  private static File term_f;
  private static File voted_f;
  static {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    log_f = new File(classLoader.getResource("log.txt").getFile());
    term_f = new File(classLoader.getResource("term.txt").getFile());
    voted_f = new File(classLoader.getResource("voted.txt").getFile());    
  }
  
  public static int getTerm() {
    int term = 0;
    try {
      BufferedReader in = new BufferedReader(new FileReader(term_f));
      term = Integer.parseInt(in.readLine());
      in.close();
    } catch (Exception e) {      
      e.printStackTrace();
    } 
    return term;
  }
  public static void setTerm(int term) { 
    System.out.println("Updating Rersistent Term");   
    try {
      FileOutputStream outstream = new FileOutputStream(term_f, false);
      PrintWriter out = new PrintWriter(outstream);
      out.print(term);   
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public static String getVoted() {
    String voted = null;
    try {
      BufferedReader in = new BufferedReader(new FileReader(voted_f)); 
      voted = in.readLine();
      in.close();
    } catch (Exception e) {      
      e.printStackTrace();
    }  
    return voted;
  }
  
  public static void setVoted(String voted) {
    try {
      FileOutputStream outstream = new FileOutputStream(voted_f, false);
      PrintWriter out = new PrintWriter(outstream);
      out.print(voted);
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }    
  }
  
  public static Log getLog() {
    Log log = null;
    try {
      FileInputStream instream = new FileInputStream(log_f);
      ObjectInputStream in = new ObjectInputStream(instream);
      log = (Log)in.readObject();
      in.close();
      instream.close();
    } catch (Exception e) {      
      System.out.println("No Persistent Log");
    } 
    return log;
  }
  
  public static void setLog(Log log) {
    try {
      FileOutputStream outstream = new FileOutputStream(log_f);
      ObjectOutputStream out = new ObjectOutputStream(outstream);
      out.writeObject(log);
      out.close();
      outstream.close();
      System.out.println("log written");
    } catch (Exception e) {
      e.printStackTrace();
    }   
  }
 }