# ===== Build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :services:eureka-server:clean :services:eureka-server:bootJar --no-daemon

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/services/eureka-server/build/libs/eureka-server-*.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-Duser.timezone=UTC"
ENV JAVA_OPTS=""

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

