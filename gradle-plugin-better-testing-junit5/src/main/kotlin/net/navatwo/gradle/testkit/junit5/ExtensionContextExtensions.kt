package net.navatwo.gradle.testkit.junit5

import org.junit.jupiter.api.extension.ExtensionContext
import java.util.Optional
import kotlin.reflect.KClass

internal fun <A : Annotation> ExtensionContext.collectAnnotations(annotationClass: KClass<A>): List<A> {
  return sequence {
    var currentContext: ExtensionContext? = this@collectAnnotations
    while (currentContext != null) {
      val methodAnn = currentContext.testMethod.flatMap { m ->
        Optional.ofNullable(m.getAnnotation(annotationClass.java))
      }

      yield(methodAnn)

      val classAnn = currentContext.testClass.flatMap { c ->
        Optional.ofNullable(c.getAnnotation(annotationClass.java))
      }
      yield(classAnn)

      currentContext = currentContext.parent.orElse(null)
    }
  }
    .mapNotNull { it.orElse(null) }
    .toList()
}
