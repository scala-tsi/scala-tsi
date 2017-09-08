package nl.codestar.scala.ts.plugin

import org.clapper.classutil.ClassFinder
import sbt.Keys._
import sbt._

object Plugin extends AutoPlugin {
  object autoImport {
    val typescript = TaskKey[Seq[File]]("typescript", "Invoke the typescript generator")
    val inputDirectory = SettingKey[File]("typescript-input-directory")
    val outputFile = SettingKey[File]("typescript-output-file", "File where all typescript interfaces will be written to")
  }

  import autoImport._

  override def trigger = allRequirements

  lazy val typescriptSettings: Seq[Def.Setting[_]] =
    Seq(
      inputDirectory := scalaSource.value,
      outputFile := (target.value / "typescript"),
      typescript := generateTypescriptTask.value
    )

  override lazy val projectSettings =
    inConfig(Compile)(typescriptSettings)



  lazy val generateTypescriptTask = Def.task {
    Seq.empty[File]
  }
}
