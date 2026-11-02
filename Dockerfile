# syntax=docker/dockerfile:1
# ---- build stage: compile + package the WAR inside the container ----
FROM docker.io/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
# Warm the dependency cache on the pom alone so source edits don't refetch.
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline || true
COPY src ./src
RUN mvn -q -DskipTests package

# ---- runtime stage: Tomcat 9 (javax.* servlet namespace) ----
FROM docker.io/library/tomcat:9-jre17
RUN rm -rf /usr/local/tomcat/webapps/*
# Deploy as ROOT so the app serves at http://localhost:8080/
COPY --from=build /src/target/task-tracker.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
