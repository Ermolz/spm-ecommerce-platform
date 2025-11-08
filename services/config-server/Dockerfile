# ===== Build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :services:config-server:clean :services:config-server:bootJar --no-daemon

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/services/config-server/build/libs/config-server-*.jar /app/app.jar
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="native"
EXPOSE 8888
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
