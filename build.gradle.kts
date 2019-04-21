import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.30"
}

group = "io.blazeq"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}


tasks.withType<Jar> {
    manifest.attributes.apply {
        put("Implementation-Title", "Aesthetics of Pennies")
        put("Implementation-Version", version)
        put("Main-Class", "MainKt")
    }

    baseName = project.name + "-all"

    from(configurations.compile.map { if (it.isDirectory) it else zipTree(it) })
}
