# ===== Build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :services:admin-server:clean :services:admin-server:bootJar --no-daemon

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/services/admin-server/build/libs/admin-server-*.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-Duser.timezone=UTC"
ENV JAVA_OPTS=""

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

