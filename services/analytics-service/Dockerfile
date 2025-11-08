# ===== Build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :services:analytics-service:clean :services:analytics-service:bootJar --no-daemon

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/services/analytics-service/build/libs/analytics-service-*.jar /app/app.jar
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="docker"
EXPOSE 8084
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
