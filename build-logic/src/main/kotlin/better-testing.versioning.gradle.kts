
group = "net.navatwo.gradle"
version = "0.0.1-SNAPSHOT"

val isRelease = providers.environmentVariable("RELEASE")
  .map { it.isNotBlank() }
  .getOrElse(false)

if (isRelease) {
  version = version.toString().substringBefore("-SNAPSHOT")
}
