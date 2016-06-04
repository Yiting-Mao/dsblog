# dsblog
a distributed blog system

how to run:
assuming you are at directory dsblog, input:
cd raft,
mvn clean compile,
mvn exec:java,

after this, you will see:
usage: '<l> <id>' to start a leader, '<f> <id>' to start a follower, 'c' to start a client.

id and ip are set in Configuration.java