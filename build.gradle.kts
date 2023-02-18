import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val kotlinVersion = "1.8.10"
    id("org.springframework.boot") version "2.7.7"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("org.flywaydb.flyway") version "9.14.1"
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

group = "io.harmny"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

val coroutinesVersion = "1.6.4"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${coroutinesVersion}")

    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springdoc:springdoc-openapi-ui:1.6.14")

    implementation("io.arrow-kt:arrow-core:1.1.3")

    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Jar> {
    archiveFileName.set("${project.name}.jar")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
