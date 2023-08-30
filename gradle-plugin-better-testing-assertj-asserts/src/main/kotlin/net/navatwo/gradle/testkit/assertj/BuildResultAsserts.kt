package net.navatwo.gradle.testkit.assertj

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.AbstractStringAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.InstanceOfAssertFactory
import org.assertj.core.api.ObjectAssert
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import java.util.function.Consumer

/**
 * Asserts that [taskPath] executed in `this` build, returning an [BuildTaskAssert] wrapping the task.
 * @param taskPath The path of the task to assert executed
 */
fun <SELF> SELF.task(taskPath: String): BuildTaskAssert
    where SELF : AbstractObjectAssert<out SELF, BuildResult> {
  return this.extracting(
    {
      it.task(taskPath)
    },
    InstanceOfAssertFactory(BuildTask::class.java, BuildTaskAssert.Factory),
  )
}

/**
 * Asserts that [taskPath] did not execute in `this` build.
 * @param taskPath The path of the task to assert not executed
 */
fun <SELF> SELF.taskDidNotRun(taskPath: String): SELF
    where SELF : AbstractObjectAssert<out SELF, BuildResult> {
  satisfies(
    Consumer { buildResult ->
      val taskPaths = buildResult.tasks.map { it.path }
      assertThat(taskPaths).`as` { "\"$taskPath\" executed in build" }.doesNotContain(taskPath)
    }
  )

  return this
}

/**
 * Extracts the [BuildResult.getOutput] as an [AbstractStringAssert].
 */
fun ObjectAssert<BuildResult>.output(): AbstractStringAssert<*> {
  return extracting { it.output }
    .`as` { "Build output" }
    .asString()
}
