package net.navatwo.gradle.testkit.junit5

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
  val gradleVersion: String = NO_OVERRIDE_VERSION,
) {
  companion object {
    internal const val NO_OVERRIDE_VERSION = "!!NO_OVERRIDE!!"

    internal val GradleTestKitConfiguration.effectiveGradleVersion: String?
      get() {
        return gradleVersion.takeIf { gradleVersion != NO_OVERRIDE_VERSION }
          ?: System.getProperty("net.navatwo.gradle.testkit.junit5.gradleVersion", null)
      }

    internal val GradleTestKitConfiguration.effectiveWithPluginClasspath: Boolean
      get() {
        val isInternalTest = System.getProperty("net.navatwo.gradle.testkit.junit5.internal", null)?.toBoolean()
        val ifInternalDoNotUseClasspath = isInternalTest?.let { !it }
        return ifInternalDoNotUseClasspath ?: withPluginClasspath
      }
  }
}
