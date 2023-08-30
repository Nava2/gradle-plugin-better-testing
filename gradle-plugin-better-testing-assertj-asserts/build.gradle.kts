plugins {
  id("better-testing.kotlin")
}

dependencies {
  api(libs.assertj)

  compileOnly(gradleTestKit())

  testImplementation(gradleTestKit())

  testImplementation(project(":gradle-plugin-better-testing-junit5"))

  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.engine)
}
