import org.gradle.kotlin.dsl.invoke

plugins {
  kotlin("jvm")

  id("io.gitlab.arturbosch.detekt")
  id("com.jaredsburrows.license")
  id("com.vanniktech.maven.publish")
}

repositories {
  mavenCentral()
}

group = "net.navatwo.gradle"
version = "0.0.6-SNAPSHOT"

val isRelease = providers.environmentVariable("RELEASE")
  .map { it.isNotBlank() }
  .getOrElse(false)

if (isRelease) {
  version = version.toString().substringBefore("-SNAPSHOT")
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

// java artifacts (javadoc, sources) are now handled by the Vanniktech plugin

mavenPublishing {
  publishToMavenCentral()

  if (isRelease) {
    signAllPublications()
  }
}

mavenPublishing {
  pom {
    name.set("gradle-plugin-better-testing")
    description.set("Test fixtures for testing Gradle Plugins")
    url.set("https://github.com/Nava2/gradle-plugin-better-testing")
    inceptionYear.set("2023")
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

val testKitDirectory: Directory = rootProject.layout.projectDirectory.dir(".gradle/testKit")

tasks.clean {
  delete(testKitDirectory)
}

tasks.test {
  systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
  systemProperty("net.navatwo.gradle.testkit.junit5.testKitDirectory", testKitDirectory.asFile.toString())
}
