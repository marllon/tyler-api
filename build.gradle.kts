plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.tylerproject"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Cloud Functions
    implementation("com.google.cloud.functions:functions-framework-api:1.1.0")
    
    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.2.0")
    
    // Firestore
    implementation("com.google.cloud:google-cloud-firestore:3.15.4")
    
    // HTTP
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Stripe
    implementation("com.stripe:stripe-java:24.16.0")
    
    // Mercado Pago (via HTTP client)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Email providers
    implementation("com.sendgrid:sendgrid-java:4.10.2")
    
    // Security
    implementation("org.mindrot:jbcrypt:0.4")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Validation
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.tylerproject.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("tyler-backend")
    archiveClassifier.set("")
    mergeServiceFiles()
    
    manifest {
        attributes(
            "Main-Class" to "com.google.cloud.functions.invoker.runner.Invoker",
            "Start-Class" to "com.tylerproject.MainKt"
        )
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
