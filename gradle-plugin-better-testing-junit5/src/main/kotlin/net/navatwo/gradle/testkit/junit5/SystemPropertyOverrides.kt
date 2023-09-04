package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.BuildDirectoryMode

/**
 * Defines system property overrides.
 */
internal object SystemPropertyOverrides {
  private const val SYSTEM_PREFIX = "net.navatwo.gradle.testkit.junit5"

  /**
   * @see GradleTestKitConfiguration.projectsRoot
   */
  internal const val SYSTEM_PROJECT_ROOTS = "$SYSTEM_PREFIX.projectRoots"

  /**
   * @see GradleTestKitConfiguration.testKitDirectory
   */
  internal const val SYSTEM_TEST_KIT_DIRECTORY = "$SYSTEM_PREFIX.testKitDirectory"

  /**
   * @see GradleTestKitConfiguration.withPluginClasspath
   */
  private const val SYSTEM_IS_INTERNAL = "$SYSTEM_PREFIX.internal"

  /**
   * @see GradleTestKitConfiguration.withPluginClasspath
   */
  internal const val SYSTEM_WITH_PLUGIN_CLASSPATH = "$SYSTEM_PREFIX.withPluginClasspath"

  /**
   * @see GradleTestKitConfiguration.gradleVersion
   */
  internal const val SYSTEM_GRADLE_VERSION = "$SYSTEM_PREFIX.gradleVersion"

  /**
   * @see GradleTestKitConfiguration.buildDirectoryMode
   */
  internal const val SYSTEM_BUILD_DIRECTORY_MODE = "$SYSTEM_PREFIX.buildDirectoryMode"

  fun systemConfiguration(): GradleTestKitConfiguration = GradleTestKitConfiguration(
    projectsRoot = System.getProperty(SYSTEM_PROJECT_ROOTS, GradleTestKitConfiguration.NO_OVERRIDE_VERSION),
    testKitDirectory = System.getProperty(SYSTEM_TEST_KIT_DIRECTORY, GradleTestKitConfiguration.NO_OVERRIDE_VERSION),
    withPluginClasspath = System.getProperty(
      SYSTEM_WITH_PLUGIN_CLASSPATH,
      GradleTestKitConfiguration.DEFAULT_WITH_PLUGIN_CLASSPATH.toString(),
    ).toBoolean(),
    gradleVersion = System.getProperty(SYSTEM_GRADLE_VERSION, GradleTestKitConfiguration.NO_OVERRIDE_VERSION),
    buildDirectoryMode = BuildDirectoryMode.valueOf(
      System.getProperty(SYSTEM_BUILD_DIRECTORY_MODE, BuildDirectoryMode.UNSET.name),
    ),
  )

  internal fun internalConfiguration(): GradleTestKitConfiguration = GradleTestKitConfiguration(
    withPluginClasspath = run {
      val isInternalTest = System.getProperty(
        SYSTEM_IS_INTERNAL,
        GradleTestKitConfiguration.DEFAULT_WITH_PLUGIN_CLASSPATH.toString()
      ).toBoolean()
      val ifInternalDoNotUseClasspath = !isInternalTest
      ifInternalDoNotUseClasspath
    },
  )
}
