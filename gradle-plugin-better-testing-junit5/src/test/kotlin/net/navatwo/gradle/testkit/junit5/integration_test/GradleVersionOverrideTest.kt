package net.navatwo.gradle.testkit.junit5.integration_test

import net.navatwo.gradle.testkit.junit5.GradleProject
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test

private const val GRADLE_VERSION_OVERRIDE = "8.2"

@GradleTestKitConfiguration(gradleVersion = GRADLE_VERSION_OVERRIDE)
class GradleVersionOverrideTest {
  @Test
  @GradleProject(projectDir = "dump-version")
  fun `gradleVersion in configuration overrides gradle version`(
    @GradleProject.Runner gradleRunner: GradleRunner
  ) {
    val result = gradleRunner.withArguments("dumpVersion").build()
    assertThat(result.output).contains("gradle_version=$GRADLE_VERSION_OVERRIDE")
  }
}
