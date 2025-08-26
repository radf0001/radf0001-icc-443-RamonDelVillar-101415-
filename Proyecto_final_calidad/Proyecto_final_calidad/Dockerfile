# Etapa de build
FROM gradle:8.9-jdk21-alpine AS build
WORKDIR /home/gradle/project
COPY . .
# Produce el frontend antes del jar y activa producción
RUN gradle --no-daemon -Pvaadin.productionMode clean vaadinBuildFrontend bootJar

# Etapa de runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
# Si tu imagen no trae curl, cambia a wget o quita el HEALTHCHECK
HEALTHCHECK --interval=10s --timeout=3s --retries=5 CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
