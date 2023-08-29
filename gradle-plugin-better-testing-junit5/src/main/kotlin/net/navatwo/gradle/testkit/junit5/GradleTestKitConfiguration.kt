package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.GradleVersionOverride.Companion.NO_OVERRIDE_VERSION
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.GradleVersionOverride.Companion.isNoOverride

/**
 * Used to set configuration values for the [GradleTestKitProjectExtension].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class GradleTestKitConfiguration(
  /**
   * Sets the root path for all [GradleProject] annotations.
   *
   * Default value: `src/test/projects`.
   */
  val projectsRoot: String = "src/test/projects",
  /**
   * Annotates the directory used for [org.gradle.testkit.runner.GradleRunner.withTestKitDir].
   *
   * Default value: `build/test-kit`
   */
  val testKitDirectory: String = "build/test-kit",
  /**
   * If true, calls [org.gradle.testkit.runner.GradleRunner.withPluginClasspath].
   */
  val withPluginClasspath: Boolean = true,

  /**
   * If specified, sets the gradle version via [org.gradle.testkit.runner.GradleRunner.withGradleVersion].
   *
   * This can also be overridden by setting the system property `net.navatwo.gradle.testkit.junit5.gradleVersion`.
   */
  val gradleVersion: GradleVersionOverride = GradleVersionOverride(NO_OVERRIDE_VERSION),
) {
  annotation class GradleVersionOverride(
    /**
     * The Gradle version to use for the test.
     */
    val version: String,
  ) {
    companion object {
      const val NO_OVERRIDE_VERSION = "!!NO_OVERRIDE!!"

      internal fun GradleVersionOverride.isNoOverride(): Boolean = version == NO_OVERRIDE_VERSION
    }
  }

  companion object {
    internal val GradleTestKitConfiguration.effectiveGradleVersion: String?
      get() {
        return System.getProperty("net.navatwo.gradle.testkit.junit5.gradleVersion", null)
          ?: gradleVersion.version.takeIf { !gradleVersion.isNoOverride() }
      }

    internal val GradleTestKitConfiguration.effectiveWithPluginClasspath: Boolean
      get() {
        val isInternalTest = System.getProperty("net.navatwo.gradle.testkit.junit5.internal", null)?.toBoolean()
        val ifInternalDoNotUseClasspath = isInternalTest?.let { !it }
        return ifInternalDoNotUseClasspath ?: withPluginClasspath
      }
  }
}
