package nl.codestar.scalatsi.plugin

import sbt.Keys._
import sbt._
import sbt.info.BuildInfo

object TypescriptGenPlugin extends AutoPlugin {
  object autoImport {
    // User settings
    val typescriptClassesToGenerateFor = settingKey[Seq[String]]("Classes to generate typescript interfaces for")
    val typescriptGenerationImports = settingKey[Seq[String]]("Additional imports (i.e. your packages)")
    val typescriptOutputLocation = settingKey[File]("Directory or file where all typescript interfaces will be written to")
    @deprecated("Use typescriptOutputLocation", "0.2.0")
    val typescriptOutputFile = typescriptOutputLocation

    val generateTypescript = taskKey[Unit]("Generate typescript this project")

    //
    val generateTypescriptGeneratorApplication = taskKey[Seq[File]]("Generate an app to generate typescript interfaces")
  }

  import autoImport._

  override def trigger = allRequirements

  private val scala_ts_compiler_version = BuildInfo.version

  override lazy val projectSettings = Seq(
    // User settings
    typescriptGenerationImports := Seq(),
    typescriptClassesToGenerateFor := Seq(),
    typescriptOutputLocation := target.value / "typescript-interfaces",
    // Add the library to the dependencies
    libraryDependencies += "nl.codestar" %% "scala-tsi" % scala_ts_compiler_version,
    // Task settings
    generateTypescript := runTypescriptGeneration.value,
    generateTypescriptGeneratorApplication in Compile := createTypescriptGenerationTemplate(
      typescriptGenerationImports.value,
      typescriptClassesToGenerateFor.value,
      sourceManaged.value,
      typescriptOutputLocation.value
    ),
    sourceGenerators in Compile += generateTypescriptGeneratorApplication in Compile
  )

  def createTypescriptGenerationTemplate(
    imports: Seq[String],
    typesToGenerate: Seq[String],
    sourceManaged: File,
    typescriptOutputFile: File
  ): Seq[File] = {
    val targetFile = sourceManaged / "nl" / "codestar" / "scalasti" / "generator" / "ApplicationTypescriptGeneration.scala"

    val toWrite: String = txt
      .generateTypescriptApplicationTemplate(imports, typesToGenerate, typescriptOutputFile.getAbsolutePath)
      .body
      .stripMargin

    IO.write(targetFile, toWrite)
    Seq(targetFile)
  }

  def runTypescriptGeneration: Def.Initialize[Task[Unit]] =
    (runMain in Compile)
      .toTask(" nl.codestar.scalatsi.generator.ApplicationTypescriptGeneration")
      .dependsOn(generateTypescriptGeneratorApplication in Compile)
}
