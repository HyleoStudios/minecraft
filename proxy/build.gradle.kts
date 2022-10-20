import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    application
    kotlin("kapt") version "1.7.10"
    kotlin("jvm") version "1.7.10"
    id("org.springframework.boot") version "2.7.3"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    kotlin("plugin.spring") version "1.7.10"
}

group = "io.hyleo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")
    compileOnly("me.hugmanrique:buycraft-api:1.0-SNAPSHOT")

    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.springframework.boot:spring-boot-starter-test")


    // Redis Bungee

    implementation("com.google.guava:guava:31.1-jre")
    implementation("redis.clients:jedis:4.3.0")
    implementation("org.apache.commons:commons-pool2:2.11.1")
    implementation("com.squareup.okhttp:okhttp:2.7.5")
    testImplementation("junit:junit:4.13.2")

}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xcontext-receivers")
        jvmTarget = "17"
    }
}

tasks.withType<ProcessResources> {
    filesMatching("velocity-plugin.json") {
        expand(project.properties)
    }
}

tasks.withType<BootJar>() {

}

tasks.withType<Jar> {
    destinationDirectory.set(file("./srv/plugins"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["mainClass"] = "proxy.ProxyPlugin"
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
