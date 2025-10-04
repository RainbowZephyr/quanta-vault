plugins {
    kotlin("jvm") version "2.2.10"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "io.github.rainbowzephyr"
var baseVersion = "0.1.0"
version = System.getenv().getOrDefault("VERSION", "$baseVersion-SNAPSHOT")

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")

    testImplementation(kotlin("test"))
}

java {
    sourceCompatibility = JavaVersion.toVersion("21")
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
