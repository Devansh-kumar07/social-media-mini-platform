FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
RUN mvn -pl api-gateway -am dependency:go-offline
COPY . .
RUN mvn -pl api-gateway -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-3.2.5.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

