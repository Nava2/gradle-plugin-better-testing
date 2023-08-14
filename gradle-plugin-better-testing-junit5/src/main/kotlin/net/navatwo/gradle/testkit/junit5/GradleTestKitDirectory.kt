package net.navatwo.gradle.testkit.junit5

/**
 * Annotates the directory used for [org.gradle.testkit.runner.GradleRunner.withTestKitDir].
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GradleTestKitDirectory(
  /**
   * Directory for all [GradleProject]s. This is relative to the project root.
   */
  val directory: String,
)
