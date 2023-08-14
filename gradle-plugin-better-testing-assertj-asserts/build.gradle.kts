plugins {
  id("better-testing.kotlin")
}

dependencies {
  api(libs.assertj)

  compileOnly(gradleTestKit())
}