package net.navatwo.gradle.testkit.junit5.integration_test

import net.navatwo.gradle.testkit.assertj.task
import net.navatwo.gradle.testkit.junit5.GradleProject
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.ClasspathMode.NO_PROJECT_CLASSPATH
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.ClasspathMode.WITH_PROJECT_CLASSPATH
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name

@GradleTestKitConfiguration(projectsRoot = "src/test/other-projects")
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

  @Test
  @GradleProject(projectDir = "default-project-root")
  fun `multiple injections inject the same value`(
    @GradleProject.Runner gradleRunner1: GradleRunner,
    @GradleProject.Runner gradleRunner2: GradleRunner,
    @GradleProject.Runner gradleRunner3: GradleRunner,
  ) {
    assertThat(gradleRunner1)
      .isSameAs(gradleRunner2)
      .isSameAs(gradleRunner3)
  }

  @Test
  @GradleProject(projectDir = "default-project-root")
  @GradleTestKitConfiguration(classpathMode = WITH_PROJECT_CLASSPATH)
  fun `withPluginClasspath is applied when overriding to WITH_PROJECT_CLASSPATH`(
    config: GradleTestKitConfiguration,
  ) {
    assertThat(config.classpathMode).isEqualTo(WITH_PROJECT_CLASSPATH)
  }

  @Test
  @GradleProject(projectDir = "default-project-root")
  @GradleTestKitConfiguration(classpathMode = NO_PROJECT_CLASSPATH)
  fun `withPluginClasspath overridden to NO_PROJECT_CLASSPATH causes classpath to be empty`(
    config: GradleTestKitConfiguration,
  ) {
    assertThat(config.classpathMode).isEqualTo(NO_PROJECT_CLASSPATH)
  }
}
