package net.navatwo.gradle.testkit.assertj

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import org.gradle.testkit.runner.TaskOutcome.SKIPPED
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

/**
 * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [TaskOutcome.SUCCESS]
 */
fun AbstractObjectAssert<*, BuildTask>.isSuccess(): AbstractObjectAssert<*, BuildTask> {
  satisfies { task ->
    assertThat(task.outcome)
      .`as` { "\"${task.path}\" did not succeed: ${task.outcome}" }
      .isEqualTo(TaskOutcome.SUCCESS)
  }
  return this
}

/**
 * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [TaskOutcome.FAILED]
 */
fun <SELF : AbstractObjectAssert<out SELF, BuildTask>> SELF.isFailed(): SELF {
  satisfies { task ->
    assertThat(task.outcome)
      .`as` { "\"${task.path}\" did not fail: ${task.outcome}" }
      .isEqualTo(TaskOutcome.FAILED)
  }
  return this
}

private fun AbstractObjectAssert<*, BuildTask>.didNotRunWithStatus(
  taskOutcome: TaskOutcome,
): AbstractObjectAssert<*, BuildTask> {
  satisfies { task ->
    assertThat(task.outcome)
      .`as` { "${task.path} status was not $taskOutcome: ${task.outcome}" }
      .isEqualTo(taskOutcome)
  }

  return this
}

/**
 * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [UP_TO_DATE]
 */
fun AbstractObjectAssert<*, BuildTask>.isUpToDate(): AbstractObjectAssert<*, BuildTask> {
  return didNotRunWithStatus(UP_TO_DATE)
}

/**
 * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [SKIPPED]
 */
fun AbstractObjectAssert<*, BuildTask>.isSkipped(): AbstractObjectAssert<*, BuildTask> {
  return didNotRunWithStatus(SKIPPED)
}

/**
 * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [NO_SOURCE]
 */
fun AbstractObjectAssert<*, BuildTask>.isNoSource(): AbstractObjectAssert<*, BuildTask> {
  return didNotRunWithStatus(NO_SOURCE)
}

/**
 * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [FROM_CACHE]
 */
fun AbstractObjectAssert<*, BuildTask>.isFromCache(): AbstractObjectAssert<*, BuildTask> {
  return didNotRunWithStatus(FROM_CACHE)
}
