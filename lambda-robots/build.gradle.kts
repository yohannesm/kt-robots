import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0")
    }
}

plugins {
    id("maven")
    id("java")
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    kotlin("jvm")
}

group = "io.onema.ktrobots.lambda"
version = "dev"
repositories {
    jcenter()
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(project(":commons"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.72")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.72")

    implementation("com.amazonaws:aws-lambda-java-core:1.2.0")
    implementation("com.amazonaws:aws-lambda-java-events:2.2.7")
    implementation("com.amazonaws:aws-java-sdk-dynamodb:1.11.771")

    // Log dependencies
    implementation("com.amazonaws:aws-lambda-java-log4j2:1.1.0")
    implementation("org.apache.logging.log4j:log4j-core:2.13.2")
    implementation("org.apache.logging.log4j:log4j-api:2.13.2")

    implementation("com.fasterxml.jackson.core:jackson-core:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.11.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")

    implementation("io.ktor:ktor-client-cio:1.3.1")
    implementation("io.ktor:ktor-client-jackson:1.3.1")
    implementation("io.ktor:ktor-client-logging-jvm:1.3.1")
    implementation("khttp:khttp:1.0.0")

}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

shadow {
    applicationDistribution.from("src/dist")
}
tasks.shadowJar{
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}
task("deploy-robots", Exec::class) {
    workingDir("../")
    dependsOn("shadowJar")
    commandLine("serverless", "deploy", "--aws-profile", "default")
}

task("delete-robots", Exec::class) {
    workingDir("../")
    commandLine("serverless", "remove", "--aws-profile", "default")
}
