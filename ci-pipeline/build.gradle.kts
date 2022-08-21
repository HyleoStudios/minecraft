plugins {
    kotlin("js") version "1.7.10"
}

group = "io.hyleo"
version = "1.0-SNAPSHOT"
val main = "Main"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(npm("actions/github", "latest"))
}

kotlin {
    js(LEGACY) {
        binaries.executable()
        nodejs {

        }
    }
}

tasks.register<Copy>("CopyGeneratedJSToDistribution") {
    dependsOn("CopyGeneratedNodeModuleToRoot")
    from("${buildDir}/compileSync/main/kotlin/$main.js") {
        rename(main, "index")
    }
    into("$rootDir/dist")
}

tasks.register<Copy>("CopyGeneratedNodeModuleToRoot") {
    from("${buildDir}/js/node_modules") {
        exclude("**/.bin")
    }
    into("$rootDir/node_modules")
}

tasks.named("assemble") {
    finalizedBy("CopyGeneratedNodeModuleToRoot")
}

tasks.named("CopyGeneratedNodeModuleToRoot") {
    finalizedBy("CopyGeneratedJSToDistribution")
}