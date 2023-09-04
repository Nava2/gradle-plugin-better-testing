package net.navatwo.gradle.testkit.junit5

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TestExecutionDirectoryTest {
  @TempDir
  private lateinit var root: File
  private lateinit var cleanedDirs: Set<File>

  @BeforeEach
  fun setup() {
    val child1Build = root.resolve("child").run {
      resolve("build").apply {
        mkdirs()

        resolve("foo.txt").apply {
          writeText("foo!")
        }
      }
    }

    val child2Build = root.resolve("other-child").run {
      resolve("deeper/build").apply {
        mkdirs()

        resolve("foo2.txt").apply {
          writeText("foo2!")
        }
      }
    }

    val rootGradle = root.resolve(".gradle").apply {
      mkdirs()
      resolve("foo3.txt").apply {
        writeText("foo3!")
      }
    }

    cleanedDirs = setOf(child1Build, child2Build, rootGradle)
  }

  @Test
  fun `Cleaned removes specified recursive directories`() {
    TestExecutionDirectory.Cleaned(root, setOf("build", ".gradle")).use {
      assertThat(cleanedDirs).allSatisfy {
        assertThat(it).doesNotExist()
      }

      for (dir in cleanedDirs) {
        dir.mkdirs()
        dir.resolve("test.foo").apply {
          writeText("wahoo! $this")
        }
      }
    }

    assertThat(cleanedDirs).allSatisfy {
      assertThat(it).doesNotExist()
    }
  }

  @Test
  fun `Dirty changes nothing`() {
    val allFilesInRoot = root.walkTopDown().toList()

    val testFile = TestExecutionDirectory.Dirty(root).use {
      assertThat(root.walkTopDown().toList()).containsExactlyElementsOf(allFilesInRoot)

      root.resolve("testFile.f").apply {
        writeText("foo")
      }
    }

    assertThat(testFile).hasContent("foo")
    assertThat(root.walkTopDown().toList()).containsExactlyInAnyOrderElementsOf(allFilesInRoot + testFile)
  }

  @Test
  fun `Pristine is a new, temporary directory, cleaning defaults`() {
    val buildTestFile = root.resolve("build/foo.txt").apply {
      parentFile.mkdirs()
      writeText("test")
    }

    val testRoot = TestExecutionDirectory.Pristine(root).use { dir ->
      assertThat(dir.root).isNotEqualTo(root)
      assertThat(dir.sourceRoot).isEqualTo(root)

      val relativeBuildTestFile = dir.root.resolve(buildTestFile.relativeTo(dir.sourceRoot))
      assertThat(relativeBuildTestFile).doesNotExist()

      dir.root.resolve("testFile.f").apply {
        writeText("foo")
      }

      dir.root
    }

    assertThat(testRoot).doesNotExist()
    assertThat(buildTestFile).hasContent("test")
  }
}
