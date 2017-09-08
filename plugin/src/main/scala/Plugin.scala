import nl.codestar.scala.ts.interface._
import nl.codestar.scala.ts.interface.dsl._
import sbt._
import sbt.Keys._


object Import {
  object TypescriptKeys {
    val typescript =
      TaskKey[Seq[File]]("typescript", "Invoke the typescript generator")

    val inputDirectory = SettingKey[File]("typescript-input-directory")
    val outputFile = SettingKey[File]("typescript-output-file", "File where all typescript interfaces will be written to")
  }
}

object Plugin extends AutoPlugin {
  import Import.TypescriptKeys._

  override def trigger = allRequirements
  override lazy val projectSettings = inTask(typescript) {
    Seq(
      commands += generateTypescriptCommand,
      inputDirectory := sourceDirectory.value,
      outputFile := sourceManaged.value,
      typescript := generateTypescript(streams.value, inputDirectory.value)
    )
  }

  val autoImport = Import

  lazy val generateTypescriptCommand = Command.command("generateTypescript") {
    state =>
      state
  }

  def generateTypescript(streams: TaskStreams, sourceDirectory: File): Seq[File] = {
    val files = sourceDirectory ** "*.scala"

    files.get.map(file => println(file.getName))

    Seq.empty
  }

}
