plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.Recipe"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(jdkVersion = 21)
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.grpc") {
            useVersion("1.64.0")
            because("force all grpc modules to same version")
        }
    }
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-cio:3.0.0")
    implementation("com.google.genai:google-genai:1.4.1")
    implementation("com.google.firebase:firebase-admin:9.2.0")
}