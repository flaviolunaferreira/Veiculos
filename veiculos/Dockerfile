FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /workspace
COPY . .
RUN ./gradlew build --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
 
RUN groupadd -r appgroup && useradd -r -g appgroup -s /bin/false appuser
USER appuser


COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 9081
ENV JAVA_OPTS="-Dspring.profiles.active=prod -Djava.security.egd=file:/dev/./urandom"

# Comando de execução
ENTRYPOINT ["java", "-jar", "app.jar"]