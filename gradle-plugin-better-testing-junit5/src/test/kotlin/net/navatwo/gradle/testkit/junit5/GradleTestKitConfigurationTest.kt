package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.ClasspathMode.NO_PROJECT_CLASSPATH
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.Companion.merge
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GradleTestKitConfigurationTest {
  @Test
  fun `verify merge keeps order of first in wins`() {
    val methodAnn = GradleTestKitConfiguration(
      projectsRoot = "method",
      // Not overridden
      // testKitDirectory = "method",
      gradleVersion = "method",
    )
    val classAnn = GradleTestKitConfiguration(
      projectsRoot = "class",
      testKitDirectory = "class",
      gradleVersion = "class",
      classpathMode = NO_PROJECT_CLASSPATH,
    )

    assertThat(merge(methodAnn, classAnn)).isEqualTo(
      GradleTestKitConfiguration(
        projectsRoot = methodAnn.projectsRoot,
        testKitDirectory = classAnn.testKitDirectory,
        classpathMode = classAnn.classpathMode,
        gradleVersion = methodAnn.gradleVersion,
      )
    )
  }
}
