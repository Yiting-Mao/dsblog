package cs271.raft.message;

public enum MessageType {
  APPENDENTRY,
  CONFIGURATION,
  REQUESTVOTE,
  RPCREPLY,
  CLIENTREQUEST,
  TOCLIENT;
}