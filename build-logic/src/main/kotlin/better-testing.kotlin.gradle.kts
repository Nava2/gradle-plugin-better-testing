import org.gradle.kotlin.dsl.invoke

plugins {
  `maven-publish`
  signing

  kotlin("jvm")

  id("io.gitlab.arturbosch.detekt")
  id("com.jaredsburrows.license")

  id("better-testing.versioning")
}

repositories {
  mavenCentral()
}

val javaVersion: Provider<String> = providers.fileContents(
  rootProject.layout.projectDirectory.file(".java-version"),
).asText.map { it.substringBefore('.') }

kotlin {
  jvmToolchain {
    languageVersion.set(javaVersion.map { JavaLanguageVersion.of(it) })
  }
}

tasks.test {
  useJUnitPlatform()
}

detekt {
  parallel = true
  autoCorrect = true

  buildUponDefaultConfig = true
  config.from(rootProject.file("detekt.yaml"))

  allRules = false // activate all available (even unstable) rules.
}

dependencies {
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")
}

tasks.named("check") {
  dependsOn(tasks.named("projectHealth"))
}

licenseReport {
  generateTextReport = true
  generateHtmlReport = true
  generateCsvReport = false
  generateJsonReport = false
}

java {
  withJavadocJar()
  withSourcesJar()
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      pom {
        name.set("gradle-plugin-better-testing")
        description.set("Test fixtures for testing Gradle Plugins")
        url.set("https://github.com/Nava2/gradle-plugin-better-testing")

        licenses {
          license {
            name.set("MIT License")
            url.set("https://opensource.org/licenses/MIT")
          }
        }

        developers {
          developer {
            id.set("Nava2")
            name.set("Kevin Brightwell")
            email.set("kevin.brightwell2+gradle-plugin-better-testing@gmail.com")
          }
        }

        scm {
          url.set("https://github.com/Nava2/gradle-plugin-better-testing.git")
        }
      }
    }
  }
}

fun ProviderFactory.gradleOrSystemProperty(propertyName: String): Provider<String> {
  return gradleProperty(propertyName)
    .orElse(systemProperty(propertyName))
}

signing {
  sign(publishing.publications["maven"])
}

tasks.withType<Jar> {
  manifest {
    val gitCommit = providers.exec {
      executable("git")
      args("rev-parse", "HEAD")
    }.standardOutput.asText.map { it.trim() }
    val gitIsDirty = providers.exec {
      executable("git")
      args("status", "--porcelain")
    }.standardOutput.asText
      .map { it.trim() }
      .map { it.isNotBlank() }

    attributes(
      "Git-Commit" to gitCommit,
      "Git-IsDirty" to gitIsDirty,
    )
  }
}
