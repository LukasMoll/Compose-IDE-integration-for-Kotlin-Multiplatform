FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties /workspace/
COPY gradle /workspace/gradle

RUN chmod +x gradlew

COPY src /workspace/src
COPY antlr /workspace/antlr

RUN ./gradlew --no-daemon clean installDist -x test

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app
COPY --from=builder /workspace/build/install/kotlin_ide_integration /app

ENV JAVA_TOOL_OPTIONS="-Dktor.deployment.host=0.0.0.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

CMD ["./bin/kotlin_ide_integration"]
