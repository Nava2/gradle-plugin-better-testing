package net.navatwo.gradle.testkit.junit5.integration_test

import net.navatwo.gradle.testkit.junit5.GradleProject
import net.navatwo.gradle.testkit.junit5.GradleProject.Root
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path

class BeforeEachParameterInjectionTest {
  private lateinit var fromSetup: Path

  @BeforeEach
  fun setupWithParameters(@Root rootPath: Path) {
    fromSetup = rootPath
  }

  @Test
  @GradleProject("default-project-root")
  fun `verify setup has parameters`(@Root rootPath: Path) {
    Assertions.assertThat(fromSetup).isEqualTo(rootPath)
  }
}
