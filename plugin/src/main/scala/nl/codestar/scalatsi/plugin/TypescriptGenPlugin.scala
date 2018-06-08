package nl.codestar.scalatsi.plugin

import sbt.Keys._
import sbt._
import sbt.info.BuildInfo

object TypescriptGenPlugin extends AutoPlugin {
  object autoImport {
    val generateTypescript =
      taskKey[Unit]("Generate typescript this project")
    val generateTypescriptGeneratorApplication = taskKey[Seq[File]]("Generate an application that will generate typescript from the classes that are configured")
    val typescriptClassesToGenerateFor =
      settingKey[Seq[String]]("Classes to generate typescript interfaces for")
    val typescriptGenerationImports = settingKey[Seq[String]](
      "Additional imports (i.e. your packages so you don't need to prefix your classes)")
    val typescriptOutputFile = settingKey[File](
      "File where all typescript interfaces will be written to")
  }

  import autoImport._

  override def trigger = allRequirements

  private val scala_ts_compiler_version = BuildInfo.version

  override lazy val projectSettings = Seq(
    // User settings
    libraryDependencies += "nl.codestar" %% "scala-tsi" % scala_ts_compiler_version,
    typescriptGenerationImports := Seq(),
    typescriptClassesToGenerateFor := Seq(),
    typescriptOutputFile := target.value / "scala-interfaces.ts",
    // Task settings
    generateTypescript := runTypescriptGeneration.value,
    generateTypescriptGeneratorApplication in Compile := createTypescriptGenerationTemplate(
      typescriptGenerationImports.value,
      typescriptClassesToGenerateFor.value,
      sourceManaged.value,
      typescriptOutputFile.value),
    sourceGenerators in Compile += generateTypescriptGeneratorApplication in Compile,
  )

  def createTypescriptGenerationTemplate(
      imports: Seq[String],
      typesToGenerate: Seq[String],
      sourceManaged: File,
      typescriptOutputFile: File): Seq[File] = {
    val targetFile = sourceManaged / "nl" / "codestar" / "scala" / "ts" / "generator" / "ApplicationTypescriptGeneration.scala"

    val toWrite: String = txt
      .generateTypescriptApplicationTemplate(
        imports,
        typesToGenerate,
        typescriptOutputFile.getAbsolutePath)
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
