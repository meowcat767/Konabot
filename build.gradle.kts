plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.3.0")

    // Environment variable loader
    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    // Logging (slf4j)
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.slf4j:slf4j-simple:2.0.17")

    // HTTP client
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    // JSON parser
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.20.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")
}

application {
    mainClass.set("site.meowcat.Main") // your fully qualified main class
}

// Make a fat/uber JAR
tasks.register<Jar>("fatJar") {
    group = "build"
    archiveClassifier.set("all") // output will be mybot-1.0-all.jar

    manifest {
        attributes["Main-Class"] = "site.meowcat.Main"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Include compiled classes
    from(sourceSets.main.get().output)

    // Include runtime dependencies
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

// Optional: make `build` also build the fatJar
tasks.build {
    dependsOn("fatJar")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
