# dsblog
a distributed blog system

how to run:
assuming you are at directory dsblog, input:
cd raft,
mvn clean compile,
mvn exec:java,

after this, you will see:
usage: '<l>/<c>/<f> <id>' to start a leader/ candidate/ follower, 'u' to start a user
for example: 'f 3' starts server 3 as a follower.

When there is no persistent configuration stored, you can specify init configuration after seeing:
"input server ids(1-5) for init configuration, separated by space. Press enter to use default(1 2 3)"
for example '1 3 4' sets server 1, 3, 4 in the configuration.

id and ip correlation is in Configuration.java