package net.navatwo.gradle.testkit.junit5.integration_test

import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration
import net.navatwo.gradle.testkit.junit5.collectAnnotations
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.*
import kotlin.reflect.jvm.javaMethod

class GradleTestKitProjectExtensionTest {
  @Test
  fun `collectAnnotations verify`() {
    val rootContext = mock(ExtensionContext::class.java)
    `when`(rootContext.testMethod).thenReturn(Optional.empty())
    `when`(rootContext.testClass).thenReturn(Optional.empty())
    `when`(rootContext.parent).thenReturn(Optional.empty())

    val classContext = mock(ExtensionContext::class.java)
    `when`(classContext.testMethod).thenReturn(Optional.empty())
    `when`(classContext.testClass).thenReturn(Optional.ofNullable(Clazz::class.java))
    `when`(classContext.parent).thenReturn(Optional.of(rootContext))

    val methodContext = mock(ExtensionContext::class.java)
    `when`(methodContext.testMethod).thenReturn(Optional.ofNullable(this::testMethod.javaMethod))
    `when`(methodContext.testClass).thenReturn(Optional.ofNullable(Clazz::class.java))
    `when`(methodContext.parent).thenReturn(Optional.of(classContext))

    assertThat(methodContext.collectAnnotations(GradleTestKitConfiguration::class))
      .containsExactly(
        GradleTestKitConfiguration(projectsRoot = "method"),
        GradleTestKitConfiguration(projectsRoot = "clazz"),
        GradleTestKitConfiguration(projectsRoot = "clazz"),
      )
  }

  @GradleTestKitConfiguration(projectsRoot = "method")
  private fun testMethod() = Unit

  @GradleTestKitConfiguration(projectsRoot = "clazz")
  private class Clazz
}
