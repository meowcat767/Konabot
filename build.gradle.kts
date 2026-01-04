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
    // Fully qualified main class
    mainClass.set("site.meowcat.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
