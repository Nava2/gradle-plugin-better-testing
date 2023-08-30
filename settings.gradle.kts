pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }

  includeBuild("build-logic")
}

rootProject.name = "gradle-plugin-better-testing"

include(
  ":gradle-plugin-better-testing-assertj-asserts",
  ":gradle-plugin-better-testing-junit5",
)
