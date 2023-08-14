import org.gradle.kotlin.dsl.invoke

plugins {
  kotlin("jvm")
  id("io.gitlab.arturbosch.detekt")
}

repositories {
  mavenCentral()
}

group = "net.navatwo"
version = "1.0-SNAPSHOT"

val javaVersion = providers.fileContents(rootProject.layout.projectDirectory.file(".java-version"))

kotlin {
  jvmToolchain {
    languageVersion.set(javaVersion.asText.map { JavaLanguageVersion.of(it) })
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