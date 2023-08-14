package net.navatwo.gradle.testkit.junit5

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GradleProject(
  /**
   * The project directory to load from. This is relative to [GradleProjectsRoot.directory].
   */
  val projectDir: String
) {

  /**
   * Denotes a [java.io.File] or [java.nio.file.Path] parameter that should be resolved to the root directory of the
   * [GradleProject].
   */
  @Target(AnnotationTarget.VALUE_PARAMETER)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class Root

  /**
   * Denotes a [org.gradle.testkit.runner.GradleRunner] parameter that should be resolved to a
   * [org.gradle.testkit.runner.GradleRunner] that has been configured.
   */
  @Target(AnnotationTarget.VALUE_PARAMETER)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class Runner
}
