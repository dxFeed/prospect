import com.dxfeed.gradle.autover.AutoVersionPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    `maven-publish`
    kotlin("jvm") version Versions.kotlin
    id("org.jlleitschuh.gradle.ktlint")
}

apply<AutoVersionPlugin>()

group = "com.dxfeed.prospect"
// version is managed by auto-version plugin

repositories {
    mavenCentral()
}

dependencies {
    implementation(Libs.`kotlin-stdlib-jdk8`)

    testImplementation(Libs.`kotlin-reflect`)
    testImplementation(Libs.`kotlin-test-junit5`)
    testImplementation(Libs.`junit-jupiter`)
    testImplementation(Libs.`assertk-jvm`)
}

configurations.all {
    val configurationName = name
    if (listOf("kotlinCompiler", "ktlint").any { configurationName.startsWith(it) }) {
        return@all
    }

    resolutionStrategy {
        failOnVersionConflict()
        force(
            Libs.`kotlin-stdlib`,
            Libs.`kotlin-stdlib-common`,
            Libs.`kotlin-stdlib-jdk7`,
            Libs.`kotlin-stdlib-jdk8`,
            Libs.`junit-jupiter-api`
        )
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = Versions.java.toString()
        apiVersion = "1.4"
        languageVersion = "1.4"
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Add explicit-api option only for production code, not tests
tasks.named<KotlinCompile>("compileKotlin") {
    kotlinOptions.freeCompilerArgs += "-Xexplicit-api=strict"
}

ktlint {
    version.set(Versions.ktlint)
    reporters {
        reporter(ReporterType.HTML)
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

fun Project.isSnapshotVersion() = "SNAPSHOT" in "${project.version}"

// TODO: remove when there a snapshot repository
tasks.withType<PublishToMavenRepository> {
    doFirst {
        if (project.isSnapshotVersion()) {
            throw GradleException("Publishing snapshot versions is not supported")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
    repositories {
        val publishRepository = System.getenv("PROSPECT_PUBLISH_REPOSITORY")
        if (!publishRepository.isNullOrBlank()) {
            maven {
                name = "prospect"
                url = project.uri(publishRepository)

                val publishUsername = System.getenv("PROSPECT_PUBLISH_USERNAME")
                val publishPassword = System.getenv("PROSPECT_PUBLISH_PASSWORD")
                if (!publishUsername.isNullOrBlank() && !publishPassword.isNullOrBlank()) {
                    credentials {
                        username = publishUsername
                        password = publishPassword
                    }
                }
            }
        }
    }
}
