# FreeACS TR-069 Server based on Akka

This project aims to port mostly all relevant functionality from the current/old FreeACS stack. 

Links:

https://doc.akka.io/docs/akka-http/current/

https://doc.akka.io/docs/akka/current/distributed-data.html?language=scala

http://slick.lightbend.com/doc/3.2.3/


Test:

curl -v -X POST -d '<soap><Body><Inform></Inform></Body></soap>' http://localhost:9000/tr069 -u easycwmp:easycwmp


