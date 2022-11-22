FROM openjdk:11
WORKDIR /persistent-developer
ARG version
COPY ./target/scala-2.13/persistent-developer-assembly-${version}.jar /persistent-developer/application.jar
ENTRYPOINT java -jar /persistent-developer/application.jar