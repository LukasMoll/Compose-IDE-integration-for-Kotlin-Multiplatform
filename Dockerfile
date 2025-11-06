FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties /workspace/
COPY gradle /workspace/gradle

RUN chmod +x gradlew

COPY src /workspace/src
COPY antlr /workspace/antlr

RUN ./gradlew --no-daemon clean installDist -x test

FROM eclipse-temurin:17-jdk-jammy AS runtime

WORKDIR /app
COPY --from=builder /workspace/build/install/kotlin_ide_integration /app

RUN apt-get update && \
    apt-get install -y curl unzip && \
    curl -Lo kotlinc.zip https://github.com/JetBrains/kotlin/releases/download/v1.9.20/kotlin-compiler-1.9.20.zip && \
    unzip kotlinc.zip -d /opt && \
    rm kotlinc.zip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV PATH="/opt/kotlinc/bin:${PATH}"

ENV JAVA_TOOL_OPTIONS="-Dktor.deployment.host=0.0.0.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

CMD ["./bin/kotlin_ide_integration"]
