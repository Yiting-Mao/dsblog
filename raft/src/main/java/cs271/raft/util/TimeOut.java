package cs271.raft.util;

import java.util.Random;

public class TimeOut {
  static int lowBound = 2000;
  static int highBound = 5000;
  static int heartBeat = 1000;
  private int timeOut;  
  private long preTime;
  public TimeOut() {
    randomTimeOut();
  }
  public TimeOut(int s) {
    timeOut = heartBeat;
    preTime = System.currentTimeMillis();
  }
  public void randomTimeOut() {
    Random rand = new Random();
    timeOut = rand.nextInt((highBound - lowBound) + 1) + lowBound;
    preTime = System.currentTimeMillis();
  }
  public void refresh() {
    preTime = System.currentTimeMillis();
  }
  public boolean isTimeOut() {
    return System.currentTimeMillis() - preTime > timeOut? true : false;
  }
  public int getTimeOut() {
    return this.timeOut;
  }
  public long getPreTime() {
    return preTime;
  }
  public void setPreTime(long preTime) {
    this.preTime = preTime;
  }
  public int getRemain() {
    return (int) (timeOut - (System.currentTimeMillis() - preTime));
  }
}