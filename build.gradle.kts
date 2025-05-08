plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.allopen") version "2.0.21"
    id("io.quarkus")
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
    id("org.flywaydb.flyway") version "11.3.4"
    id("org.jooq.jooq-codegen-gradle") version "3.20.1"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation("io.quarkus:quarkus-jdbc-postgresql:3.19.2")
    implementation("io.quarkus:quarkus-flyway:3.19.2")
    implementation("org.jooq:jooq:3.20.1")
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-rest:3.19.2")
    implementation("io.quarkus:quarkus-kotlin:3.19.2")
    implementation("io.quarkus:quarkus-rest-jackson:3.19.12")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

    jooqCodegen("org.jooq:jooq-meta-extensions:3.20.1")

    testImplementation("io.quarkus:quarkus-junit5:3.19.2")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("io.mockk:mockk:1.13.10")
}

group = "dev.dayyan"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
    }
}

jooq {
    configuration {
        generator {
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                properties {
                    property {
                        key = "scripts"
                        value = "src/main/resources/db/migration/V*.sql"
                    }
                    property {
                        key = "sort"
                        value = "flyway"
                    }
                }
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin", "build/generated-sources/jooq")
        }
    }
}

ktlint {
    verbose.set(true)
}

tasks.named("compileKotlin") {
    dependsOn("jooqCodegen")
}

tasks.named("runKtlintCheckOverMainSourceSet") {
    dependsOn("jooqCodegen")
}

tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
