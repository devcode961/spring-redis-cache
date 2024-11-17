FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
EXPOSE 8080
COPY target/spring-redis-example.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]