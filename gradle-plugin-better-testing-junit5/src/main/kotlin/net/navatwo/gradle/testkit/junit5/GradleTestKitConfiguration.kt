package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.Companion.DEFAULT_PROJECT_ROOTS
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.Companion.DEFAULT_TESTKIT_DIRECTORY
import java.lang.annotation.Inherited

/**
 * Used to set configuration values for the [GradleTestKitProjectExtension].
 *
 * This annotation can be applied to test methods, or classes. The closest specified value will be used per method.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Inherited
annotation class GradleTestKitConfiguration(
  /**
   * Sets the root path for all [GradleProject] annotations.
   *
   * Default value: [DEFAULT_PROJECT_ROOTS].
   * System Override: `net.navatwo.gradle.testkit.junit5.projectRoots` - [SystemPropertyOverrides.SYSTEM_PROJECT_ROOTS]
   */
  val projectsRoot: String = NO_OVERRIDE_VERSION,

  /**
   * Annotates the directory used for [org.gradle.testkit.runner.GradleRunner.withTestKitDir].
   *
   * Default value: [DEFAULT_TESTKIT_DIRECTORY]
   */
  val testKitDirectory: String = NO_OVERRIDE_VERSION,

  /**
   * If true, calls [org.gradle.testkit.runner.GradleRunner.withPluginClasspath].
   *
   * System Override: `net.navatwo.gradle.testkit.junit5.withPluginClasspath` - [SystemPropertyOverrides.SYSTEM_WITH_PLUGIN_CLASSPATH]
   */
  val withPluginClasspath: Boolean = DEFAULT_WITH_PLUGIN_CLASSPATH,

  /**
   * If specified, sets the gradle version via [org.gradle.testkit.runner.GradleRunner.withGradleVersion].
   *
   * **Default:** Uses the version of gradle on the classpath.
   * **System Override:** `net.navatwo.gradle.testkit.junit5.gradleVersion` - [SystemPropertyOverrides.SYSTEM_GRADLE_VERSION]
   */
  val gradleVersion: String = NO_OVERRIDE_VERSION,
) {
  companion object {
    internal const val NO_OVERRIDE_VERSION = "!!NO_OVERRIDE!!"

    internal const val DEFAULT_PROJECT_ROOTS = "src/test/projects"

    internal const val DEFAULT_TESTKIT_DIRECTORY = "build/test-kit"

    internal const val DEFAULT_WITH_PLUGIN_CLASSPATH = true

    internal const val DEFAULT_GRADLE_VERSION = NO_OVERRIDE_VERSION

    internal val GradleTestKitConfiguration.effectiveGradleVersion: String?
      get() = gradleVersion.takeIf { it != NO_OVERRIDE_VERSION }

    internal val DEFAULT = GradleTestKitConfiguration(
      projectsRoot = DEFAULT_PROJECT_ROOTS,
      testKitDirectory = DEFAULT_TESTKIT_DIRECTORY,
      withPluginClasspath = DEFAULT_WITH_PLUGIN_CLASSPATH,
      gradleVersion = DEFAULT_GRADLE_VERSION,
    )

    /**
     * Merges [next] into [current] where any value not set in [current] will be read from
     * [next].
     */
    internal fun merge(
      current: GradleTestKitConfiguration,
      next: GradleTestKitConfiguration
    ): GradleTestKitConfiguration {
      fun <T : Any> ifNotDefault(default: T, current: T, next: T): T = when {
        current == default -> next
        next == default -> current
        else -> current
      }

      return GradleTestKitConfiguration(
        projectsRoot = ifNotDefault(NO_OVERRIDE_VERSION, current.projectsRoot, next.projectsRoot),
        testKitDirectory = ifNotDefault(NO_OVERRIDE_VERSION, current.testKitDirectory, next.testKitDirectory),
        withPluginClasspath = ifNotDefault(
          default = DEFAULT_WITH_PLUGIN_CLASSPATH,
          current = current.withPluginClasspath,
          next = next.withPluginClasspath,
        ),
        gradleVersion = ifNotDefault(NO_OVERRIDE_VERSION, current.gradleVersion, next.gradleVersion),
      )
    }
  }
}
