package net.navatwo.gradle.testkit.assertj

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.AbstractStringAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectAssert
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask

/**
 * Asserts that [taskPath] executed in `this` build, returning an [ObjectAssert] wrapping the task.
 * @param taskPath The path of the task to assert executed
 */
fun <SELF> SELF.task(taskPath: String): AbstractObjectAssert<*, BuildTask>
  where SELF : AbstractObjectAssert<out SELF, BuildResult> {
  return extracting { buildResult ->
    val taskPaths = buildResult.tasks.map { it.path }
    assertThat(taskPaths).`as` { "\"$taskPath\" did not execute in build" }.contains(taskPath)

    val task = buildResult.task(taskPath)
    assertThat(task).isNotNull()
    task!!
  }
}

/**
 * Extracts the [BuildResult.getOutput] as an [AbstractStringAssert].
 */
fun ObjectAssert<BuildResult>.output(): AbstractStringAssert<*> {
  return extracting { it.output }
    .`as` { "Build output" }
    .asString()
}
