package net.navatwo.gradle.testkit.junit5.integration_test

import net.navatwo.gradle.testkit.junit5.GradleProject
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.BuildDirectoryMode.CLEAN_BUILD
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.BuildDirectoryMode.DIRTY_BUILD
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.BuildDirectoryMode.PRISTINE
import net.navatwo.gradle.testkit.junit5.TestExecutionDirectory.Pristine.Companion.TEMP_DIRECTORY_PREFIX
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class BuildDirectoryModeTests {
  @Test
  @GradleProject(projectDir = "default-project-root")
  @GradleTestKitConfiguration(buildDirectoryMode = CLEAN_BUILD)
  fun `clean build is in tree`(
    @GradleProject.Root projectRoot: File,
  ) {
    assertThat(projectRoot.name).doesNotStartWith(TEMP_DIRECTORY_PREFIX)
  }

  @Test
  @GradleProject(projectDir = "default-project-root")
  @GradleTestKitConfiguration(buildDirectoryMode = DIRTY_BUILD)
  fun `dirty build is in tree`(
    @GradleProject.Root projectRoot: File,
  ) {
    assertThat(projectRoot.name).doesNotStartWith(TEMP_DIRECTORY_PREFIX)
  }

  @Test
  @GradleProject(projectDir = "default-project-root")
  @GradleTestKitConfiguration(buildDirectoryMode = PRISTINE)
  fun `pristine build is in temp directory`(
    @GradleProject.Root projectRoot: File,
  ) {
    assertThat(projectRoot.name).startsWith(TEMP_DIRECTORY_PREFIX)
  }
}
