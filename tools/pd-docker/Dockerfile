FROM ibm-semeru-runtimes:open-11.0.17_8-jre-focal
WORKDIR /persistent-developer
ARG JAR_PATH="./target/scala-2.13/persistent-developer-assembly-0.1.jar"
COPY ${JAR_PATH} application.jar
ENTRYPOINT java -jar application.jar