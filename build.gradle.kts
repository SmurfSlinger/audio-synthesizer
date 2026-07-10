plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "edu.cs5700"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("synthesizer.app.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
