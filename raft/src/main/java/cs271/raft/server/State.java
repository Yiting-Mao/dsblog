package cs271.raft.server;

public enum State {
  LEADER,
  FOLLOWER,
  CANDIDATE,
}