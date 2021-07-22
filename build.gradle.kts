import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    id("java")
    kotlin("jvm") version "1.5.10"
}

allprojects {
    group = "io.onema.ktrobots"
    version = "1.0-SNAPSHOT"
    repositories {
        mavenCentral()
        jcenter()
    }

    tasks.withType<JavaCompile> {
        java.sourceCompatibility = JavaVersion.VERSION_11
        java.targetCompatibility = JavaVersion.VERSION_11
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    dependencies {
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach {
        archives(it)
    }
}

subprojects {
    repositories {
        mavenCentral()
    }
}
repositories {
    mavenCentral()
}
