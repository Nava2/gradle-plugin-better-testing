package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.BuildDirectoryMode
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.Companion.DEFAULT_PROJECT_ROOTS
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.Companion.DEFAULT_TESTKIT_DIRECTORY
import org.gradle.internal.impldep.org.eclipse.jgit.errors.NotSupportedException
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KProperty1

/**
 * Used to set configuration values for the [GradleTestKitProjectExtension].
 *
 * This annotation can be applied to test methods, or classes. The closest specified value will be used per method.
 */
@Retention(RUNTIME)
@Target(CLASS, FUNCTION)
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
   * System Override: `net.navatwo.gradle.testkit.junit5.testKitDirectory` - [SystemPropertyOverrides.SYSTEM_TEST_KIT_DIRECTORY]
   */
  val testKitDirectory: String = NO_OVERRIDE_VERSION,

  /**
   * If [ClasspathMode.WITH_PROJECT_CLASSPATH], calls
   * [org.gradle.testkit.runner.GradleRunner.withPluginClasspath].
   *
   * @see ClasspathMode
   * System Override: `net.navatwo.gradle.testkit.junit5.withPluginClasspath` - [SystemPropertyOverrides.SYSTEM_WITH_PLUGIN_CLASSPATH]
   */
  val classpathMode: ClasspathMode = ClasspathMode.UNSET,

  /**
   * If specified, sets the gradle version via [org.gradle.testkit.runner.GradleRunner.withGradleVersion].
   *
   * **Default:** Uses the version of gradle on the classpath.
   * **System Override:** `net.navatwo.gradle.testkit.junit5.gradleVersion` - [SystemPropertyOverrides.SYSTEM_GRADLE_VERSION]
   */
  val gradleVersion: String = NO_OVERRIDE_VERSION,

  /**
   * Sets the build directory mode.
   *
   * **Default:** [BuildDirectoryMode.CLEAN_BUILD]
   * **System Override:** `net.navatwo.gradle.testkit.junit5.buildDirectoryMode` - [SystemPropertyOverrides.SYSTEM_BUILD_DIRECTORY_MODE]
   *
   * @see BuildDirectoryMode
   */
  val buildDirectoryMode: BuildDirectoryMode = BuildDirectoryMode.UNSET,
) {
  enum class ClasspathMode {
    /**
     * Use the project classpath, i.e. call [org.gradle.testkit.runner.GradleRunner.withPluginClasspath].
     */
    WITH_PROJECT_CLASSPATH {
      override fun setupRunner(runner: GradleRunner) {
        runner.withPluginClasspath()
      }
    },

    /**
     * Do not add the project classpath, i.e. do not call [org.gradle.testkit.runner.GradleRunner.withPluginClasspath].
     */
    NO_PROJECT_CLASSPATH {
      override fun setupRunner(runner: GradleRunner) = Unit
    },

    /**
     * Unset value
     */
    UNSET {
      override fun setupRunner(runner: GradleRunner) {
        throw NotSupportedException("Must specify [PluginClasspathMode] specifically.")
      }
    }
    ;

    internal abstract fun setupRunner(runner: GradleRunner)
  }

  enum class BuildDirectoryMode {
    /**
     * Always use a fully clean test directory by copying into a temporary directory.
     *
     * Note: This makes debugging harder, it's often better to use another option unless required.
     */
    PRISTINE {
      override fun setupTestDirectory(projectRoot: File) = TestExecutionDirectory.Pristine(projectRoot)
    },

    /**
     * Runs build in tree, with a clean build directory.
     */
    CLEAN_BUILD {
      override fun setupTestDirectory(projectRoot: File) = TestExecutionDirectory.Cleaned(projectRoot)
    },

    /**
     * Runs build in tree, with all directories as-is.
     */
    DIRTY_BUILD {
      override fun setupTestDirectory(projectRoot: File) = TestExecutionDirectory.Dirty(projectRoot)
    },

    /**
     * Unset, use default.
     */
    UNSET {
      override fun setupTestDirectory(projectRoot: File): TestExecutionDirectory {
        throw NotSupportedException("UNSET is not a valid configuration.")
      }
    },
    ;

    internal abstract fun setupTestDirectory(projectRoot: File): TestExecutionDirectory
  }

  companion object {
    internal const val NO_OVERRIDE_VERSION = "!!NO_OVERRIDE!!"

    internal const val DEFAULT_PROJECT_ROOTS = "src/test/projects"

    internal const val DEFAULT_TESTKIT_DIRECTORY = "build/test-kit"

    private val DEFAULT_WITH_PLUGIN_CLASSPATH = ClasspathMode.WITH_PROJECT_CLASSPATH

    private const val DEFAULT_GRADLE_VERSION = NO_OVERRIDE_VERSION

    internal val GradleTestKitConfiguration.effectiveGradleVersion: String?
      get() = gradleVersion.takeIf { it != NO_OVERRIDE_VERSION }

    internal val DEFAULT = GradleTestKitConfiguration(
      projectsRoot = DEFAULT_PROJECT_ROOTS,
      testKitDirectory = DEFAULT_TESTKIT_DIRECTORY,
      classpathMode = DEFAULT_WITH_PLUGIN_CLASSPATH,
      gradleVersion = DEFAULT_GRADLE_VERSION,
      buildDirectoryMode = BuildDirectoryMode.CLEAN_BUILD,
    )

    /**
     * Merges [next] into [current] where any value not set in [current] will be read from
     * [next].
     */
    internal fun merge(
      current: GradleTestKitConfiguration,
      next: GradleTestKitConfiguration
    ): GradleTestKitConfiguration {
      fun <T : Any> ifNotDefault(default: T, property: KProperty1<GradleTestKitConfiguration, T>): T {
        val currentValue = property.get(current)
        val nextValue = property.get(next)
        return when {
          currentValue == default -> nextValue
          nextValue == default -> currentValue
          else -> currentValue
        }
      }

      return GradleTestKitConfiguration(
        projectsRoot = ifNotDefault(NO_OVERRIDE_VERSION, GradleTestKitConfiguration::projectsRoot),
        testKitDirectory = ifNotDefault(NO_OVERRIDE_VERSION, GradleTestKitConfiguration::testKitDirectory),
        classpathMode = ifNotDefault(ClasspathMode.UNSET, GradleTestKitConfiguration::classpathMode),
        gradleVersion = ifNotDefault(NO_OVERRIDE_VERSION, GradleTestKitConfiguration::gradleVersion),
        buildDirectoryMode = ifNotDefault(BuildDirectoryMode.UNSET, GradleTestKitConfiguration::buildDirectoryMode)
      )
    }
  }
}
