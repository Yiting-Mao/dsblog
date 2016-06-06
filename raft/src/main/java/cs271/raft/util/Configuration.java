package cs271.raft.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class Configuration implements Serializable{
  private static int PORT = 6667;
  
  private static Map<Integer, String> Ids;
  static {
    Ids = new HashMap<Integer, String>();
    Ids.put(1, "128.111.84.202");
    Ids.put(2, "128.111.84.210");
    Ids.put(3, "128.111.84.228");
    Ids.put(4, "128.111.84.245");
    Ids.put(5, "128.111.84.253");
  } 
  private List<String> Ips;
  private boolean inChange;
  private List<String> newIps;
  private int index;
  
  public static Map<Integer, String> getIds() {
    return Ids;
  }
  public static int getPORT() {
    return PORT;
  }
  public Configuration() {
    
  }
  public Configuration(String input) {
    if(input.equals("")) {
      Ips = Arrays.asList("128.111.84.202", "128.111.84.210", "128.111.84.228");
    }
    else {
      String[] parts = input.split(" ");     
      Ips = new ArrayList<String>();
      for(String part : parts) {
        int id = Integer.parseInt(part);
        Ips.add(Ids.get(id));
      }
    }    
    System.out.println(Ips);
    inChange = false;
    index = -1;
  }
  
  public void changeConfiguration (String newIds, int index) {
    if(newIds != null) {
      newIps = new ArrayList<String>();
      String[] parts = newIds.split(" ");
      for(String part : parts) {
        int id = Integer.parseInt(part);
        newIps.add(Ids.get(id));
      }
      inChange = true;
    }   
    this.index = index;   
  }
  
  public void commitConfiguration (int index) {
    if (inChange) {
      Ips = newIps;
      newIps = null;
      inChange = false;
      this.index = index;
    }
  }
  
  public int getIndex() {
    return index;
  }
  public int getSize() {
    return Ips.size();
  }
  public List<String> getIps() {
    return Ips;
  }

  public void setIps(List<String> iPs) {
    Ips = iPs;
  }

  public boolean isInChange() {
    return inChange;
  }

  public void setInChange(boolean inChange) {
    this.inChange = inChange;
  }

  public List<String> getNewIps() {
    return newIps;
  }

  public boolean contains(String ip) {
    if (Ips.contains(ip)) {
      return true;
    } else if (inChange && newIps.contains(ip)) {
      return true;
    } else {
      return false;
    }
  }
}