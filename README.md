# gradle-plugin-better-testing
A collection of libraries to make testing gradle plugins in Kotlin better.

## JUnit 5 Extensions

Provides a JUnit 5 extension for loading Gradle Projects in a consistent way within tests.


### Full example

```gradle
dependencies {
  testImplementation("net.navatwo:gradle-plugin-better-testing-junit5:0.0.6")
}

tasks.test {
  // Enable auto-detection of JUnit extensions, avoids adding `@ExtendsWith(...)` to every test.
  systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

  // Configure the gradle version from a gradle property, e.g. when running a test matrix across multiple gradle 
  // versions 
  // To use an environment variable, switch `gradleProperty` with `environmentVariable`.
  val gradleVersion = providers.gradleProperty("test.gradleVersion")
  if (gradleVersion.isPresent) {
     systemProperty("net.navatwo.gradle.testkit.junit5.gradleVersion", gradleVersion.get())
  }

  // Use a shared directory across all projects 
  val testKitDirectory: Directory = rootProject.layout.projectDirectory.dir(".gradle/testKit")
  systemProperty("net.navatwo.gradle.testkit.junit5.testKitDirectory", testKitDirectory.asFile.toString())
}
```

### Add to your project
```gradle
dependencies {
  testImplementation("net.navatwo:gradle-plugin-better-testing-junit5:0.0.6")
}
```

This provides sensible defaults for performance and ease of use. All defaults are overridden via annotations. To use
this extension, enable extensions in your project either via:
1. **(Recommended)** Add `junit.jupiter.extensions.autodetection.enabled` as `true` to your Test JVM arguments, e.g.:
     ```gradle
     tasks.test {
       systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
     }
     ```
2. Add `@ExtendsWith(GradleTestKitProjectExtension::class)` to your test class

This extension assumes projects are defined in a "projects" directory (e.g. `src/test/projects`). This can be
overridden by annotating your test class with [`GradleProjectsRoot`](gradle-plugin-better-testing-junit5/src/main/kotlin/net/navatwo/gradle/testkit/junit5/GradleProjectsRoot.kt)
(e.g. `@GradleProjectsRoot("src/test/other-projects")`).

To use a specific project, annotate a test method with [`GradleProject`](gradle-plugin-better-testing-junit5/src/main/kotlin/net/navatwo/gradle/testkit/junit5/GradleProject.kt):

```kotlin
@Test
@GradleProject("lazy-evaluation-successful")
fun `lazy evaluation is successful`(@GradleProject.Runner gradleRunner: GradleRunner) {
  assertThat(gradleRunner.withArguments("tasks").build()).task(":tasks").isSuccess()
}
```

Annotating with `@GradleProject.Runner` allows injecting a pre-configured [`GradleRunner`](https://docs.gradle.org/current/javadoc/org/gradle/testkit/runner/GradleRunner.html).

### Configuration

Test suite and method level configurations are done via the
[`GradleTestKitConfiguration`](gradle-plugin-better-testing-junit5/src/main/kotlin/net/navatwo/gradle/testkit/junit5/GradleTestKitConfiguration.kt)
annotation. Any values specified in these annotations will be used to override default values. The value specified 
"closest" to the test method will always take precedence.

### Manipulating projects in `@BeforeEach`

If looking to have common setup within a test class, annotating a method with `@BeforeEach` and passing the parameter
`@GradleProject.Runner` or `@GradleProject.Root` allows for shared configuration. For example, see 
[`BeforeEachParameterInjectionTest.kt`](gradle-plugin-better-testing-junit5/src/test/kotlin/net/navatwo/gradle/testkit/junit5/integration_test/BeforeEachParameterInjectionTest.kt).

### Gradle TestKit 

By default, this extension will set any injected `GradleRunner` to share a `TestKit` directory in the `build/`
directory for the project - `${project_dir}/build/test-kit`. This can be overridden by annotating your test class with
[`GradleTestKitConfiguration`](gradle-plugin-better-testing-junit5/src/main/kotlin/net/navatwo/gradle/testkit/junit5/GradleTestKitConfiguration.kt).
This is done to _greatly_ improve the speed of tests by avoiding re-downloading Gradle
dependencies with each test run.

We recommend to share the test kit directory across all projects to avoid needless downloading. For example:
```kotlin
val testKitDirectory: Directory = rootProject.layout.projectDirectory.dir(".gradle/testKit")

tasks.test {
   systemProperty("net.navatwo.gradle.testkit.junit5.testKitDirectory", testKitDirectory.asFile.toString())
}
```

## Development

### Releasing

#### Prerequisites

1. **GPG Key Setup**: Generate a GPG key for signing artifacts:
   ```shell
   gpg --full-generate-key
   gpg --list-secret-keys --keyid-format=long
   gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>
   ```

2. **Sonatype Account**: Create an account at [central.sonatype.com](https://central.sonatype.com) and generate user tokens.

#### Configuration

Setup your local `~/.gradle/gradle.properties` with the following variables:

```properties
# Maven Central credentials (using new Sonatype Central)
mavenCentralUsername=<sonatype user token>
mavenCentralPassword=<sonatype user token password>

# GPG signing configuration (choose one approach)
# Option 1: File-based signing (if you have a secring.gpg file)
signing.keyId=<last 8 chars of key ID>
signing.password=<gpg key password>
signing.secretKeyRingFile=/Users/my_user/.gnupg/secring.gpg

# Option 2: GPG agent signing (recommended for modern GPG)
signing.gnupg.executable=gpg
signing.gnupg.keyName=<KEY_ID>
signing.gnupg.passphrase=<gpg key password>
signing.gnupg.useLegacyGpg=false
```

#### Publishing Process

```shell
# Clean the repo first to not have any old artifacts
./gradlew clean

# Verify the repo is in good shape
./gradlew check

# Tag a version
git tag v0.0.6

# Publish to Maven Central using the Vanniktech plugin
RELEASE=1 ./gradlew build publishToMavenCentral

# Push tags to github
git push --tags

# Create a new release: https://github.com/Nava2/gradle-plugin-better-testing/releases
# Update version to next patch version in `build-logic/src/main/kotlin/better-testing.kotlin.gradle.kts`,
#    e.g. `0.0.7-SNAPSHOT`
```

#### Notes

- Uses the [Vanniktech Maven Publish Plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/) for simplified publishing
- Artifacts are signed with GPG and published to Maven Central
- The `RELEASE=1` environment variable strips `-SNAPSHOT` from the version
