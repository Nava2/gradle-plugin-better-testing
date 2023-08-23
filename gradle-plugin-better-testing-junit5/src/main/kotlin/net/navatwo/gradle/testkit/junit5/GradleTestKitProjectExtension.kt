package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.junit5.GradleTestKitProjectExtension.Companion.DEFAULT_TEST_KIT_DIRECTORY
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
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
 * overridden by annotating your test class with [GradleProjectsRoot] (e.g.
 * `@GradleProjectsRoot("src/test/other-projects")`).
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
 * directory for the project - [DEFAULT_TEST_KIT_DIRECTORY]). This can be overridden by annotating your test class with
 * [GradleTestKitDirectory]. This is done to _greatly_ improve the speed of tests by avoiding re-downloading Gradle
 * dependencies with each test run.
 *
 * @see GradleProject
 * @see GradleProjectsRoot
 * @see GradleTestKitDirectory
 */
class GradleTestKitProjectExtension : BeforeAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private val logger: Logger = Logger.getLogger(GradleTestKitProjectExtension::class.qualifiedName)

  override fun beforeAll(context: ExtensionContext) {
    val store = context.getStore(Namespace.GLOBAL)

    val projectsRootDirectoryPath = context.findClassAnnotation<GradleProjectsRoot>()?.directory
      ?: DEFAULT_GRADLE_PROJECT_ROOT_DIRECTORY

    val projectsRootDirectory = Paths.get(projectsRootDirectoryPath)
    check(projectsRootDirectory.exists() && projectsRootDirectory.isDirectory()) {
      "Gradle project root directory '$projectsRootDirectory' does not exist or is not a directory."
    }

    store.put(GradleProjectsRoot::class, projectsRootDirectory)

    val gradleTestKitDirectoryPath = context.findClassAnnotation<GradleTestKitDirectory>()?.directory
      ?: DEFAULT_TEST_KIT_DIRECTORY
    val gradleTestKitDirectory = Paths.get(gradleTestKitDirectoryPath).absolute()
    check(gradleTestKitDirectory.isDirectory() || gradleTestKitDirectory.notExists()) {
      "Gradle test kit directory '$gradleTestKitDirectory' exists but is not a directory."
    }

    store.put(GradleTestKitDirectory::class, gradleTestKitDirectory)
  }

  override fun beforeEach(context: ExtensionContext) {
    val testMethod = context.requiredTestMethod?.kotlinFunction
    val relativeProjectRootPath = testMethod?.findAnnotation<GradleProject>()?.projectDir
      ?: return // Nothing to do for us if there's no annotation

    val store = context.getStore(Namespace.GLOBAL)
    val projectsRootDirectory = store.get(GradleProjectsRoot::class, Path::class.java)

    val projectRoot = projectsRootDirectory.resolve(relativeProjectRootPath)
    check(projectRoot.exists() && projectRoot.isDirectory()) {
      "Gradle project root directory '$projectRoot' does not exist or is not a directory."
    }

    val tempDirectory = TempDirectory(Files.createTempDirectory("gradle-testkit-project"))
    store.put(GradleProject.Root::class, tempDirectory)

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

    val projectRoot = store.get(GradleProject.Root::class, TempDirectory::class.java).path

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
        store.getOrComputeIfAbsent(GradleProject.Runner::class) {
          val runner = GradleRunner.create()
            .withProjectDir(projectRoot.toFile())

          if (System.getProperty("net.navatwo.gradle.testkit.junit5.internal", "false") != "true") {
            runner.withPluginClasspath()
          }

          val gradleVersionOverride = System.getProperty(
            "net.navatwo.gradle.testkit.junit5.gradleVersion",
            null
          )
          if (gradleVersionOverride != null) {
            runner.withGradleVersion(gradleVersionOverride)
          }

          val gradleTestKitDirectory = store.get(GradleTestKitDirectory::class, Path::class.java)
          if (gradleTestKitDirectory != null) {
            runner.withTestKitDir(gradleTestKitDirectory.toFile())
          }

          runner
        }
      }

      else -> error("Unsupported parameter: $parameterContext")
    }
  }

  override fun afterEach(context: ExtensionContext) {
    val store = context.getStore(Namespace.GLOBAL)

    val tempDirectory = store.get(GradleProject.Root::class, TempDirectory::class.java)
    try {
      tempDirectory?.close()
    } catch (ioe: IOException) {
      logger.log(Level.WARNING, "Could not close ${tempDirectory.path.absolute()}", ioe)
    }
  }

  companion object {
    internal const val DEFAULT_GRADLE_PROJECT_ROOT_DIRECTORY = "src/test/projects"
    internal const val DEFAULT_TEST_KIT_DIRECTORY = "build/test-kit"
  }

  private data class TempDirectory(val path: Path) : AutoCloseable {
    override fun close() {
      path.toFile().deleteRecursively()
    }
  }
}

private inline fun <reified A : Annotation> ExtensionContext.findClassAnnotation(): A? = findClassAnnotation(A::class)

private fun <A : Annotation> ExtensionContext.findClassAnnotation(annotationClass: KClass<out A>): A? {
  val testClass = testClass.map { it.kotlin }.orElse(null)
    ?: return null

  val fromTestClass = testClass.annotations.asSequence().filterIsInstance(annotationClass.java).singleOrNull()
  return fromTestClass ?: parent.map { it.findClassAnnotation(annotationClass) }.orElse(null)
}
