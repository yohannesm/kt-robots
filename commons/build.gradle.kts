import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maven")
    kotlin("jvm")
}

group = "io.onema.ktrobots.commons"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation("com.amazonaws:aws-java-sdk-dynamodb:1.11.771")
    implementation(kotlin("stdlib-jdk8"))

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")

}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}