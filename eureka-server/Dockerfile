FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY connectsphere-service/pom.xml connectsphere-service/pom.xml
COPY media-service/pom.xml media-service/pom.xml
COPY search-service/pom.xml search-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
RUN mvn -pl eureka-server -am dependency:go-offline
COPY . .
RUN mvn -pl eureka-server -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/eureka-server/target/eureka-server-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
