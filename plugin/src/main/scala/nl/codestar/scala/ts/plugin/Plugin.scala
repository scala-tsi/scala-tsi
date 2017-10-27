package nl.codestar.scala.ts.plugin

import org.clapper.classutil.ClassFinder
import sbt.Keys._
import sbt._

object Plugin extends AutoPlugin {
  object autoImport {
    val typescript = TaskKey[File]("typescript", "Generate typescript for classes")
    val typescriptClassesToGenerateFor = SettingsKey[Seq[String]]("Classes to generate typescript interfaces for")
    val typescriptGenerationImports = SettingsKey[Seq[String]]("additional imports (i.e. your packages so you don't need to prefix your classes)")
    //val inputDirectory = SettingKey[File]("typescript-input-directory")
    //val outputFile = SettingKey[File]("typescript-output-file", "File where all typescript interfaces will be written to")
  }

  import autoImport._

  override def trigger = allRequirements
  // Do we need this?
  //override def `requires`: Plugins = JvmPlugin

  lazy val typescriptSettings: Seq[Def.Setting[_]] =
    Seq(
      typescriptGenerationImports := Seq("nl.codestar.scala.ts.maakhiertypescriptvan._")
      typescriptClassesToGenerateFor := Seq("HierWilIkTypescriptVan"),
      typescript := createTypescriptGenerationTemplate(typescriptGenerationImports.value, typescriptClassesToGenerateFor.value, ???)
    )

  override lazy val projectSettings =
    inConfig(Compile)(typescriptSettings)



  def createTypescriptGenerationTemplate(imports: Seq[String], typesToGenerate: Seq[String], targetDir: File): File = {
    val towrite: String =
      """
        |package dummy
        |object Application extends App {
        |  println("Hello world!")
        |}""".stripMargin

    val file = new File(targetdir, "hello.scala")
    file.createNewFile()
    file
  }
}
