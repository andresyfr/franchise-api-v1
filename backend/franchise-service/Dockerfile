# Build context: backend/
#   docker build -f backend/franchise-service/Dockerfile backend

# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle gradle
COPY franchise-service/build.gradle franchise-service/
RUN chmod +x gradlew && ./gradlew --no-daemon :franchise-service:dependencies > /dev/null

COPY franchise-service/src franchise-service/src
RUN ./gradlew --no-daemon :franchise-service:bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
RUN groupadd --system spring && useradd --system --gid spring spring
WORKDIR /app

COPY --from=build /workspace/franchise-service/build/libs/app.jar app.jar

USER spring:spring
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
