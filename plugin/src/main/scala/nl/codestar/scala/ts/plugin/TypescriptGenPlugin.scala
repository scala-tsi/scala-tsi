package nl.codestar.scala.ts.plugin

import sbt.Keys._
import sbt.{Def, _}

object TypescriptGenPlugin extends AutoPlugin {
  object autoImport {
    val generateTypescript =
      TaskKey[Unit]("generateTypescript", "Generate typescript this project")
    val generateTypescriptGeneratorApplication = TaskKey[Seq[File]](
      "generateTypescriptGeneratorApplication",
      "Generate an application that will generate typescript from the classes that are configured")
    val typescriptClassesToGenerateFor =
      SettingKey[Seq[String]]("Classes to generate typescript interfaces for")
    val typescriptGenerationImports = SettingKey[Seq[String]](
      "Additional imports (i.e. your packages so you don't need to prefix your classes)")
    //val inputDirectory = SettingKey[File]("typescript-input-directory")
    val typescriptOutputFile = SettingKey[File](
      "File where all typescript interfaces will be written to")
  }

  import autoImport._

  override def trigger = allRequirements
  // Do we need this?
  //override def `requires`: Plugins = JvmPlugin

  // TODO: Automatically get this from the main build.sbt, e.g. with sbt-buildinfo
  private val scala_ts_compiler_version = "0.1.0-SNAPSHOT"

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
      .toTask(" nl.codestar.scala.ts.generator.ApplicationTypescriptGeneration")
      .dependsOn(generateTypescriptGeneratorApplication in Compile)
}
