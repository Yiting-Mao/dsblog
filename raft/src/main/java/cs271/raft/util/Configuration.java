package cs271.raft.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class Configuration {
  static int PORT = 6666;
  
  private static Map<Integer, String> Ids;
  static {
    Ids = new HashMap<Integer, String>();
    Ids.put(1, "128.111.84.202");
    Ids.put(2, "128.111.84.228");
    Ids.put(3, "128.111.84.253");
  } 
  private static List<String> Ips = Arrays.asList("128.111.84.202", "128.111.84.228", "128.111.84.253");
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

  public static void setPORT(int port) {
    PORT = port;
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

  public static Map<Integer, String> getIds() {
    return Ids;
  }

  public static void setIds(Map<Integer, String> ids) {
    Ids = ids;
  }
}