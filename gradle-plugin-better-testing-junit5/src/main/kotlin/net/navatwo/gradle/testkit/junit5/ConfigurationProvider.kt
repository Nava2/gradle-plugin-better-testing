package net.navatwo.gradle.testkit.junit5

import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.Companion.DEFAULT
import org.junit.jupiter.api.extension.ExtensionContext

internal object ConfigurationProvider {
  fun getConfigForContext(context: ExtensionContext): GradleTestKitConfiguration {
    val testKitConfigurations = context.collectAnnotations(GradleTestKitConfiguration::class)
    val implicitConfigurations = listOf(
      SystemPropertyOverrides.systemConfiguration(),
      SystemPropertyOverrides.internalConfiguration(),
      DEFAULT,
    )
    return (testKitConfigurations + implicitConfigurations)
      .reduce { acc, config -> GradleTestKitConfiguration.merge(acc, config) }
  }
}
