package cs271;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import cs271.Message;
import cs271.Record;

/** 
 * This structure is used to transmit data between servers.
 * When receiving sych request from other server, it sends its log, time table and server id.
 * @author Yiting Mao
 * @since 2016-04-23
 */
class TransData implements Serializable {
  List<Record> log;
  int[][] table;
  int id;
  public TransData() {
    
  }
  public TransData(int id, List<Record> log, int[][] table) {
    this.id = id;
    this.log = log;
    this.table = table;
  }
  public int getId() {
    return id;
  }
}