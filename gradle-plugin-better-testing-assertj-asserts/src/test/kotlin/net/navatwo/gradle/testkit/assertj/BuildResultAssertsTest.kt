package net.navatwo.gradle.testkit.assertj

import net.navatwo.gradle.testkit.junit5.GradleProject
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration
import net.navatwo.gradle.testkit.junit5.GradleTestKitProjectExtension
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(GradleTestKitProjectExtension::class)
@GradleTestKitConfiguration(
  withPluginClasspath = false,
)
class BuildResultAssertsTest {
  @Test
  @GradleProject("task-states")
  fun `non-caching gradle tasks are correct`(
    @GradleProject.Runner runner: GradleRunner,
  ) {
    val firstRun = runner.withArguments("firstRun").build()

    assertThat(firstRun.tasks).hasSize(2)
    assertThat(firstRun).task(":upToDate").isSuccess()
    assertThat(firstRun).task(":firstRun").isSuccess()

    val secondRun = runner.withArguments("secondRun", "--continue").buildAndFail()

    assertThat(secondRun.tasks).hasSize(5)
    assertThat(secondRun).taskDidNotRun(":secondRun")

    assertThat(secondRun).task(":success").isSuccess()
    assertThat(secondRun).task(":failed").isFailed()
    assertThat(secondRun).task(":upToDate").isUpToDate()
    assertThat(secondRun).task(":noSource").isNoSource()
    assertThat(secondRun).task(":skipped").isSkipped()
  }

  @Test
  @GradleProject("cached-tasks")
  fun `verifying cached tasks works`(
    @GradleProject.Runner runner: GradleRunner,
    @GradleProject.Root root: File,
  ) {
    val firstRun = runner.withArguments("cached").build()
    assertThat(firstRun).task(":cached").isSuccessOrFromCache()

    val secondRun = runner.withArguments(":cached", "--build-cache").build()

    assertThat(secondRun).task(":cached").isUpToDate()

    // force to pull from cache
    root.resolve("build/cached.txt").delete()

    val cachedRun = runner.build()
    assertThat(cachedRun).task(":cached").isFromCache()
  }
}
