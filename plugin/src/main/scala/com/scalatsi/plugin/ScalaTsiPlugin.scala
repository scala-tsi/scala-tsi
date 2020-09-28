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
    val generateTypescript = taskKey[Unit]("Generate typescript for this project").withRank(KeyRanks.ATask)
    val typescriptCreateExporter =
      taskKey[Seq[File]]("Generate an application that will generate typescript from the classes that are configured")
    val typescriptRunExporter = taskKey[Unit]("Run the application created by typescriptCreateExporter")
    val typescriptDeleteExporter =
      taskKey[Unit]("Remove the application created by typescriptCreateExporter")

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
    generateTypescript := typescriptDeleteExporter.dependsOn(typescriptRunExporter).dependsOn(typescriptCreateExporter).value,
    typescriptCreateExporter in Compile := createTypescriptExporter.value,
    typescriptRunExporter := runTypescriptExporter.value,
    typescriptDeleteExporter := deleteTypescriptExporter.value,
    sourceGenerators in Compile += (typescriptCreateExporter in Compile)
  )

  override lazy val projectSettings: Seq[Def.Setting[_]] = baseScalaTsiSettings

  private lazy val targetFile               = Def.setting { sourceManaged.value / "com" / "scalatsi" / "generator" / "ExportTypescript.scala" }
  private lazy val runTypescriptExporter    = (runMain in Compile).toTask(" com.scalatsi.generator.ExportTypescript")
  private lazy val deleteTypescriptExporter = Def.task(IO.delete(targetFile.value))

  private lazy val createTypescriptExporter = Def.task {
    val target          = targetFile.value
    val typesToGenerate = typescriptExports.value ++ typescriptClassesToGenerateFor.value

    val toWrite: String = txt
      .ExportTypescriptTemplate(
        typescriptGenerationImports.value,
        typesToGenerate,
        typescriptOutputFile.value.getAbsolutePath,
        typescriptStyleSemicolons.value
      )
      .body
      .stripMargin

    IO.write(target, toWrite)
    Seq(target)
  }
}
