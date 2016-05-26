package cs271.raft.util;

import java.util.List;

public class Configuration {
  static int PORT = 6666;
  private static List<String> Ips;
  private static boolean inChange = false;
  private static List<String> newIps;
  
  public static void setConfiguration (List<String> ips) {
    newIps = null;    
    Ips = ips;
  }
  
  public static void changeConfiguration (List<String> Ips) {
    newIps = Ips;
    inChange = true;
  }
  
  public static void commitConfiguration () {
    if (inChange) {
      Ips = newIps;
      newIps = null;
      inChange = false;
    }
  }
  
  public static int getSize() {
    return Ips.size();
  }

  public static int getPORT() {
    return PORT;
  }

  public static void setPORT(int pORT) {
    PORT = pORT;
  }

  public static List<String> getIps() {
    return Ips;
  }

  public static void setIps(List<String> iPs) {
    Ips = iPs;
  }

  public static boolean isInChange() {
    return inChange;
  }

  public static void setInChange(boolean inChange) {
    Configuration.inChange = inChange;
  }

  public static List<String> getNewIps() {
    return newIps;
  }

  public static void setNewIps(List<String> newIps) {
    Configuration.newIps = newIps;
  }
}