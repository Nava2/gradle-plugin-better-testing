plugins {
  kotlin("jvm") version "1.9.23"
  `kotlin-dsl`
}

repositories {
  mavenCentral()
}

val kotlinVersion = "1.9.23"
val detektVersion = "1.23.6"

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

  implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$detektVersion")
  implementation("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")

  implementation("com.jaredsburrows:gradle-license-plugin:0.9.7")
}

val javaVersion = providers.fileContents(rootProject.layout.projectDirectory.file(".java-version"))

kotlin {
  jvmToolchain {
    languageVersion.set(javaVersion.asText.map { JavaLanguageVersion.of(it) })
  }
}
