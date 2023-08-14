plugins {
  alias(libs.plugins.kotlin)
  alias(libs.plugins.dependencyanalysis)
  alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

repositories {
  mavenCentral()
}

group = "net.navatwo"
version = "1.0-SNAPSHOT"

apiValidation {
  ignoredProjects += "gradle-plugin-better-testing"
}
