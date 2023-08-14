plugins {
  id("better-testing.kotlin")
}

dependencies {
  api(libs.junit.jupiter.api)

  implementation(kotlin("reflect"))
  implementation(gradleTestKit())

  testImplementation(libs.assertj)

  testImplementation(project(":gradle-plugin-better-testing-assertj-asserts"))

  testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.test {
  jvmArgs(
    "-Djunit.jupiter.extensions.autodetection.enabled=true",

    // We need to avoid calling `withPluginClasspath()` when we write our own tests.
    "-Dnet.navatwo.gradle.testkit.junit5.internal=true",
  )

  val gradleVersionOverride = providers.gradleProperty("net.navatwo.gradle.testkit.junit5.testing.gradleVersion")
    .map { version ->
      "-Dnet.navatwo.gradle.testkit.junit5.gradleVersion=$version"
    }
  if (gradleVersionOverride.isPresent) {
    jvmArgs(gradleVersionOverride.get())
  }
}