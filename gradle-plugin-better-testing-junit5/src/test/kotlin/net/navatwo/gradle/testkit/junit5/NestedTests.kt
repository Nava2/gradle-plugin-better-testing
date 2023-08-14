package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.assertj.isSuccess
import net.navatwo.gradle.testkit.assertj.task
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.name

@GradleProjectsRoot(directory = "src/test/other-projects")
class NestedTests {

  @GradleProjectsRoot(directory = "src/test/projects")
  @Nested
  inner class OverrideGradleProjectsRoot {
    @Test
    @GradleProject(projectDir = "default-project-root")
    fun `default-project-root`(
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
    fun `default-project-root`(
      @GradleProject.Root projectRoot: Path,
      @GradleProject.Runner gradleRunner: GradleRunner
    ) {
      assertThat(gradleRunner.projectDir).isEqualTo(projectRoot.toFile())

      assertThat(gradleRunner.withArguments("touch").build()).task(":touch").isSuccess()

      assertThat(projectRoot)
        .isDirectoryContaining { it.name == "build.gradle.kts" }
        .isDirectoryContaining { it.name == "other-projects-default-project-root.keep" }

      assertThat(projectRoot.resolve("build"))
        .isDirectoryContaining { it.name == "touch" }
    }
  }
}
