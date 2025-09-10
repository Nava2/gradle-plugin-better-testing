plugins {
  alias(libs.plugins.dependencyanalysis)
  alias(libs.plugins.kotlinx.binary.compatibility.validator)

  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.kotlin) apply false
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

apiValidation {
  ignoredProjects += "gradle-plugin-better-testing"
}

extensions.findByName("buildScan")?.withGroovyBuilder {
  setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
  setProperty("termsOfServiceAgree", "yes")
}
