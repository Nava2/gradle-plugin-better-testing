tasks.create("dumpVersion") {
  doLast {
    println("gradle_version=${gradle.gradleVersion}")
  }
}
