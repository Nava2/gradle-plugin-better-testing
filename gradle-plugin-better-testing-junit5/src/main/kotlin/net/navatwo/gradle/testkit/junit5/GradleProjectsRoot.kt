package net.navatwo.gradle.testkit.junit5

/**
 * Defines the root directory for all [GradleProject]s.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GradleProjectsRoot(
  /**
   * Directory for all [GradleProject]s. This is relative to the project root. Defaults to
   * [GradleTestKitProjectExtension.DEFAULT_GRADLE_PROJECT_ROOT_DIRECTORY].
   */
  val directory: String,
)
