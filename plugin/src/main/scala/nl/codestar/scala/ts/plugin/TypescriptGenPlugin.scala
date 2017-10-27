package nl.codestar.scala.ts.plugin

import java.io.PrintWriter

import org.clapper.classutil.ClassFinder
import sbt.Keys._
import sbt._

object TypescriptGenPlugin extends AutoPlugin {
  object autoImport {
    val typescript = TaskKey[Seq[File]]("typescript", "Generate typescript for classes")
    val typescriptClassesToGenerateFor = SettingKey[Seq[String]]("Classes to generate typescript interfaces for")
    val typescriptGenerationImports = SettingKey[Seq[String]]("additional imports (i.e. your packages so you don't need to prefix your classes)")
    //val inputDirectory = SettingKey[File]("typescript-input-directory")
    //val outputFile = SettingKey[File]("typescript-output-file", "File where all typescript interfaces will be written to")
  }

  import autoImport._

  override def trigger = allRequirements
  // Do we need this?
  //override def `requires`: Plugins = JvmPlugin

  lazy val typescriptSettings: Seq[Def.Setting[_]] =
    Seq(
      typescriptGenerationImports := Seq("nl.codestar.scala.ts.maakhiertypescriptvan._"),
      typescriptClassesToGenerateFor := Seq("HierWilIkTypescriptVan"),
      typescript := createTypescriptGenerationTemplate(typescriptGenerationImports.value, typescriptClassesToGenerateFor.value, sourceManaged.value)
    )

  override lazy val projectSettings =
    inConfig(Compile)(typescriptSettings)

  def createTypescriptGenerationTemplate(imports: Seq[String], typesToGenerate: Seq[String], sourceManaged: File): Seq[File] = {
    val targetFile = sourceManaged / "Dummy.scala"

    println(s"Going to write dummy scala file to ${targetFile.absolutePath}")

    val towrite: String =
      """
        |object Dummy extends App {
        |  println("Hello world from the SBT plugin!")
        |}""".stripMargin

    IO.write(targetFile, towrite)
    Seq(targetFile)
  }
}
