# ===== Build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :services:inventory-service:clean :services:inventory-service:bootJar --no-daemon

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/services/inventory-service/build/libs/inventory-service-*.jar /app/app.jar
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="docker"
EXPOSE 8082
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
