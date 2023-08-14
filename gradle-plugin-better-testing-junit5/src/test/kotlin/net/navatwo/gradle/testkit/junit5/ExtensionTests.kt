package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.assertj.isSuccess
import net.navatwo.gradle.testkit.assertj.task
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name

@GradleProjectsRoot(directory = "src/test/other-projects")
class ExtensionTests {

  @Test
  @GradleProject(projectDir = "default-project-root")
  fun `project root as Path`(
    @GradleProject.Root projectRoot: Path,
    @GradleProject.Runner gradleRunner: GradleRunner
  ) {
    assertThat(gradleRunner.projectDir).isEqualTo(projectRoot.toFile())
  }

  @Test
  @GradleProject(projectDir = "default-project-root")
  fun `project root as File`(
    @GradleProject.Root projectRoot: File,
    @GradleProject.Runner gradleRunner: GradleRunner
  ) {
    assertThat(gradleRunner.projectDir).isEqualTo(projectRoot)
  }

  @Test
  @GradleProject(projectDir = "default-project-root")
  fun `execute a build is successful and runs in the temp directory`(
    @GradleProject.Root projectRoot: Path,
    @GradleProject.Runner gradleRunner: GradleRunner,
  ) {
    assertThat(gradleRunner.withArguments("touch").build()).task(":touch").isSuccess()

    // Verify the project path has build sources
    assertThat(projectRoot.resolve("build")).isDirectoryContaining { it.name == "touch" }

    // Verify the original path is unmodified
    val originalRoot = Paths.get("src/test/projects/default-project-root")
    assertThat(originalRoot.resolve("build")).doesNotExist()
  }

  @Test
  @GradleProject(projectDir = "default-project-root")
  fun `no parameters is useless, but does not throw`() {
    assertThat(true).isTrue()
  }
}
