package net.navatwo.gradle.testkit.assertj

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.AssertFactory
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import org.gradle.testkit.runner.TaskOutcome.SKIPPED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class BuildTaskAssert internal constructor(
  private val task: BuildTask,
) : AbstractObjectAssert<BuildTaskAssert, BuildTask>(task, BuildTaskAssert::class.java) {

  /**
   * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [TaskOutcome.SUCCESS]
   */
  fun isSuccess() = assertTaskOutcome("did not succeed", SUCCESS)

  /**
   * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [TaskOutcome.SUCCESS] or [TaskOutcome.FROM_CACHE]
   */
  fun isSuccessOrFromCache(): BuildTaskAssert {
    assertThat(task.outcome)
      .`as` { "\"${task.path}\" did not succeed: ${task.outcome}" }
      .isIn(SUCCESS, FROM_CACHE)

    return this
  }

  /**
   * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [TaskOutcome.FAILED]
   */
  fun isFailed() = assertTaskOutcome("did not fail", FAILED)

  private fun didNotRunWithStatus(taskOutcome: TaskOutcome) = assertTaskOutcome("status was not", taskOutcome)

  /**
   * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [UP_TO_DATE]
   */
  fun isUpToDate() = didNotRunWithStatus(UP_TO_DATE)

  /**
   * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [SKIPPED]
   */
  fun isSkipped() = didNotRunWithStatus(SKIPPED)

  /**
   * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [NO_SOURCE]
   */
  fun isNoSource() = didNotRunWithStatus(NO_SOURCE)

  /**
   * Asserts that `this` [BuildTask] has [BuildTask.getOutcome] of [FROM_CACHE]
   */
  fun isFromCache() = didNotRunWithStatus(FROM_CACHE)

  private fun assertTaskOutcome(negativeJoiner: String, expectedOutcome: TaskOutcome): BuildTaskAssert {
    assertThat(task.outcome)
      .`as` { "\"${task.path}\" $negativeJoiner: ${task.outcome}" }
      .isEqualTo(expectedOutcome)

    return this
  }

  internal data object Factory : AssertFactory<BuildTask, BuildTaskAssert> {
    override fun createAssert(t: BuildTask) = BuildTaskAssert(t)
  }
}
