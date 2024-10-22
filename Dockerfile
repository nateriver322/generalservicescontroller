FROM maven:3.9.9-eclipse-temurin-17-focal AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-alpine
COPY --from=build /target/*.jar joborder-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "joborder-0.0.1-SNAPSHOT.jar"]