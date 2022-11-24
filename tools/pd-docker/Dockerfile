FROM openjdk:11
WORKDIR /persistent-developer
ARG JAR_PATH="./target/scala-2.13/persistent-developer-assembly-0.1.jar"
COPY ${JAR_PATH} /persistent-developer/application.jar
ENTRYPOINT java -jar /persistent-developer/application.jar