package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.assertj.isSuccess
import net.navatwo.gradle.testkit.assertj.task
import net.navatwo.gradle.testkit.junit5.GradleProject.Root
import net.navatwo.gradle.testkit.junit5.GradleProject.Runner
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name

private const val TEST_KIT_DIRECTORY = "build/root-NestedTestKitTests/test-kit"

@GradleProjectsRoot(directory = "src/test/other-projects")
@GradleTestKitDirectory(directory = TEST_KIT_DIRECTORY)
class NestedTestKitTests {
  private val testKitDir = File(TEST_KIT_DIRECTORY)

  @BeforeEach
  fun cleanTestKitDir() {
    testKitDir.deleteRecursively()
  }

  @AfterEach
  fun checkTestKitDir() {
    assertThat(testKitDir)
      .`as` { "$testKitDir should be created by [GradleRunner] executions" }
      .isDirectory()
  }

  @GradleProjectsRoot(directory = "src/test/projects")
  @Nested
  inner class TestKitPathIsInherited {
    @Test
    @GradleProject(projectDir = "default-project-root")
    fun `default-project-root with inheriting changed test kit path`(
      @Root projectRoot: Path,
      @Runner gradleRunner: GradleRunner
    ) {
      assertThat(gradleRunner.projectDir).isEqualTo(projectRoot.toFile())
      assertThat(gradleRunner.withArguments("touch").build()).task(":touch").isSuccess()

      assertThat(projectRoot).isDirectory()
        .isDirectoryContaining { it.name == "build.gradle.kts" }
        .isDirectoryContaining { it.name == "projects-default-project-root.keep" }
    }
  }
}
