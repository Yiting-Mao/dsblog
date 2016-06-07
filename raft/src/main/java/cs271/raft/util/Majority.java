package cs271.raft.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import cs271.raft.util.Configuration;

public class Majority {
  public static int getValue(Map<String, Integer> records, Configuration conf, String ip) {
    // System.out.println("getting majority value");
//     for (Map.Entry<String, Integer> entry : records.entrySet()) {
//       System.out.println(entry.getKey()+ ":" + entry.getValue());
//     }
    // conf.print();
    if (!conf.isInChange()) {
    //  System.out.println("not in change");
      List<Integer> tmp = new ArrayList<Integer>();
      List<String> ips = conf.getIps();
      for (int i = 0; i < ips.size(); i++) {
        if (ip.equals(ips.get(i))) continue;
        tmp.add(records.get(ips.get(i)));
      }
      // System.out.println("Printing tmp");
//       for (int i = 0; i < tmp.size(); i++) {
//         System.out.println(tmp.get(i));
//       }
      if (conf.contains(ip)) {
        return getMid(tmp, true);
      } else {
        return getMid(tmp, false);
      }      
    } else {
      //System.out.println("in change");
      List<String> old_ips = conf.getIps();
      List<Integer> tmp_old = new ArrayList<Integer>(); 
      for (int i = 0; i < old_ips.size(); i++) {
        if (ip.equals(old_ips.get(i))) continue;
        tmp_old.add(records.get(old_ips.get(i)));
      }
      
      List<String> new_ips = conf.getNewIps();
      List<Integer> tmp_new = new ArrayList<Integer>();
      for (int i = 0; i < new_ips.size(); i++) {
        if (ip.equals(new_ips.get(i))) continue;
        tmp_new.add(records.get(new_ips.get(i)));
      }
      // System.out.println("Printing tmp_old");
//       for (int i = 0; i < tmp_old.size(); i++) {
//         System.out.print(tmp_old.get(i) + " ");
//       }
//       System.out.println("Printing tmp_new");
//       for (int i = 0; i < tmp_new.size(); i++) {
//         System.out.print(tmp_new.get(i) + " ");
//       }
      boolean in = old_ips.contains(ip) ? true : false;
      int mid_old = getMid(tmp_old, in);
      in = new_ips.contains(ip) ? true : false;
      int mid_new = getMid(tmp_new, in);
      return mid_old < mid_new ? mid_old : mid_new;
    }
  }
  private static int getMid(List<Integer> tmp, boolean in) {
    Collections.sort(tmp);
    if (in) {
      return tmp.get(tmp.size() / 2);
      
    } else {
      return tmp.get((tmp.size() - 1) / 2);
    }
    
  }
}