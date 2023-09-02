package net.navatwo.gradle.testkit.junit5

/**
 * Used to set configuration values for the [GradleTestKitProjectExtension].
 *
 * This annotation can be applied to test methods, or classes. The closest specified value will be used per method.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class GradleTestKitConfiguration(
  /**
   * Sets the root path for all [GradleProject] annotations.
   *
   * Default value: [DEFAULT_PROJECT_ROOTS].
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
   */
  val withPluginClasspath: Boolean = DEFAULT_WITH_PLUGIN_CLASSPATH,

  /**
   * If specified, sets the gradle version via [org.gradle.testkit.runner.GradleRunner.withGradleVersion].
   *
   * Default: Uses the version of gradle on the classpath.
   *
   * This can also be overridden by setting the system property `net.navatwo.gradle.testkit.junit5.gradleVersion`.
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
      fun <T : Any> ifNotDefault(default: T, current: T, next: T): T {
        return when {
          current == default -> next
          next == default -> current
          else -> current
        }
      }

      return GradleTestKitConfiguration(
        projectsRoot = ifNotDefault(NO_OVERRIDE_VERSION, current.projectsRoot, next.projectsRoot),
        testKitDirectory = ifNotDefault(NO_OVERRIDE_VERSION, current.testKitDirectory, next.testKitDirectory),
        withPluginClasspath = ifNotDefault(
          DEFAULT_WITH_PLUGIN_CLASSPATH,
          current.withPluginClasspath,
          next.withPluginClasspath
        ),
        gradleVersion = ifNotDefault(NO_OVERRIDE_VERSION, current.gradleVersion, next.gradleVersion),
      )
    }
  }
}
