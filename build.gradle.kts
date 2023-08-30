plugins {
  `maven-publish`
  signing

  alias(libs.plugins.kotlin)
  alias(libs.plugins.dependencyanalysis)
  alias(libs.plugins.kotlinx.binary.compatibility.validator)

  alias(libs.plugins.nexus.publish)

  id("better-testing.versioning")
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

nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
    }
  }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
  setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
  setProperty("termsOfServiceAgree", "yes")
}
