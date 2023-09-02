package net.navatwo.gradle.testkit.junit5.integration_test

import net.navatwo.gradle.testkit.assertj.task
import net.navatwo.gradle.testkit.junit5.GradleProject
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name

@GradleTestKitConfiguration(projectsRoot = "src/test/other-projects")
class NestedTests {

  @Test
  @GradleTestKitConfiguration(projectsRoot = "src/test/projects")
  @GradleProject(projectDir = "default-project-root")
  fun `with method override - default-project-root`(
    @GradleProject.Root projectRoot: Path,
    @GradleProject.Runner gradleRunner: GradleRunner
  ) {
    assertThat(gradleRunner.projectDir).isEqualTo(projectRoot.toFile())
    assertThat(gradleRunner.withArguments("touch").build()).task(":touch").isSuccess()

    assertThat(projectRoot)
      .isDirectoryContaining { it.name == "build.gradle.kts" }
      .isDirectoryContaining { it.name == "projects-default-project-root.keep" }

    assertThat(projectRoot.resolve("build"))
      .isDirectoryContaining { it.name == "touch" }
  }

  @GradleTestKitConfiguration(projectsRoot = "src/test/projects")
  @Nested
  inner class OverrideGradleProjectsRoot {
    @Test
    @GradleProject(projectDir = "default-project-root")
    fun `with class override - default-project-root`(
      @GradleProject.Root projectRoot: Path,
      @GradleProject.Runner gradleRunner: GradleRunner
    ) {
      assertThat(gradleRunner.projectDir).isEqualTo(projectRoot.toFile())
      assertThat(gradleRunner.withArguments("touch").build()).task(":touch").isSuccess()

      assertThat(projectRoot)
        .isDirectoryContaining { it.name == "build.gradle.kts" }
        .isDirectoryContaining { it.name == "projects-default-project-root.keep" }

      assertThat(projectRoot.resolve("build"))
        .isDirectoryContaining { it.name == "touch" }
    }
  }

  @Nested
  inner class NoOverrides {
    @Test
    @GradleProject(projectDir = "default-project-root")
    fun `no overrides - default-project-root`(
      @GradleProject.Root projectRoot: File,
      @GradleProject.Runner gradleRunner: GradleRunner
    ) {
      assertThat(gradleRunner.projectDir).isEqualTo(projectRoot)

      assertThat(gradleRunner.withArguments("touch").build()).task(":touch").isSuccess()

      assertThat(projectRoot)
        .isDirectoryContaining { it.name == "build.gradle.kts" }
        .isDirectoryContaining { it.name == "other-projects-default-project-root.keep" }

      assertThat(projectRoot.resolve("build"))
        .isDirectoryContaining { it.name == "touch" }
    }
  }
}
