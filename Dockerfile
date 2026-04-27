FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --user-group --home-dir /app scriba
COPY --from=build /workspace/target/*.jar /app/scriba.jar
USER scriba
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/scriba.jar"]
