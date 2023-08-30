import java.io.File

@CacheableTask
open class CachedTask @Inject constructor(
  objects: ObjectFactory,
) : DefaultTask() {
  @get:InputFile
  @get:Classpath
  val input = objects.fileProperty()
    .value(project.layout.projectDirectory.file("InputFile.txt"))

  @get:OutputFile
  val cached = objects.fileProperty()
    .value(project.layout.buildDirectory.file("cached.txt"))

  @TaskAction
  fun execute() {
    println("CachedTask!")
    cached.asFile.get().apply {
      parentFile.mkdirs()
      writeText("CachedTask!")
    }
  }
}

val cached by tasks.creating(CachedTask::class)
