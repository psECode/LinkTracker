FROM eclipse-temurin:25-jdk-alpine AS builder

RUN apk add --no-cache curl tar bash

ARG MAVEN_VERSION=3.9.12
ARG BASE_URL=https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven \
 && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
 && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
 && rm -f /tmp/apache-maven.tar.gz \
 && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME=/usr/share/maven
WORKDIR /app

COPY pom.xml .
COPY scrapper/pom.xml ./scrapper/
COPY bot/pom.xml ./bot/
COPY ai-agent/pom.xml ./ai-agent/
COPY build-report-aggregate/pom.xml ./build-report-aggregate/
COPY scrapper/migrations ./scrapper/migrations

RUN mvn -pl scrapper -am dependency:go-offline -B

COPY scrapper/src ./scrapper/src
RUN mvn -pl scrapper -am clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/scrapper/target/*.jar app.jar
COPY --from=builder /app/scrapper/migrations ./migrations

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
