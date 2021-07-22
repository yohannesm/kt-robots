import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.5.10"
    kotlin("jvm")
}

group = "io.onema.ktrobots.server"
version = "dev"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
}
//
dependencies {
    implementation(project(":commons"))

    // Local Robots
    implementation(project(":lambda-robots"))

    // Base
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Web sockets
    implementation("org.webjars:webjars-locator-core")
    implementation("org.webjars:sockjs-client:1.1.2")
    implementation("org.webjars:stomp-websocket:2.3.3")
    implementation("org.webjars:bootstrap:4.4.1")
    implementation("org.webjars:jquery:3.5.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")

    // AWS
    implementation("com.github.derjust:spring-data-dynamodb:5.1.0")
    implementation("com.amazonaws:aws-java-sdk-dynamodb:1.11.779")
    implementation("software.amazon.awssdk:lambda:2.13.13")

    // Other
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")
//    implementation("org.springdoc:springdoc-openapi-ui:1.5.8")
//    implementation("org.springdoc:springdoc-openapi-kotlin:1.5.8")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
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
task("deploy-server", Exec::class) {
    workingDir("./")
    dependsOn("bootJar")
    commandLine("./deploy-server.sh")
}
task("delete-server", Exec::class) {
    workingDir("./")
    commandLine("./delete-server.sh")
}
