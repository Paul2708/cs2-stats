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

    implementation("io.github.cdimascio:dotenv-java:3.2.0")

    implementation("de.chojo.sadu:sadu-postgresql:2.3.2")
    implementation("de.chojo.sadu:sadu-datasource:2.3.2")
    implementation("de.chojo.sadu:sadu-mapper:2.3.2")
    implementation("de.chojo.sadu:sadu-queries:2.3.2")

    implementation("org.knowm.xchart:xchart:3.8.8")

    implementation("org.json:json:20250517")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    sourceCompatibility = "21"
}

tasks.test {
    useJUnitPlatform()
}