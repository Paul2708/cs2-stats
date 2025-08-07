plugins {
    id("java")

    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.paul2708.cs2stats"
version = "0.1.0"

application {
    mainClass.set("de.paul2708.cs2stats.Main")
}

val jdaVersion = "5.6.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:$jdaVersion")

    implementation("ch.qos.logback:logback-classic:1.5.13")

    implementation("io.github.cdimascio:dotenv-java:3.2.0")

    implementation("de.chojo.sadu:sadu-postgresql:2.3.2")
    implementation("de.chojo.sadu:sadu-datasource:2.3.2")
    implementation("de.chojo.sadu:sadu-mapper:2.3.2")
    implementation("de.chojo.sadu:sadu-queries:2.3.2")
    implementation("de.chojo.sadu:sadu-updater:2.3.2")
    implementation("org.postgresql:postgresql:42.7.7")

    implementation("org.knowm.xchart:xchart:3.8.8")

    implementation("org.json:json:20250517")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    sourceCompatibility = "21"
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    manifest {
        attributes["Main-Class"] = "de.paul2708.cs2stats.Main"
    }
}

tasks.test {
    useJUnitPlatform()
}