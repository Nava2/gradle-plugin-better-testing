import java.io.File

val success by tasks.creating {
  doLast {
    println("Success!")
  }
}

val failed by tasks.creating {
  doLast {
    error("failed!")
  }
}

val upToDate by tasks.creating {
  inputs.file("InputFile.txt")
  outputs.file("build/upToDate.txt")

  doLast {
    println("upToDate!")
    File("build/upToDate.txt").apply {
      parentFile.mkdirs()
      writeText("upToDate!")
    }
  }
}

val skipped by tasks.creating {
  onlyIf { false }
}

open class NoSourceTask @Inject constructor(
  objects: ObjectFactory,
) : DefaultTask() {
  @get:InputFiles
  @SkipWhenEmpty
  val inputDir = objects.fileCollection()

  @TaskAction
  fun execute() {
    println("NoSourceTask!")
  }
}

val noSource by tasks.creating(NoSourceTask::class)

val firstRun by tasks.creating {
  dependsOn(upToDate)
}

val secondRun by tasks.creating {
  dependsOn(success, failed, noSource, upToDate, skipped)
}
