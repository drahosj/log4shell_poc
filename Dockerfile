FROM openjdk:8u181-jdk-alpine AS builder
RUN mkdir /app
COPY . /app
ADD https://dlcdn.apache.org/tomcat/tomcat-10/v10.0.14/bin/apache-tomcat-10.0.14.tar.gz /app
WORKDIR /app
RUN tar xvf apache-tomcat*.tar.gz
RUN javac -cp apache-tomcat-10.0.14/lib/catalina.jar EvilRMIServerNew.java



FROM openjdk:8u181-jdk-alpine
EXPOSE 1097
RUN mkdir /app
COPY --from=builder /app/apache-tomcat-10.0.14/lib/*.jar /app/lib/
COPY --from=builder /app/EvilRMIServerNew.class /app/
WORKDIR /app
CMD ["java", "-cp", "lib/catalina.jar:.", "EvilRMIServerNew"]
