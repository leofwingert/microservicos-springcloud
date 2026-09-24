FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY config-server/pom.xml config-server/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY pecas-service/pom.xml pecas-service/pom.xml
COPY clientes-service/pom.xml clientes-service/pom.xml
COPY representantes-service/pom.xml representantes-service/pom.xml
COPY pecas-service/src pecas-service/src
RUN mvn -pl pecas-service -am clean package -DskipTests -q

FROM eclipse-temurin:17-jre
VOLUME /tmp
COPY --from=build /app/pecas-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
