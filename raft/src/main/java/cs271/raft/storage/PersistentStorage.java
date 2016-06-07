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
import cs271.raft.util.Configuration;

public class PersistentStorage {
  private static File log_f;
  private static File term_f;
  private static File voted_f;
  private static File conf_f;
  static {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    log_f = new File(classLoader.getResource("log.txt").getFile());
    term_f = new File(classLoader.getResource("term.txt").getFile());
    voted_f = new File(classLoader.getResource("voted.txt").getFile()); 
    conf_f = new File(classLoader.getResource("conf.txt").getFile()); 
  }
  
  public static int getTerm() {
    int term = 0;
    try {
      BufferedReader in = new BufferedReader(new FileReader(term_f));
      term = Integer.parseInt(in.readLine());
      in.close();
    } catch (Exception e) {      
      //e.printStackTrace();
      System.out.println("Didn't get Persistent Term");
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
      //e.printStackTrace();
      System.out.println("Wrong when writing Persistent Term");
    } 
  }
  
  public static String getVoted() {
    String voted = null;
    try {
      BufferedReader in = new BufferedReader(new FileReader(voted_f)); 
      voted = in.readLine();
      in.close();
    } catch (Exception e) {      
      //e.printStackTrace();
      System.out.println("Didn't get Persistent VotedFor");
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
      //e.printStackTrace();
       System.out.println("Wrong when writing Persistent VotedFor");
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
     // System.out.println("log written");
    } catch (Exception e) {
      //e.printStackTrace();
      System.out.println("Wrong when writing Persistent Log");
    }   
  }
  
  public static Configuration getConfiguration() {
    Configuration conf = null;
    try {
      FileInputStream instream = new FileInputStream(conf_f);
      ObjectInputStream in = new ObjectInputStream(instream);
      conf = (Configuration)in.readObject();
      in.close();
      instream.close();
    } catch (Exception e) {      
      System.out.println("No Persistent Configuration");
    } 
    return conf;
  }
  
  public static void setConfiguration(Configuration conf) {
    try {
      FileOutputStream outstream = new FileOutputStream(conf_f);
      ObjectOutputStream out = new ObjectOutputStream(outstream);
      out.writeObject(conf);
      out.close();
      outstream.close();
     // System.out.println("configuration written");
    } catch (Exception e) {
      //e.printStackTrace();
      System.out.println("Wrong when writing Persistent Conf");
    } 
  }
 }