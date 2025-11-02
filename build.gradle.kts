val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.3.1"
    id("antlr")
}

group = "com.main"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}


dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-swagger")
    implementation("com.ucasoft.ktor:ktor-simple-cache:0.55.3")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-cors")
    implementation("org.openfolder:kotlin-asyncapi-ktor:3.1.3")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
