# ===== Build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :services:order-service:clean :services:order-service:bootJar --no-daemon

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/services/order-service/build/libs/order-service-*.jar /app/app.jar

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="docker"

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
