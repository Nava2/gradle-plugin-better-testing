package net.navatwo.gradle.testkit.junit5

import org.jetbrains.annotations.VisibleForTesting
import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.FileVisitResult.SKIP_SUBTREE
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.name

private val DEFAULT_CLEANED_BUILD_DIRS = setOf("build", ".gradle")

internal sealed interface TestExecutionDirectory : AutoCloseable {

  val root: File

  data class Cleaned(
    override val root: File,
    val directoryNamesToClean: Set<String> = DEFAULT_CLEANED_BUILD_DIRS,
  ) : TestExecutionDirectory {
    init {
      root.cleanDirectoriesWithin(directoryNamesToClean)
    }

    override fun close() {
      root.cleanDirectoriesWithin(directoryNamesToClean)
    }
  }

  data class Dirty(override val root: File) : TestExecutionDirectory {
    override fun close() = Unit
  }

  data class Pristine(
    @VisibleForTesting
    internal val sourceRoot: File,
    val directoryNamesToClean: Set<String> = DEFAULT_CLEANED_BUILD_DIRS,
  ) : TestExecutionDirectory {

    override val root: File = Files.createTempDirectory(TEMP_DIRECTORY_PREFIX).toFile()

    private val cleaned: Cleaned

    init {
      sourceRoot.copyRecursively(root, overwrite = true)
      // this must be after the copy to make sure it is cleaned
      cleaned = Cleaned(root, directoryNamesToClean)
    }

    override fun close() {
      cleaned.close()

      if (root.exists()) {
        root.deleteRecursively()
      }
    }

    companion object {
      @VisibleForTesting
      internal const val TEMP_DIRECTORY_PREFIX = "gradle-project-"
    }
  }
}

private fun File.cleanDirectoriesWithin(directoryNames: Set<String>) {
  Files.walkFileTree(toPath(), CleaningPathVisitor(directoryNames))
}

private class CleaningPathVisitor(val directoryNames: Set<String>) : SimpleFileVisitor<Path>() {
  override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
    if (dir.name in directoryNames) {
      dir.toFile().deleteRecursively()
      return SKIP_SUBTREE
    }

    return CONTINUE
  }
}
