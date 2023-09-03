package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.Companion.effectiveGradleVersion
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ExtensionContext.Store
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.kotlinFunction

/**
 * Provides a JUnit 5 extension for loading [GradleProject] in a consistent way.
 *
 * This provides sensible defaults for performance and ease of use. All defaults are overridden via annotations. To use
 * this extension, enable extensions in your project either via:
 * 1. Add `-Djunit.jupiter.extensions.autodetection.enabled=true` to your Test JVM arguments
 * 2. Add `@ExtendsWith(GradleTestKitProjectExtension::class)` to your test class
 *
 * This extension assumes projects are defined in a "projects" directory (e.g. `src/test/projects`). This can be
 * overridden by annotating your test class with [GradleTestKitConfiguration.projectsRoot] (e.g.
 * `@GradleTestKitConfiguration(projectRoots = "src/test/other-projects")`).
 *
 * To use a specific project, annotate a test method with [GradleProject]:
 * ```kotlin
 * @Test
 * @GradleProject("lazy-evaluation-successful")
 * fun `lazy evaluation is successful`(@GradleProject.Runner gradleRunner: GradleRunner) {
 *   assertThat(gradleRunner.withArguments("tasks").build()).task(":tasks").isSuccess()
 * }
 * ```
 *
 * <h3>Gradle TestKit</h3>
 *
 * By default, this extension will set any injected [GradleRunner] to share a `TestKit` directory in the `build/`
 * directory for the project - [GradleTestKitConfiguration.DEFAULT_TESTKIT_DIRECTORY]). This can be overridden by
 * annotating your test class with [GradleTestKitConfiguration.testKitDirectory]. This is done to _greatly_ improve the
 * speed of tests by avoiding re-downloading Gradle dependencies with each test run.
 *
 * @see GradleProject
 * @see GradleTestKitConfiguration
 */
class GradleTestKitProjectExtension : BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private val logger: Logger = Logger.getLogger(GradleTestKitProjectExtension::class.qualifiedName)

  override fun beforeEach(context: ExtensionContext) {
    val testMethod = context.requiredTestMethod?.kotlinFunction
    val relativeProjectRootPath = testMethod?.findAnnotation<GradleProject>()?.projectDir
      ?: return // Nothing to do for us if there's no annotation

    val store = context.getStore(Namespace.GLOBAL)

    val testKitConfig = ConfigurationProvider.getConfigForContext(context)
    store.putKey(Keys.Configuration, testKitConfig)

    val projectsRootDirectoryPath = testKitConfig.projectsRoot

    val projectsRootDirectory = Paths.get(projectsRootDirectoryPath)
    check(projectsRootDirectory.exists() && projectsRootDirectory.isDirectory()) {
      "Gradle project root directory '$projectsRootDirectory' does not exist or is not a directory."
    }

    store.putKey(Keys.ProjectsRoot, projectsRootDirectory)

    val gradleTestKitDirectoryPath = testKitConfig.testKitDirectory
    val gradleTestKitDirectory = Paths.get(gradleTestKitDirectoryPath).absolute()
    check(gradleTestKitDirectory.isDirectory() || gradleTestKitDirectory.notExists()) {
      "Gradle test kit directory '$gradleTestKitDirectory' exists but is not a directory."
    }

    store.putKey(Keys.TestKitDirectory, gradleTestKitDirectory)

    val projectRoot = projectsRootDirectory.resolve(relativeProjectRootPath)
    check(projectRoot.exists() && projectRoot.isDirectory()) {
      "Gradle project root directory '$projectRoot' does not exist or is not a directory."
    }

    val tempDirectory = TempDirectory(Files.createTempDirectory("gradle-testkit-project"))
    store.putKey(Keys.Project, tempDirectory)

    projectRoot.toFile().copyRecursively(tempDirectory.path.toFile(), overwrite = true)
  }

  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
    val testMethod = extensionContext.requiredTestMethod?.kotlinFunction
    if (testMethod?.findAnnotation<GradleProject>() == null) {
      return false // not supported!
    }

    val parameterType = parameterContext.parameter.type
    return when {
      parameterContext.isAnnotated(GradleProject.Root::class.java) && (
        parameterType == Path::class.java || parameterType == File::class.java
        ) -> true

      parameterContext.isAnnotated(GradleProject.Runner::class.java) &&
        parameterType == GradleRunner::class.java -> true

      else -> false
    }
  }

  override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
    val store = extensionContext.getStore(Namespace.GLOBAL)

    val projectRoot = store.getKey(Keys.Project).path

    val parameterType = parameterContext.parameter.type
    return when {
      parameterContext.isAnnotated(GradleProject.Root::class.java) && (
        parameterType == Path::class.java || parameterType == File::class.java
        ) -> {
        if (parameterType == Path::class.java) {
          projectRoot
        } else {
          projectRoot.toFile()
        }
      }

      parameterContext.isAnnotated(GradleProject.Runner::class.java) &&
        parameterType == GradleRunner::class.java -> {
        store.getOrComputeIfAbsent(Keys.Runner) {
          val gradleTestKitConfiguration = store.getKey(Keys.Configuration)

          val runner = GradleRunner.create()
            .withProjectDir(projectRoot.toFile())

          if (gradleTestKitConfiguration.withPluginClasspath) {
            runner.withPluginClasspath()
          }

          val gradleVersionOverride = gradleTestKitConfiguration.effectiveGradleVersion
          if (gradleVersionOverride != null) {
            runner.withGradleVersion(gradleVersionOverride)
          }

          val gradleTestKitDirectory = store.getKey(Keys.TestKitDirectory)
          runner.withTestKitDir(gradleTestKitDirectory.toFile())

          runner
        }
      }

      else -> error("Unsupported parameter: $parameterContext")
    }
  }

  override fun afterEach(context: ExtensionContext) {
    val store = context.getStore(Namespace.GLOBAL)

    val tempDirectory = store.findKey(Keys.Project)
    try {
      tempDirectory?.close()
    } catch (ioe: IOException) {
      logger.log(Level.WARNING, "Could not close ${tempDirectory?.path?.absolute()}", ioe)
    }
  }

  private sealed class Keys<T : Any>(val type: KClass<T>) {
    data object Configuration : Keys<GradleTestKitConfiguration>(GradleTestKitConfiguration::class)

    data object ProjectsRoot : Keys<Path>(Path::class)

    data object TestKitDirectory : Keys<Path>(Path::class)

    data object Project : Keys<TempDirectory>(TempDirectory::class)

    data object Runner : Keys<GradleRunner>(GradleRunner::class)
  }

  private fun <T : Any> Store.putKey(key: Keys<T>, value: T) {
    put(key, value)
  }

  private fun <T : Any> Store.getKey(key: Keys<T>): T {
    return get(key, key.type.java)!!
  }

  private fun <T : Any> Store.findKey(key: Keys<T>): T? {
    return get(key, key.type.java)
  }

  private fun <T : Any> Store.getOrComputeIfAbsent(key: Keys<T>, compute: () -> T): T {
    return getOrComputeIfAbsent(key, { compute() }, key.type.java)
  }

  private data class TempDirectory(val path: Path) : AutoCloseable {
    override fun close() {
      path.toFile().deleteRecursively()
    }
  }
}
