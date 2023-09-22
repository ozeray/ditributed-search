FROM maven:3.9.4-eclipse-temurin-20 AS MAVEN_TOOL_CHAIN_CONTAINER
#RUN mkdir src
COPY src /tmp/src
COPY ./pom.xml /tmp/
WORKDIR /tmp/
RUN mvn package
RUN ls -la /tmp

FROM eclipse-temurin:20-jdk-alpine
COPY resources /tmp/resources
COPY --from=MAVEN_TOOL_CHAIN_CONTAINER /tmp/target/ditributed-search-1.0-SNAPSHOT-jar-with-dependencies.jar /tmp/
WORKDIR /tmp/
ENTRYPOINT ["java", "-jar", "ditributed-search-1.0-SNAPSHOT-jar-with-dependencies.jar"]
CMD ["80", "docker"]