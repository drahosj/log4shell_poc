FROM openjdk:13-alpine AS builder
RUN mkdir /app
COPY . /app
ADD https://archive.apache.org/dist/tomcat/tomcat-10/v10.0.14/bin/apache-tomcat-10.0.14.tar.gz /app
WORKDIR /app
RUN tar xvf apache-tomcat*.tar.gz
RUN javac --add-modules jdk.naming.rmi --add-exports jdk.naming.rmi/com.sun.jndi.rmi.registry=ALL-UNNAMED -cp apache-tomcat-10.0.14/lib/catalina.jar EvilRMIServerNew.java



FROM openjdk:13-alpine
EXPOSE 1097
RUN mkdir /app
COPY --from=builder /app/apache-tomcat-10.0.14/lib/*.jar /app/lib/
COPY --from=builder /app/EvilRMIServerNew.class /app/
WORKDIR /app
CMD ["java", "-cp", "lib/catalina.jar:.", "EvilRMIServerNew"]
