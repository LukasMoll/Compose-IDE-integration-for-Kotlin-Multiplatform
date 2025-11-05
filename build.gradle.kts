val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.3.1"
    id("antlr")
}

repositories {
    mavenCentral()
}

group = "com.main"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}


dependencies {
    antlr("org.antlr:antlr4:4.8")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-swagger")
    implementation("com.ucasoft.ktor:ktor-simple-cache:0.55.3")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-cors")
    implementation("org.openfolder:kotlin-asyncapi-ktor:3.1.3")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("org.antlr:antlr4-runtime:4.8")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("com.microsoft.playwright:playwright:1.48.0")
}

val playwrightInstall by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Install Playwright browsers for E2E tests"
    mainClass.set("com.microsoft.playwright.CLI")
    classpath = sourceSets.test.get().runtimeClasspath
    args = listOf("install", "--with-deps")
}

tasks.test {
    dependsOn(playwrightInstall)
    systemProperty("playwright.nodejs.compression.level", "9")
    systemProperty("playwright.cli.atomic.install", "true")
}
