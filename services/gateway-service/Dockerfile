# ===== Build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :services:gateway-service:clean :services:gateway-service:bootJar --no-daemon

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/services/gateway-service/build/libs/gateway-service-*.jar /app/app.jar
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="docker"
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
