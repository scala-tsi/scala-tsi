package com.scalatsi.plugin

import sbt.Keys._
import sbt._
import sbt.info.BuildInfo

object ScalaTsiPlugin extends AutoPlugin {
  object autoImport {
    // user settings
    val typescriptExports = settingKey[Seq[String]]("Types to export typescript version for").withRank(KeyRanks.ASetting)
    val typescriptGenerationImports = settingKey[Seq[String]](
      "Additional imports, i.e. your packages so you don't need to prefix your classes."
    ).withRank(KeyRanks.BSetting)
    val typescriptOutputFile      = settingKey[File]("File where all typescript interfaces will be written to").withRank(KeyRanks.BSetting)
    val typescriptStyleSemicolons = settingKey[Boolean]("Whether to add semicolons to the exported model").withRank(KeyRanks.BMinusSetting)
    val typescriptHeader          = settingKey[Option[String]]("Optional header for the output file")
    val typescriptTaggedUnionDiscriminator =
      settingKey[Option[String]]("The discriminator field for tagged unions, or None to disable tagged unions").withRank(KeyRanks.BSetting)

    // tasks
    val generateTypescript = taskKey[Unit]("Generate typescript for this project").withRank(KeyRanks.ATask)
    val typescriptCreateExporter = taskKey[Seq[File]](
      "Generate an application that will generate typescript from the classes that are configured"
    ).withRank(KeyRanks.CTask)
    val typescriptRunExporter    = taskKey[Unit]("Run the application created by typescriptCreateExporter").withRank(KeyRanks.CTask)
    val typescriptDeleteExporter = taskKey[Unit]("Remove the application created by typescriptCreateExporter").withRank(KeyRanks.CTask)
  }

  import autoImport._

  override def trigger = noTrigger

  private val scala_ts_compiler_version = BuildInfo.version

  lazy val baseScalaTsiSettings: Seq[Def.Setting[_]] = Seq(
    // User settings
    libraryDependencies += "com.scalatsi" %% "scala-tsi" % scala_ts_compiler_version,
    typescriptGenerationImports           := Seq(),
    typescriptExports                     := Seq(),
    typescriptOutputFile                  := target.value / "scala-tsi.ts",
    typescriptHeader                      := Some("// DO NOT EDIT: generated file by scala-tsi"),
    typescriptStyleSemicolons             := false,
    typescriptTaggedUnionDiscriminator    := Some("type"),
    // Task settings
    generateTypescript                  := Def.sequential(typescriptRunExporter, typescriptDeleteExporter).value,
    typescriptCreateExporter in Compile := createTypescriptExporter.value,
    typescriptRunExporter               := runTypescriptExporter.value,
    typescriptDeleteExporter            := deleteTypescriptExporter.value,
    // Instruct SBT
    sourceGenerators in Compile += (typescriptCreateExporter in Compile),
    cleanFiles += typescriptOutputFile.value
  )

  override lazy val projectSettings: Seq[Def.Setting[_]] = baseScalaTsiSettings

  private lazy val targetFile = Def.setting { sourceManaged.value / "com" / "scalatsi" / "generator" / "ExportTypescript.scala" }
  private lazy val deleteTypescriptExporter = Def.task(IO.delete(targetFile.value))

  private lazy val createTypescriptExporter = Def.task {
    val target = targetFile.value

    val toWrite: String = txt
      .ExportTypescriptTemplate(
        imports = typescriptGenerationImports.value,
        classes = typescriptExports.value,
        targetFile = typescriptOutputFile.value.getAbsolutePath,
        useSemicolons = typescriptStyleSemicolons.value,
        header = typescriptHeader.value.getOrElse(""),
        taggedUnionDiscriminator = typescriptTaggedUnionDiscriminator.value
      )
      .body
      .stripMargin

    IO.write(target, toWrite)
    Seq(target)
  }

  private lazy val runTypescriptExporter = (runMain in Compile)
    .toTask(" com.scalatsi.generator.ExportTypescript")
    .dependsOn(createTypescriptExporter)
}
