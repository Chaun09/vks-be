FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR ./src
COPY . .
RUN mvn install -DskipTests=true

FROM openjdk:21-jdk

RUN unlink /etc/localtime;ln -s  /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime
COPY --from=build src/target/vks-be-0.0.1-SNAPSHOT.jar /run/vks-be-0.0.1-SNAPSHOT.jar

EXPOSE 8081

ENV JAVA_OPTIONS="-Xmx4096m -Xms512m"
ENTRYPOINT java -jar $JAVA_OPTIONS /run/vks-be-0.0.1-SNAPSHOT.jar
