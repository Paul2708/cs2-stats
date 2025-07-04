plugins {
    id("java")

    application
    id("com.gradleup.shadow") version "8.3.1"
}

application.mainClass = "de.paul2708.csstats.Main"
group = "de.paul2708.cs2stats"
version = "0.1.0"

val jdaVersion = "5.6.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation("ch.qos.logback:logback-classic:1.5.13")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    sourceCompatibility = "21"
}