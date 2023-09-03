plugins {
  id("better-testing.kotlin")
}

dependencies {
  api(libs.junit.jupiter.api)

  implementation(kotlin("reflect"))
  implementation(gradleTestKit())

  compileOnly(libs.jetbrains.annotations)

  testImplementation(libs.assertj)
  testImplementation(libs.mockito)

  testImplementation(project(":gradle-plugin-better-testing-assertj-asserts"))

  testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.test {
  // We need to avoid calling `withPluginClasspath()` when we write our own tests.
  systemProperty("net.navatwo.gradle.testkit.junit5.internal", true)
}