package com.scalatsi.plugin

import sbt.Keys._
import sbt._
import sbt.info.BuildInfo

object ScalaTsiPlugin extends AutoPlugin {
  object autoImport {
    // user settings
    val typescriptExports = settingKey[Seq[String]]("Types to export typescript version for")
    val typescriptGenerationImports =
      settingKey[Seq[String]]("Additional imports, i.e. your packages so you don't need to prefix your classes.")
    val typescriptOutputFile      = settingKey[File]("File where all typescript interfaces will be written to")
    val typescriptStyleSemicolons = settingKey[Boolean]("Whether to add booleans to the exported model")

    // tasks
    val generateTypescript = taskKey[Unit]("Generate typescript for this project")
    val typescriptCreateExporter =
      taskKey[Seq[File]]("Generate an application that will generate typescript from the classes that are configured")

    // deprecated
    @deprecated("Use typescriptExports", "0.4.0")
    val typescriptClassesToGenerateFor = settingKey[Seq[String]]("Types to export typescript version for")
  }

  import autoImport._

  override def trigger = allRequirements

  private val scala_ts_compiler_version = BuildInfo.version

  lazy val baseScalaTsiSettings: Seq[Def.Setting[_]] = Seq(
    // User settings
    libraryDependencies += "com.scalatsi" %% "scala-tsi" % scala_ts_compiler_version,
    typescriptGenerationImports := Seq(),
    typescriptExports := Seq(),
    typescriptClassesToGenerateFor := Seq(),
    typescriptOutputFile := target.value / "scala-tsi.ts",
    typescriptStyleSemicolons := false,
    // Task settings
    generateTypescript := runTypescriptGeneration.value,
    typescriptCreateExporter in Compile := createTypescriptExporter(
      typescriptGenerationImports.value,
      typescriptExports.value ++ typescriptClassesToGenerateFor.value,
      sourceManaged.value,
      typescriptOutputFile.value,
      typescriptStyleSemicolons.value
    ),
    sourceGenerators in Compile += typescriptCreateExporter in Compile
  )

  override lazy val projectSettings = baseScalaTsiSettings

  private def createTypescriptExporter(
    imports: Seq[String],
    typesToGenerate: Seq[String],
    sourceManaged: File,
    typescriptOutputFile: File,
    useSemicolons: Boolean
  ): Seq[File] = {
    val targetFile = sourceManaged / "com" / "scalatsi" / "generator" / "ExportTypescript.scala"

    val toWrite: String = txt
      .ExportTypescriptTemplate(
        imports,
        typesToGenerate,
        typescriptOutputFile.getAbsolutePath,
        useSemicolons
      )
      .body
      .stripMargin

    IO.write(targetFile, toWrite)
    Seq(targetFile)
  }

  def runTypescriptGeneration: Def.Initialize[Task[Unit]] =
    (runMain in Compile)
      .toTask(" com.scalatsi.generator.ExportTypescript")
      .dependsOn(typescriptCreateExporter in Compile)
}
