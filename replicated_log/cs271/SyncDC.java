package cs271;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import cs271.DataCenter;
import cs271.Message;
import cs271.TransData;

public class SyncDC implements Runnable {
  Socket socket;
  DataCenter dc;
  ObjectInputStream inStream = null;
  ObjectOutputStream outStream = null;
  public SyncDC(Socket socket, DataCenter dc) {
    this.socket = socket;
    this.dc = dc;
  }
  
  private synchronized void updateData(TransData td) {
    Iterator<Record> it = td.log.iterator();
    while (it.hasNext()) {
      Record record = it.next();
      int remote_id = record.getId();
      int time = record.getTime();
      int local_id = dc.getId();
      if (dc.table[local_id][remote_id] < time) {
        dc.addLog(record);
        dc.addBlog(new Message(record.user, 'p', record.post));
      }
    }
    for (int i = 0; i < dc.DCNUM; ++i) {
      for (int j = 0; j < dc.DCNUM; ++j) {
        if(dc.table[i][j] < td.table[i][j]) {
          dc.table[i][j] = td.table[i][j];
        }
      }
    }
    for (int i = 0; i < dc.DCNUM; ++i) {
      if(dc.table[dc.getId()][i] < td.table[td.getId()][i]) {
        dc.table[dc.getId()][i] = td.table[td.getId()][i];
      }
    }
     
  }
  public void run() {
    try {
      outStream =  new ObjectOutputStream(socket.getOutputStream());
      inStream = new ObjectInputStream(socket.getInputStream());
      Message message = new Message(null, 'r', Integer.toString(dc.getId()));
      outStream.writeObject(message);
      TransData td = (TransData) inStream.readObject();
      updateData(td);
      socket.close();
    } catch (SocketException se) {
       se.printStackTrace();
       System.exit(0);
    } catch (IOException e) {
       e.printStackTrace();
    } catch (ClassNotFoundException cn) {
       cn.printStackTrace();
    } 
  } 
}