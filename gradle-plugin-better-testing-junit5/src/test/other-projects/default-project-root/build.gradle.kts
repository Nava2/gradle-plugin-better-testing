plugins {
  kotlin("jvm") version "1.9.0"
}

repositories {
  mavenCentral()
}

tasks.create("touch") {
  doLast {
    file("build/touch").run {
      parentFile.mkdirs()
      writeText("touched")
    }
  }
}